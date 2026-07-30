import os

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\DBAdapter\SyncDb_Helper.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("            try { database.endTransaction(); } catch (Exception ignored) {}", "")
content = content.replace("            try { databases.endTransaction(); } catch (Exception ignored) {}", "")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
