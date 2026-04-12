// export_d2s.js
//   Converts the planner's character state into the "rip" JSON format
//   used by the bug-free-eureka export tool, then opens the external tool
//   with the JSON pre-loaded so the user can download a .d2s game save file.

// Property key to D2 property text mapping
// Maps the planner's internal stat keys to the text format the export tool's parser expects
var propKeyToText = {
	// Enhanced Damage
	e_damage: function(v) { return "+" + v + "% Enhanced Damage" },
	e_def: function(v) { return "+" + v + "% Enhanced Defense" },

	// Attributes
	strength: function(v) { return "+" + v + " to Strength" },
	dexterity: function(v) { return "+" + v + " to Dexterity" },
	vitality: function(v) { return "+" + v + " to Vitality" },
	energy: function(v) { return "+" + v + " to Energy" },
	all_attributes: function(v) { return "+" + v + " to all Attributes" },

	// Life/Mana
	life: function(v) { return "+" + v + " to Life" },
	mana: function(v) { return "+" + v + " to Mana" },
	max_life: function(v) { return "Increase Maximum Life " + v + "%" },
	max_mana: function(v) { return "Increase Maximum Mana " + v + "%" },
	life_per_level: function(v) { return "+" + v + " to Life (Based on Character Level)" },
	mana_per_level: function(v) { return "+" + v + " to Mana (Based on Character Level)" },

	// Resistances
	fRes: function(v) { return "Fire Resist +" + v + "%" },
	cRes: function(v) { return "Cold Resist +" + v + "%" },
	lRes: function(v) { return "Lightning Resist +" + v + "%" },
	pRes: function(v) { return "Poison Resist +" + v + "%" },
	all_res: function(v) { return "All Resistances +" + v },
	fRes_max: function(v) { return "+" + v + "% to Maximum Fire Resist" },
	cRes_max: function(v) { return "+" + v + "% to Maximum Cold Resist" },
	lRes_max: function(v) { return "+" + v + "% to Maximum Lightning Resist" },
	pRes_max: function(v) { return "+" + v + "% to Maximum Poison Resist" },

	// Attack
	ar: function(v) { return "+" + v + " to Attack Rating" },
	ar_skillup: function(v) { return "+" + v + "% Bonus to Attack Rating" },
	ar_per_level: function(v) { return "+" + v + " to Attack Rating (Based on Character Level)" },
	ar_vs_demons: function(v) { return "+" + v + " to Attack Rating against Demons" },
	ar_vs_undead: function(v) { return "+" + v + " to Attack Rating against Undead" },

	// Damage
	damage_min: function(v) { return "+" + v + " to Minimum Damage" },
	damage_max: function(v) { return "+" + v + " to Maximum Damage" },
	min_damage_per_level: function(v) { return "+" + v + " to Minimum Damage (Based on Character Level)" },
	max_damage_per_level: function(v) { return "+" + v + " to Maximum Damage (Based on Character Level)" },
	damage_vs_demons: function(v) { return "+" + v + "% Damage to Demons" },
	damage_vs_undead: function(v) { return "+" + v + "% Damage to Undead" },
	damage_vs_undead_per_level: function(v) { return "+" + v + "% Damage to Undead (Based on Character Level)" },

	// Elemental Damage
	fDamage_max: function(v) { return "Adds 1-" + v + " Fire Damage" },
	cDamage_max: function(v) { return "Adds 1-" + v + " Cold Damage" },
	lDamage_max: function(v) { return "Adds 1-" + v + " Lightning Damage" },
	mDamage_min: function(v, item) {
		var max = item.mDamage_max || v;
		return "Adds " + v + "-" + max + " Magic Damage"
	},

	// Leech
	life_leech: function(v) { return v + "% Life Stolen per Hit" },
	mana_leech: function(v) { return v + "% Mana Stolen per Hit" },

	// Speed
	ias: function(v) { return v + "% Increased Attack Speed" },
	fcr: function(v) { return v + "% Faster Cast Rate" },
	fhr: function(v) { return v + "% Faster Hit Recovery" },
	frw: function(v) { return v + "% Faster Run/Walk" },

	// Skills
	all_skills: function(v) { return "+" + v + " to All Skills" },
	skills_amazon: function(v) { return "+" + v + " to Amazon Skills" },
	skills_assassin: function(v) { return "+" + v + " to Assassin Skills" },
	skills_barbarian: function(v) { return "+" + v + " to Barbarian Skills" },
	skills_druid: function(v) { return "+" + v + " to Druid Skills" },
	skills_necromancer: function(v) { return "+" + v + " to Necromancer Skills" },
	skills_paladin: function(v) { return "+" + v + " to Paladin Skills" },
	skills_sorceress: function(v) { return "+" + v + " to Sorceress Skills" },

	// Skill tabs
	skills_javelins: function(v) { return "+" + v + " to Javelin and Spear Skills (Amazon Only)" },
	skills_passives: function(v) { return "+" + v + " to Passive and Magic Skills (Amazon Only)" },
	skills_bows: function(v) { return "+" + v + " to Bow and Crossbow Skills (Amazon Only)" },
	skills_martial: function(v) { return "+" + v + " to Martial Arts (Assassin Only)" },
	skills_shadow: function(v) { return "+" + v + " to Shadow Disciplines (Assassin Only)" },
	skills_traps: function(v) { return "+" + v + " to Traps (Assassin Only)" },
	skills_warcries: function(v) { return "+" + v + " to Warcries (Barbarian Only)" },
	skills_masteries: function(v) { return "+" + v + " to Combat Masteries (Barbarian Only)" },
	skills_combat_barbarian: function(v) { return "+" + v + " to Combat Skills (Barbarian Only)" },
	skills_elemental: function(v) { return "+" + v + " to Elemental (Druid Only)" },
	skills_shapeshifting: function(v) { return "+" + v + " to Shape Shifting (Druid Only)" },
	skills_summoning_druid: function(v) { return "+" + v + " to Summoning (Druid Only)" },
	skills_summoning_necromancer: function(v) { return "+" + v + " to Summoning (Necromancer Only)" },
	skills_poisonBone: function(v) { return "+" + v + " to Poison and Bone (Necromancer Only)" },
	skills_curses: function(v) { return "+" + v + " to Curses (Necromancer Only)" },
	skills_offensive: function(v) { return "+" + v + " to Offensive Auras (Paladin Only)" },
	skills_defensive: function(v) { return "+" + v + " to Defensive Auras (Paladin Only)" },
	skills_combat_paladin: function(v) { return "+" + v + " to Combat Skills (Paladin Only)" },
	skills_cold: function(v) { return "+" + v + " to Cold Skills (Sorceress Only)" },
	skills_lightning: function(v) { return "+" + v + " to Lightning Skills (Sorceress Only)" },
	skills_fire: function(v) { return "+" + v + " to Fire Skills (Sorceress Only)" },

	// Misc combat
	cblow: function(v) { return v + "% Chance of Crushing Blow" },
	dstrike: function(v) { return v + "% Deadly Strike" },
	owounds: function(v) { return v + "% Chance of Open Wounds" },
	pmh: function(v) { return "Prevent Monster Heal" },
	itd: function(v) { return "Ignore Target's Defense" },
	knockback: function(v) { return "Knockback" },
	cbf: function(v) { return "Cannot Be Frozen" },
	half_freeze: function(v) { return "Half Freeze Duration" },
	slows_target: function(v) { return "Slows Target by " + v + "%" },
	freezes_target: function(v) { return "Freezes Target +" + v },
	melee_splash: function(v) { return "Melee Attacks Deal Splash Damage" },

	// Defense
	defense: function(v) { return "+" + v + " Defense" },
	defense_bonus: function(v) { return v + "% Enhanced Defense" },
	defense_per_level: function(v) { return "+" + v + " Defense (Based on Character Level)" },
	block: function(v) { return v + "% Increased Chance of Blocking" },
	pdr: function(v) { return "Damage Reduced by " + v + "%" },
	damage_reduced: function(v) { return "Damage Reduced by " + v },
	mDamage_reduced: function(v) { return "Magic Damage Reduced by " + v },

	// Absorb
	fAbsorb: function(v) { return v + "% Fire Absorb" },
	cAbsorb: function(v) { return v + "% Cold Absorb" },
	lAbsorb: function(v) { return v + "% Lightning Absorb" },
	fAbsorb_flat: function(v) { return "+" + v + " Fire Absorb" },
	cAbsorb_flat: function(v) { return "+" + v + " Cold Absorb" },
	lAbsorb_flat: function(v) { return "+" + v + " Lightning Absorb" },

	// Other
	mf: function(v) { return v + "% Better Chance of Getting Magic Items" },
	gf: function(v) { return v + "% Extra Gold from Monsters" },
	mf_per_level: function(v) { return v + "% Better Chance of Getting Magic Items (Based on Character Level)" },
	gf_per_level: function(v) { return v + "% Extra Gold from Monsters (Based on Character Level)" },
	light_radius: function(v) { return "+" + v + " to Light Radius" },
	experience: function(v) { return "+" + v + "% to Experience Gained" },
	life_replenish: function(v) { return "Replenish Life +" + v },
	life_per_kill: function(v) { return "+" + v + " Life after each Kill" },
	mana_per_kill: function(v) { return "+" + v + " Mana after each Kill" },
	life_per_hit: function(v) { return "+" + v + " Life after each Hit" },
	mana_per_hit: function(v) { return "+" + v + " Mana after each Hit" },
	thorns: function(v) { return "Attacker Takes Damage of " + v },
	thorns_per_level: function(v) { return "Attacker Takes Damage of " + v + " (Based on Character Level)" },
	damage_to_mana: function(v) { return v + "% Damage Taken Goes to Mana" },
	req: function(v) { return "Requirements " + v + "%" },
	sockets: function(v) { return "Socketed (" + v + ")" },
	indestructible: function(v) { return "Indestructible" },

	// Enemy modifiers
	enemy_fRes: function(v) { return "-" + v + "% to Enemy Fire Resistance" },
	enemy_cRes: function(v) { return "-" + v + "% to Enemy Cold Resistance" },
	enemy_lRes: function(v) { return "-" + v + "% to Enemy Lightning Resistance" },
	enemy_pRes: function(v) { return "-" + v + "% to Enemy Poison Resistance" },
	enemy_phyRes: function(v) { return "-" + v + "% to Enemy Physical Resistance" },
	target_defense: function(v) { return "-" + v + "% Target Defense" },

	// Regen
	mana_regen: function(v) { return "Regenerate Mana " + v + "%" },
	pierce: function(v) { return v + "% chance of Piercing Attack" },
};

// Keys to skip when building property text (these are metadata, not D2 properties)
var skipKeys = {
	name:1, type:1, subtype:1, base:1, rarity:1, group:1, tier:1, img:1,
	req_level:1, req_strength:1, req_dexterity:1, set_bonuses:1,
	twoHanded:1, twoHands:1, size:1, not:1, only:1, upgrade:1, downgrade:1,
	boss_item:1, pd2:1, debug:1, base_defense:1, def_low:1, def_high:1,
	durability:1, max_sockets:1, nonmetal:1, range:1, base_damage_min:1,
	base_damage_max:1, base_min_alternate:1, base_max_alternate:1,
	kick_min:1, smite_min:1, smite_max:1, velocity:1, weapon_frames:1,
	mDamage_max:1,  // handled together with mDamage_min
	special:1, sup:1, durability_extra:1, autorepair:1, autoreplenish:1,
	stack_size:1, ethereal:1,  // ethereal is handled separately in the item name
	// ctc/cskill handled separately
	strike_skill:1, strike_chance:1, strike_level:1,
	strike_skill_1:1, strike_chance_1:1, strike_level_1:1,
	cast_skill:1, cast_chance:1, cast_level:1,
	gethit_skill:1, gethit_chance:1, gethit_level:1,
	charges_skill:1, charges_charges:1, charges_level:1,
	aura:1, aura_lvl:1,
	// open wounds damage display value (owounds_dps is informational)
	owounds_dps:1,
	// set tracking keys
	set_IK:1, set_Mav:1, set_Gris:1, set_TO:1, set_TR:1, set_Nat:1,
	set_Ald:1, set_BK:1, set_Disciple:1, set_Angelic:1, set_Cathan:1,
	set_Cow:1, set_Brethren:1, set_Hwanin:1, set_Naj:1, set_Orphan:1,
	set_Sander:1, set_Sazabi:1, set_Arcanna:1, set_Arctic:1, set_Berserker:1,
	set_Civerb:1, set_Cleglaw:1, set_Death:1, set_Hsarus:1, set_Infernal:1,
	set_Iratha:1, set_Isenhart:1, set_Milabrega:1, set_Sigon:1, set_Tancred:1,
	set_Vidala:1,
	// dmg_pois is a display helper, handled separately
	dmg_pois:1, dmg_pois_time:1,
	// misc display helpers
	owounds_duration:1, owounds_dps_per_level:1,
	explosive_attack:1, magic_attack:1,
	extra_Skeleton_Warriors:1, extra_Skeleton_Archers:1, extra_Revives:1,
	extra_Spirit_Wolf:1, extra_Valkyrie:1, extra_Grizzly:1,
	extra_mainhand_attack:1, curse_effectiveness:1,
	pierce_skillup:1, // handled as bonus
	ar_bonus:1, // some items use this but it maps to ar_skillup
};


// Convert an item's properties to an array of text strings
function itemPropsToText(item) {
	var props = [];
	for (var key in item) {
		if (skipKeys[key]) continue;
		var val = item[key];
		if (val === 0 || val === "" || val === "none" || typeof val === 'undefined') continue;

		// Handle individual skill bonuses (skill_XXX keys)
		if (key.startsWith("skill_")) {
			var skillName = key.substring(6).split("_").join(" ");
			props.push("+" + val + " to " + skillName);
			continue;
		}

		// Handle oskill bonuses
		if (key.startsWith("oskill_")) {
			var oskillName = key.substring(7).split("_").join(" ");
			props.push("+" + val + " to " + oskillName);
			continue;
		}

		// Handle element-specific skill bonuses
		if (key.startsWith("skills_") && key.endsWith("_all")) {
			var elem = key.replace("skills_","").replace("_all","");
			var elemMap = {fire:"Fire",cold:"Cold",lightning:"Lightning",poison:"Poison",magic:"Magic"};
			if (elemMap[elem]) {
				props.push("+" + val + " to " + elemMap[elem] + " Skills");
				continue;
			}
		}

		if (propKeyToText[key]) {
			props.push(propKeyToText[key](val, item));
		}
	}

	// Handle ctc skills
	if (item.strike_skill && item.strike_chance && item.strike_level) {
		props.push(item.strike_chance + "% Chance to Cast Level " + item.strike_level + " " + item.strike_skill + " on Striking");
	}
	if (item.strike_skill_1 && item.strike_chance_1 && item.strike_level_1) {
		props.push(item.strike_chance_1 + "% Chance to Cast Level " + item.strike_level_1 + " " + item.strike_skill_1 + " on Striking");
	}
	if (item.cast_skill && item.cast_chance && item.cast_level) {
		props.push(item.cast_chance + "% Chance to Cast Level " + item.cast_level + " " + item.cast_skill + " when you Kill an Enemy");
	}
	if (item.gethit_skill && item.gethit_chance && item.gethit_level) {
		props.push(item.gethit_chance + "% Chance to Cast Level " + item.gethit_level + " " + item.gethit_skill + " when Struck");
	}

	// Handle auras
	if (item.aura && item.aura_lvl) {
		props.push("Level " + item.aura_lvl + " " + item.aura + " Aura When Equipped");
	}

	// Handle poison damage (special format)
	if (item.dmg_pois && item.dmg_pois_time) {
		props.push(Math.round(item.dmg_pois) + " poison damage over " + item.dmg_pois_time + " seconds");
	}

	// Handle pierce_skillup (bonus to pierce from items, different from pierce skill)
	if (item.pierce_skillup) {
		props.push("+" + item.pierce_skillup + "% to Pierce");
	}

	// Handle ar_bonus
	if (item.ar_bonus) {
		props.push("+" + item.ar_bonus + "% Bonus to Attack Rating");
	}

	return props;
}


// Determine the quality/rarity string for the rip format
function getItemRarity(item) {
	if (item.rarity === "set") return "Set";
	if (item.rarity === "rw") return "Unique";  // Runewords are treated as unique quality in rip format
	if (item.rarity === "magic") return "Magic";
	if (item.rarity === "rare") return "Rare";
	if (item.rarity === "crafted") return "Crafted";
	return "Unique";  // Default for named items
}


// Build a rip-format item from a planner equipment entry
function buildRipItem(itemName, slotGroup) {
	if (!itemName || itemName === "none") return null;

	// Find the item in equipment arrays
	var item = findEquipmentItem(itemName, slotGroup);
	if (!item) return null;

	// Get the base name
	var baseName = item.base || "";
	if (!baseName && typeof bases !== 'undefined') {
		// For charms and other items without a base field
		baseName = item.name;
	}

	var rarity = getItemRarity(item);
	var socketCount = item.sockets || 0;
	var typeStr = rarity + " " + baseName.split("_").join(" ");
	if (socketCount > 0) {
		typeStr += " (" + socketCount + ")";
	}

	// Clean item name (remove runeword base suffixes)
	var cleanName = item.name;
	if (item.rarity === "rw") {
		// Runeword names have format "Name - Base"
		var dashIndex = cleanName.indexOf(" \u00AD \u00AD - \u00AD \u00AD ");
		if (dashIndex > 0) {
			cleanName = cleanName.substring(0, dashIndex);
		}
	}

	var ripItem = {
		name: (item.ethereal ? "(Ethereal) " : "") + cleanName,
		type: typeStr,
		props: itemPropsToText(item)
	};

	// Add socket info
	if (socketCount > 0) {
		ripItem.sockets = [];
		// Add socketed items if we know them
		if (typeof socketed !== 'undefined' && socketed[slotGroup]) {
			for (var i = 0; i < socketed[slotGroup].items.length; i++) {
				var s = socketed[slotGroup].items[i];
				if (s.name && s.name !== "") {
					ripItem.sockets.push({
						name: s.name,
						props: []  // Socketable props are part of item totals
					});
				}
			}
		}
	}

	return ripItem;
}


// Find an equipment item by name across all equipment arrays
function findEquipmentItem(name, slotGroup) {
	if (typeof equipment === 'undefined') return null;

	// Search all equipment arrays
	var arrays = ["weapon", "helm", "armor", "offhand", "gloves", "boots", "belt",
		"amulet", "ring1", "charms"];

	for (var a = 0; a < arrays.length; a++) {
		var arr = equipment[arrays[a]];
		if (!arr) continue;
		for (var i = 0; i < arr.length; i++) {
			if (arr[i].name === name) return arr[i];
		}
	}

	return null;
}


// Build the full rip JSON from planner state
function buildRipJson() {
	if (typeof character === 'undefined' || !character.class_name) {
		alert("No character loaded. Please select a class first.");
		return null;
	}

	// Character name (use class name if no custom name)
	var charName = character.class_name || "MyCharacter";

	// Build stats array: [Level, Life, Mana, Str, Dex, Vit, Ene, ...skills]
	var stats = [
		["Level", character.level || 1],
		["Life", "ignored"],
		["Mana", "ignored"],
		["Strength", (character.starting_strength || 0) + (character.strength_added || 0)],
		["Dexterity", (character.starting_dexterity || 0) + (character.dexterity_added || 0)],
		["Vitality", (character.starting_vitality || 0) + (character.vitality_added || 0)],
		["Energy", (character.starting_energy || 0) + (character.energy_added || 0)]
	];

	// Add skills - include all class skills so the external tool can
	// determine the character class even when no skill points are allocated
	if (typeof skills !== 'undefined') {
		for (var s = 0; s < skills.length; s++) {
			stats.push([skills[s].name, skills[s].level]);
		}
	}

	// Build equipment array
	var equipmentItems = [];
	var equipSlots = ["helm", "armor", "gloves", "boots", "belt", "amulet", "ring1", "ring2", "weapon", "offhand"];
	for (var e = 0; e < equipSlots.length; e++) {
		var slot = equipSlots[e];
		if (typeof equipped !== 'undefined' && equipped[slot] && equipped[slot].name && equipped[slot].name !== "none") {
			var ripItem = buildRipItem(equipped[slot].name, slot);
			if (ripItem) equipmentItems.push(ripItem);
		}
	}

	// Build swap array
	var swapItems = [];
	if (typeof swapEquipped !== 'undefined') {
		var swapSlots = ["weapon", "offhand"];
		for (var sw = 0; sw < swapSlots.length; sw++) {
			var swSlot = swapSlots[sw];
			if (swapEquipped[swSlot] && swapEquipped[swSlot].name && swapEquipped[swSlot].name !== "none") {
				var swapItem = buildRipItem(swapEquipped[swSlot].name, "swap_" + swSlot);
				if (swapItem) swapItems.push(swapItem);
			}
		}
	}

	// Build inventory (charms)
	var inventoryItems = [];
	if (typeof equipped !== 'undefined' && equipped.charms) {
		for (var c = 0; c < equipped.charms.length; c++) {
			var charm = equipped.charms[c];
			if (charm && charm.name && charm.name !== "none" && charm.name !== "") {
				var charmItem = buildRipItem(charm.name, "charms");
				if (charmItem) {
					// Set charm position in inventory (row 4+ for charms area)
					charmItem.position = { x: c % 10, y: 4 + Math.floor(c / 10) };
					inventoryItems.push(charmItem);
				}
			}
		}
	}

	// Build mercenary
	var mercStats = ["", ""];
	var mercEquipmentItems = [];
	if (typeof mercenary !== 'undefined' && mercenary.name && mercenary.name !== "none") {
		mercStats = ["Type: " + mercenary.name, ""];
		if (typeof mercEquipped !== 'undefined') {
			var mercSlots = ["helm", "armor", "weapon", "offhand", "gloves", "boots", "belt"];
			for (var m = 0; m < mercSlots.length; m++) {
				var mSlot = mercSlots[m];
				if (mercEquipped[mSlot] && mercEquipped[mSlot].name && mercEquipped[mSlot].name !== "none") {
					var mercItem = buildRipItem(mercEquipped[mSlot].name, "merc_" + mSlot);
					if (mercItem) mercEquipmentItems.push(mercItem);
				}
			}
		}
	}

	var rip = {
		name: charName,
		stats: stats,
		equipment: equipmentItems,
		swap: swapItems,
		inventory: inventoryItems,
		mercenary: {
			stats: mercStats,
			equipment: mercEquipmentItems
		}
	};

	return rip;
}


// Export to .d2s game file via external tool
function exportToGameFile() {
	var rip = buildRipJson();
	if (!rip) return;

	var json = JSON.stringify(rip, null, 2);

	// Copy to clipboard
	if (navigator.clipboard && navigator.clipboard.writeText) {
		navigator.clipboard.writeText(json).then(function() {
			// Open the external export tool
			var exportUrl = "https://exiledagain.github.io/bug-free-eureka/export.html";
			var w = window.open(exportUrl, "_blank");
			if (w) {
				// Try to inject the JSON after the page loads
				var attempts = 0;
				var inject = setInterval(function() {
					attempts++;
					try {
						var textarea = w.document.querySelector('#json');
						if (textarea) {
							textarea.value = json;
							clearInterval(inject);
						}
					} catch(e) {
						// Cross-origin - can't inject, user will paste manually
						clearInterval(inject);
					}
					if (attempts > 30) {
						clearInterval(inject);
					}
				}, 500);
			}
			showExportMessage("Character data copied to clipboard and export tool opened in new tab. If the data wasn't auto-filled, paste it (Ctrl+V) into the text area, then click Download.");
		}).catch(function() {
			fallbackExportCopy(json);
		});
	} else {
		fallbackExportCopy(json);
	}
}

// Fallback: show the JSON in a textarea for manual copy
function fallbackExportCopy(json) {
	var exportUrl = "https://exiledagain.github.io/bug-free-eureka/export.html";

	// Create modal overlay
	var overlay = document.createElement("div");
	overlay.id = "export_overlay";
	overlay.style.cssText = "position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.8);z-index:10000;display:flex;align-items:center;justify-content:center;";

	var modal = document.createElement("div");
	modal.style.cssText = "background:#1a1a2e;border:2px solid #9b885e;padding:20px;max-width:600px;width:90%;max-height:80vh;display:flex;flex-direction:column;color:#ddd;font-family:sans-serif;";

	modal.innerHTML = '<h3 style="color:#9b885e;margin-top:0;">Export to Game File</h3>' +
		'<p>Copy the data below and paste it into the <a href="' + exportUrl + '" target="_blank" style="color:#6666bb;">PD2 Export Tool</a>, then click Download.</p>' +
		'<textarea id="export_json_textarea" style="width:100%;height:300px;background:#111;color:#ddd;border:1px solid #444;padding:8px;font-size:12px;resize:vertical;" readonly></textarea>' +
		'<div style="margin-top:10px;display:flex;gap:10px;">' +
		'<button onclick="document.getElementById(\'export_json_textarea\').select();document.execCommand(\'copy\');window.open(\'' + exportUrl + '\',\'_blank\');" style="padding:8px 16px;background:#9b885e;color:#fff;border:none;cursor:pointer;">Copy & Open Export Tool</button>' +
		'<button onclick="document.getElementById(\'export_overlay\').remove();" style="padding:8px 16px;background:#444;color:#fff;border:none;cursor:pointer;">Close</button>' +
		'</div>';

	overlay.appendChild(modal);
	document.body.appendChild(overlay);
	document.getElementById("export_json_textarea").value = json;

	overlay.addEventListener("click", function(e) {
		if (e.target === overlay) overlay.remove();
	});
}

// Show a brief status message
function showExportMessage(msg) {
	var el = document.getElementById("export_message");
	if (!el) {
		el = document.createElement("div");
		el.id = "export_message";
		el.style.cssText = "position:fixed;bottom:20px;left:50%;transform:translateX(-50%);background:#1a1a2e;border:2px solid #9b885e;color:#ddd;padding:15px 25px;z-index:10000;font-family:sans-serif;max-width:500px;text-align:center;";
		document.body.appendChild(el);
	}
	el.textContent = msg;
	el.style.display = "block";
	setTimeout(function() { el.style.display = "none"; }, 8000);
}
