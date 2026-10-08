"""Phase03 DOM smoke against the disposable demo on loopback port 18083.

Run Java21 demo with lunea.auth.session-timeout=3s on port 18083 first.
Uses installed Edge with a fresh temporary profile; no third-party packages.
Never connect this mutation test to a real database or a user browser profile.
"""
import base64
import json
import os
from pathlib import Path
import socket
import struct
import subprocess
import tempfile
import time
import urllib.request

BASE = "http://127.0.0.1:18083"
EDGE = Path("C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe")


class CDP:
    def __init__(self, url):
        from urllib.parse import urlsplit
        uri = urlsplit(url)
        self.sock = socket.create_connection((uri.hostname, uri.port), timeout=60)
        key = base64.b64encode(os.urandom(16)).decode()
        self.sock.sendall((f"GET {uri.path} HTTP/1.1\r\nHost: {uri.netloc}\r\n"
                           f"Upgrade: websocket\r\nConnection: Upgrade\r\n"
                           f"Sec-WebSocket-Key: {key}\r\nSec-WebSocket-Version: 13\r\n\r\n").encode())
        header = b""
        while not header.endswith(b"\r\n\r\n"):
            header += self.read(1)
        if b" 101 " not in header:
            raise RuntimeError("CDP websocket handshake failed")
        self.sequence = 0

    def read(self, size):
        result = b""
        while len(result) < size:
            chunk = self.sock.recv(size - len(result))
            if not chunk:
                raise RuntimeError("CDP disconnected")
            result += chunk
        return result

    def call(self, method, params=None):
        self.sequence += 1
        data = json.dumps({"id": self.sequence, "method": method, "params": params or {}}).encode()
        mask = os.urandom(4)
        length = len(data)
        header = bytes([0x81, 0x80 | length]) if length < 126 else bytes([0x81, 0xfe]) + struct.pack("!H", length)
        self.sock.sendall(header + mask + bytes(c ^ mask[i % 4] for i, c in enumerate(data)))
        message = b""
        while True:
            first, second = self.read(2)
            size = second & 127
            if size == 126:
                size = struct.unpack("!H", self.read(2))[0]
            elif size == 127:
                size = struct.unpack("!Q", self.read(8))[0]
            frame = self.read(size)
            if first & 15 == 8:
                raise RuntimeError("CDP closed")
            message += frame
            if not first & 128:
                continue
            result = json.loads(message)
            message = b""
            if result.get("id") == self.sequence:
                if "error" in result:
                    raise RuntimeError(result["error"]["message"])
                return result["result"]


def main():
    # This endpoint exists only on the isolated demo profile. Refuse other apps.
    with urllib.request.urlopen(BASE + "/api/demo/recovery/messages") as response:
        assert response.status == 200
    # Refuse an occupied debug port instead of attaching to another browser.
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", 19223))
    with tempfile.TemporaryDirectory(prefix="lunea-phase03-edge-") as profile:
        edge = subprocess.Popen([str(EDGE), "--headless=new", "--disable-gpu", "--no-first-run",
                                 "--remote-debugging-port=19223", "--remote-debugging-address=127.0.0.1",
                                 "--user-data-dir=" + profile, BASE], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        client = None
        try:
            for _ in range(100):
                try:
                    with urllib.request.urlopen("http://127.0.0.1:19223/json/list") as response:
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
            expression = Path(__file__).with_name("verify-phase03-browser.js").read_text(encoding="utf-8")
            result = client.call("Runtime.evaluate", {"expression": expression, "awaitPromise": True, "returnByValue": True})
            if "exceptionDetails" in result:
                raise RuntimeError(result["exceptionDetails"].get("exception", {}).get("description", "Browser test failed"))
            report = result["result"]["value"]
            client.call("Emulation.setDeviceMetricsOverride", {"width": 390, "height": 844, "deviceScaleFactor": 1, "mobile": True})
            mobile = client.call("Runtime.evaluate", {"expression": "document.documentElement.scrollWidth <= 390", "returnByValue": True})
            passed = mobile["result"]["value"]
            report["results"].append({"name": "mobile 390px no horizontal overflow", "passed": passed})
            report["passed" if passed else "failed"] += 1
            output = Path(__file__).resolve().parent.parent / "target" / "phase03-browser"
            output.mkdir(exist_ok=True)
            for view in ("recovery", "reset"):
                client.call("Runtime.evaluate", {"expression": f"authView('{view}')"})
                shot = client.call("Page.captureScreenshot", {"captureBeyondViewport": True})
                (output / f"{view}-mobile.png").write_bytes(base64.b64decode(shot["data"]))
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
            time.sleep(1)


if __name__ == "__main__":
    main()
