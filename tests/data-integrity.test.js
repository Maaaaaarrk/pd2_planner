const { describe, it } = require('node:test');
const assert = require('node:assert');
const { loadDataFiles, loadSkillFiles, loadAllScripts } = require('./load-data');

const { unequipped, sets, equipment, stats } = loadDataFiles();
const skillData = loadSkillFiles();

// Equipment categories that contain gear items
const gearCategories = ['weapon', 'helm', 'armor', 'offhand', 'gloves', 'boots', 'belt', 'amulet', 'ring1', 'ring2'];

// Keys used in set_bonuses and item stats that are not in unequipped
const SPECIAL_KEYS = new Set([
  'strike_skill', 'strike_chance', 'strike_level',
  'cast_skill', 'cast_chance', 'cast_level',
  'gethit_skill', 'gethit_chance', 'gethit_level',
  'dmg_pois', 'dmg_pois_time',
  'reanimate',
  'ar_per_level', 'defense_per_level',
  'ar_skillup', 'pierce_skillup',
  'abs_cold_lvl',
  'dexterity_per_level',
  'oskill_Poison_Strike',
]);

// Item-only keys that are metadata, not stats
const ITEM_META_KEYS = new Set([
  'name', 'base', 'type', 'subtype', 'rarity', 'img', 'only', 'not',
  'req_level', 'req', 'twoHanded', 'sockets', 'indestructible', 'ethereal',
  'autoreplenish', 'size', 'pd2', 'pod', 'boss_item',
  'set_bonuses', 'charges_skill', 'charges_charges', 'charges_level',
]);

// Collect all set items grouped by set key
function getSetItemsByKey() {
  const setItems = {};
  for (const category of gearCategories) {
    const items = equipment[category];
    if (!items) continue;
    for (const item of items) {
      if (item.rarity !== 'set') continue;
      for (const key of Object.keys(item)) {
        if (key.startsWith('set_') && key !== 'set_bonuses' && item[key] === 1) {
          if (!setItems[key]) setItems[key] = [];
          setItems[key].push({ item, category });
        }
      }
    }
  }
  return setItems;
}

const setItemsByKey = getSetItemsByKey();

// ─── Script Loading ──────────────────────────────────────────────────

describe('script loading', () => {
  it('all scripts referenced by index.html load without parse errors', () => {
    const errors = loadAllScripts();
    if (errors.length > 0) {
      const msg = errors.map(e => `${e.file}: ${e.error}`).join('\n  ');
      assert.fail(`Script parse errors:\n  ${msg}`);
    }
  });
});

// ─── Structural Sanity ───────────────────────────────────────────────

describe('structural sanity', () => {
  it('equipment categories exist and have items', () => {
    for (const cat of gearCategories) {
      assert.ok(Array.isArray(equipment[cat]), `equipment.${cat} should be an array`);
    }
  });

  it('sets object is non-empty', () => {
    assert.ok(Object.keys(sets).length > 0, 'sets should have entries');
  });

  it('unequipped has expected number of properties', () => {
    const count = Object.keys(unequipped).length;
    assert.ok(count > 100, `unequipped should have >100 properties, got ${count}`);
  });

  it('charms category exists', () => {
    assert.ok(Array.isArray(equipment.charms), 'equipment.charms should be an array');
    assert.ok(equipment.charms.length > 0, 'equipment.charms should not be empty');
  });
});

// ─── Set Reference Integrity ─────────────────────────────────────────

describe('set reference integrity', () => {
  it('every set item set_bonuses[0] references a valid set key', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (!item.set_bonuses) continue;
        const setKey = item.set_bonuses[0];
        if (!sets[setKey]) {
          errors.push(`${item.name}: set_bonuses[0] = "${setKey}" not found in sets`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid set references:\n  ${errors.join('\n  ')}`);
  });

  it('every set_XX property on set items maps to a valid set key', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (item.rarity !== 'set') continue;
        for (const key of Object.keys(item)) {
          if (key.startsWith('set_') && key !== 'set_bonuses' && item[key] === 1) {
            if (!sets[key]) {
              errors.push(`${item.name}: property "${key}" not found in sets`);
            }
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid set keys:\n  ${errors.join('\n  ')}`);
  });

  it('every set in sets has at least one item referencing it', () => {
    const errors = [];
    for (const setKey of Object.keys(sets)) {
      if (!setItemsByKey[setKey] || setItemsByKey[setKey].length === 0) {
        errors.push(`${setKey} ("${sets[setKey][0]}") has no items referencing it`);
      }
    }
    assert.strictEqual(errors.length, 0, `Orphaned sets:\n  ${errors.join('\n  ')}`);
  });
});

// ─── Set Member Count ────────────────────────────────────────────────

describe('set member count consistency', () => {
  it('sets array length matches item count + 1', () => {
    const errors = [];
    for (const setKey of Object.keys(sets)) {
      const setArray = sets[setKey];
      const itemCount = setItemsByKey[setKey] ? setItemsByKey[setKey].length : 0;
      const expectedLength = itemCount + 1;
      if (setArray.length !== expectedLength) {
        errors.push(
          `${setKey} ("${setArray[0]}"): array length ${setArray.length}, ` +
          `expected ${expectedLength} (${itemCount} items + 1 name)`
        );
      }
    }
    assert.strictEqual(errors.length, 0, `Set array length mismatches:\n  ${errors.join('\n  ')}`);
  });
});

// ─── Per-Item set_bonuses Structure ──────────────────────────────────

describe('per-item set_bonuses structure', () => {
  it('set_bonuses arrays are always length 7', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (!item.set_bonuses) continue;
        if (item.set_bonuses.length !== 7) {
          errors.push(`${item.name}: set_bonuses length ${item.set_bonuses.length}, expected 7`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid set_bonuses lengths:\n  ${errors.join('\n  ')}`);
  });

  it('set_bonuses[0] is a string (set key)', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (!item.set_bonuses) continue;
        if (typeof item.set_bonuses[0] !== 'string') {
          errors.push(`${item.name}: set_bonuses[0] is ${typeof item.set_bonuses[0]}, expected string`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid set_bonuses[0] types:\n  ${errors.join('\n  ')}`);
  });

  it('set_bonuses[1..6] are objects', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (!item.set_bonuses) continue;
        for (let i = 1; i <= 6; i++) {
          const slot = item.set_bonuses[i];
          if (typeof slot !== 'object' || slot === null || Array.isArray(slot)) {
            errors.push(`${item.name}: set_bonuses[${i}] is not a plain object`);
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid set_bonuses slot types:\n  ${errors.join('\n  ')}`);
  });
});

// ─── Stat Property Validation ────────────────────────────────────────

describe('stat property validation', () => {
  it('all stat keys in shared set bonuses exist in unequipped', () => {
    const errors = [];
    for (const setKey of Object.keys(sets)) {
      const setArray = sets[setKey];
      for (let i = 1; i < setArray.length; i++) {
        const bonusObj = setArray[i];
        for (const stat of Object.keys(bonusObj)) {
          if (!(stat in unequipped) && !SPECIAL_KEYS.has(stat)) {
            errors.push(`${setKey}[${i}]: stat "${stat}" not found in unequipped`);
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Unknown stats in shared set bonuses:\n  ${errors.join('\n  ')}`);
  });

  it('all stat keys in per-item set bonuses exist in unequipped', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (!item.set_bonuses) continue;
        for (let i = 1; i <= 6; i++) {
          const bonusObj = item.set_bonuses[i];
          if (!bonusObj) continue;
          for (const stat of Object.keys(bonusObj)) {
            if (!(stat in unequipped) && !SPECIAL_KEYS.has(stat)) {
              errors.push(`${item.name} set_bonuses[${i}]: stat "${stat}" not found in unequipped`);
            }
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Unknown stats in per-item set bonuses:\n  ${errors.join('\n  ')}`);
  });
});

// ─── No Duplicate Set Members ────────────────────────────────────────

describe('no duplicate set members', () => {
  it('no two items with the same name in a set', () => {
    const errors = [];
    for (const setKey of Object.keys(setItemsByKey)) {
      const names = setItemsByKey[setKey].map(e => e.item.name);
      const seen = new Set();
      for (const name of names) {
        if (seen.has(name)) {
          errors.push(`${setKey}: duplicate item "${name}"`);
        }
        seen.add(name);
      }
    }
    assert.strictEqual(errors.length, 0, `Duplicate set members:\n  ${errors.join('\n  ')}`);
  });
});

// ─── Required Properties on Set Items ────────────────────────────────

describe('required properties on set items', () => {
  it('every set item has name, rarity "set", and img (except amulets/rings)', () => {
    const noImgCategories = new Set(['amulet', 'ring1', 'ring2']);
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (item.rarity !== 'set') continue;
        if (!item.name) errors.push(`${category}: set item missing name`);
        if (!item.img && !noImgCategories.has(category)) {
          errors.push(`${item.name || '(unnamed)'} in ${category}: missing img`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Missing required properties:\n  ${errors.join('\n  ')}`);
  });

  it('every set item has a set_bonuses array', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (item.rarity !== 'set') continue;
        if (!Array.isArray(item.set_bonuses)) {
          errors.push(`${item.name} in ${category}: missing set_bonuses array`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Missing set_bonuses:\n  ${errors.join('\n  ')}`);
  });
});

// ─── All Items Validation ────────────────────────────────────────────

describe('all items validation', () => {
  it('every equipment item has a name', () => {
    const errors = [];
    for (const category of [...gearCategories, 'charms']) {
      const items = equipment[category];
      if (!items) continue;
      for (let idx = 0; idx < items.length; idx++) {
        if (!items[idx].name) {
          errors.push(`${category}[${idx}]: missing name`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Items missing name:\n  ${errors.join('\n  ')}`);
  });

  it('unique and set items in gear categories have an img (except amulets/rings)', () => {
    const noImgCategories = new Set(['amulet', 'ring1', 'ring2']);
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (item.rarity !== 'unique' && item.rarity !== 'set') continue;
        if (noImgCategories.has(category)) continue;
        if (!item.img) {
          errors.push(`${item.name} in ${category}: missing img`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Items missing img:\n  ${errors.join('\n  ')}`);
  });

  it('items with a base reference use valid base names', () => {
    // Just verify base is a non-empty string when present
    const errors = [];
    for (const category of [...gearCategories, 'charms']) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if ('base' in item && (typeof item.base !== 'string' || item.base === '')) {
          errors.push(`${item.name} in ${category}: invalid base "${item.base}"`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid base references:\n  ${errors.join('\n  ')}`);
  });

  it('no duplicate item names within a category (excluding _2 variants)', () => {
    const errors = [];
    for (const category of gearCategories) {
      const items = equipment[category];
      if (!items) continue;
      const names = new Map();
      for (const item of items) {
        if (!item.name || item.name.endsWith('_2')) continue;
        if (names.has(item.name)) {
          errors.push(`${category}: duplicate "${item.name}"`);
        }
        names.set(item.name, true);
      }
    }
    assert.strictEqual(errors.length, 0, `Duplicate items:\n  ${errors.join('\n  ')}`);
  });

  it('req_level is a valid number when present', () => {
    const errors = [];
    for (const category of [...gearCategories, 'charms']) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        if (!('req_level' in item)) continue;
        if (item.req_level !== '' && (typeof item.req_level !== 'number' || item.req_level < 0)) {
          errors.push(`${item.name} in ${category}: invalid req_level "${item.req_level}"`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid req_level values:\n  ${errors.join('\n  ')}`);
  });

  it('all item stat keys have display entries in var stats (item_metadata.js)', () => {
    // Keys that are item metadata, not displayable stats
    const metaKeys = new Set([
      'name', 'base', 'type', 'subtype', 'rarity', 'img', 'only', 'not',
      'req_level', 'req', 'twoHanded', 'sockets', 'indestructible', 'ethereal',
      'autoreplenish', 'size', 'pd2', 'pod', 'boss_item',
      'set_bonuses', 'charges_skill', 'charges_charges', 'charges_level',
      'aura', 'aura_lvl', 'ctc', 'melee_splash',
    ]);
    // CTC/trigger keys with optional _N suffixes - handled by special display code
    const ctcPattern = /^(strike|cast|gethit|onblock|ondeath|onkill|onlevel|charges)_(skill|chance|level|charges)(_\d+)?$/;
    // Keys referenced via compound stat entries (inside another entry's index array)
    const compoundKeys = new Set(['dmg_pois_time', 'equipped_skill_level', 'random_skill_level', 'mindmg_per_energy']);

    // Build full set of known stat keys (top-level + referenced in index arrays)
    const knownStats = new Set(Object.keys(stats));
    for (const entry of Object.values(stats)) {
      if (entry.index) entry.index.forEach(k => knownStats.add(k));
    }

    const errors = [];
    for (const category of [...gearCategories, 'charms']) {
      const items = equipment[category];
      if (!items) continue;
      for (const item of items) {
        for (const key of Object.keys(item)) {
          if (key.startsWith('set_') && key !== 'set_bonuses') continue;
          if (metaKeys.has(key)) continue;
          if (ctcPattern.test(key)) continue;
          if (compoundKeys.has(key)) continue;
          if (!knownStats.has(key)) {
            errors.push(`${item.name} in ${category}: stat "${key}" has no display entry in var stats`);
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Item stats missing from var stats in item_metadata.js:\n  ${errors.join('\n  ')}`);
  });
});

// ─── Skill Data Validation ───────────────────────────────────────────

describe('skill data validation', () => {
  const mods = ['PD2'];
  const classes = ['amazon', 'assassin', 'barbarian', 'druid', 'necromancer', 'paladin', 'sorceress'];

  it('all skill files load without errors', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (data.error) {
          errors.push(`${mod}/${cls}.js: ${data.error}`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Skill files with load errors:\n  ${errors.join('\n  ')}`);
  });

  it('all skill files produce a skills array', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.skills) {
          errors.push(`${mod}/${cls}.js: skills array not found`);
        } else if (!Array.isArray(data.skills)) {
          errors.push(`${mod}/${cls}.js: skills is not an array`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Missing skills arrays:\n  ${errors.join('\n  ')}`);
  });

  it('all skill files produce a character object', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.character) {
          errors.push(`${mod}/${cls}.js: character object not found`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Missing character objects:\n  ${errors.join('\n  ')}`);
  });

  it('every skill has required properties', () => {
    const requiredProps = ['name', 'key', 'level', 'req', 'reqlvl'];
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.skills) continue;
        for (let i = 0; i < data.skills.length; i++) {
          const skill = data.skills[i];
          for (const prop of requiredProps) {
            if (!(prop in skill)) {
              errors.push(`${mod}/${cls} skill[${i}] "${skill.name || '?'}": missing "${prop}"`);
            }
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Skills missing required properties:\n  ${errors.join('\n  ')}`);
  });

  it('skill indices are sequential', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.skills) continue;
        for (let i = 0; i < data.skills.length; i++) {
          const skill = data.skills[i];
          if ('i' in skill && skill.i !== i) {
            errors.push(`${mod}/${cls} skill "${skill.name}": index ${skill.i} != position ${i}`);
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Skill index mismatches:\n  ${errors.join('\n  ')}`);
  });

  it('skill prerequisites reference valid indices', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.skills) continue;
        const maxIdx = data.skills.length - 1;
        for (const skill of data.skills) {
          if (!skill.req) continue;
          for (const reqIdx of skill.req) {
            if (reqIdx < 0 || reqIdx > maxIdx) {
              errors.push(`${mod}/${cls} "${skill.name}": prereq index ${reqIdx} out of range [0..${maxIdx}]`);
            }
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Invalid skill prerequisites:\n  ${errors.join('\n  ')}`);
  });

  it('every skill has a data object with values array', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.skills) continue;
        for (const skill of data.skills) {
          if (!skill.data) {
            errors.push(`${mod}/${cls} "${skill.name}": missing data object`);
          } else if (!skill.data.values || !Array.isArray(skill.data.values)) {
            errors.push(`${mod}/${cls} "${skill.name}": data.values is not an array`);
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Skills missing data:\n  ${errors.join('\n  ')}`);
  });

  it('character objects have required base stats', () => {
    const requiredStats = ['class_name', 'strength', 'dexterity', 'vitality', 'energy', 'life', 'mana'];
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.character) continue;
        for (const stat of requiredStats) {
          if (!(stat in data.character)) {
            errors.push(`${mod}/${cls} character: missing "${stat}"`);
          }
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Characters missing required stats:\n  ${errors.join('\n  ')}`);
  });

  it('each class has a reasonable number of skills (20-40)', () => {
    const errors = [];
    for (const mod of mods) {
      for (const cls of classes) {
        const data = skillData[mod][cls];
        if (!data.skills) continue;
        const count = data.skills.length;
        if (count < 20 || count > 40) {
          errors.push(`${mod}/${cls}: ${count} skills (expected 20-40)`);
        }
      }
    }
    assert.strictEqual(errors.length, 0, `Unexpected skill counts:\n  ${errors.join('\n  ')}`);
  });
});
