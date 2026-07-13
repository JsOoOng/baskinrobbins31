import csv
import sys
sys.stdout.reconfigure(encoding='utf-8')
with open('키오스크_요구사항정의서_V2.1.1.CSV', 'r', encoding='cp949') as f:
    reader = csv.reader(f)
    headers = next(reader)
    for row in reader:
        if len(row) > 11 and row[1].strip():
            print(f"{row[1]} | {row[7]} | {row[11]}")
