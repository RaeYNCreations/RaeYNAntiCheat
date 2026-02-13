package com.raeyn.anticheat.checks;

/**
 * Enumeration of all cheat types detected by RaeYNAntiCheat
 * Covers all commonly known and used cheats in Minecraft
 */
public enum CheatType {
    // ===== MOVEMENT CHEATS =====
    FLY("Fly", "Unauthorized flying", CheatCategory.MOVEMENT, 8),
    SPEED("Speed", "Moving faster than normal", CheatCategory.MOVEMENT, 7),
    NOFALL("NoFall", "Avoiding fall damage", CheatCategory.MOVEMENT, 6),
    PHASE("Phase", "Moving through solid blocks", CheatCategory.MOVEMENT, 9),
    JESUS("Jesus", "Walking on water/lava", CheatCategory.MOVEMENT, 7),
    
    // ===== COMBAT CHEATS =====
    KILLAURA("KillAura", "Attacking multiple entities simultaneously", CheatCategory.COMBAT, 10),
    REACH("Reach", "Hitting from too far away", CheatCategory.COMBAT, 8),
    CRITICALS("Criticals", "Forcing critical hits", CheatCategory.COMBAT, 7),
    AUTOCLICKER("AutoClicker", "Automated clicking", CheatCategory.COMBAT, 6),
    VELOCITY("Velocity", "Reducing or negating knockback", CheatCategory.COMBAT, 8),
    ANTIKNOCKBACK("AntiKnockback", "Negating knockback completely", CheatCategory.COMBAT, 8),
    
    // ===== BLOCK CHEATS =====
    XRAY("X-Ray", "Seeing through blocks to find ores", CheatCategory.BLOCK, 9),
    NUKER("Nuker", "Breaking blocks too quickly in radius", CheatCategory.BLOCK, 9),
    SCAFFOLD("Scaffold", "Impossible block placements", CheatCategory.BLOCK, 7),
    FASTBREAK("FastBreak", "Breaking blocks too fast", CheatCategory.BLOCK, 7),
    FASTPLACE("FastPlace", "Placing blocks too fast", CheatCategory.BLOCK, 6),
    
    // ===== PLAYER STATE CHEATS =====
    INFINITE_HEALTH("InfiniteHealth", "Taking no damage / god mode", CheatCategory.PLAYER, 10),
    REGENERATION("Regeneration", "Abnormally fast health regeneration", CheatCategory.PLAYER, 8),
    OP_GUARD("OPGuard", "Preventing de-op / permission manipulation", CheatCategory.PLAYER, 10),
    FREECAM("Freecam", "Camera separated from player body", CheatCategory.PLAYER, 7),
    
    // ===== INVENTORY CHEATS =====
    CHEST_STEALER("ChestStealer", "Stealing items from chests too fast", CheatCategory.INVENTORY, 6),
    INVENTORY_MOVE("InventoryMove", "Moving while in inventory screen", CheatCategory.INVENTORY, 5),
    AUTO_ARMOR("AutoArmor", "Automatically equipping armor", CheatCategory.INVENTORY, 6),
    
    // ===== OTHER CHEATS =====
    FASTBOW("FastBow", "Shooting arrows without full draw", CheatCategory.OTHER, 7),
    BLINK("Blink", "Packet manipulation / teleportation", CheatCategory.OTHER, 8),
    TIMER("Timer", "Game speed manipulation", CheatCategory.OTHER, 9);
    
    private final String name;
    private final String description;
    private final CheatCategory category;
    private final int baseWeight;  // Base violation weight (1-10, higher = more severe)
    
    CheatType(String name, String description, CheatCategory category, int baseWeight) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.baseWeight = baseWeight;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public CheatCategory getCategory() {
        return category;
    }
    
    public int getBaseWeight() {
        return baseWeight;
    }
    
    /**
     * Categories of cheats for organizational purposes
     */
    public enum CheatCategory {
        MOVEMENT("Movement"),
        COMBAT("Combat"),
        BLOCK("Block Interaction"),
        PLAYER("Player State"),
        INVENTORY("Inventory"),
        OTHER("Other");
        
        private final String displayName;
        
        CheatCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
