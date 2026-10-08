"""Phase04 browser checks on the disposable H2 demo at loopback port 18084.

Start the current Java21 jar with the demo profile first. Uses a new Edge
profile and the existing CDP helper; never attaches to a user's browser.
"""
import base64
import json
from pathlib import Path
import runpy
import socket
import subprocess
import sys
import tempfile
import time
import urllib.request

BASE = "http://127.0.0.1:18084"
EDGE = Path("C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe")
CDP = runpy.run_path(str(Path(__file__).with_name("verify-phase03-browser.py")))["CDP"]


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    with urllib.request.urlopen(BASE + "/api/demo/recovery/messages") as response:
        assert response.status == 200, "Refuse a non-demo application"
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", 19224))
    with tempfile.TemporaryDirectory(prefix="lunea-phase04-edge-") as profile:
        assert Path(profile).resolve().parent == Path(tempfile.gettempdir()).resolve()
        edge = subprocess.Popen([str(EDGE), "--headless=new", "--disable-gpu", "--no-first-run",
                                 "--remote-debugging-port=19224", "--remote-debugging-address=127.0.0.1",
                                 "--user-data-dir=" + profile, BASE], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        client = None
        try:
            for _ in range(100):
                try:
                    with urllib.request.urlopen("http://127.0.0.1:19224/json/list") as response:
                        pages = json.load(response)
                    page = next(p for p in pages if p.get("type") == "page" and p.get("url", "").startswith(BASE))
                    client = CDP(page["webSocketDebuggerUrl"])
                    break
                except (OSError, StopIteration):
                    time.sleep(.2)
            assert client, "Edge did not start"
            client.call("Runtime.enable")
            for _ in range(100):
                ready = client.call("Runtime.evaluate", {"expression": "typeof state !== 'undefined' && state.demo", "returnByValue": True})
                if ready.get("result", {}).get("value"):
                    break
                time.sleep(.2)
            expression = Path(__file__).with_name("verify-phase04-browser.js").read_text(encoding="utf-8")
            result = client.call("Runtime.evaluate", {"expression": expression, "awaitPromise": True, "returnByValue": True})
            if "exceptionDetails" in result:
                raise RuntimeError(result["exceptionDetails"].get("exception", {}).get("description", "Browser test failed"))
            report = result["result"]["value"]
            output = Path(__file__).resolve().parent.parent / "target" / "phase04-browser"
            output.mkdir(exist_ok=True)
            for width in (1280, 390):
                client.call("Emulation.setDeviceMetricsOverride", {"width": width, "height": 844, "deviceScaleFactor": 1, "mobile": width == 390})
                fits = client.call("Runtime.evaluate", {"expression": f"document.documentElement.scrollWidth <= {width}", "returnByValue": True})["result"]["value"]
                report["results"].append({"name": f"staff page fits {width}px viewport", "passed": fits})
                report["passed" if fits else "failed"] += 1
                for view in ("staff", "roles"):
                    client.call("Runtime.evaluate", {"expression": f"workspaceView('{view}')"})
                    time.sleep(.4)
                    shot = client.call("Page.captureScreenshot", {"captureBeyondViewport": True})
                    (output / f"{view}-{width}.png").write_bytes(base64.b64decode(shot["data"]))
            print(json.dumps(report, ensure_ascii=False, indent=2))
            assert report["failed"] == 0, "Browser checkpoints failed"
        finally:
            if client:
                try:
                    client.call("Browser.close")
                except (OSError, RuntimeError):
                    pass
                client.sock.close()
            try:
                edge.wait(timeout=5)
            except subprocess.TimeoutExpired:
                subprocess.run(["taskkill", "/PID", str(edge.pid), "/T", "/F"], check=False,
                               stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                edge.wait(timeout=5)
            # Edge's launcher can exit before its browser process; close only this
            # fresh profile's remaining processes before TemporaryDirectory removes it.
            profile_literal = profile.replace("'", "''")
            cleanup = f"$phase04Profile='{profile_literal}'; Get-CimInstance Win32_Process -Filter \"name='msedge.exe'\" | Where-Object {{ $_.CommandLine -and $_.CommandLine.Contains($phase04Profile) }} | ForEach-Object {{ Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }}"
            subprocess.run(["powershell", "-NoProfile", "-Command", cleanup], check=True,
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
                           creationflags=subprocess.CREATE_NO_WINDOW)
            time.sleep(1)


if __name__ == "__main__":
    main()
