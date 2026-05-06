import os
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Replace hardcoded Color(0xFFF97316) with MaterialTheme.colorScheme.primary
    if 'Color(0xFFF97316)' in content:
        if 'import androidx.compose.material3.MaterialTheme' not in content:
            content = content.replace('import androidx.compose.ui.graphics.Color\n', 'import androidx.compose.ui.graphics.Color\nimport androidx.compose.material3.MaterialTheme\n')
        content = content.replace('Color(0xFFF97316)', 'MaterialTheme.colorScheme.primary')

    # 2. Update TopAppBar colors to use primary theme
    # Look for TopAppBarDefaults.topAppBarColors(...)
    top_app_bar_pattern = re.compile(r'TopAppBarDefaults\.topAppBarColors\s*\((.*?)\)', re.DOTALL)
    
    def replace_colors(match):
        inner = match.group(1)
        # If it's already using primary, skip
        if 'MaterialTheme.colorScheme.primary' in inner and 'containerColor' in inner:
            return match.group(0)
            
        return 'TopAppBarDefaults.topAppBarColors(\n                    containerColor = MaterialTheme.colorScheme.primary,\n                    titleContentColor = MaterialTheme.colorScheme.onPrimary,\n                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,\n                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary\n                )'

    new_content = top_app_bar_pattern.sub(replace_colors, content)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filepath}")

directory = 'c:/Users/b7ues/Downloads/Code/FoodieNow/app/src/main/java/com/example/foodienow/feature'

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))

print("Done")
