import re

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\Activity\SyncFromServerAc.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add queue declaration and executeNextSync()
queue_code = """
    private java.util.Queue<Runnable> syncQueue = new java.util.LinkedList<>();
    private void executeNextSync() {
        Runnable r = syncQueue.poll();
        if(r != null) {
            r.run();
        } else {
            AllSyncDone();
        }
    }
"""
if "java.util.Queue<Runnable> syncQueue" not in content:
    content = content.replace("public class SyncFromServerAc", "public class SyncFromServerAc" + queue_code)

# 2. Modify callRunSync
# We want to replace lines like `spresenter.cllDoctor(empId);` with `syncQueue.add(() -> spresenter.cllDoctor(empId));`
# ONLY inside `callRunSync`.

# Find callRunSync block
match = re.search(r'(private void callRunSync[^\{]+\{)(.*?)(\n\s*\}\s*private void hitMain)', content, re.DOTALL)
if match:
    block_start = match.group(1)
    block_body = match.group(2)
    block_end = match.group(3)
    
    # Replace spresenter calls in block_body
    block_body = re.sub(r'^\s*spresenter\.([a-zA-Z0-9_]+)\(([^)]*)\);', r'        syncQueue.add(() -> spresenter.\1(\2));', block_body, flags=re.MULTILINE)
    
    # Add executeNextSync at the end of callRunSync
    if "executeNextSync();" not in block_body:
        block_body += "\n        executeNextSync();\n"
        
    content = content[:match.start()] + block_start + block_body + block_end + content[match.end():]

# 3. Replace all `hitMain();` calls with `executeNextSync();`
content = content.replace("hitMain();", "executeNextSync();")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Patched SyncFromServerAc successfully.")
