"""HTTP verification against the default-profile app and Phase04's isolated PG16 DB.

Requires the staff integration fixtures in phase04_test on loopback port 15434
and the current jar on 127.0.0.1:18085 with Hibernate validate. It refuses every
other database/port and never reads production configuration or credentials.
"""
import http.cookiejar
import json
from pathlib import Path
import subprocess
import sys
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parent.parent
BASE = "http://127.0.0.1:18085"
PSQL = ROOT / ".local/phase01/postgres16/pgsql/bin/psql.exe"


def sql(statement):
    return subprocess.check_output([str(PSQL), "-h", "127.0.0.1", "-p", "15434", "-U", "phase04_test",
                                    "-d", "phase04_test", "-X", "-A", "-t", "-v", "ON_ERROR_STOP=1", "-c", statement],
                                   encoding="utf-8").strip()


class Client:
    def __init__(self):
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
        self.csrf = None

    def request(self, method, route, body=None, headers=None):
        values = {"Accept": "application/json", **(headers or {})}
        if method != "GET":
            if self.csrf:
                values[self.csrf["headerName"]] = self.csrf["token"]
            values["Content-Type"] = "application/json"
        request = urllib.request.Request(BASE + route, data=None if body is None else json.dumps(body).encode(), headers=values, method=method)
        try:
            response = self.opener.open(request, timeout=15)
        except urllib.error.HTTPError as error:
            response = error
        with response:
            raw = response.read()
            return response.status, json.loads(raw) if raw else None

    def login(self, identifier, password):
        _, self.csrf = self.request("GET", "/api/auth/csrf")
        status, user = self.request("POST", "/api/auth/login", {"identifier": identifier, "password": password})
        _, self.csrf = self.request("GET", "/api/auth/csrf")
        return status, user


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    assert sql("select current_database()") == "phase04_test"
    # ActiveStatus preserves its existing ordinal representation: INACTIVE=0, ACTIVE=1.
    identifier = sql("select a.email from accounts a join employees e on e.account_id=a.id join employee_roles er on er.employee_id=e.id join roles r on r.id=er.role_id where r.name='ADMIN' and a.status='ACTIVE' and e.status=1 order by e.id desc limit 1")
    assert identifier.endswith("@lunea.test"), "Require disposable integration fixtures"
    results = []

    def check(name, passed):
        results.append({"name": name, "passed": bool(passed)})
        assert passed, name

    client = Client()
    check("anonymous employee route denied", client.request("GET", "/api/employees")[0] == 401)
    check("staff mutation requires CSRF", client.request("POST", "/api/employees", {})[0] == 403)
    status, actor = client.login(identifier, "StaffPass123")
    check("real employee login and fresh ADMIN grants", status == 200 and "ROLE_MANAGE" in actor["permissions"])
    _, roles = client.request("GET", "/api/roles")
    _, permissions = client.request("GET", "/api/permissions")
    _, stores = client.request("GET", "/api/stores")
    role = next(r for r in roles if r["name"].startswith("TEST_") and r["permissions"] == ["INVENTORY_READ"])
    branch = next(s for s in stores if s["status"] == "ACTIVE")
    outside = next(s for s in stores if s["status"] == "ACTIVE" and s["id"] != branch["id"])
    email = f"http-{time.time_ns()}@lunea.test"
    form = {"fullName": "Phase04 HTTP Staff", "email": email, "password": "HttpStaff123", "status": "ACTIVE",
            "roleIds": [role["id"]], "storeIds": [branch["id"]], "primaryStoreId": branch["id"]}
    status, target = client.request("POST", "/api/employees?employeeId=999999", form, {"X-Forwarded-For": "8.8.8.8"})
    check("create identity associations and safe response", status == 201 and target["primaryStoreId"] == branch["id"] and "password" not in json.dumps(target).lower())
    audit = sql(f"select employee_id || '|' || object_type || '|' || ip_address from audit_logs where action='EMPLOYEE_CREATED' and object_id='{target['id']}'")
    check("database audit binds authenticated actor and remote IP", audit == f"{actor['employeeId']}|EMPLOYEE|127.0.0.1")
    counts = sql("select (select count(*) from accounts) || '|' || (select count(*) from employees) || '|' || (select count(*) from audit_logs)")
    invalid = {**form, "email": "invalid-" + email, "storeIds": [branch["id"], 9223372036854775807]}
    check("late invalid store is controlled and writes nothing", client.request("POST", "/api/employees", invalid)[0] == 400 and counts == sql("select (select count(*) from accounts) || '|' || (select count(*) from employees) || '|' || (select count(*) from audit_logs)"))
    check("duplicate normalized identity conflicts", client.request("POST", "/api/employees", {**form, "email": email.upper()})[0] == 409)
    worker = Client()
    check("created employee authenticates", worker.login(email, "HttpStaff123")[0] == 200)
    check("permission AND branch scope enforced over HTTP", worker.request("GET", f"/api/inventory/store/{branch['id']}")[0] == 200 and worker.request("GET", f"/api/inventory/store/{outside['id']}")[0] == 403)
    check("nonmanager and unfinished customer flows denied", worker.request("GET", "/api/employees")[0] == 403 and worker.request("GET", "/api/customers")[0] == 403)
    check("grant revoke applies to existing session", client.request("PUT", f"/api/roles/{role['id']}/permissions", {"permissionIds": []})[0] == 200 and worker.request("GET", f"/api/inventory/store/{branch['id']}")[0] == 403)
    check("grant restore applies to existing session", client.request("PUT", f"/api/roles/{role['id']}/permissions", {"permissionIds": role["permissionIds"]})[0] == 200 and worker.request("GET", f"/api/inventory/store/{branch['id']}")[0] == 200)
    admin = next(r for r in roles if r["name"] == "ADMIN")
    check("second ADMIN assignment and reserved grants rejected", client.request("POST", "/api/employees", {**form, "email": "admin-" + email, "roleIds": [admin["id"]]})[0] == 409 and client.request("PUT", f"/api/roles/{admin['id']}/permissions", {"permissionIds": []})[0] == 409)
    check("self deactivation is denied", client.request("DELETE", f"/api/employees/{actor['employeeId']}")[0] == 403)
    update = {**form, "fullName": "Phase04 HTTP Updated", "password": "", "roleIds": [], "storeIds": [], "primaryStoreId": None}
    status, changed = client.request("PUT", f"/api/employees/{target['id']}", update)
    check("update replaces scope while preserving password", status == 200 and changed["roleIds"] == [] and changed["storeIds"] == [] and worker.request("GET", f"/api/inventory/store/{branch['id']}")[0] == 403)
    status, page = client.request("GET", "/api/employees?keyword=" + email + "&size=1")
    check("search paging uses current persisted profile", status == 200 and page["totalElements"] == 1 and page["content"][0]["fullName"] == update["fullName"])
    check("deactivate revokes existing HTTP session", client.request("DELETE", f"/api/employees/{target['id']}")[0] == 204 and worker.request("GET", "/api/auth/me")[0] == 401)
    history = sql(f"select count(*) from audit_logs where object_type='EMPLOYEE' and object_id='{target['id']}'")
    check("deactivation preserves profile and audit history", client.request("GET", f"/api/employees/{target['id']}")[1]["status"] == "INACTIVE" and history == "3")
    print(json.dumps({"passed": len(results), "failed": 0, "results": results}, indent=2))


if __name__ == "__main__":
    main()
