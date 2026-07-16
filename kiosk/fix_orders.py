import re

with open(r'f:\박지성\baskinrobbins31\kiosk\src\main\resources\init_data.sql', 'r', encoding='utf-8') as f:
    text = f.read()

new_text = re.sub(r"'HERE',\s*0,\s*0,\s*0,\s*'COMPLETED'", r"'HERE', 0, 0, 'COMPLETED'", text)
new_text = re.sub(r"'HERE',\s*0,\s*0,\s*0,\s*'WAITING'", r"'HERE', 0, 0, 'WAITING'", new_text)
new_text = re.sub(r"'TOGO',\s*0,\s*0,\s*0,\s*'COMPLETED'", r"'TOGO', 0, 0, 'COMPLETED'", new_text)

with open(r'f:\박지성\baskinrobbins31\kiosk\src\main\resources\init_data.sql', 'w', encoding='utf-8') as f:
    f.write(new_text)

print('Done fixing extra zeros!')
