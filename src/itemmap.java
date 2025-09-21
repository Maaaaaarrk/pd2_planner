import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The itemmap class serves as a utility container for mapping and managing certain properties and types.
 * It provides functionalities encapsulating PROP_MAP and TYPE_MAP which are crucial parts of this class.
 * This class is designed to be non-instantiable from outside as it has a private constructor.
 * <p>
 * Fields:
 * - PROP_MAP: Represents a mapping or storage structure for certain properties.
 * - TYPE_MAP: Represents a mapping or storage structure for certain types.
 * <p>
 * Methods:
 * - itemmap(): A private constructor to restrict the instantiation of this class. This ensures that the class
 * can only be accessed through static members or methods (if any), to maintain consistency and control over
 * its usage.
 */
public final class itemmap {

    public static final Map<String, String> PROP_MAP;
    public static final Map<String, String> TYPE_MAP;
    public static final ArrayList<String> TYPES;

    //	:0, dexterity:0,, mana:0,  stamina:0, block:0, base_defense:0,
    // main stats			*/	, mRes_max:0,,
    // stats				*/	 fcr:0, fbr:0, fhr:0, frw:0, ias:0, pierce:0,  dstrike:0, cstrike:0, owounds:0, fDamage:0, cDamage:0, lDamage:0, pDamage:0, fPierce:0, cPierce:0, lPierce:0, pPierce:0, pdr:0, damage_reduced:0:0,,  :0,:0, life_per_hit:0, mana_per_hit:0, life_per_ranged_hit:0, mana_per_ranged_hit:0,
    // stats (indirect)		*/	life_per_kill:0,  velocity:0, :0, max_mana:0, max_stamina:0, damage_bonus:0,  ar_bonus:0, ar_bonus_per_level:0, max_energy:0, ias_per_8_dexterity:0, ar_per_socketed:0, all_skills_ember:0, baseSpeed:0,
    // stats (per level)	*/	life_per_level:0, mana_per_level:0, defense_per_level:0, ar_per_level:0, stamina_per_level:0, strength_per_level:0, dexterity_per_level:0, vitality_per_level:0, fRes_per_level:0, cRes_per_level:0, lRes_per_level:0, pRes_per_level:0, fAbsorb_flat_per_level:0, cAbsorb_flat_per_level:0, mf_per_level:0, gf_per_level:0, fcr_per_level:0, dstrike_per_level:0, e_def_per_level:0,
    // attack damage		*/	base_damage_min:0, base_damage_max:0, damage_min:0, damage_max:0, fDamage_min:0, fDamage_max:0, cDamage_min:0, cDamage_max:0, lDamage_min:0,  pDamage_min:0, pDamage_max:0, pDamage_all:0, pDamage_duration:0, pDamage_duration_override:0, mDamage_min:0, mDamage_max:0, min_damage_per_level:0, max_damage_per_level:0, fDamage_max_per_level:0, cDamage_max_per_level:0, kick_min:2, kick_damage_per_level:0, smite_min:0, smite_max:0, e_damage:0, e_max_damage_per_level:0, lDamage_max_per_2_energy:0, cDamage_per_ice:0, cDamage_per_socketed:0,
    // other				*/	ctc:"", cskill:"", ibc:0, , life_regen:0, mana_regen:0, damage_to_mana:0,melee_defense:0, damage_vs_demons:0, damage_vs_undead:0, ar_vs_demons:0, ar_vs_undead:0, ar_vs_undead_per_level:0, damage_vs_undead_per_level:0, damage_vs_demons_per_level:0, life_per_demon_kill:0, thorns_lightning:0, thorns:0, thorns_per_level:0, thorns_reflect:0, poison_length_reduced:0, curse_length_reduced:0, light_radius:0, slower_stam_drain:0, heal_stam:0, heal_stam_per_level:0, discount:0,
    // other, affects enemy	*/	enemy_fRes:0, enemy_cRes:0, enemy_lRes:0, enemy_pRes:0, enemy_defense:0, enemy_defense_flat:0, target_defense:0, monster_defense_per_hit:0, slows_target:0, slow_enemies:0, freezes_target:0, flee_on_hit:0, blind_on_hit:0,
    // other, boolean		*/	 pmh:0, :0, peace:0, knockback:0, half_freeze:0, melee_splash:0, glow:0, fade:0,
    // item properties		*/	 e_def:0, req:0,  :0, stack_size:0, twoHanded:0, twoHands:0, range:0, throw_min:0, throw_max:0, base_min_alternate:0, base_max_alternate:0, def_low:0, def_high:0, durability:0, max_sockets:0, tier:0, nonmetal:0, name:"none", group:"", type:"", set_bonuses:"", rarity:"", size:"", not:"", only:"", upgrade:"", downgrade:"", aura:"", aura_lvl:0, special:0, sup:0, req_level:0, req_strength:0, req_dexterity:0,
    // misc					*/	bonus_corpse_explosion:0, phys_Lightning_Surge:0, extraValkyrie:0, extraGrizzly:0, extraFireGolem:0, extraHydra:0, radius_FreezingArrow:0, reset_cooldown_on_kill:0, cdr_on_striking:0, reanimate:0, half_Battle_Orders:0, bonus_sanctuary_rate:0, extra_Bone_Spears:0, extra_arrows_Cold_Arrow:0, extra_arrows_Magic_Arrow:0, extra_arrows_Fire_Arrow:0, extra_arrows_Ice_Arrow:0, extra_conversion_Magic_Arrow:0,
    // misc (non-item)		*/	flamme:0, ias_skill:0, ar_shrine_bonus:0, charge_ember:0, charge_thunder:0, charge_ice:0, summon_damage_bonus:0, hammer_bonus:0, hammer_on_hit:0, redeem_chance:0, redeem_amount:0, absorb_elemental:0, absorb_melee:0, absorb_es_redirect:0, absorb_es_deplete:0, duration:0, radius:0, amountSummoned:0, enemy_damage:0, enemy_attack:0, enemy_physRes:0, enemy_allRes:0, fleeing:0, flee_distance:0, dimmedVision:0, confused:0, attraction:0, enemy_ias:0, enemy_frw:0,
    // misc (PD2/vanilla)	*/	slow_ranged_attacks:0,

    // skill tabs			*/	skills_javelins:0, skills_passives:0, skills_bows:0, skills_martial:0, skills_shadow:0, skills_traps:0, skills_warcries:0, skills_masteries:0, skills_combat_barbarian:0, skills_elemental:0, skills_shapeshifting:0, skills_summoning_druid:0, skills_summoning_necromancer:0, skills_poisonBone:0, skills_curses:0, skills_offensive:0, skills_defensive:0, skills_combat_paladin:0, skills_cold:0, skills_lightning:0, skills_fire:0, skills_amazon:0, skills_assassin:0, skills_barbarian:0, skills_druid:0, skills_necromancer:0, skills_paladin:0, skills_sorceress:0, skills_fire_all:0, skills_cold_all:0, skills_poison_all:0,
    // skills   amazon		*/	skill_Jab:0, skill_Power_Strike:0, skill_Poison_Javelin:0, skill_Fend:0, skill_Lightning_Bolt:0, skill_Charged_Strike:0, skill_Plague_Javelin:0, skill_Impale:0, skill_Lightning_Strike:0, skill_Lightning_Fury:0, skill_Inner_Sight:0, skill_Critical_Strike:0, skill_Slow_Missiles:0, skill_Dodge:0, skill_Avoid:0, skill_Penetrate:0, skill_Evade:0, skill_Decoy:0, skill_Valkyrie:0, skill_Pierce:0, skill_Cold_Arrow:0, skill_Magic_Arrow:0, skill_Multiple_Shot:0, skill_Fire_Arrow:0, skill_Ice_Arrow:0, skill_Guided_Arrow:0, skill_Exploding_Arrow:0, skill_Strafe:0, skill_Immolation_Arrow:0, skill_Freezing_Arrow:0,
    // skills   assassin	*/	skill_Tiger_Strike:0, skill_Cobra_Strike:0, skill_Phoenix_Strike:0, skill_Dragon_Talon:0, skill_Dragon_Claw:0, skill_Dragon_Tail:0, skill_Dragon_Flight:0, skill_Fists_of_Fire:0, skill_Claws_of_Thunder:0, skill_Blades_of_Ice:0, skill_Claw_Mastery:0, skill_Psychic_Hammer:0, skill_Burst_of_Speed:0, skill_Weapon_Block:0, skill_Cloak_of_Shadows:0, skill_Fade:0, skill_Shadow_Warrior:0, skill_Mind_Blast:0, skill_Venom:0, skill_Shadow_Master:0, skill_Fire_Blast:0, skill_Shock_Web:0, skill_Blade_Sentinel:0, skill_Charged_Bolt_Sentry:0, skill_Wake_of_Fire:0, skill_Blade_Fury:0, skill_Lightning_Sentry:0, skill_Wake_of_Inferno:0, skill_Death_Sentry:0, skill_Blade_Shield:0,
    // skills   barbarian	*/	skill_Howl:0, skill_Find_Potion:0, skill_Taunt:0, skill_Shout:0, skill_Find_Item:0, skill_Battle_Cry:0, skill_Battle_Orders:0, skill_Grim_Ward:0, skill_War_Cry:0, skill_Battle_Command:0, skill_Sword_Mastery:0, skill_Axe_Mastery:0, skill_Mace_Mastery:0, skill_Polearm_Mastery:0, skill_Spear_Mastery:0, skill_Throwing_Mastery:0, skill_Increased_Stamina:0, skill_Iron_Skin:0, skill_Increased_Speed:0, skill_Natural_Resistance:0, skill_Double_Swing:0, skill_Double_Throw:0, skill_Frenzy:0, skill_Bash:0, skill_Stun:0, skill_Concentrate:0, skill_Berserk:0, skill_Leap:0, skill_Leap_Attack:0, skill_Whirlwind:0,
    // skills   druid		*/	skill_Firestorm:0, skill_Molten_Boulder:0, skill_Arctic_Blast:0, skill_Fissure:0, skill_Cyclone_Armor:0, skill_Twister:0, skill_Volcano:0, skill_Tornado:0, skill_Armageddon:0, skill_Hurricane:0, skill_Werewolf:0, skill_Lycanthropy:0, skill_Werebear:0, skill_Feral_Rage:0, skill_Maul:0, skill_Rabies:0, skill_Fire_Claws:0, skill_Hunger:0, skill_Shock_Wave:0, skill_Fury:0, skill_Raven:0, skill_Poison_Creeper:0, skill_Heart_of_Wolverine:0, skill_Summon_Spirit_Wolf:0, skill_Carrion_Vine:0, skill_Oak_Sage:0, skill_Summon_Dire_Wolf:0, skill_Solar_Creeper:0, skill_Spirit_of_Barbs:0, skill_Summon_Grizzly:0,
    // skills   necromancer	*/	skill_Skeleton_Mastery:0, skill_Summon_Resist:0, skill_Raise_Skeleton:0, skill_Golem_Mastery:0, skill_Clay_Golem:0, skill_Raise_Skeletal_Mage:0, skill_Blood_Golem:0, skill_Iron_Golem:0, skill_Fire_Golem:0, skill_Revive:0, skill_Poison_Dagger:0, skill_Teeth:0, skill_Bone_Armor:0, skill_Corpse_Explosion:0, skill_Poison_Explosion:0, skill_Bone_Spear:0, skill_Bone_Wall:0, skill_Bone_Spirit:0, skill_Bone_Prison:0, skill_Poison_Nova:0, skill_Amplify_Damage:0, skill_Dim_Vision:0, skill_Weaken:0, skill_Iron_Maiden:0, skill_Terror:0, skill_Confuse:0, skill_Life_Tap:0, skill_Attract:0, skill_Decrepify:0, skill_Lower_Resist:0,
    // skills   paladin		*/	skill_Prayer:0, skill_Resist_Fire:0, skill_Defiance:0, skill_Resist_Cold:0, skill_Cleansing:0, skill_Resist_Lightning:0, skill_Vigor:0, skill_Meditation:0, skill_Redemption:0, skill_Salvation:0, skill_Might:0, skill_Holy_Fire:0, skill_Thorns:0, skill_Blessed_Aim:0, skill_Concentration:0, skill_Holy_Freeze:0, skill_Holy_Shock:0, skill_Sanctuary:0, skill_Fanaticism:0, skill_Conviction:0, skill_Sacrifice:0, skill_Smite:0, skill_Holy_Bolt:0, skill_Zeal:0, skill_Charge:0, skill_Vengeance:0, skill_Blessed_Hammer:0, skill_Conversion:0, skill_Holy_Shield:0, skill_Fist_of_the_Heavens:0,
    // skills   sorceress	*/	skill_Ice_Bolt:0, skill_Frozen_Armor:0, skill_Frost_Nova:0, skill_Ice_Blast:0, skill_Shiver_Armor:0, skill_Glacial_Spike:0, skill_Blizzard:0, skill_Chilling_Armor:0, skill_Frozen_Orb:0, skill_Cold_Mastery:0, skill_Charged_Bolt:0, skill_Static_Field:0, skill_Telekinesis:0, skill_Nova:0, skill_Lightning:0, skill_Chain_Lightning:0, skill_Teleport:0, skill_Energy_Shield:0, skill_Lightning_Mastery:0, skill_Thunder_Storm:0, skill_Fire_Bolt:0, skill_Warmth:0, skill_Blaze:0, skill_Inferno:0, skill_Fire_Ball:0, skill_Fire_Wall:0, skill_Enchant:0, skill_Meteor:0, skill_Fire_Mastery:0, skill_Hydra:0,
    // oskills				*/	oskill_Warp:0, oskill_Ball_Lightning:0, 																															// Enigma, Ondal's Wisdom
    // oskills  amazon		*/	oskill_Inner_Sight:0, oskill_Lethal_Strike:0, oskill_Valkyrie:0, oskill_Magic_Arrow:0, oskill_Guided_Arrow:0, oskill_Multiple_Shot:0, 								// Blackoak Shield, Insight, Harmony, Witherstring, Wizendraw, Witchwild String, Widowmaker, Doomslinger
    // oskills  barbarian	*/	oskill_Battle_Command:0, oskill_Battle_Orders:0, oskill_Battle_Cry:0, oskill_Bash:0, oskill_Edged_Weapon_Mastery:0, oskill_Whirlwind:0, 							// Call to Arms, Passion, The Grandfather, Chaos
    // oskills  druid		*/	oskill_Lycanthropy:0, oskill_Werebear:0, oskill_Werewolf:0, oskill_Feral_Rage:0, oskill_Flame_Dash:0, oskill_Summon_Dire_Wolf:0, 									// Frostwind, Wolfhowl, Beast, Flamebellow, Boneflesh
    // oskills  necromancer	*/	oskill_Desecrate:0, 																																				// Radament's Sphere
    // oskills  paladin		*/	oskill_Zeal:0, oskill_Vengeance:0, 																																	// Passion, Kingslayer
    // oskills  sorceress	*/	oskill_Frigerate:0, oskill_Shiver_Armor:0, oskill_Cold_Mastery:0, oskill_Hydra:0, oskill_Fire_Ball:0, oskill_Fire_Wall:0, oskill_Meteor:0, oskill_Fire_Mastery:0, 	// Frostwind, Medusa's Gaze, Bing Sz Wang, Dragonscale, Trang-Oul's Set
    //  oskill_Berserk:0, oskill_Teleport:0, oskill_Heart_of_Wolverine:0,

    //   not used (yet?)	*///	energy_per_level:0, lAbsorb_flat_per_level:0, mAbsorb_flat_per_level:0, mAbsorb:0, pDamage_max_per_level:0, e_damage_per_level:0, stun_length:0, base:"", img:"", original_tier:0,
    //   unorganized		*/	durability_extra:0, experience:0, skills_class:0, skills_tree1:0, skills_tree2:0, skills_tree3:0, weapon:"", armor:"", shield:"", item_defense:0, block_skillup:0, velocity_skillup:0, dodge:0, avoid:0, evade:0, edged_damage:0, edged_ar:0, edged_cstrike:0, pole_damage:0, pole_ar:0, pole_cstrike:0, blunt_damage:0, blunt_ar:0, blunt_cstrike:0, thrown_damage:0, thrown_ar:0, thrown_pierce:0, claw_damage:0, claw_ar:0, claw_cstrike:0, summon_damage:0, summon_defense:0, all_skills_per_level:0, reset_on_kill:0, explosive_attack:0, magic_attack:0, ctc_temp1:0, ctc_temp2:0, ar_vs_demons_per_level:0, lDamage_max_per_level:0, pod_changes:0,
    //  mDamage:0,
    // owounds_duration:0, owounds_dps:0, owounds_dps_per_level:0, extra_mainhand_attack:0, mDamage_per_frenzy_charge:0, frenzy_duration:0, thrown_cstrike:0, counterattack:0, pulverize:0, charge_tiger:0, charge_cobra:0, charge_bonus_explosion:0, charge_bonus_meteor:0, charge_bonus_fork:0, charge_bonus_static:0, charge_bonus_icicles:0, charge_bonus_accuracy:0, charge_bonus_reduce:0, charge_bonus_leech:0,

    // PD2 skills			*/	skill_Javelin_and_Spear_Mastery:0, skill_Slow_Movement:0, skill_Claw_and_Dagger_Mastery:0, skill_Chain_Lightning_Sentry:0, skill_General_Mastery:0, skill_Throwing_Mastery:0, skill_Polearm_and_Spear_Mastery:0, skill_Combat_Reflexes:0, skill_Gust:0, skill_Raise_Skeleton_Archer:0, skill_Blood_Warp:0, skill_Poison_Strike:0, skill_Curse_Mastery:0, skill_Dark_Pact:0, skill_Holy_Light:0, skill_Joust:0, skill_Holy_Nova:0, skill_Cold_Enchant:0, skill_Ice_Barrage:0, skill_Lesser_Hydra:0, skill_Enchant_Fire:0, skill_Combustion:0, skill_Deep_Wounds:0, skill_Holy_Sword:0,


    static {
        LinkedHashMap<String, String> p = new LinkedHashMap<>();
        p.put("*enr", "energy");
        p.put("*vit", "vitality");
        p.put("Deep-Wounds", "Deep-Wounds");
        p.put("abs-cold", "cAbsorb_flat");
        p.put("abs-cold%", "cAbsorb");
        p.put("abs-fire", "fAbsorb_flat");
        p.put("abs-fire%", "fAbsorb");
        p.put("abs-ltng", "lAbsorb_flat");
        p.put("abs-ltng%", "lAbsorb");
        p.put("ac", "defense");
        p.put("ac%", "defense_bonus");
        p.put("ac-hth", "ac-hth");
        p.put("ac-miss", "missile_defense");
        p.put("ac/lvl", "ac/lvl");
        p.put("addxp", "experience");
        p.put("all-stats", "all_attributes");
        p.put("allskills", "all_skills");
        p.put("ama", "ama");
        p.put("ass", "ass");
        p.put("att", "ar");
        p.put("att%", "att%");
        p.put("att-dem/lvl", "att-dem/lvl");
        p.put("att-demon", "att-demon");
        p.put("att-skill", "att-skill");
        p.put("att-und/lvl", "att-und/lvl");
        p.put("att-undead", "att-undead");
        p.put("att/lvl", "att/lvl");
        p.put("aura", "aura");
        p.put("balance2", "balance2");
        p.put("bar", "bar");
        p.put("block", "block");
        p.put("block-skill", "block-skill");
        p.put("block2", "block2");
        p.put("blood-warp-life-reduction", "blood-warp-life-reduction");
        p.put("bloody", "bloody");
        p.put("cast-skill", "cast-skill");
        p.put("cast1", "fcr");
        p.put("cast2", "fcr");
        p.put("cast3", "fcr");
        p.put("charged", "charged");
        p.put("cheap", "cheap");
        p.put("cold-len", "cold-len");
        p.put("cold-max", "cold-max");
        p.put("cold-min", "cold-min");
        p.put("coldskill", "skills_cold_all");
        p.put("crush", "cblow");
        p.put("curse-effectiveness", "curse-effectiveness");
        p.put("curse-res", "curse-res");
        p.put("dclone-clout", "dclone-clout");
        p.put("deadly", "deadly");
        p.put("deadly/lvl", "deadly/lvl");
        p.put("death-skill", "death-skill");
        p.put("deep-wounds", "deep-wounds");
        p.put("demon-heal", "demon-heal");
        p.put("dev-clout", "dev-clout");
        p.put("dex", "dexterity");
        p.put("dex/lvl", "dex/lvl");
        p.put("dmg", "dmg");
        p.put("dmg%", "dmg%");
        p.put("dmg%/eth", "dmg%/eth");
        p.put("dmg%/lvl", "dmg%/lvl");
        p.put("dmg-ac", "dmg-ac");
        p.put("dmg-cold", "cDamage_max");
        p.put("dmg-dem/lvl", "dmg-dem/lvl");
        p.put("dmg-demon", "dmg-demon");
        p.put("dmg-elem", "dmg-elem");
        p.put("dmg-fire", "dmg-fire");
        p.put("dmg-ltng", "lDamage_max");
        p.put("dmg-mag", "dmg-mag");
        p.put("dmg-max", "dmg-max");
        p.put("dmg-min", "dmg-min");
        p.put("dmg-norm", "dmg-norm");
        p.put("dmg-pois", "dmg-pois");
        p.put("dmg-to-mana", "dmg-to-mana");
        p.put("dmg-und/lvl", "dmg-und/lvl");
        p.put("dmg-undead", "dmg-undead");
        p.put("dmg/lvl", "dmg/lvl");
        p.put("dragonflight-reduction", "dragonflight-reduction");
        p.put("dru", "dru");
        p.put("dur", "dur");
        p.put("eaglehorn-raven", "eaglehorn-raven");
        p.put("ease", "ease");
        p.put("enr", "energy");
        p.put("equipped-skill", "equipped-skill");
        p.put("es-efficiency", "es-efficiency");
        p.put("ethereal", "ethereal");
        p.put("explosivearrow", "explosivearrow");
        p.put("extra-cold", "extra-cold");
        p.put("extra-fire", "extra-fire");
        p.put("extra-grizzly", "extra-grizzly");
        p.put("extra-ltng", "extra-ltng");
        p.put("extra-pois", "extra-pois");
        p.put("extra-revives", "extra-revives");
        p.put("extra-shadow", "extra-shadow");
        p.put("extra-skele-war", "extra-skele-war");
        p.put("extra-spirits", "extra-spirits");
        p.put("extra-spiritwolf", "extra-spiritwolf");
        p.put("fire-max", "fire-max");
        p.put("fire-min", "fire-min");
        p.put("fireskill", "skills_fire_all");
        p.put("freeze", "freeze");
        p.put("gethit-skill", "gethit-skill");
        p.put("gold%", "gf");
        p.put("gold%/lvl", "gold%/lvl");
        p.put("grims-extra-skele-mage", "grims-extra-skele-mage");
        p.put("gust-reduction", "gust-reduction");
        p.put("half-freeze", "half-freeze");
        p.put("heal-hit", "heal-hit");
        p.put("heal-kill", "life_per_kill");
        p.put("hit-skill", "hit_kill");
        p.put("howl", "howl");
        p.put("hp", "life");
        p.put("hp%", "max_life");
        p.put("hp/lvl", "hp/lvl");
        p.put("ignore-ac", "itd");
        p.put("inc-splash-radius", "inc-splash-radius");
        p.put("indestruct", "indestructible");
        p.put("joust-reduction", "joust-reduction");
        p.put("joust-reduction-zeraes", "joust-reduction-zeraes");
        p.put("kick", "kick");
        p.put("kill-skill", "kill-skill");
        p.put("knock", "knock");
        p.put("leapspeed", "leapspeed");
        p.put("levelup-skill", "levelup-skill");
        p.put("lifesteal", "life_leech");
        p.put("lifesteal-cap", "lifesteal-cap");
        p.put("light", "light");
        p.put("light-thorns", "light-thorns");
        p.put("ltng-max", "ltng-max");
        p.put("ltng-min", "ltng-min");
        p.put("ltngskill", "skills_lightning_all");
        p.put("mag%", "mf");
        p.put("mag%/lvl", "mf/lvl");
        p.put("magicarrow", "magicarrow");
        p.put("magskill", "skills_magic_all");
        p.put("mana", "mana");
        p.put("mana%", "max_mana");
        p.put("mana-kill", "mana_per_kill");
        p.put("mana/lvl", "mana/lvl");
        p.put("manasteal", "mana_leech");
        p.put("map-glob-arealevel", "map-glob-arealevel");
        p.put("map-glob-boss-dropcorruptedunique", "map-glob-boss-dropcorruptedunique");
        p.put("map-glob-boss-dropfacet", "map-glob-boss-dropfacet");
        p.put("map-glob-boss-droppuzzlebox", "map-glob-boss-droppuzzlebox");
        p.put("map-glob-boss-dropskillers", "map-glob-boss-dropskillers");
        p.put("map-glob-boss-dropubermats", "map-glob-boss-dropubermats");
        p.put("map-glob-density", "map-glob-density");
        p.put("map-glob-dropcorrupted", "map-glob-dropcorrupted");
        p.put("map-glob-monsterrarity", "map-glob-monsterrarity");
        p.put("map-mon-att-pierce", "map-mon-att-pierce");
        p.put("map-mon-crush", "map-mon-crush");
        p.put("map-mon-curseresist-hp%", "map-mon-curseresist-hp%");
        p.put("map-mon-deadlystrike", "map-mon-deadlystrike");
        p.put("map-mon-dropcharms", "map-mon-dropcharms");
        p.put("map-mon-dropjewels", "map-mon-dropjewels");
        p.put("map-mon-hp%", "map-mon-hp%");
        p.put("map-mon-phys-as-extra-cold", "map-mon-phys-as-extra-cold");
        p.put("map-mon-phys-as-extra-fire", "map-mon-phys-as-extra-fire");
        p.put("map-mon-phys-as-extra-ltng", "map-mon-phys-as-extra-ltng");
        p.put("map-mon-phys-as-extra-mag", "map-mon-phys-as-extra-mag");
        p.put("map-mon-phys-as-extra-pois", "map-mon-phys-as-extra-pois");
        p.put("map-mon-regen", "map-mon-regen");
        p.put("map-mon-velocity%", "map-mon-velocity%");
        p.put("map-play-ac%", "map-play-ac%");
        p.put("map-play-addxp", "map-play-addxp");
        p.put("map-play-balance1", "map-play-balance1");
        p.put("map-play-block", "map-play-block");
        p.put("map-play-mag-gold%", "map-play-mag-gold%");
        p.put("map-play-regen", "map-play-regen");
        p.put("map-play-res-all", "map-play-res-all");
        p.put("map-play-speed-all", "map-play-speed-all");
        p.put("max-deadly", "max-deadly");
        p.put("maxcurse", "maxcurse");
        p.put("maxlevel-clout", "maxlevel-clout");
        p.put("mindmg/energy", "mindmg/energy");
        p.put("move2", "frw");
        p.put("nec", "nec");
        p.put("no-wolves", "no-wolves");
        p.put("nofreeze", "cbf");
        p.put("noheal", "pmh");
        p.put("openwounds", "openwounds");
        p.put("oskill", "oskill");
        p.put("pal", "pal");
        p.put("pierce", "pierce");
        p.put("pierce-cold", "pierce-cold");
        p.put("pierce-fire", "pierce-fire");
        p.put("pierce-ltng", "pierce-ltng");
        p.put("pierce-phys", "pierce-phys");
        p.put("pierce-pois", "pierce-pois");
        p.put("pois-len", "pois-len");
        p.put("pois-max", "pois-max");
        p.put("pois-min", "pois-min");
        p.put("poisskill", "skills_poison_all");
        p.put("randclassskill1", "randclassskill1");
        p.put("randclassskill2", "randclassskill2");
        p.put("rathma-clout", "rathma-clout");
        p.put("reanimate", "reanimate");
        p.put("red-dmg", "mAbsorb_flat");
        p.put("red-dmg%", "mRes");
        p.put("red-mag", "mDamage_reduced");
        p.put("reduce-ac", "reduce-ac");
        p.put("regen", "life_replenish");
        p.put("regen-mana", "mana_regen");
        p.put("regen-stam", "regen-stam");
        p.put("rep-charge", "autoreplenish");
        p.put("rep-dur", "autorepair");
        p.put("res-all", "all_res");
        p.put("res-all-max", "res-all-max");
        p.put("res-cold", "cRes");
        p.put("res-cold-max", "cRes_max");
        p.put("res-fire", "fRes");
        p.put("res-fire-max", "fRes_max");
        p.put("res-ltng", "lRes");
        p.put("res-ltng-max", "lRes_max");
        p.put("res-pois", "pRes");
        p.put("res-pois-len", "res-pois-len");
        p.put("res-pois-max", "pRes_max");
        p.put("rip", "peace");
        p.put("skill", "skill");
        p.put("skill-rand", "skill-rand");
        p.put("skilltab", "skilltab");
        p.put("slow", "slow");
        p.put("sock", "sockets");
        // p.put("socketed-text", "socketed-text");
        p.put("sor", "sor");
        p.put("sorc-skill-rand-ctc", "sorc-skill-rand-ctc");
        p.put("splash%/missinghp%", "splash%/missinghp%");
        p.put("stam", "stam");
        p.put("stamdrain", "stamdrain");
        p.put("state", "state");
        p.put("str", "strength");
        p.put("str/lvl", "str/lvl");
        p.put("stupidity", "stupidity");
        p.put("swing1", "swing1");
        p.put("swing2", "swing2");
        p.put("swing3", "swing3");
        p.put("thorns", "thorns");
        p.put("thorns/lvl", "thorns/lvl");
        p.put("vit", "vitality");
        p.put("vit/lvl", "vit/lvl");
        PROP_MAP = Collections.unmodifiableMap(p);


        ArrayList<String> types = new ArrayList<>();
        types.add("Helm");
        types.add("Armor");
        types.add("Gloves");
        types.add("Boots");
        types.add("Belt");
        types.add("Amulet");
        types.add("Ring1");
        types.add("Weapon");
        types.add("Offhand");
        TYPES = types;
        //
    /*
    Helm
    Armor
    Gloves
    Boots
    Belt
    Amulet
    Ring1
    Weapon
    Offhand
     */
        LinkedHashMap<String, String> t = new LinkedHashMap<>();
        t.put("2-Handed Sword", "Weapon");
        t.put("Aerin Shield", "Offhand");
        t.put("Alpha Helm", "Helm");
        t.put("Amulet", "Amulet");
        t.put("Ancient Axe", "Weapon");
        t.put("Ancient Shield", "Offhand");
        t.put("Ancient Sword", "Weapon");
        t.put("AncientArmor", "Armor");
        t.put("Arbalest", "Weapon");
        t.put("Archon Plate", "Armor");
        t.put("Axe", "Weapon");
        t.put("Balista", "Weapon");
        t.put("Balrog Skin", "Armor");
        t.put("Barbed Club", "Weapon");
        t.put("Barbed Shield", "Offhand");
        t.put("Bardiche", "Weapon");
        t.put("Basinet", "Helm");
        t.put("Bastard Sword", "Weapon");
        t.put("Battle Axe", "Weapon");
        t.put("Battle Belt", "Belt");
        t.put("Battle Boots", "Boots");
        t.put("Battle Guantlets", "Gloves");
        t.put("Battle Hammer", "Weapon");
        t.put("Battle Scythe", "Weapon");
        t.put("Battle Staff", "Weapon");
        t.put("Battle Sword", "Weapon");
        t.put("Bearded Axe", "Weapon");
        t.put("Bec-de-Corbin", "Weapon");
        t.put("Belt", "Belt");
        t.put("Bill", "Weapon");
        t.put("Blade", "Weapon");
        t.put("Bone Helm", "Helm");
        t.put("Bone Knife", "Weapon");
        t.put("Bone Shield", "Offhand");
        t.put("Bone Wand", "Weapon");
        t.put("Boneweave", "Armor");
        t.put("Bracers", "Gloves");
        t.put("Bramble Mitts", "Gloves");
        t.put("Brandistock", "Weapon");
        t.put("Breast Plate", "Armor");
        t.put("Broad Axe", "Weapon");
        t.put("Broad Sword", "Weapon");
        t.put("Buckler", "Offhand");
        t.put("Burnt Wand", "Weapon");
        t.put("Cap", "Helm");
        t.put("Casque", "Helm");
        t.put("Cedar Staff", "Weapon");
        t.put("CedarBow", "Weapon");
        t.put("Ceremonial Bow", "Weapon");
        t.put("Ceremonial Javelin", "Weapon");
        t.put("Ceremonial Pike", "Weapon");
        t.put("Chain Boots", "Boots");
        t.put("Chain Mail", "Armor");
        t.put("Champion Axe", "Weapon");
        t.put("Champion Sword", "Weapon");
        t.put("Chaos Armor", "Armor");
        t.put("Chu-Ko-Nu", "Weapon");
        t.put("Cinquedeas", "Weapon");
        t.put("Claymore", "Weapon");
        t.put("Cleaver", "Weapon");
        t.put("Club", "Weapon");
        t.put("Colossal Sword", "Weapon");
        t.put("Colossus Blade", "Weapon");
        t.put("Colossus Girdle", "Weapon");
        t.put("Composite Bow", "Weapon");
        t.put("Conquest Sword", "Weapon");
        t.put("Crossbow", "Weapon");
        t.put("Crowbill", "Weapon");
        t.put("Crown", "Helm");
        t.put("Crusader Bow", "Weapon");
        t.put("Crusader Guantlets", "Gloves");
        t.put("Crystal Sword", "Weapon");
        t.put("Crystalline Globe", "Weapon");
        t.put("Cudgel", "Weapon");
        t.put("Cuirass", "Armor");
        t.put("Cutlass", "Weapon");
        t.put("Dacian Falx", "Weapon");
        t.put("Dagger", "Weapon");
        t.put("Death Mask", "Helm");
        t.put("Decapitator", "Weapon");
        t.put("Defender", "Offhand");
        t.put("Demonhide Armor", "Armor");
        t.put("Demonhide Boots", "Boots");
        t.put("Demonhide Gloves", "Gloves");
        t.put("Demonhide Sash", "Belt");
        t.put("Devil Star", "Weapon");
        t.put("Diamond Mail", "Armor");
        t.put("Dimensional Blade", "Weapon");
        t.put("Dirk", "Weapon");
        t.put("Divine Scepter", "Weapon");
        t.put("Double Axe", "Weapon");
        t.put("Double Bow", "Weapon");
        t.put("Dragon Shield", "Offhand");
        t.put("Dream Spirit", "Helm");
        t.put("Edge Bow", "Weapon");
        t.put("Embossed Plate", "Armor");
        t.put("Espadon", "Weapon");
        t.put("Executioner Sword", "Weapon");
        t.put("Falchion", "Weapon");
        t.put("Field Plate", "Armor");
        t.put("Flail", "Weapon");
        t.put("Flamberge", "Weapon");
        t.put("Flanged Mace", "Weapon");
        t.put("Francisca", "Weapon");
        t.put("Full Helm", "Helm");
        t.put("Full Plate Mail", "Armor");
        t.put("Fuscina", "Weapon");
        t.put("Gauntlets", "Gloves");
        t.put("Ghost Armor", "Armor");
        t.put("Ghost Spear", "Weapon");
        t.put("Giant Axe", "Weapon");
        t.put("Giant Conch", "Helm");
        t.put("Giant Sword", "Weapon");
        t.put("Giant Thresher", "Weapon");
        t.put("Girdle", "Belt");
        t.put("Gladius", "Weapon");
        t.put("Gloves", "Gloves");
        t.put("Glowing Orb", "Weapon");
        t.put("Gnarled Staff", "Weapon");
        t.put("Gothic Axe", "Weapon");
        t.put("Gothic Bow", "Weapon");
        t.put("Gothic Plate", "Armor");
        t.put("Gothic Shield", "Offhand");
        t.put("Gothic Staff", "Weapon");
        t.put("Gothic Sword", "Weapon");
        t.put("Grand Crown", "Helm");
        t.put("Grand Matron Bow", "Weapon");
        t.put("Grand Scepter", "Weapon");
        t.put("Grave Wand", "Weapon");
        t.put("Great Axe", "Weapon");
        t.put("Great Helm", "Helm");
        t.put("Great Maul", "Weapon");
        t.put("Great Sword", "Weapon");
        t.put("Grim Helm", "Helm");
        t.put("Grim Scythe", "Weapon");
        t.put("Grim Shield", "Offhand");
        t.put("Grim Wand", "Weapon");
        t.put("Guardian Crown", "Helm");
        t.put("Halberd", "Weapon");
        t.put("Hammer", "Weapon");
        t.put("Hand Axe", "Weapon");
        t.put("Hard Leather", "Armor");
        t.put("Hatchet", "Weapon");
        t.put("Hawk Helm", "Helm");
        t.put("Heavy Belt", "Belt");
        t.put("Heavy Boots", "Boots");
        t.put("Heavy Bracers", "Gloves");
        t.put("Heavy Crossbow", "Weapon");
        t.put("Heavy Gloves", "Gloves");
        t.put("Heirophant Trophy", "Offhand");
        t.put("Hellspawn Skull", "Offhand");
        t.put("Helm", "Helm");
        t.put("Heraldic Shield", "Offhand");
        t.put("Highland Blade", "Weapon");
        t.put("Holy Water Sprinkler", "Weapon");
        t.put("Hunter�s Bow", "Weapon");
        t.put("Hydra Bow", "Weapon");
        t.put("Hyperion", "Offhand");
        t.put("Jagged Star", "Weapon");
        t.put("Jawbone Visor", "Helm");
        t.put("Jo Stalf", "Weapon");
        t.put("Kite Shield", "Offhand");
        t.put("Knout", "Weapon");
        t.put("Kris", "Weapon");
        t.put("Lacquered Plate", "Armor");
        t.put("Lance", "Weapon");
        t.put("Large Axe", "Weapon");
        t.put("Large Shield", "Offhand");
        t.put("Leather Armor", "Armor");
        t.put("Leather Boots", "Boots");
        t.put("Legendary Mallet", "Weapon");
        t.put("Light Belt", "Belt");
        t.put("Light Crossbow", "Weapon");
        t.put("Light Gauntlets", "Gloves");
        t.put("Light Plate", "Armor");
        t.put("Light Plate Boots", "Boots");
        t.put("Linked Mail", "Armor");
        t.put("Lion Helm", "Helm");
        t.put("Lochaber Axe", "Weapon");
        t.put("Long Battle Bow", "Weapon");
        t.put("Long Bow", "Weapon");
        t.put("Long Siege Bow", "Weapon");
        t.put("Long Staff", "Weapon");
        t.put("Long Sword", "Weapon");
        t.put("Long War Bow", "Weapon");
        t.put("Luna", "Offhand");
        t.put("Mace", "Weapon");
        t.put("Mage Plate", "Armor");
        t.put("Maiden Javelin", "Weapon");
        t.put("Martel de Fer", "Weapon");
        t.put("Mask", "Helm");
        t.put("Matriarchal Pike", "Weapon");
        t.put("Maul", "Weapon");
        t.put("Mesh Armor", "Armor");
        t.put("Mesh Belt", "Belt");
        t.put("Mesh Boots", "Boots");
        t.put("Military Axe", "Weapon");
        t.put("Military Pick", "Weapon");
        t.put("Mithral Point", "Weapon");
        t.put("Monarch", "Offhand");
        t.put("Morning Star", "Weapon");
        t.put("Mummified Trophy", "Offhand");
        t.put("Naga", "Weapon");
        t.put("Ornate Armor", "Armor");
        t.put("Overseer Skull", "Offhand");
        t.put("Partizan", "Weapon");
        t.put("Pavise", "Offhand");
        t.put("Petrified Wand", "Weapon");
        t.put("Phase Blade", "Weapon");
        t.put("Pike", "Weapon");
        t.put("Plate Boots", "Boots");
        t.put("Plate Mail", "Armor");
        t.put("Poignard", "Weapon");
        t.put("Poleaxe", "Weapon");
        t.put("Quarterstaff", "Weapon");
        t.put("Quhab", "Weapon");
        t.put("Quilted Armor", "Armor");
        t.put("Razor Bow", "Weapon");
        t.put("Repeating Crossbow", "Weapon");
        t.put("Ring", "Ring1");
        t.put("Ring Mail", "Armor");
        t.put("Rondel", "Weapon");
        t.put("Round Shield", "Offhand");
        t.put("Rune Bow", "Weapon");
        t.put("Rune Scepter", "Weapon");
        t.put("Rune Staff", "Weapon");
        t.put("Rune Sword", "Weapon");
        t.put("Runic Talons", "Weapon");
        t.put("Russet Armor", "Armor");
        t.put("Saber", "Weapon");
        t.put("Sallet", "Helm");
        t.put("Sash", "Belt");
        t.put("Scale Mail", "Armor");
        t.put("Scepter", "Weapon");
        t.put("Scimitar", "Weapon");
        t.put("Scissors Suwayyah", "Weapon");
        t.put("Scutum", "Offhand");
        t.put("Scythe", "Weapon");
        t.put("Seraph Rod", "Weapon");
        t.put("SerpentSkin Armor", "Armor");
        t.put("Shako", "Helm");
        t.put("Shamshir", "Weapon");
        t.put("Sharkskin Belt", "Belt");
        t.put("Sharkskin Boots", "Boots");
        t.put("Sharkskin Gloves", "Gloves");
        t.put("Sharktooth Armor", "Armor");
        t.put("Shillelah", "Weapon");
        t.put("Short Battle Bow", "Weapon");
        t.put("Short Bow", "Weapon");
        t.put("Short Siege Bow", "Weapon");
        t.put("Short Staff", "Weapon");
        t.put("Short Sword", "Weapon");
        t.put("Short War Bow", "Weapon");
        t.put("Siege Crossbow", "Weapon");
        t.put("Skull Cap", "Helm");
        t.put("Slayer Guard", "Helm");
        t.put("Small Shield", "Offhand");
        t.put("Spear", "Weapon");
        t.put("Spetum", "Weapon");
        t.put("Spiked Club", "Weapon");
        t.put("Spiked Shield", "Offhand");
        t.put("Spired Helm", "Helm");
        t.put("Splint Mail", "Armor");
        t.put("Staff", "Weapon");
        t.put("Stilleto", "Weapon");
        t.put("Studded Leather", "Armor");
        t.put("Sun Spirit", "Helm");
        t.put("Swirling Crystal", "Weapon");
        t.put("Tabar", "Weapon");
        t.put("Templar Coat", "Armor");
        t.put("Thunder Maul", "Weapon");
        t.put("Tigulated Mail", "Armor");
        t.put("Tomb Wand", "Weapon");
        t.put("Tower Shield", "Offhand");
        t.put("Tresllised Armor", "Armor");
        t.put("Trident", "Weapon");
        t.put("Troll Belt", "Belt");
        t.put("Tulwar", "Weapon");
        t.put("Tusk Sword", "Weapon");
        t.put("Twin Axe", "Weapon");
        t.put("Voulge", "Weapon");
        t.put("Wand", "Weapon");
        t.put("War Axe", "Weapon");
        t.put("War Belt", "Belt");
        t.put("War Boots", "Boots");
        t.put("War Club", "Weapon");
        t.put("War Fork", "Weapon");
        t.put("War Gauntlets", "Gloves");
        t.put("War Hammer", "Weapon");
        t.put("War Hat", "Helm");
        t.put("War Scepter", "Weapon");
        t.put("War Scythe", "Weapon");
        t.put("War Spear", "Weapon");
        t.put("War Staff", "Weapon");
        t.put("War Sword", "Weapon");
        t.put("Winged Helm", "Helm");
        t.put("Wire Fleece", "Armor");
        t.put("Yari", "Weapon");
        t.put("Yew Wand", "Weapon");
        t.put("Zweihander", "Weapon");
        t.put("aegis", "Offhand");
        t.put("amulet", "Amulet");
        t.put("archon staff", "Weapon");
        t.put("armet", "Helm");
        t.put("ataghan", "Weapon");
        t.put("balrog blade", "Weapon");
        t.put("balrog spear", "Weapon");
        t.put("battle cestus", "Weapon");
        t.put("battle dart", "Weapon");
        t.put("berserker axe", "Weapon");
        t.put("blade barrier", "Offhand");
        t.put("blood spirit", "Helm");
        t.put("bloodlord skull", "Offhand");
        t.put("bone visage", "Helm");
        t.put("boneweave boots", "Offhand");
        t.put("caduceus", "Weapon");
        t.put("colossus crossbow", "Weapon");
        t.put("conqueror crown", "Helm");
        t.put("corona", "Helm");
        t.put("cryptic axe", "Weapon");
        t.put("cryptic sword", "Weapon");
        t.put("demon crossbow", "Weapon");
        t.put("demonhead", "Helm");
        t.put("destroyer helm", "Helm");
        t.put("diadem", "Helm");
        t.put("dimensional shard", "Weapon");
        t.put("dusk shroud", "Armor");
        t.put("earth spirit", "Helm");
        t.put("elder staff", "Weapon");
        t.put("eldritch orb", "Weapon");
        t.put("elegant blade", "Weapon");
        t.put("ettin axe", "Weapon");
        t.put("fanged knife", "Weapon");
        t.put("feral claws", "Weapon");
        t.put("flying axe", "Weapon");
        t.put("fury visor", "Helm");
        t.put("ghost glaive", "Weapon");
        t.put("glorious axe", "Weapon");
        t.put("hurlbat", "Weapon");
        t.put("hyperion spear", "Weapon");
        t.put("kraken shell", "Armor");
        t.put("legend spike", "Weapon");
        t.put("legendary mallet", "Weapon");
        t.put("lich wand", "Weapon");
        t.put("matriarchal bow", "Weapon");
        t.put("matriarchal javelin", "Weapon");
        t.put("matriarchal spear", "Weapon");
        t.put("mighty scepter", "Weapon");
        t.put("mirrored boots", "Boots");
        t.put("mithril coil", "Belt");
        t.put("myrmidon greaves", "Boots");
        t.put("ogre axe", "Weapon");
        t.put("ogre gauntlets", "Gloves");
        t.put("ogre maul", "Weapon");
        t.put("phase blade", "Weapon");
        t.put("ring", "Ring1");
        t.put("sacred armor", "Armor");
        t.put("sacred rondache", "Offhand");
        t.put("scarabshell boots", "Boots");
        t.put("scourge", "Weapon");
        t.put("shadow plate", "Armor");
        t.put("silver-edged axe", "Weapon");
        t.put("sky spirit", "Helm");
        t.put("spiderweb sash", "Belt");
        t.put("spired helm", "Helm");
        t.put("succubae skull", "Offhand");
        t.put("thresher", "Weapon");
        t.put("thunder maul", "Weapon");
        t.put("tiara", "Helm");
        t.put("tomahawk", "Weapon");
        t.put("troll nest", "Offhand");
        t.put("truncheon", "Weapon");
        t.put("tyrant club", "Weapon");
        t.put("unearthed wand", "Weapon");
        t.put("vambraces", "Gloves");
        t.put("vampirebone gloves", "Gloves");
        t.put("vampirefang belt", "Belt");
        t.put("vortex shield", "Offhand");
        t.put("war fist", "Weapon");
        t.put("war fork", "Weapon");
        t.put("war pike", "Weapon");
        t.put("war spike", "Weapon");
        t.put("ward", "Weapon");
        t.put("ward bow", "Weapon");
        t.put("winged axe", "Weapon");
        t.put("winged harpoon", "Weapon");
        t.put("winged knife", "Weapon");
        t.put("wrist sword", "Weapon");
        t.put("wyrrnhide boots", "Boots");
        t.put("zakarum shield", "Offhand");
        TYPE_MAP = Collections.unmodifiableMap(t);
    }

    private itemmap() {
    }
}
