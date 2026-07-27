const fs = require('fs');

const code = fs.readFileSync('f:/박지성/baskinrobbins31/kiosk-frontend/src/views/customer/MenuView.vue', 'utf8');

const match = code.match(/<style scoped>([\s\S]*?)<\/style>/);
if (match) {
    const cssCode = match[1];
    let braces = 0;
    const lines = cssCode.split('\n');
    for (let i = 0; i < lines.length; i++) {
        for (let char of lines[i]) {
            if (char === '{') braces++;
            if (char === '}') braces--;
        }
        if (braces < 0) {
            console.log(`Extra } found at CSS line ${i+1}`);
            console.log(`File line: ${i + 1 + code.substr(0, match.index).split('\n').length}`);
            console.log(`Line content: ${lines[i]}`);
            braces = 0;
        }
    }
}
