import re

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\DBAdapter\SyncDb_Helper.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Find all instances of:
# public boolean Insert[A-Za-z]+\(List<[^>]+> [^)]+\) {
#     boolean isTrue = false;
# And change to:
# public boolean Insert[A-Za-z]+\(List<[^>]+> [^)]+\) {
#     boolean isTrue = true;

content = re.sub(
    r'(public boolean Insert[A-Za-z0-9_]+\([^)]+\)\s*\{\s*)boolean isTrue = false;',
    r'\1boolean isTrue = true;',
    content
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed SyncDb_Helper.java empty list bugs")
