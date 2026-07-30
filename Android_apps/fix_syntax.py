import os

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\DBAdapter\SyncDb_Helper.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
"""            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        } catch (Exception exception) {""",
"""            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception exception) {
            try { database.endTransaction(); } catch (Exception ignored) {}""")

content = content.replace(
"""            databases.setTransactionSuccessful();
        } finally {
            databases.endTransaction();
        } catch (Exception exception) {""",
"""            databases.setTransactionSuccessful();
            databases.endTransaction();
        } catch (Exception exception) {
            try { databases.endTransaction(); } catch (Exception ignored) {}""")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Syntax fixed.")
