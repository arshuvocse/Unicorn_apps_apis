import re

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\Activity\SyncFromServerAc.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# The broken replacement was:
# public class SyncFromServerAc
#    private java.util.Queue<Runnable> syncQueue = new java.util.LinkedList<>();
#    private void executeNextSync() { ... }
#  extends AppCompatActivity implements ISyncMaster.View {

# Let's fix this by finding the broken string and fixing it.
broken_pattern = re.compile(r'public class SyncFromServerAc\s+private java\.util\.Queue<Runnable> syncQueue = new java\.util\.LinkedList\<\>\(\);\s+private void executeNextSync\(\) \{\s+Runnable r = syncQueue\.poll\(\);\s+if\(r != null\) \{\s+r\.run\(\);\s+\} else \{\s+AllSyncDone\(\);\s+\}\s+\}\s+extends AppCompatActivity implements ISyncMaster\.View \{')

fixed_str = """public class SyncFromServerAc extends AppCompatActivity implements ISyncMaster.View {
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

if broken_pattern.search(content):
    content = broken_pattern.sub(fixed_str, content)
else:
    print("Could not find the exact broken pattern. Attempting more generic fix.")
    # Generic fix: just find `public class SyncFromServerAc` and the `extends` part and reorder.
    # We can also just remove the queue_code from between and put it inside the block.
    queue_code = re.search(r'(private java\.util\.Queue<Runnable> syncQueue.*?AllSyncDone\(\);\s+\}\s+\})', content, re.DOTALL)
    if queue_code:
        q_str = queue_code.group(1)
        # remove it from its current bad place
        content = content.replace(q_str, "")
        # put it after the class opening brace
        content = re.sub(r'(public class SyncFromServerAc.*?\{)', r'\1\n' + q_str + '\n', content, flags=re.DOTALL)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed class declaration.")
