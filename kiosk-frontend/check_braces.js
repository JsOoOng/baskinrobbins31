const fs = require('fs');

const code = fs.readFileSync('f:/박지성/baskinrobbins31/kiosk-frontend/src/views/customer/MenuView.vue', 'utf8');

const match = code.match(/<script setup>([\s\S]*?)<\/script>/);
if (match) {
    const scriptCode = match[1];
    let curly = 0;
    let paren = 0;
    const lines = scriptCode.split('\n');
    for (let i = 0; i < lines.length; i++) {
        for (let char of lines[i]) {
            if (char === '{') curly++;
            if (char === '}') curly--;
            if (char === '(') paren++;
            if (char === ')') paren--;
        }
        if (curly < 0) {
            console.log("Extra '}' at line", i+1);
            curly = 0;
        }
        if (paren < 0) {
            console.log("Extra ')' at line", i+1);
            paren = 0;
        }
    }
    console.log(`Final count: curly=${curly}, paren=${paren}`);
}
