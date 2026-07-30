import re

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\Activity\SyncFromServerAc.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# First, remove ALL `executeNextSync();` that we previously added replacing `hitMain();`
content = content.replace("executeNextSync();", "")

# Re-add executeNextSync to callRunSync
content = content.replace("syncQueue.add(() -> spresenter.cllOtherEmpList(\"49\"));", "syncQueue.add(() -> spresenter.cllOtherEmpList(\"49\"));\n        executeNextSync();")

# Add executeNextSync at the END of every onGetXXX method
blocks = re.split(r'(public void onGet[a-zA-Z0-9_]+\([^)]*\)\s*\{)', content)

new_content = blocks[0]

for i in range(1, len(blocks), 2):
    method_decl = blocks[i]
    method_body = blocks[i+1]
    
    # We want to insert `executeNextSync();` right before the last closing brace of the method body.
    # The method body ends at the first matching closing brace at the root level, but simpler:
    # Since it's a simple split, the method body goes until the next "public void" or end of file.
    # Actually, using regex to find the closing brace is hard.
    # Let's just find the last `}` before the end of the method_body (which goes until the next split or EOF).
    # Since we split by `public void onGet...`, `method_body` contains everything until the next `public void onGet...`
    # The last `}` in `method_body` is the end of the method (or class if it's the last one).
    
    # Let's use a smarter regex to just replace the old grouping logic.
    # Actually, the grouping logic is fine as long as we also call `executeNextSync()`.
    # Let's just append `executeNextSync();\n` before the last `}`.
    last_brace_idx = method_body.rfind('}')
    if last_brace_idx != -1:
        method_body = method_body[:last_brace_idx] + "    executeNextSync();\n" + method_body[last_brace_idx:]
        
    new_content += method_decl + method_body

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Fixed SyncFromServerAc callbacks successfully.")
