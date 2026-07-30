import os
import re

file_path = r'd:\CSTL_Projects\Unicorn\Android_apps\app\src\main\java\com\creatrix\unicorn\Presenter\SyncMasterPresenter.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# I want to inject `if(view != null) view.onGetXXX("Error", false);`
# But I must be careful not to duplicate it. I'll remove my previous injections first.
content = re.sub(r'\s*if\(view != null\) view\.onGet[a-zA-Z0-9_]+\("Error", false\);', '', content)

# Now, we parse the file method by method.
blocks = re.split(r'(public void c[al]+[a-zA-Z0-9_]+\([^)]*\)\s*\{)', content)

new_content = blocks[0]

for i in range(1, len(blocks), 2):
    method_decl = blocks[i]
    method_body = blocks[i+1]
    
    # find view.onGetXXX(
    match = re.search(r'view\.(on[a-zA-Z0-9_]+)\(', method_body)
    if match:
        callback_name = match.group(1)
        error_call = f'if(view != null) view.{callback_name}("Error", false);'
        
        # Inject into onFailure block AT THE BEGINNING
        # We can find `public void onFailure(@NonNull Call<...> call, @NonNull Throwable t) {`
        method_body = re.sub(
            r'(public void onFailure\([^)]+\)\s*\{)',
            r'\1\n                    ' + error_call,
            method_body
        )
        
        # Inject into the main catch block AT THE BEGINNING
        method_body = re.sub(
            r'(\}\s*catch\s*\([^)]+\)\s*\{)',
            r'\1\n            ' + error_call,
            method_body
        )
        
    new_content += method_decl + method_body

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Repatched SyncMasterPresenter successfully.")
