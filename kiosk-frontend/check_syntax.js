const fs = require('fs');
const acorn = require('acorn');

const code = fs.readFileSync('f:/박지성/baskinrobbins31/kiosk-frontend/src/views/customer/MenuView.vue', 'utf8');

const match = code.match(/<script setup>([\s\S]*?)<\/script>/);
if (match) {
    const scriptCode = match[1];
    try {
        acorn.parse(scriptCode, { sourceType: 'module', ecmaVersion: 2022 });
        console.log("Syntax is valid!");
    } catch (e) {
        console.error("Syntax Error:", e.message);
        console.error("At Line in script:", e.loc.line);
        console.error("At Line in file:", e.loc.line + code.substr(0, match.index).split('\n').length);
        
        // Print the lines around the error
        const lines = scriptCode.split('\n');
        for(let i = Math.max(0, e.loc.line - 5); i < Math.min(lines.length, e.loc.line + 5); i++) {
            console.log(`${i+1}: ${lines[i]}`);
        }
    }
} else {
    console.log("No script setup found");
}
