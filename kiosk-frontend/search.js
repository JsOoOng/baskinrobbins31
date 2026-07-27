const fs = require('fs');
const path = require('path');

function search(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            if (!fullPath.includes('node_modules') && !fullPath.includes('.git') && !fullPath.includes('dist')) {
                search(fullPath);
            }
        } else {
            if (fullPath.endsWith('.vue') || fullPath.endsWith('.js') || fullPath.endsWith('.css') || fullPath.endsWith('.java')) {
                const content = fs.readFileSync(fullPath, 'utf8');
                if (content.includes('<<<<<<< HEAD')) {
                    console.log('Conflict found in:', fullPath);
                }
            }
        }
    }
}
search('f:/박지성/baskinrobbins31/kiosk-frontend/src');
search('f:/박지성/baskinrobbins31/kiosk/src');
