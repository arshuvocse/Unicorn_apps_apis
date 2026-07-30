import re
import os

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\DBAdapter\SyncDb_Helper.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# We need to find methods that instantiate a SQLiteDatabase and have a loop containing execSQL,
# but do not have beginTransaction().
# Since parsing java with regex is brittle, we can explicitly target the exact signature 
# of the unoptimized methods.
# Actually, a simpler robust approach for this file:
# Look for every instance of:
# SQLiteDatabase database = dbHelperMain.getWritableDatabase();
# or 
# SQLiteDatabase databases = dbHelperMain.getWritableDatabase();
# And check if there is a beginTransaction immediately after or around it.
# If not, we can inject beginTransaction, setTransactionSuccessful, and endTransaction.

# A more robust regex replacement approach specifically for the pattern in this file:
# Pattern:
# SQLiteDatabase database = dbHelperMain.getWritableDatabase();
# for (int i = 0; i < nList.size(); i++) {
#     ...
#     database.execSQL(insertQuery);
# }
# 
# Let's replace:
# SQLiteDatabase database = dbHelperMain.getWritableDatabase();
# with:
# SQLiteDatabase database = dbHelperMain.getWritableDatabase();
# database.beginTransaction();
# try {
#
# But wait, there are already try-catch blocks in the method.
# Example:
#     public boolean InsertNSMInfo(List<NSM> nList) {
#         boolean isTrue = true;
#         try {
#             String tableName = "tblNSMInfo";
#             SQLiteDatabase database = dbHelperMain.getWritableDatabase();
#             for (int i = 0; i < nList.size(); i++) {
#                 ...
#                 database.execSQL(insertQuery);
#             }
#         } catch (Exception exception) {
#             Log.e("DBEX", exception.toString());
#         }
#         return isTrue;
#     }

# Since we want to wrap the loop or the whole block after getWritableDatabase, we can do this:
# Find: `SQLiteDatabase (database|databases) = dbHelperMain.getWritableDatabase();`
# And add `\n            \1.beginTransaction();` after it.
# But we must also add `\1.setTransactionSuccessful(); \1.endTransaction();` at the end of the try block, or right after the for loop.
# It is much safer to add setTransactionSuccessful() and endTransaction() right after the for loop.

import re

# Find the for loops followed by execSQL
# A typical pattern:
#             for (int i = 0; i < nList.size(); i++) {
#                 ...
#                 database.execSQL(...);
#             }

def optimize_inserts(text):
    # Find all methods in the file
    # We will look for "public boolean Insert"
    method_pattern = re.compile(r'public boolean Insert[A-Za-z0-9_]+\(.*?\)\s*\{.*?(?=public boolean Insert|$)', re.DOTALL)
    
    methods = method_pattern.findall(text)
    new_text = text
    
    for method in methods:
        # Check if it lacks beginTransaction
        if "beginTransaction" not in method and "execSQL(" in method and "for (" in method:
            # We want to wrap the for loop with transaction.
            # But the for loop might be multiple lines. 
            # A simpler way is to replace the database creation:
            # SQLiteDatabase database = dbHelperMain.getWritableDatabase();
            # database.beginTransaction();
            # And then before the end of the try block or return, add the endTransaction.
            # However, injecting code securely via regex in Java is tricky.
            
            # Let's just find the loop block.
            # Usually it's:
            # for (int i = 0; i < ...size(); i++) { ... }
            # Let's use a simpler string replacement for the specific methods we know are slow,
            # or just do a clever replacement:
            pass

# Let's write a python parser that balances braces to find the for-loop.
def add_transactions_to_file(file_content):
    lines = file_content.split('\n')
    out_lines = []
    
    in_db_method = False
    db_var_name = ""
    
    i = 0
    while i < len(lines):
        line = lines[i]
        out_lines.append(line)
        
        # Detect database initialization
        match = re.search(r'SQLiteDatabase\s+(\w+)\s*=\s*dbHelperMain\.getWritableDatabase\(\);', line)
        if match:
            # Check if next few lines contain beginTransaction
            has_txn = False
            for j in range(i+1, min(i+10, len(lines))):
                if 'beginTransaction' in lines[j]:
                    has_txn = True
                    break
            
            if not has_txn:
                db_var = match.group(1)
                # We will inject beginTransaction right here
                indent = line[:len(line) - len(line.lstrip())]
                out_lines.append(f"{indent}{db_var}.beginTransaction();")
                out_lines.append(f"{indent}try {{")
                
                # Now we need to find the catch block corresponding to the outer try, OR just wrap the rest of the method body.
                # Actually, simpler: we just wrap the loop!
                # Let's pop what we just added and do it at the loop level.
                out_lines.pop()
                out_lines.pop()
        i += 1
    return '\n'.join(out_lines)

# Let's use an even simpler approach.
# Replace all instances of `database.execSQL(insertQuery);` or similar inside for loops.
# Actually, the safest way is to do this:
# 1. find getWritableDatabase()
# 2. inject beginTransaction()
# 3. find the end of the method (or catch block) and inject setTransactionSuccessful() and endTransaction()

def inject_transactions_safely(text):
    # We will use regex to find the try-catch blocks containing getWritableDatabase and execSQL but NOT beginTransaction
    
    # Let's find:
    # try {
    #     ...
    #     SQLiteDatabase database = dbHelperMain.getWritableDatabase();
    #     ...
    #     database.execSQL(...);
    #     ...
    # } catch (Exception
    
    pattern = re.compile(r'(try\s*\{.*?(SQLiteDatabase\s+(\w+)\s*=\s*dbHelperMain\.getWritableDatabase\(\);).*?(?=\}\s*catch))', re.DOTALL)
    
    def replacer(match):
        block = match.group(1)
        db_var = match.group(3)
        
        if "beginTransaction" in block:
            return block # Already optimized
        
        # Inject beginTransaction right after getWritableDatabase
        db_decl = match.group(2)
        new_db_decl = db_decl + f"\n            {db_var}.beginTransaction();"
        
        block = block.replace(db_decl, new_db_decl)
        
        # Add setTransactionSuccessful and endTransaction at the end of the try block
        # The block ends right before "} catch"
        block = block + f"\n            {db_var}.setTransactionSuccessful();\n        }} finally {{\n            {db_var}.endTransaction();\n        "
        
        return block
    
    return pattern.sub(replacer, text)


new_content = inject_transactions_safely(content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)
print("Optimization complete.")
