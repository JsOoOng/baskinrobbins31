import pandas as pd

file_path = 'f:\\박지성\\baskinrobbins31\\키오스크_요구사항정의서_V2.1.1.xlsx'
xls = pd.ExcelFile(file_path)

with open('excel_output.txt', 'w', encoding='utf-8') as f:
    f.write('Sheets: ' + ', '.join(xls.sheet_names) + '\n\n')
    for sheet in xls.sheet_names:
        f.write(f'--- {sheet} ---\n')
        df = pd.read_excel(xls, sheet)
        f.write(df.to_string() + '\n\n')
