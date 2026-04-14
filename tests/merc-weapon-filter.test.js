const { describe, it } = require('node:test');
const assert = require('node:assert');
const { loadDataFiles } = require('./load-data');

const { equipment } = loadDataFiles();

// Replicate the loadItems filtering logic from data/functions.js
// to verify which weapons are visible for each merc type.
function getVisibleWeapons(className) {
  const group = 'weapon';
  const visible = [];
  for (const item of equipment[group]) {
    let halt = 0;
    if (typeof item.only !== 'undefined') {
      if (item.only !== className) {
        halt = 1;
        if (className === 'Barb (merc)' && item.only === 'barbarian') { halt = 0; }
        if (className === 'Rogue Scout' && item.only === 'amazon' && (item.type === 'bow' || item.type === 'crossbow')) { halt = 0; }
        if (className === 'Iron Wolf' && item.only === 'sorceress') { halt = 0; }
        if (className === 'Iron Wolf' && item.only === 'paladin') { halt = 0; }
        if (className === item.only) { halt = 0; }
      }
    }
    if (halt === 0) {
      if (className === 'clear') { halt = 1; }
      if (typeof item.not !== 'undefined') { for (const n of item.not) { if (n === className) { halt = 1; } } }
      if (className === 'Rogue Scout') { if ((group === 'offhand' && item.type !== 'quiver' && item.name !== 'Offhand') || (group === 'weapon' && item.type !== 'bow' && item.type !== 'crossbow' && item.name !== 'Weapon')) { halt = 1; } }
      if (className === 'Desert Guard') { if (group === 'offhand' || (group === 'weapon' && item.type !== 'polearm' && item.type !== 'spear' && item.name !== 'Weapon')) { halt = 1; } }
      if (className === 'Iron Wolf') { if ((group === 'offhand' && item.type !== 'shield' && item.name !== 'Offhand') || (group === 'weapon' && ((item.type !== 'sword' && item.type !== 'orb' && item.type !== 'scepter' && item.subtype !== 'mace') || typeof item.twoHanded !== 'undefined') && item.name !== 'Weapon')) { halt = 1; } }
      if (className === 'Ascendant') { if (group === 'offhand' || (group === 'weapon' && item.type !== 'staff' && item.name !== 'Weapon')) { halt = 1; } }
      if (className === 'Barb (merc)') { if (group === 'offhand' || (group === 'weapon' && item.type !== 'sword' && item.type !== 'axe' && item.name !== 'Weapon' && item.subtype !== 'hammer' && item.subtype !== 'mace')) { halt = 1; } }
    }
    if (halt === 0) {
      visible.push(item);
    }
  }
  return visible;
}

// -- Iron Wolf (Act 3 merc) weapon visibility --

describe('Iron Wolf merc weapon filter', () => {
  const ironWolfWeapons = getVisibleWeapons('Iron Wolf');
  const ironWolfNames = ironWolfWeapons.map(i => i.name);

  it('shows Tal Rashas Lidless Eye (sorceress orb) for Iron Wolf', () => {
    assert.ok(
      ironWolfNames.includes("Tal Rasha's Lidless Eye"),
      "Tal Rasha's Lidless Eye should appear in Iron Wolf weapon list"
    );
  });

  it('shows all sorceress orbs for Iron Wolf', () => {
    const allOrbs = equipment.weapon.filter(i => i.type === 'orb');
    const visibleOrbs = ironWolfWeapons.filter(i => i.type === 'orb');
    assert.strictEqual(
      visibleOrbs.length, allOrbs.length,
      'All ' + allOrbs.length + ' orbs should be visible for Iron Wolf, got ' + visibleOrbs.length + '. ' +
      'Missing: ' + allOrbs.filter(o => !ironWolfNames.includes(o.name)).map(o => o.name).join(', ')
    );
  });

  it('shows swords for Iron Wolf', () => {
    const visibleSwords = ironWolfWeapons.filter(i => i.type === 'sword' && typeof i.twoHanded === 'undefined');
    assert.ok(visibleSwords.length > 0, 'Iron Wolf should see one-handed swords');
  });

  it('shows scepters for Iron Wolf', () => {
    const visibleScepters = ironWolfWeapons.filter(i => i.type === 'scepter');
    assert.ok(visibleScepters.length > 0, 'Iron Wolf should see scepters');
  });

  it('hides two-handed weapons from Iron Wolf', () => {
    const twoHanders = ironWolfWeapons.filter(i => typeof i.twoHanded !== 'undefined' && i.name !== 'Weapon');
    assert.strictEqual(twoHanders.length, 0,
      'Iron Wolf should not see two-handed weapons, but found: ' + twoHanders.map(i => i.name).join(', ')
    );
  });

  it('hides bows from Iron Wolf', () => {
    const bows = ironWolfWeapons.filter(i => i.type === 'bow');
    assert.strictEqual(bows.length, 0, 'Iron Wolf should not see bows');
  });
});

// -- Class restriction integrity --

describe('class restriction integrity', () => {
  it('sorceress-only items are hidden from non-sorceress characters', () => {
    const amazonWeapons = getVisibleWeapons('amazon');
    const sorcItems = amazonWeapons.filter(i => i.only === 'sorceress');
    assert.strictEqual(sorcItems.length, 0,
      'Amazon should not see sorceress-only items, but found: ' + sorcItems.map(i => i.name).join(', ')
    );
  });

  it('assassin-only items are hidden from non-assassin characters', () => {
    const barbarianWeapons = getVisibleWeapons('barbarian');
    const assassinItems = barbarianWeapons.filter(i => i.only === 'assassin');
    assert.strictEqual(assassinItems.length, 0,
      'Barbarian should not see assassin-only items, but found: ' + assassinItems.map(i => i.name).join(', ')
    );
  });

  it('sorceress sees her own class-restricted weapons', () => {
    const sorcWeapons = getVisibleWeapons('sorceress');
    const sorcOnlyItems = sorcWeapons.filter(i => i.only === 'sorceress');
    const allSorcOnly = equipment.weapon.filter(i => i.only === 'sorceress');
    assert.strictEqual(sorcOnlyItems.length, allSorcOnly.length,
      'Sorceress should see all ' + allSorcOnly.length + ' sorceress-only weapons'
    );
  });
});

// -- Other merc class restrictions --

describe('other merc class restrictions', () => {

  it('Rogue Scout sees amazon bows', () => {
    const rogueWeapons = getVisibleWeapons('Rogue Scout');
    const amazonBows = rogueWeapons.filter(i => i.only === 'amazon' && (i.type === 'bow' || i.type === 'crossbow'));
    assert.ok(amazonBows.length > 0, 'Rogue Scout should see amazon bows/crossbows');
  });
});
