const fs = require('fs');
const acorn = require('acorn');

const code = fs.readFileSync('f:/박지성/baskinrobbins31/kiosk-frontend/src/views/customer/MenuView.vue', 'utf8');

const match = code.match(/<script setup>([\s\S]*?)<\/script>/);
if (match) {
    const scriptCode = match[1];
    
    // Naive brace tracking
    const stack = [];
    const lines = scriptCode.split('\n');
    let insideString = false;
    let stringChar = '';
    
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        let j = 0;
        while(j < line.length) {
            const c = line[j];
            if (!insideString) {
                if (c === "'" || c === '"' || c === '`') {
                    insideString = true;
                    stringChar = c;
                } else if (c === '/' && line[j+1] === '/') {
                    break; // line comment
                } else if (c === '{') {
                    stack.push(i+1);
                } else if (c === '}') {
                    stack.pop();
                }
            } else {
                if (c === '\\') {
                    j++; // skip escaped char
                } else if (c === stringChar) {
                    insideString = false;
                }
            }
            j++;
        }
    }
    
    console.log("Unclosed braces at script lines:", stack);
    stack.forEach(lineNum => {
        console.log(`Line ${lineNum}: ${lines[lineNum-1]}`);
    });
}
