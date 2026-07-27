const fs = require('fs');
const path = require('path');

const entities = {};

function search(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            search(fullPath);
        } else if (fullPath.endsWith('.java')) {
            const content = fs.readFileSync(fullPath, 'utf8');
            if (content.includes('@Entity')) {
                const match = content.match(/public\s+class\s+(\w+)/);
                if (match) {
                    const className = match[1];
                    if (!entities[className]) entities[className] = [];
                    entities[className].push(fullPath);
                }
            }
        }
    }
}
search('f:/박지성/baskinrobbins31/kiosk/src/main/java/com/kiosk');

for (const className in entities) {
    if (entities[className].length > 1) {
        console.log(`Duplicate Entity: ${className}`);
        entities[className].forEach(p => console.log('  ' + p));
    }
}
