import java.util.*;

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
    public static final Map<String, String> NAME_CORRECTION_MAP;
    public static final ArrayList<String> CORRECTED_NAMES;
    public static final ArrayList<String> SKIP_NAME_PART;
    public static final ArrayList<String> PER_LEVEL_STATS;

    static {
        LinkedHashMap<String, String> p = new LinkedHashMap<>();
        p.put("*enr", "energy");
        p.put("*vit", "vitality");
        p.put("Deep-Wounds", "owounds_dps");
        p.put("abs-cold", "cAbsorb_flat");
        p.put("abs-cold%", "cAbsorb");
        p.put("abs-fire", "fAbsorb_flat");
        p.put("abs-fire%", "fAbsorb");
        p.put("abs-ltng", "lAbsorb_flat");
        p.put("abs-ltng%", "lAbsorb");
        p.put("ac", "defense");
        p.put("ac%", "defense_bonus");
        p.put("ac-hth", "missile_defense");
        p.put("ac-miss", "missile_defense");
        p.put("ac/lvl", "defense_per_level");
        p.put("addxp", "experience");
        p.put("all-stats", "all_attributes");
        p.put("allskills", "all_skills");
        p.put("ama", "skills_amazon");
        p.put("ass", "skills_assassin");
        p.put("att", "ar");
        p.put("att%", "ar_skillup");
        p.put("att-dem/lvl", "ar_vs_demons_per_level");
        p.put("att-demon", "ar_vs_demons");
        p.put("att-skill", "ar_skillup");
        p.put("att-und/lvl", "damage_vs_undead_per_level");
        p.put("att-undead", "ar_vs_undead");
        p.put("att/lvl", "ar_per_level");
        p.put("aura", "aura"); // handled in processor
        p.put("balance2", "fhr");
        p.put("balance1", "fhr");
        p.put("balance3", "fhr");
        p.put("bar", "skills_barbarian");
        p.put("block", "ibc");
        p.put("block-skill", "block-skill");
        p.put("block2", "fbr");
        p.put("block3", "fbr");
        p.put("block1", "ibc");
        p.put("blood-warp-life-reduction", "blood-warp-life-reduction");
        p.put("bloody", "bloody"); // Ignored
        p.put("cast-skill", "cast-skill"); // in processor
        p.put("cast1", "fcr");
        p.put("cast2", "fcr");
        p.put("cast3", "fcr");
        p.put("charged", "charged"); // in processor
        p.put("cheap", "discount");
        p.put("cold-len", "cold-len"); // ignored
        p.put("cold-max", "cDamage_max");
        p.put("cold-min", "cDamage_min");
        p.put("coldskill", "skills_cold_all");
        p.put("crush", "cblow");
        p.put("curse-effectiveness", "curse_effectiveness");
        p.put("curse-res", "curse_length_reduced");
        p.put("dclone-clout", "dclone-clout"); // ignore
        p.put("deadly", "dstrike");
        p.put("deadly/lvl", "dstrike_per_level");
        p.put("death-skill", "death-skill"); // in processor
        p.put("deep-wounds", "owounds_dps");
        p.put("demon-heal", "life_per_demon_kill");
        p.put("dev-clout", "dev-clout"); // ignore
        p.put("dex", "dexterity");
        p.put("dex/lvl", "dexterity_per_level");
        p.put("dmg", "base_damage_min");
        p.put("dmg%", "e_damage");
        p.put("dmg%/eth", "dmg_per_eth");
        p.put("dmg%/lvl", "e_max_damage_per_level");
        p.put("dmg-ac", "monster_defense_per_hit");
        p.put("dmg-cold", "cDamage_max");
        p.put("dmg-dem/lvl", "damage_vs_demons_per_level");
        p.put("dmg-demon", "damage_vs_demons");
        p.put("dmg-elem", "dmg-elem"); // handled in processor
        p.put("dmg-fire", "fDamage_max");
        p.put("dmg-ltng", "lDamage_max");
        p.put("dmg-mag", "dmg-mag"); // handled in processor
        p.put("dmg-max", "damage_max");
        p.put("dmg-min", "damage_min");
        p.put("dmg-norm", "dmg-norm"); // duplicates handled in processor
        p.put("dmg-pois", "dmg-pois"); // in calc
        p.put("dmg-to-mana", "damage_to_mana");
        p.put("dmg-und/lvl", "damage_vs_undead_per_level");
        p.put("dmg-undead", "damage_vs_undead");
        p.put("dmg/lvl", "max_damage_per_level");
        p.put("dragonflight-reduction", "dragonflight-reduction");
        p.put("dru", "skills_druid");
        p.put("dur", "durability_extra");
        p.put("eaglehorn-raven", "extra_Raven_Damage");
        p.put("ease", "req");
        p.put("enr", "energy");
        p.put("equipped-skill", "equipped-skill"); // handled in processor
        p.put("es-efficiency", "es-efficiency"); // ignored helper for es on equip
        p.put("ethereal", "ethereal");
        p.put("explosivearrow", "explosive_attack");
        p.put("extra-cold", "cDamage");
        p.put("extra-fire", "fDamage");
        p.put("extra-grizzly", "extraGrizzly");
        p.put("extra-ltng", "lDamage");
        p.put("extra-pois", "pDamage");
        p.put("extra-revives", "extra_Revives");
        p.put("extra-shadow", "extra_Shadow");
        p.put("extra-skele-war", "extra_Skeleton_Warriors");
        p.put("extra-spirits", "extra_Spirits");
        p.put("extra-spiritwolf", "extra-extra_Spirit_Wolf");
        p.put("fire-max", "fDamage_max");
        p.put("fire-min", "fDamage_min");
        p.put("fireskill", "skills_fire_all");
        p.put("freeze", "freezes_target");
        p.put("gethit-skill", "gethit-skill"); // in processor
        p.put("gold%", "gf");
        p.put("gold%/lvl", "gf_per_level");
        p.put("grims-extra-skele-mage", "extra_Skeleton_Mages");
        p.put("gust-reduction", "gust_Cd_Reduction");
        p.put("half-half_freeze", "half-freeze");
        p.put("heal-hit", "life_per_hit");
        p.put("heal-kill", "life_per_kill");
        p.put("hit-skill", "hit-skill");
        p.put("howl", "howl"); // in processor
        p.put("hp", "life");
        p.put("hp%", "max_life");
        p.put("hp/lvl", "life_per_level");
        p.put("ignore-ac", "itd");
        p.put("inc-splash-radius", "extra_Melee_Splash");
        p.put("indestruct", "indestructible");
        p.put("joust-reduction", "joust_Cd_Reduction");
        p.put("joust-reduction-zeraes", "joust_Cd_Reduction");
        p.put("kick", "kick_min");
        p.put("kill-skill", "kill-skill"); // in processor
        p.put("knock", "knockback");
        p.put("leapspeed", "leapspeed");
        p.put("levelup-skill", "levelup-skill"); // in processor
        p.put("lifesteal", "life_leech");
        p.put("lifesteal-cap", "lifesteal_cap");
        p.put("light", "light_radius");
        p.put("light-thorns", "thorns_lightning");
        p.put("ltng-max", "lDamage_max");
        p.put("ltng-min", "lDamage_min");
        p.put("ltngskill", "skills_lightning_all");
        p.put("mag%", "mf");
        p.put("mag%/lvl", "mf_per_level");
        p.put("magicarrow", "magic_attack");
        p.put("magskill", "skills_magic_all");
        p.put("mana", "mana");
        p.put("mana%", "max_mana");
        p.put("mana-kill", "mana_per_kill");
        p.put("mana/lvl", "mana_per_level");
        p.put("manasteal", "mana_leech");
        p.put("maxcurse", "maxcurse");
        p.put("maxlevel-clout", "maxlevel-clout"); // ignore
        p.put("mindmg/energy", "mindmg_energy");
        p.put("move2", "frw");
        p.put("move1", "frw");
        p.put("move3", "frw");
        p.put("Light", "light_radius");
        p.put("nec", "skills_necromancer");
        p.put("no-wolves", "no_wolves");
        p.put("nofreeze", "cbf");
        p.put("noheal", "pmh");
        p.put("openwounds", "owounds");
        p.put("oskill", "oskill");
        p.put("pal", "skills_paladin");
        p.put("pierce", "pierce_skillup");
        p.put("pierce-cold", "enemy_cRes");
        p.put("pierce-fire", "enemy_fRes");
        p.put("pierce-ltng", "enemy_lRes");
        p.put("pierce-phys", "enemy_phyRes");
        p.put("pierce-pois", "enemy_pRes");
        p.put("pois-len", "pDamage_duration");
        p.put("pois-max", "pDamage_max");
        p.put("pois-min", "pDamage_min");
        p.put("poisskill", "skills_poison_all");
        p.put("randclassskill1", "randclassskill1"); // torch unused
        p.put("randclassskill2", "randclassskill2"); // spiritward
        p.put("rathma-clout", "rathma-clout"); // ignore
        p.put("reanimate", "reanimate");
        p.put("red-dmg", "damage_reduced");
        p.put("red-dmg%", "pdr");
        p.put("red-mag", "mDamage_reduced");
        p.put("reduce-ac", "target_defense");
        p.put("regen", "life_replenish");
        p.put("regen-mana", "mana_regen");
        p.put("regen-stam", "heal_stam");
        p.put("rep-charge", "autoreplenish");
        p.put("rep-dur", "autorepair");
        p.put("res-all", "all_res");
        p.put("res-all-max", "res-all-max"); // in processor
        p.put("res-cold", "cRes");
        p.put("res-cold-max", "cRes_max");
        p.put("res-fire", "fRes");
        p.put("res-fire-max", "fRes_max");
        p.put("res-ltng", "lRes");
        p.put("res-ltng-max", "lRes_max");
        p.put("res-pois", "pRes");
        p.put("res-pois-len", "poison_length_reduced");
        p.put("res-pois-max", "pRes_max");
        p.put("rip", "peace");
        p.put("skill", "skill");
        p.put("skill-rand", "skill-rand"); // in processor
        p.put("skilltab", "skilltab"); // done in java
        p.put("slow", "slows_target");
        p.put("sock", "sockets");
        p.put("socketed-text", "socketed-text"); // ignored
        p.put("sor", "skills_sorceress");
        p.put("sorc-skill-rand-ctc", "sorc_skill_rand_ctc");
        p.put("splash%/missinghp%", "splash_missinghp");
        p.put("stam", "stamina");
        p.put("stamdrain", "slower_stam_drain");
        p.put("state", "state"); // ignored is achievement aura
        p.put("str", "strength");
        p.put("str/lvl", "strength_per_level");
        p.put("stupidity", "blind_on_hit");
        p.put("swing1", "ias");
        p.put("swing2", "ias");
        p.put("swing3", "ias");
        p.put("thorns", "thorns");
        p.put("thorns/lvl", "thorns_per_level");
        p.put("vit", "vitality");
        p.put("vit/lvl", "vitality_per_level");
        p.put("half-freeze", "half_freeze");
        p.put("max-deadly", "max_dstrike");
        p.put("splash", "melee_splash");
        p.put("abs-cold/lvl", "abs_cold_lvl");
        p.put("dmg-ltng/lvl", "lDamage_max_per_level");
        p.put("res-fire/lvl", "fRes_per_level");
        p.put("res-cold/lvl", "cRes_per_level");
        p.put("res-ltng/lvl", "lRes_per_level");
        p.put("dmg-cold/lvl", "cDamage_max_per_level");
        p.put("regen-stam/lvl", "heal_stam_per_level");
        p.put("abs-mag", "mAbsorb_flat");
        p.put("extra-hydra", "extraHydra");
        p.put("extra-golem", "extraGolem");
        p.put("extra-valk", "extraValkyries");
        p.put("extra-skele-archer", "extra_Skeleton_Archers");
        p.put("infinityspeed", "frw");
        p.put("crush-efficiency", "crush_efficiency");
        p.put("extra-cold-arrows", "extra_cold_arrows");
        PROP_MAP = Collections.unmodifiableMap(p);


        PER_LEVEL_STATS = new ArrayList<>();
        PER_LEVEL_STATS.add("defense_per_level");
        PER_LEVEL_STATS.add("ar_vs_demons_per_level");
        PER_LEVEL_STATS.add("damage_vs_undead_per_level");
        PER_LEVEL_STATS.add("ar_per_level");
        PER_LEVEL_STATS.add("dstrike_per_level");
        PER_LEVEL_STATS.add("dexterity_per_level");
        PER_LEVEL_STATS.add("e_max_damage_per_level");
        PER_LEVEL_STATS.add("damage_vs_demons_per_level");
        PER_LEVEL_STATS.add("max_damage_per_level");
        PER_LEVEL_STATS.add("gf_per_level");
        PER_LEVEL_STATS.add("life_per_level");
        PER_LEVEL_STATS.add("mf_per_level");
        PER_LEVEL_STATS.add("mana_per_level");
        PER_LEVEL_STATS.add("strength_per_level");
        PER_LEVEL_STATS.add("thorns_per_level");
        PER_LEVEL_STATS.add("vitality_per_level");
        PER_LEVEL_STATS.add("lDamage_max_per_level");
        PER_LEVEL_STATS.add("fRes_per_level");
        PER_LEVEL_STATS.add("cRes_per_level");
        PER_LEVEL_STATS.add("lRes_per_level");
        PER_LEVEL_STATS.add("cDamage_max_per_level");
        PER_LEVEL_STATS.add("heal_stam_per_level");


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
        t.put("Minion Skull", "Offhand");
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
        t.put("Greater Talons", "Weapon");
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
        t.put("boneweave boots", "Boots");
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
        t.put("ward bow", "Weapon");
        t.put("winged axe", "Weapon");
        t.put("winged harpoon", "Weapon");
        t.put("winged knife", "Weapon");
        t.put("wrist sword", "Weapon");
        t.put("wyrrnhide boots", "Boots");
        t.put("zakarum shield", "Offhand");
        t.put("Chain Gloves", "Gloves");
        t.put("Lacerator", "Weapon");
        t.put("ward", "Offhand");
        t.put("colossus girdle", "Belt");
        t.put("greaves", "Boots");
        t.put("Sabre", "Weapon");
        t.put("Loricated Mail", "Armor");
        t.put("Hellforge Plate", "Armor");
        t.put("Reinforced Mace", "Weapon");
        t.put("Cantor Trophy", "Offhand");
        t.put("Circlet", "Helm");
        t.put("Hunter's Guise", "Helm");
        t.put("Avenger Guard", "Helm");
        t.put("Arrows", "Offhand");
        t.put("Bolts", "Offhand");
        t.put("Gargoyle Head", "Offhand");
        t.put("Rondache", "Offhand");
        t.put("Spirit Mask", "Helm");
        t.put("Blade Bow", "Weapon");
        t.put("Spirit Mask", "Helm");
        t.put("Sharp Arrows", "Offhand");


        LinkedHashMap<String, String> ncc = new LinkedHashMap<>();
        ncc.put("Jo Stalf", "Jo Staff");
        ncc.put("Darkforge Spawn", "Darkforce Spawn");
        ncc.put("Bracers", "Chain Gloves");
        ncc.put("Irices Shard", "Spectral Shard");
        ncc.put("Heirophant Trophy", "Hierophant Trophy");
        ncc.put("Deaths's Web", "Death's Web");
        ncc.put("Akarats Devotion", "Akarat's Devotion");
        ncc.put("Titangrip", "Titan's Grip");
        ncc.put("Souldrain", "Soul Drainer");
        ncc.put("Gloves", "Leather Gloves");
        ncc.put("Crusader Guantlets", "Crusader Gauntlets");
        ncc.put("Battle Guantlets", "Battle Gauntlets");
        ncc.put("Lavagout", "Lava Gout");
        ncc.put("AncientArmor", "Ancient Armor");
        ncc.put("Victors Silk", "Silks of the Victor");
        ncc.put("The Spirit Shroud", "Spirit Shroud");
        ncc.put("Blinkbats Form", "Blinkbat's Form");
        ncc.put("Ornate Armor", "Ornate Plate");
        ncc.put("Tresllised Armor", "Trellised Armor");
        ncc.put("Ironpelt", "Iron Pelt");
        ncc.put("Que-Hegan's Wisdon", "Que-Hegan's Wisdom");
        ncc.put("Skin of the Flayerd One", "Skin of the Flayed One");
        ncc.put("SerpentSkin Armor", "Serpentskin Armor");
        ncc.put("Spiritforge", "Spirit Forge");
        ncc.put("Steel Carapice", "Steel Carapace");
        ncc.put("Hard Leather", "Hard Leather Armor");
        ncc.put("Light Plate Boots", "Light Plated Boots");
        ncc.put("Gorerider", "Gore Rider");
        ncc.put("Leather Boots", "Boots");
        ncc.put("Itheraels Path", "Itherael's Path");
        ncc.put("Wyrrnhide Boots", "Wyrmhide Boots");
        ncc.put("Merman's Speed", "Merman's Sprocket");
        ncc.put("Shadowdancer", "Shadow Dancer");
        ncc.put("Wartraveler", "War Traveler");
        ncc.put("Plate Boots", "Light Plated Boots");
        ncc.put("Verdugo's Hearty Cord", "Verdungo's Hearty Cord");
        ncc.put("Thudergod's Vigor", "Thundergod's Vigor");
        ncc.put("Lenyms Cord", "Lenymo");
        ncc.put("Gloomstrap", "Gloom's Trap");
        ncc.put("Girdle", "Plated Belt");
        ncc.put("Headhunter's Glory", "Head Hunter's Glory");
        ncc.put("Kerke's Sanctuary", "Gerke's Sanctuary");
        ncc.put("Mirror Shield", "Twilight's Reflection");
        ncc.put("Mosers Blessed Circle", "Moser's Blessed Circle");
        ncc.put("Raekors Virtue", "Raekor's Virtue");
        ncc.put("Cerebus", "Cerebus' Bite");
        ncc.put("Spiritkeeper", "Spirit Keeper");
        ncc.put("Kalans Legacy", "Kalan's Legacy");
        ncc.put("Martyr", "Martyrdom");
        ncc.put("Bonesob", "Bonesnap");
        ncc.put("Shillelah", "Shillelagh");
        ncc.put("Balista", "Ballista");
        ncc.put("Espadon", "Espandon");
        ncc.put("Zeraes Resolve", "Zerae's Resolve");
        ncc.put("Wraithflight", "Wraith Flight");
        ncc.put("Hunter�s Bow", "Hunter's Bow");
        ncc.put("Whichwild String", "Witchwild String");
        ncc.put("The Reedeemer", "The Redeemer");
        ncc.put("The Minataur", "The Minotaur");
        ncc.put("The Humongous", "Humongous");
        ncc.put("The Generals Tan Do Li Ga", "The General's Tan Do Li Ga");
        ncc.put("The Chieftan", "The Chieftain");
        ncc.put("The Atlantian", "The Atlantean");
        ncc.put("Stilleto", "Stiletto");
        ncc.put("Steelpillar", "Steel Pillar");
        ncc.put("Skullcollector", "Skull Collector");
        ncc.put("Sigurd's Staunch", "Siggard's Staunch");
        ncc.put("Mithral Point", "Mithril Point");
        ncc.put("2-Handed Sword", "Two Handed Sword");
        ncc.put("Runemaster", "Rune Master");
        ncc.put("Rixots Keen", "Rixot's Keen");
        ncc.put("Rimeraven", "Raven Claw");
        ncc.put("Razoredge", "Razor's Edge");
        ncc.put("Pus Spiter", "Pus Spitter");
        ncc.put("Pullspite", "Stormstrike");
        ncc.put("Pompe's Wrath", "Pompeii's Wrath");
        ncc.put("Piercerib", "Rogue's Bow");
        ncc.put("Colossal Sword", "Colossus Sword");
        ncc.put("Mindrend", "Skull Splitter");
        ncc.put("Maelstromwrath", "Maelstrom");
        ncc.put("Leoric's Mithril Blade", "Leoric's Mithril Bane");
        ncc.put("Lazarus Spire", "Spire of Lazarus");
        ncc.put("Cedarbow", "Cedar Bow");
        ncc.put("Krintizs Skewer", "Skewer of Krintiz");
        ncc.put("Kinemils Awl", "Kinemil's Awl");
        ncc.put("Iros Torch", "Torch of Iro");
        ncc.put("Ironward", "Astreon's Iron Ward");
        ncc.put("Hadriels Hand", "Hadriel's Hand");
        ncc.put("Gutsiphon", "Gut Siphon");
        ncc.put("Griswolds Edge", "Griswold's Edge");
        ncc.put("Godstrike Arch", "Goldstrike Arch");
        ncc.put("Fechmars Axe", "Axe of Fechmar");
        ncc.put("Silver-edged Axe", "Silver Edged Axe");
        ncc.put("Earthshifter", "Earth Shifter");
        ncc.put("Doomspittle", "Doomslinger");
        ncc.put("Djinnslayer", "Djinn Slayer");
        ncc.put("Dimoaks Hew", "Dimoak's Hew");
        ncc.put("Demonlimb", "Demon Limb");
        ncc.put("Deathcleaver", "Death Cleaver");
        ncc.put("Culwens Point", "Culwen's Point");
        ncc.put("Aidans Scar", "Aidan's Scar");
        ncc.put("Jadetalon", "Jade Talon");
        ncc.put("Cutthroat1", "Bartuc's Cut Throat");
        ncc.put("Stalkers Cull", "Stalker's Cull");
        ncc.put("Wisp", "Wisp Projector");
        ncc.put("War Bonnet", "Biggin's Bonnet");
        ncc.put("Overlords Helm", "Overlord's Helm");
        ncc.put("Nightmares Feast", "Ursa's Nightmare");
        ncc.put("Aldur's Gauntlet", "Aldur's Rhythm");
        ncc.put("McAuley's Paragon", "Sander's Paragon");
        ncc.put("McAuley's Riprap", "Sander's Riprap");
        ncc.put("McAuley's Taboo", "Sander's Taboo");
        ncc.put("McAuley's Superstition", "Sander's Superstition");
        ncc.put("Cow King's Hoofs", "Cow King's Hooves");
        ncc.put("Fathom", "Death's Fathom");
        ncc.put("Bloodraven's Charge", "Blood Raven's Charge");
        ncc.put("Shadowkiller", "Shadow Killer");
        ncc.put("Vampiregaze", "Vampire Gaze");
        ncc.put("Steelshade", "Steel Shade");
        ncc.put("Giantskull", "Giant Skull");
        ncc.put("Valkiry Wing", "Valkyrie Wing");
        ncc.put("Peasent Crown", "Peasant Crown");
        ncc.put("Umes Lament", "Ume's Lament");
        ncc.put("Griswolds's Redemption", "Griswold's Redemption");
        ncc.put("Haemosu's Adament", "Haemosu's Adamant");
        ncc.put("Spiritual Custodian", "Dark Adherent");
        ncc.put("Tal Rasha's Howling Wind", "Tal Rasha's Guardianship");
        ncc.put("Venomsward", "Venom Ward");
        ncc.put("Heaven's Taebaek", "Taebaek's Glory");
        ncc.put("Wihtstan's Guard", "Whitstan's Guard");
        ncc.put("Hwanin's Seal", "Hwanin's Blessing");
        ncc.put("Tal Rasha's Fire-Spun Cloth", "Tal Rasha's Fine-Spun Cloth");
        ncc.put("Saber", "Sabre");


        // ncc.put("", "");

        Map<String, String> lowercased = new LinkedHashMap<>(ncc.size());
        ArrayList<String> correctionNames = new ArrayList<>(ncc.size());
        for (Map.Entry<String, String> e : ncc.entrySet()) {
            String key = e.getKey();
            lowercased.put(key == null ? null : key.toLowerCase(java.util.Locale.ROOT), e.getValue());
            correctionNames.add(e.getValue());
        }

        CORRECTED_NAMES = correctionNames;
        NAME_CORRECTION_MAP = Collections.unmodifiableMap(lowercased);

        ArrayList<String> skipNames = new ArrayList<>();
        skipNames.add("Khalim");
        skipNames.add("Hell Forge Hammer");
        skipNames.add("Horadric Staff");
        skipNames.add("Staff of Kings");
        SKIP_NAME_PART = skipNames;


        Map<String, String> lowercasType = new LinkedHashMap<>(t.size());
        for (Map.Entry<String, String> e : t.entrySet()) {
            String key = e.getKey();
            if (key != null) {
                String correctedCheck = NAME_CORRECTION_MAP.get(norm(key));
                if (correctedCheck != null) {
                    lowercasType.put(correctedCheck.toLowerCase(java.util.Locale.ROOT), e.getValue());
                }
            }
            lowercasType.put(key == null ? null : key.toLowerCase(java.util.Locale.ROOT), e.getValue());
        }

        TYPE_MAP = lowercasType;

    }

    private static final Set<String> AMAZON_BASES = Set.of(
            // Bows
            "stag bow", "ashwood bow", "matriarchal bow",
            "reflex bow", "ceremonial bow", "grand matron bow",
            // Javelins
            "maiden javelin", "ceremonial javelin", "matriarchal javelin",
            // Spears
            "maiden spear", "maiden pike", "ceremonial spear",
            "ceremonial pike", "matriarchal spear", "matriarchal pike"
    );

    private static final Set<String> ASSASSIN_BASES = Set.of(
            // Normal claws
            "katar", "wrist blade", "hatchet hands", "cestus", "claws", "blade talons", "scissors katar",
            // Exceptional claws
            "quhab", "wrist spike", "fascia", "hand scythe", "greater claws", "greater talons", "scissors quhab",
            // Elite claws
            "suwayyah", "wrist sword", "war fist", "battle cestus", "feral claws", "runic talons", "scissors suwayyah"
    );

    private static final Set<String> BARBARIAN_BASES = Set.of(
            // Barbarian helms
            "jawbone cap", "fanged helm", "horned helm", "jawbone visor",
            "assault helmet", "avenger guard", "savage helmet", "lion helm",
            "slayer guard", "fury visor", "destroyer helm", "conqueror crown", "guardian crown"
    );

    private static final Set<String> DRUID_BASES = Set.of(
            // Druid pelts
            "wolf head", "hawk helm", "antlers", "falcon mask", "spirit mask",
            "alpha helm", "griffon headdress", "hunter's guise", "sacred feathers", "totemic mask",
            "blood spirit", "sun spirit", "earth spirit", "sky spirit", "dream spirit"
    );

    private static final Set<String> NECROMANCER_BASES = Set.of(
            // Shrunken heads
            "preserved head", "zombie head", "unraveller head", "gargoyle head", "demon head",
            "mummified trophy", "fetish trophy", "sexton trophy", "cantor trophy", "hierophant trophy",
            "minion skull", "hellspawn skull", "overseer skull", "succubus skull", "bloodlord skull",
            "succubae skull"
    );

    private static final Set<String> PALADIN_BASES = Set.of(
            // Auric shields
            "targe", "rondache", "heraldic shield", "aerin shield", "crown shield",
            "akaran targe", "akaran rondache", "protector shield", "gilded shield", "royal shield",
            "sacred targe", "sacred rondache", "kurast shield", "zakarum shield", "vortex shield"
    );

    private static final Set<String> SORCERESS_BASES = Set.of(
            // Orbs
            "eagle orb", "sacred globe", "smoked sphere", "clasped orb", "jared's stone",
            "glowing orb", "crystalline globe", "cloudy sphere", "sparkling ball", "swirling crystal",
            "heavenly stone", "eldritch orb", "demon heart", "vortex orb", "dimensional shard"
    );

    // Returns the class name if the base is class-specific; otherwise null.
    public static String classForBaseOrNull(String baseName) {
        if (baseName == null) return null;
        String checkBaseName = baseName.toLowerCase();
        if (AMAZON_BASES.contains(checkBaseName)) return "amazon";
        if (ASSASSIN_BASES.contains(checkBaseName)) return "assassin";
        if (BARBARIAN_BASES.contains(checkBaseName)) return "barbarian";
        if (DRUID_BASES.contains(checkBaseName)) return "druid";
        if (NECROMANCER_BASES.contains(checkBaseName)) return "necromancer";
        if (PALADIN_BASES.contains(checkBaseName)) return "paladin";
        if (SORCERESS_BASES.contains(checkBaseName)) return "sorceress";
        return null; // Not class-specific
    }

    private itemmap() {
    }

    public static String checkForRename(String trim) {
        String check = norm(trim);
        if (NAME_CORRECTION_MAP.containsKey(check)) {
            //   System.err.println("Renamed: " + check);
            return NAME_CORRECTION_MAP.get(check);
        }
        return trim;
    }

    // Normalize to lower-case for exact, case-insensitive matching
    public static String norm(String s) {
        return s.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public static String getGroupBaseType(String baseType) {
        return TYPE_MAP.get(norm(baseType));
    }

    public static boolean typeChecker(String groupBaseType) {
        return itemmap.TYPES.contains(groupBaseType);
    }

    public static boolean skipCheck(String name) {
        for (int i = 0; i < itemmap.SKIP_NAME_PART.size(); i++) {
            if (name.contains(itemmap.SKIP_NAME_PART.get(i))) {
                return true;
            }
        }
        return false;
    }
}
