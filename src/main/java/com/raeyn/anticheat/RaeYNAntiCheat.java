package com.raeyn.anticheat;

import com.raeyn.anticheat.checks.CheckManager;
import com.raeyn.anticheat.config.ConfigManager;
import com.raeyn.anticheat.logging.ViolationLogger;
import com.raeyn.anticheat.punishment.PunishmentManager;
import com.raeyn.anticheat.tracking.ViolationTracker;
import com.raeyn.anticheat.util.RaeYNCheatIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RaeYNAntiCheat - Server-Side ONLY Anti-Cheat for NeoForge 1.21.1
 * 
 * An extension to RaeYNCheat to cover server-side only in-game anti-cheat
 * after boot protection is finished. Takes over after player logs into server
 * where RaeYNCheat left off.
 * 
 * KEY FEATURES:
 * - SERVER-SIDE ONLY - No client installation required
 * - Tracks all known common cheats (X-ray, Flying, Infinite Health, OP Guard, etc.)
 * - Extremely high performance, low tick impact
 * - Not super aggressive, but powerful
 * - False positive tracker
 * - Percentage-based certainty tracker (0-100%)
 * - Progressive ban system for 95%+ cheat certainty
 * - Comprehensive logging system
 * - Optional integration with RaeYNCheat (soft dependency)
 * 
 * CHEAT DETECTION COVERAGE:
 * - Movement: Fly, Speed, NoFall, Phase, Jesus
 * - Combat: KillAura, Reach, Criticals, AutoClicker, Velocity
 * - Block: X-Ray, Nuker, Scaffold, FastBreak, FastPlace
 * - Player: Infinite Health, Regeneration, OP Guarding, Freecam
 * - Inventory: ChestStealer, InventoryMove, AutoArmor
 * - Other: FastBow, Blink, Timer, AntiKnockback
 */
@Mod("raeynantiacheat")
public class RaeYNAntiCheat {
    
    public static final String MOD_ID = "raeynantiacheat";
    private static final Logger LOGGER = LoggerFactory.getLogger(RaeYNAntiCheat.class);
    
    private static RaeYNAntiCheat instance;
    
    private ConfigManager configManager;
    private ViolationLogger violationLogger;
    private ViolationTracker violationTracker;
    private PunishmentManager punishmentManager;
    private CheckManager checkManager;
    private RaeYNCheatIntegration raeynCheatIntegration;
    
    private boolean raeynCheatPresent = false;
    
    public RaeYNAntiCheat(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        
        // Verify we're running on server side
        if (FMLEnvironment.dist.isClient()) {
            LOGGER.warn("RaeYNAntiCheat is SERVER-SIDE ONLY and should not be installed on clients!");
            LOGGER.warn("This mod will not function on the client side.");
            return;
        }
        
        LOGGER.info("========================================");
        LOGGER.info("  RaeYNAntiCheat - Server-Side ONLY");
        LOGGER.info("========================================");
        LOGGER.info("Initializing RaeYNAntiCheat...");
        
        // Check for RaeYNCheat integration
        raeynCheatPresent = ModList.get().isLoaded("raeyncheat");
        if (raeynCheatPresent) {
            LOGGER.info("RaeYNCheat detected! Enabling integration...");
            LOGGER.info("This mod will take over after RaeYNCheat boot protection completes");
        } else {
            LOGGER.info("RaeYNCheat not found - running standalone mode");
            LOGGER.info("Boot protection will not be available");
        }
        
        // Register common setup
        modEventBus.addListener(this::commonSetup);
        
        // Register configuration
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigManager.SPEC);
        
        LOGGER.info("RaeYNAntiCheat initialized successfully");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Skip if running on client
        if (FMLEnvironment.dist.isClient()) {
            return;
        }
        
        LOGGER.info("Setting up RaeYNAntiCheat server-side components...");
        
        // Initialize configuration manager
        configManager = new ConfigManager();
        
        // Initialize logging system
        violationLogger = new ViolationLogger();
        LOGGER.info("Violation logging system initialized");
        
        // Initialize violation tracker with false positive detection
        violationTracker = new ViolationTracker();
        LOGGER.info("Violation tracker initialized with false positive detection");
        
        // Initialize punishment manager with progressive system
        punishmentManager = new PunishmentManager();
        LOGGER.info("Progressive punishment system initialized (95% threshold)");
        
        // Initialize check manager with all cheat detections
        checkManager = new CheckManager();
        LOGGER.info("Cheat detection modules loaded:");
        LOGGER.info("  - Movement checks (Fly, Speed, NoFall, Phase, Jesus)");
        LOGGER.info("  - Combat checks (KillAura, Reach, Criticals, AutoClicker, Velocity)");
        LOGGER.info("  - Block checks (X-Ray, Nuker, Scaffold, FastBreak, FastPlace)");
        LOGGER.info("  - Player checks (Infinite Health, Regeneration, OP Guard, Freecam)");
        LOGGER.info("  - Inventory checks (ChestStealer, InventoryMove, AutoArmor)");
        LOGGER.info("  - Other checks (FastBow, Blink, Timer, AntiKnockback)");
        
        // Initialize RaeYNCheat integration if present
        if (raeynCheatPresent) {
            raeynCheatIntegration = new RaeYNCheatIntegration();
            LOGGER.info("RaeYNCheat integration enabled");
        }
        
        // Register event listeners to NeoForge event bus (server-side only)
        NeoForge.EVENT_BUS.register(new com.raeyn.anticheat.listeners.PlayerEventListener());
        NeoForge.EVENT_BUS.register(new com.raeyn.anticheat.listeners.MovementEventListener());
        NeoForge.EVENT_BUS.register(new com.raeyn.anticheat.listeners.CombatEventListener());
        NeoForge.EVENT_BUS.register(new com.raeyn.anticheat.listeners.BlockEventListener());
        NeoForge.EVENT_BUS.register(new com.raeyn.anticheat.listeners.ServerEventListener());
        
        LOGGER.info("========================================");
        LOGGER.info("RaeYNAntiCheat setup complete!");
        LOGGER.info("Server-side protection is now ACTIVE");
        if (raeynCheatPresent) {
            LOGGER.info("Integrated with RaeYNCheat boot protection");
        }
        LOGGER.info("========================================");
    }
    
    public static RaeYNAntiCheat getInstance() {
        return instance;
    }
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ViolationLogger getViolationLogger() {
        return violationLogger;
    }
    
    public ViolationTracker getViolationTracker() {
        return violationTracker;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public CheckManager getCheckManager() {
        return checkManager;
    }
    
    public RaeYNCheatIntegration getRaeYNCheatIntegration() {
        return raeynCheatIntegration;
    }
    
    public boolean isRaeYNCheatPresent() {
        return raeynCheatPresent;
    }
}
