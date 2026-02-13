package com.raeyn.anticheat.checks;

import com.raeyn.anticheat.RaeYNAntiCheat;
import net.minecraft.server.level.ServerPlayer;

/**
 * Base interface for all cheat detection checks
 * Provides a modular, extensible architecture for detection modules
 */
public interface Check {
    
    /**
     * Get the cheat type this check detects
     */
    CheatType getCheatType();
    
    /**
     * Check if this detection module is enabled in config
     */
    boolean isEnabled();
    
    /**
     * Get the sensitivity level (1-10)
     */
    int getSensitivity();
    
    /**
     * Perform the cheat check on a player
     * 
     * @param player The player to check
     * @param data Additional context data specific to the check
     * @return The violation level (0.0 = no violation, 1.0 = definite violation)
     */
    double check(ServerPlayer player, Object... data);
    
    /**
     * Called when a violation is detected
     * 
     * @param player The player who violated
     * @param violationLevel The severity of the violation (0.0-1.0)
     * @param details Additional details about the violation
     */
    default void onViolation(ServerPlayer player, double violationLevel, String details) {
        if (violationLevel > 0) {
            RaeYNAntiCheat.getInstance().getViolationTracker()
                .addViolation(player.getUUID(), getCheatType(), violationLevel, details);
        }
    }
    
    /**
     * Get a human-readable name for this check
     */
    default String getName() {
        return getCheatType().getName();
    }
    
    /**
     * Get the description of what this check detects
     */
    default String getDescription() {
        return getCheatType().getDescription();
    }
}
