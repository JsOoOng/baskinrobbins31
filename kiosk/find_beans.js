const fs = require('fs');
const path = require('path');

const beans = {};

function search(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            search(fullPath);
        } else if (fullPath.endsWith('.java')) {
            const content = fs.readFileSync(fullPath, 'utf8');
            if (content.includes('@Service') || content.includes('@Repository') || content.includes('@Controller') || content.includes('@RestController') || content.includes('@Component')) {
                const match = content.match(/(?:public|protected|private|)\s*(?:abstract\s+)?(?:class|interface)\s+(\w+)/);
                if (match) {
                    const className = match[1];
                    if (!beans[className]) beans[className] = [];
                    beans[className].push(fullPath);
                }
            }
        }
    }
}
search('f:/박지성/baskinrobbins31/kiosk/src/main/java/com/kiosk');

let found = false;
for (const className in beans) {
    if (beans[className].length > 1) {
        console.log(`Duplicate Bean: ${className}`);
        beans[className].forEach(p => console.log('  ' + p));
        found = true;
    }
}
if (!found) console.log('No duplicate beans found!');
