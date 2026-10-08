"""Recovery smoke on the dedicated default-profile PostgreSQL test app at 18084.

Start only against lunea_phase03_http_test with SMTP pointed to a closed loopback
port. This script registers a test account; never point it at a business DB.
"""
import http.cookiejar
import json
import urllib.error
import urllib.request
import uuid

BASE = "http://127.0.0.1:18084"
client = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
results = []


def check(name, passed):
    results.append({"name": name, "passed": bool(passed)})


def call(path, body=None, csrf=None):
    headers = {"Accept": "application/json"}
    if body is not None:
        headers["Content-Type"] = "application/json"
    if csrf:
        headers[csrf["headerName"]] = csrf["token"]
    request = urllib.request.Request(BASE + path, headers=headers,
                                     data=None if body is None else json.dumps(body).encode())
    try:
        response = client.open(request, timeout=15)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        raw = response.read().decode()
        return response.status, json.loads(raw) if raw else None


email = "phase03-http-" + str(uuid.uuid4()) + "@lunea.test"
password = "HttpRecovery123"
check("anonymous private API denied", call("/api/auth/me")[0] == 401)
check("ordinary profile exposes no demo mailbox", call("/api/demo/recovery/messages")[0] == 401)
check("recovery requires CSRF", call("/api/auth/recovery/request", {"identifier": email})[0] == 403)
csrf = call("/api/auth/csrf")[1]
check("registration on separate PostgreSQL HTTP fixture", call("/api/auth/register", {
    "fullName": "Phase03 HTTP Test", "email": email, "password": password,
    "confirmPassword": password, "termsAccepted": True
}, csrf)[0] == 201)
known = call("/api/auth/recovery/request", {"identifier": email}, csrf)
unknown = call("/api/auth/recovery/request", {"identifier": "missing@lunea.test"}, csrf)
check("SMTP connection failure and unknown account have identical generic 202", known == unknown and known[0] == 202)
check("ordinary recovery response contains only message", list(known[1]) == ["message"])
raw_token = "R" * 43
status, error = call("/api/auth/recovery/reset", {
    "token": raw_token, "password": "ChangedHttp456", "confirmPassword": "ChangedHttp456"
}, csrf)
check("invalid reset rejected with safe error contract", status == 400 and error["code"] == "INVALID_RECOVERY_TOKEN"
      and raw_token not in json.dumps(error) and "ChangedHttp456" not in json.dumps(error))
check("delivery failure and invalid reset preserve login credential", call("/api/auth/login", {
    "identifier": email, "password": password
}, csrf)[0] == 200)
check("logged-in private API works", call("/api/auth/me")[0] == 200)
check("authenticated caller still cannot access demo mailbox", call("/api/demo/recovery/messages")[0] == 403)
csrf = call("/api/auth/csrf")[1]
check("logout still invalidates session", call("/api/auth/logout", {}, csrf)[0] == 204
      and call("/api/auth/me")[0] == 401)
report = {"passed": sum(r["passed"] for r in results), "failed": sum(not r["passed"] for r in results), "results": results}
print(json.dumps(report, indent=2))
assert report["failed"] == 0, "HTTP checkpoints failed"
