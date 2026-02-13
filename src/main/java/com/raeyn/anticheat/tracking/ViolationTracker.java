package com.raeyn.anticheat.tracking;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.CheatType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Central violation tracker with false positive detection
 * Manages all player violation data and certainty calculations
 */
public class ViolationTracker {
    
    private final Map<UUID, PlayerViolationData> playerData = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // Configuration (loaded from config)
    private long decayTimeMs = 300000; // 5 minutes default
    private double falsePositiveThreshold = 0.15; // If violations drop significantly, might be FP
    
    public ViolationTracker() {
        // Start periodic cleanup task (every minute)
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredViolations, 1, 1, TimeUnit.MINUTES);
        
        RaeYNAntiCheat.getLogger().info("Violation tracker initialized with decay time: " + (decayTimeMs / 1000) + "s");
    }
    
    /**
     * Add a violation for a player
     */
    public void addViolation(UUID playerId, CheatType type, double severity, String details) {
        PlayerViolationData data = playerData.computeIfAbsent(playerId, PlayerViolationData::new);
        
        Violation violation = new Violation(type, severity, details);
        data.addViolation(violation);
        
        // Check for potential false positive
        detectFalsePositive(data, violation);
        
        // Log the violation
        RaeYNAntiCheat.getInstance().getViolationLogger()
            .logViolation(playerId, type, severity, data.getCertaintyPercentage(), details);
        
        // Check if punishment threshold is reached
        if (data.getCertaintyPercentage() >= 95.0) {
            RaeYNAntiCheat.getInstance().getPunishmentManager()
                .handleViolation(playerId, data.getCertaintyPercentage(), type);
        }
    }
    
    /**
     * Detect potential false positives
     * Uses heuristics to identify violations that may not be legitimate
     */
    private void detectFalsePositive(PlayerViolationData data, Violation violation) {
        // Heuristic 1: Single violation of a type with no recent history
        List<Violation> typeViolations = data.getViolations(violation.getCheatType());
        if (typeViolations.size() == 1) {
            // First violation of this type - low confidence
            // Don't mark as FP but reduce weight
            return;
        }
        
        // Heuristic 2: Low severity violation that doesn't fit pattern
        if (violation.getSeverity() < 0.3) {
            long recentHighSeverity = typeViolations.stream()
                .filter(v -> v.getAgeSeconds() < 60)
                .filter(v -> v.getSeverity() > 0.7)
                .count();
            
            if (recentHighSeverity == 0) {
                // Low severity with no recent high severity - possible FP
                violation.setFlaggedAsFalsePositive(true);
                data.recalculateCertainty();
                
                RaeYNAntiCheat.getLogger().debug("Flagged potential false positive for player " + 
                    data.getPlayerId() + " - " + violation.getCheatType().getName());
            }
        }
        
        // Heuristic 3: Server TPS-based detection
        // If server TPS is low, movement/timing checks are less reliable
        // (This would be implemented with actual TPS tracking)
    }
    
    /**
     * Get violation data for a player
     */
    public PlayerViolationData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, PlayerViolationData::new);
    }
    
    /**
     * Get certainty percentage for a player
     */
    public double getCertainty(UUID playerId) {
        return getPlayerData(playerId).getCertaintyPercentage();
    }
    
    /**
     * Clear all violations for a player
     */
    public void clearViolations(UUID playerId) {
        PlayerViolationData data = playerData.get(playerId);
        if (data != null) {
            data.clear();
        }
    }
    
    /**
     * Remove a player's data entirely
     */
    public void removePlayer(UUID playerId) {
        playerData.remove(playerId);
    }
    
    /**
     * Clean up expired violations for all players
     */
    private void cleanupExpiredViolations() {
        for (PlayerViolationData data : playerData.values()) {
            data.removeExpiredViolations(decayTimeMs);
            
            // Remove player data if no violations remain
            if (data.getTotalViolationCount() == 0) {
                playerData.remove(data.getPlayerId());
            }
        }
    }
    
    /**
     * Get all players with active violations
     */
    public Set<UUID> getPlayersWithViolations() {
        return new HashSet<>(playerData.keySet());
    }
    
    /**
     * Get players above a certain certainty threshold
     */
    public List<UUID> getPlayersAboveThreshold(double threshold) {
        List<UUID> players = new ArrayList<>();
        for (PlayerViolationData data : playerData.values()) {
            if (data.getCertaintyPercentage() >= threshold) {
                players.add(data.getPlayerId());
            }
        }
        return players;
    }
    
    /**
     * Get global statistics
     */
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("trackedPlayers", playerData.size());
        
        int totalViolations = playerData.values().stream()
            .mapToInt(PlayerViolationData::getTotalViolationCount)
            .sum();
        stats.put("totalViolations", totalViolations);
        
        int totalFalsePositives = playerData.values().stream()
            .mapToInt(PlayerViolationData::getFalsePositiveCount)
            .sum();
        stats.put("totalFalsePositives", totalFalsePositives);
        
        double avgCertainty = playerData.values().stream()
            .mapToDouble(PlayerViolationData::getCertaintyPercentage)
            .average()
            .orElse(0.0);
        stats.put("avgCertainty", avgCertainty);
        
        stats.put("playersAbove50", getPlayersAboveThreshold(50.0).size());
        stats.put("playersAbove75", getPlayersAboveThreshold(75.0).size());
        stats.put("playersAbove95", getPlayersAboveThreshold(95.0).size());
        
        return stats;
    }
    
    /**
     * Reload configuration
     */
    public void reload() {
        // Reload decay time from config
        // decayTimeMs = config value
        RaeYNAntiCheat.getLogger().info("Violation tracker reloaded");
    }
    
    /**
     * Shutdown cleanup
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
