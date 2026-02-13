package com.raeyn.anticheat.util;

import com.raeyn.anticheat.RaeYNAntiCheat;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Integration utility for RaeYNCheat mod
 * Provides soft dependency integration without requiring RaeYNCheat to be present
 * 
 * This allows RaeYNAntiCheat to work with RaeYNCheat when available, but also
 * function independently when RaeYNCheat is not installed.
 */
public class RaeYNCheatIntegration {
    
    private boolean initialized = false;
    private Class<?> raeynCheatClass = null;
    private Method getInstanceMethod = null;
    private Method recordViolationMethod = null;
    private Method getViolationCountMethod = null;
    
    public RaeYNCheatIntegration() {
        try {
            // Attempt to load RaeYNCheat class via reflection
            raeynCheatClass = Class.forName("com.raeyncreations.raeyncheat.RaeYNCheat");
            
            // Get methods we might want to call
            getInstanceMethod = raeynCheatClass.getMethod("getInstance");
            recordViolationMethod = raeynCheatClass.getMethod("recordViolation", UUID.class);
            getViolationCountMethod = raeynCheatClass.getMethod("getChecksumViolationCount", UUID.class);
            
            initialized = true;
            RaeYNAntiCheat.getLogger().info("Successfully integrated with RaeYNCheat!");
            RaeYNAntiCheat.getLogger().info("RaeYNAntiCheat will coordinate with RaeYNCheat boot protection");
        } catch (ClassNotFoundException e) {
            RaeYNAntiCheat.getLogger().info("RaeYNCheat not found - running in standalone mode");
        } catch (NoSuchMethodException e) {
            RaeYNAntiCheat.getLogger().warn("RaeYNCheat found but integration failed - version mismatch?");
        }
    }
    
    /**
     * Check if RaeYNCheat integration is active
     */
    public boolean isIntegrated() {
        return initialized;
    }
    
    /**
     * Notify RaeYNCheat of a player violation
     * This can be used to coordinate punishment between the two systems
     */
    public void notifyViolation(UUID playerId) {
        if (!initialized) return;
        
        try {
            Object raeynCheatInstance = getInstanceMethod.invoke(null);
            if (raeynCheatInstance != null && recordViolationMethod != null) {
                recordViolationMethod.invoke(raeynCheatInstance, playerId);
                RaeYNAntiCheat.getLogger().debug("Notified RaeYNCheat of violation for player: " + playerId);
            }
        } catch (Exception e) {
            RaeYNAntiCheat.getLogger().error("Failed to notify RaeYNCheat of violation", e);
        }
    }
    
    /**
     * Get RaeYNCheat violation count for a player
     * This can be used to factor in boot protection violations when calculating certainty
     */
    public int getRaeYNCheatViolationCount(UUID playerId) {
        if (!initialized) return 0;
        
        try {
            Object raeynCheatInstance = getInstanceMethod.invoke(null);
            if (raeynCheatInstance != null && getViolationCountMethod != null) {
                Object result = getViolationCountMethod.invoke(raeynCheatInstance, playerId);
                if (result instanceof Integer) {
                    return (Integer) result;
                }
            }
        } catch (Exception e) {
            RaeYNAntiCheat.getLogger().error("Failed to get RaeYNCheat violation count", e);
        }
        
        return 0;
    }
    
    /**
     * Check if a player has passed RaeYNCheat boot protection
     * Players who haven't passed boot protection should be handled more carefully
     */
    public boolean hasPassedBootProtection(ServerPlayer player) {
        if (!initialized) {
            // If RaeYNCheat is not present, assume boot protection is not applicable
            return true;
        }
        
        // If player has RaeYNCheat violations, they've been through boot protection
        return getRaeYNCheatViolationCount(player.getUUID()) >= 0;
    }
    
    /**
     * Get combined certainty including RaeYNCheat violations
     * This provides a holistic view of the player's cheat likelihood
     */
    public double getCombinedCertainty(UUID playerId, double antiCheatCertainty) {
        if (!initialized) {
            return antiCheatCertainty;
        }
        
        int raeynCheatViolations = getRaeYNCheatViolationCount(playerId);
        if (raeynCheatViolations > 0) {
            // Add up to 20% certainty based on RaeYNCheat violations
            double raeynCheatBonus = Math.min(20.0, raeynCheatViolations * 5.0);
            return Math.min(100.0, antiCheatCertainty + raeynCheatBonus);
        }
        
        return antiCheatCertainty;
    }
    
    /**
     * Log integration status
     */
    public String getIntegrationStatus() {
        if (initialized) {
            return "§aIntegrated with RaeYNCheat";
        } else {
            return "§eStandalone mode (RaeYNCheat not present)";
        }
    }
}
