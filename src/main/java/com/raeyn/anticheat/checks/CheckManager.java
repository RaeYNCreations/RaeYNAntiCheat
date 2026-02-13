package com.raeyn.anticheat.checks;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.impl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all cheat detection checks
 * Provides a centralized registry and control for all detection modules
 */
public class CheckManager {
    
    private final Map<CheatType, Check> checks = new HashMap<>();
    private final List<Check> activeChecks = new ArrayList<>();
    
    public CheckManager() {
        registerChecks();
        RaeYNAntiCheat.getLogger().info("Registered " + checks.size() + " cheat detection modules");
    }
    
    /**
     * Register all available checks
     */
    private void registerChecks() {
        // Movement checks
        register(new FlyCheck());
        register(new SpeedCheck());
        register(new NoFallCheck());
        register(new PhaseCheck());
        register(new JesusCheck());
        
        // Combat checks
        register(new KillAuraCheck());
        register(new ReachCheck());
        register(new CriticalsCheck());
        register(new AutoClickerCheck());
        register(new VelocityCheck());
        register(new AntiKnockbackCheck());
        
        // Block checks
        register(new XRayCheck());
        register(new NukerCheck());
        register(new ScaffoldCheck());
        register(new FastBreakCheck());
        register(new FastPlaceCheck());
        
        // Player state checks
        register(new InfiniteHealthCheck());
        register(new RegenerationCheck());
        register(new OPGuardCheck());
        register(new FreecamCheck());
        
        // Inventory checks
        register(new ChestStealerCheck());
        register(new InventoryMoveCheck());
        register(new AutoArmorCheck());
        
        // Other checks
        register(new FastBowCheck());
        register(new BlinkCheck());
        register(new TimerCheck());
    }
    
    /**
     * Register a check
     */
    private void register(Check check) {
        checks.put(check.getCheatType(), check);
        if (check.isEnabled()) {
            activeChecks.add(check);
        }
    }
    
    /**
     * Get a specific check by type
     */
    public Check getCheck(CheatType type) {
        return checks.get(type);
    }
    
    /**
     * Get all registered checks
     */
    public List<Check> getAllChecks() {
        return new ArrayList<>(checks.values());
    }
    
    /**
     * Get all active (enabled) checks
     */
    public List<Check> getActiveChecks() {
        return new ArrayList<>(activeChecks);
    }
    
    /**
     * Reload all checks from configuration
     */
    public void reload() {
        activeChecks.clear();
        for (Check check : checks.values()) {
            if (check.isEnabled()) {
                activeChecks.add(check);
            }
        }
        RaeYNAntiCheat.getLogger().info("Reloaded checks: " + activeChecks.size() + " active");
    }
    
    /**
     * Get statistics about loaded checks
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", checks.size());
        stats.put("active", activeChecks.size());
        stats.put("disabled", checks.size() - activeChecks.size());
        
        // Count by category
        for (CheatType.CheatCategory category : CheatType.CheatCategory.values()) {
            int count = (int) activeChecks.stream()
                .filter(c -> c.getCheatType().getCategory() == category)
                .count();
            stats.put(category.name().toLowerCase(), count);
        }
        
        return stats;
    }
}
