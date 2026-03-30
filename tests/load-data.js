const { readFileSync, readdirSync } = require('fs');
const { createContext, runInContext } = require('vm');
const path = require('path');

const dataDir = path.join(__dirname, '..', 'data');

function loadDataFiles() {
  const context = createContext({});

  const files = ['items.js', 'items_equipment.js', 'item_metadata.js'];
  for (const file of files) {
    const code = readFileSync(path.join(dataDir, file), 'utf-8');
    runInContext(code, context, { filename: file });
  }

  return {
    unequipped: context.unequipped,
    sets: context.sets,
    equipment: context.equipment,
    stats: context.stats,
    bases: context.bases,
  };
}

function loadSkillFiles() {
  const context = createContext({ skills: [] });
  const results = {};
  const mods = ['PD2', 'PoD', 'vanilla'];
  const classes = ['amazon', 'assassin', 'barbarian', 'druid', 'necromancer', 'paladin', 'sorceress'];

  for (const mod of mods) {
    results[mod] = {};
    for (const cls of classes) {
      const filePath = path.join(dataDir, 'skills', mod, `${cls}.js`);
      try {
        const freshContext = createContext({ skills: [] });
        const code = readFileSync(filePath, 'utf-8');
        runInContext(code, freshContext, { filename: `${mod}/${cls}.js` });

        // Find the skills array and character object by naming convention
        const skillsKey = mod === 'PoD' ? `skills_${cls}`
          : mod === 'PD2' ? `skills_pd2_${cls}`
          : `skills_${cls}_vanilla`;
        const charKey = mod === 'PoD' ? `character_${cls}`
          : mod === 'PD2' ? `character_pd2_${cls}`
          : `character_${cls}_vanilla`;

        results[mod][cls] = {
          skills: freshContext[skillsKey] || null,
          character: freshContext[charKey] || null,
          filePath,
        };
      } catch (e) {
        results[mod][cls] = { skills: null, character: null, filePath, error: e.message };
      }
    }
  }
  return results;
}

function loadAllScripts() {
  // Loads all scripts referenced by index.html to check for parse errors
  const indexPath = path.join(__dirname, '..', 'index.html');
  const html = readFileSync(indexPath, 'utf-8');
  const scriptRegex = /src="\.?\/?([^"]+\.js)"/g;
  const errors = [];
  let match;

  while ((match = scriptRegex.exec(html)) !== null) {
    const src = match[1];
    if (src.startsWith('http')) continue; // skip external scripts
    const filePath = path.join(__dirname, '..', src);
    try {
      const code = readFileSync(filePath, 'utf-8');
      // Parse-check: create a fresh context and try to run the script
      const context = createContext({ skills: [], document: { getElementById: () => null } });
      runInContext(code, context, { filename: src });
    } catch (e) {
      errors.push({ file: src, error: e.message });
    }
  }
  return errors;
}

module.exports = { loadDataFiles, loadSkillFiles, loadAllScripts };
