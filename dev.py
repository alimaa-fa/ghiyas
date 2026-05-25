import os
import subprocess
import shutil
import webbrowser
import time
import threading
import http.server
import socketserver
import sys

# === Configuration ===
BASE_COMMAND = ["./gradlew", ":webApp:jsBrowserDevelopmentWebpack"]
BUILD_DIR = os.path.join("webApp", "build")
JS_FILE = "webApp.js"
HTML_FILE = "index.html"
PORT = 8080

def print_step(msg):
    print(f"\n\033[94m[{'*'*10}] {msg} ...\033[0m")

def find_file(filename, search_path):
    """Recursive search to find the exact path of a file"""
    for root, dirs, files in os.walk(search_path):
        if filename in files:
            return os.path.join(root, filename)
    return None

def main():
    # 1. Check for -clean flag
    build_command = list(BASE_COMMAND)
    if "-clean" in sys.argv or "--clean" in sys.argv:
        build_command.insert(1, "clean")
        print("\n\033[93m[*] Clean flag detected. Running full clean build...\033[0m")
    else:
        print("\n\033[92m[*] Running fast incremental build...\033[0m")

    # 2. Run Gradle Build
    print_step("1. Running Gradle task")
    try:
        subprocess.run(build_command, check=True)
    except subprocess.CalledProcessError:
        print("\n\033[91m[ERROR] Gradle build failed! Check the output above.\033[0m")
        sys.exit(1)

    # 3. Search for files
    print_step("2. Locating HTML and JS files in build directory")
    if not os.path.exists(BUILD_DIR):
        print(f"\n\033[91m[ERROR] Build directory not found: {BUILD_DIR}\033[0m")
        sys.exit(1)

    js_path = find_file(JS_FILE, BUILD_DIR)
    html_path = find_file(HTML_FILE, BUILD_DIR)

    if not js_path:
        print(f"\n\033[91m[ERROR] Could not find {JS_FILE}!\033[0m")
        sys.exit(1)
    if not html_path:
        print(f"\n\033[91m[ERROR] Could not find {HTML_FILE}!\033[0m")
        sys.exit(1)

    print(f"-> JS file found at: {js_path}")
    print(f"-> HTML file found at: {html_path}")

    # 4. Copy HTML next to JS
    print_step("3. Copying HTML to JS directory")
    js_dir = os.path.dirname(js_path)
    dest_html = os.path.join(js_dir, HTML_FILE)
    shutil.copy(html_path, dest_html)
    print("-> HTML file copied successfully.")

    # 5. Start Server and open browser
    print_step("4. Starting local server and opening browser")
    os.chdir(js_dir)

    def open_browser():
        time.sleep(2) # Give the server a moment to start
        print(f"\n-> Opening browser at http://127.0.0.1:{PORT}")
        webbrowser.open(f"http://127.0.0.1:{PORT}")

    threading.Thread(target=open_browser, daemon=True).start()

    class ReusableTCPServer(socketserver.TCPServer):
        allow_reuse_address = True

    Handler = http.server.SimpleHTTPRequestHandler

    try:
        with ReusableTCPServer(("", PORT), Handler) as httpd:
            print(f"\n\033[92m-> Server is running on port {PORT}. Press Ctrl+C to stop.\033[0m\n")
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n\n[!] Server stopped successfully by user.")
    except Exception as e:
        print(f"\n\033[91m[ERROR] Server crashed: {e}\033[0m")

if __name__ == "__main__":
    main()
