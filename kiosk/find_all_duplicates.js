const fs = require('fs');
const path = require('path');

const classes = {};

function search(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            search(fullPath);
        } else if (fullPath.endsWith('.java')) {
            const content = fs.readFileSync(fullPath, 'utf8');
            const match = content.match(/(?:public|protected|private|)\s*(?:abstract\s+)?(?:class|interface|enum)\s+(\w+)/);
            if (match) {
                const className = match[1];
                if (!classes[className]) classes[className] = [];
                classes[className].push(fullPath);
            }
        }
    }
}
search('f:/박지성/baskinrobbins31/kiosk/src/main/java/com/kiosk');

let found = false;
for (const className in classes) {
    if (classes[className].length > 1) {
        // Filter out classes that might legitimately be named the same if they are standard DTOs in different features?
        // Actually, let's just log all duplicates to see.
        console.log(`Duplicate Class: ${className}`);
        classes[className].forEach(p => console.log('  ' + p));
        found = true;
    }
}
if (!found) console.log('No duplicate classes found!');
