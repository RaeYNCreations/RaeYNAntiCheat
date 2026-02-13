package com.raeyn.anticheat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Configuration manager for RaeYNAntiCheat
 * Uses NeoForge's configuration system
 */
public class ConfigManager {
    
    public static final ModConfigSpec SPEC;
    
    // Global settings
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue DEBUG_MODE;
    
    // Performance settings
    public static final ModConfigSpec.IntValue CHECK_INTERVAL;
    public static final ModConfigSpec.BooleanValue ASYNC_PROCESSING;
    public static final ModConfigSpec.IntValue MAX_CHECKS_PER_TICK;
    public static final ModConfigSpec.BooleanValue ADAPTIVE_THRESHOLDS;
    public static final ModConfigSpec.DoubleValue MIN_TPS_THRESHOLD;
    
    // Violation tracking
    public static final ModConfigSpec.IntValue DECAY_TIME;
    public static final ModConfigSpec.BooleanValue FALSE_POSITIVE_DETECTION;
    
    // Thresholds
    public static final ModConfigSpec.DoubleValue ALERT_THRESHOLD;
    public static final ModConfigSpec.DoubleValue PUNISHMENT_THRESHOLD;
    
    // Punishments
    public static final ModConfigSpec.BooleanValue PUNISHMENTS_ENABLED;
    public static final ModConfigSpec.DoubleValue PUNISHMENT_CERTAINTY_THRESHOLD;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> PUNISHMENT_STEPS;
    
    // Logging
    public static final ModConfigSpec.BooleanValue CONSOLE_LOGGING;
    public static final ModConfigSpec.BooleanValue FILE_LOGGING;
    public static final ModConfigSpec.IntValue LOG_RETENTION_DAYS;
    public static final ModConfigSpec.BooleanValue ALERTS_ENABLED;
    public static final ModConfigSpec.DoubleValue ALERTS_MIN_CERTAINTY;
    
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        
        builder.comment("RaeYNAntiCheat Configuration")
            .push("general");
        
        ENABLED = builder
            .comment("Enable or disable the entire anti-cheat system")
            .define("enabled", true);
        
        DEBUG_MODE = builder
            .comment("Enable debug mode for verbose logging")
            .define("debugMode", false);
        
        builder.pop();
        
        builder.comment("Performance optimization settings")
            .push("performance");
        
        CHECK_INTERVAL = builder
            .comment("How often to run checks (in ticks, 20 ticks = 1 second)")
            .defineInRange("checkInterval", 1, 1, 20);
        
        ASYNC_PROCESSING = builder
            .comment("Use async processing for non-critical checks")
            .define("asyncProcessing", true);
        
        MAX_CHECKS_PER_TICK = builder
            .comment("Maximum checks per tick per player")
            .defineInRange("maxChecksPerTick", 3, 1, 10);
        
        ADAPTIVE_THRESHOLDS = builder
            .comment("Adjust thresholds based on server TPS")
            .define("adaptiveThresholds", true);
        
        MIN_TPS_THRESHOLD = builder
            .comment("Minimum TPS before reducing strictness")
            .defineInRange("minTpsThreshold", 18.0, 10.0, 20.0);
        
        builder.pop();
        
        builder.comment("Violation tracking settings")
            .push("violationTracking");
        
        DECAY_TIME = builder
            .comment("How long to keep violation history (seconds)")
            .defineInRange("decayTime", 300, 60, 3600);
        
        FALSE_POSITIVE_DETECTION = builder
            .comment("Enable false positive detection")
            .define("falsePositiveDetection", true);
        
        builder.pop();
        
        builder.comment("Threshold configuration")
            .push("thresholds");
        
        ALERT_THRESHOLD = builder
            .comment("Alert admins at this certainty level")
            .defineInRange("alertThreshold", 50.0, 0.0, 100.0);
        
        PUNISHMENT_THRESHOLD = builder
            .comment("Begin punishment at this certainty level")
            .defineInRange("punishmentThreshold", 95.0, 0.0, 100.0);
        
        builder.pop();
        
        builder.comment("Progressive punishment system (RaeYNCheat-style)")
            .push("punishments");
        
        PUNISHMENTS_ENABLED = builder
            .comment("Enable the punishment system")
            .define("enabled", true);
        
        PUNISHMENT_CERTAINTY_THRESHOLD = builder
            .comment("Certainty threshold to trigger punishment")
            .defineInRange("certaintyThreshold", 95.0, 0.0, 100.0);
        
        PUNISHMENT_STEPS = builder
            .comment("Progressive punishment steps in seconds",
                "Each violation increases the punishment level",
                "0 = warning only, -1 = permanent ban",
                "Maximum 30 steps allowed")
            .defineList("steps", 
                java.util.Arrays.asList(0, 60, 300, 600, 1800, 3600, 7200, 14400, 28800, 86400, -1),
                obj -> obj instanceof Integer && (Integer) obj >= -1);
        
        builder.pop();
        
        builder.comment("Logging configuration")
            .push("logging");
        
        CONSOLE_LOGGING = builder
            .comment("Log violations to console")
            .define("console", true);
        
        FILE_LOGGING = builder
            .comment("Log violations to file")
            .define("file", true);
        
        LOG_RETENTION_DAYS = builder
            .comment("Keep logs for this many days")
            .defineInRange("retentionDays", 30, 1, 365);
        
        ALERTS_ENABLED = builder
            .comment("Alert operators in-game")
            .define("alertsEnabled", true);
        
        ALERTS_MIN_CERTAINTY = builder
            .comment("Minimum certainty to alert ops")
            .defineInRange("alertsMinCertainty", 60.0, 0.0, 100.0);
        
        builder.pop();
        
        SPEC = builder.build();
    }
    
    public ConfigManager() {
        // Configuration is automatically loaded by NeoForge
    }
    
    /**
     * Check if anti-cheat is enabled
     */
    public boolean isEnabled() {
        return ENABLED.get();
    }
    
    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugMode() {
        return DEBUG_MODE.get();
    }
    
    /**
     * Get check interval in ticks
     */
    public int getCheckInterval() {
        return CHECK_INTERVAL.get();
    }
    
    /**
     * Check if async processing is enabled
     */
    public boolean isAsyncProcessing() {
        return ASYNC_PROCESSING.get();
    }
    
    /**
     * Get decay time in milliseconds
     */
    public long getDecayTimeMs() {
        return DECAY_TIME.get() * 1000L;
    }
    
    /**
     * Get punishment threshold
     */
    public double getPunishmentThreshold() {
        return PUNISHMENT_THRESHOLD.get();
    }
    
    /**
     * Get alert threshold
     */
    public double getAlertThreshold() {
        return ALERT_THRESHOLD.get();
    }
    
    /**
     * Check if punishments are enabled
     */
    public boolean arePunishmentsEnabled() {
        return PUNISHMENTS_ENABLED.get();
    }
    
    /**
     * Get punishment certainty threshold
     */
    public double getPunishmentCertaintyThreshold() {
        return PUNISHMENT_CERTAINTY_THRESHOLD.get();
    }
    
    /**
     * Get punishment steps
     */
    public List<Integer> getPunishmentSteps() {
        return new java.util.ArrayList<>(PUNISHMENT_STEPS.get());
    }
}
