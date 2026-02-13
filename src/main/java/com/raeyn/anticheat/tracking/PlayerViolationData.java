package com.raeyn.anticheat.tracking;

import com.raeyn.anticheat.checks.CheatType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player violations with certainty calculation and false positive detection
 */
public class PlayerViolationData {
    private final UUID playerId;
    private final Map<CheatType, List<Violation>> violations = new ConcurrentHashMap<>();
    private double overallCertainty = 0.0;
    private int falsePositiveCount = 0;
    private long lastViolationTime = 0;
    
    public PlayerViolationData(UUID playerId) {
        this.playerId = playerId;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Add a new violation
     */
    public void addViolation(Violation violation) {
        violations.computeIfAbsent(violation.getCheatType(), k -> new ArrayList<>())
            .add(violation);
        lastViolationTime = System.currentTimeMillis();
        recalculateCertainty();
    }
    
    /**
     * Get all violations for a specific cheat type
     */
    public List<Violation> getViolations(CheatType type) {
        return violations.getOrDefault(type, new ArrayList<>());
    }
    
    /**
     * Get all violations across all cheat types
     */
    public List<Violation> getAllViolations() {
        List<Violation> all = new ArrayList<>();
        violations.values().forEach(all::addAll);
        return all;
    }
    
    /**
     * Remove expired violations based on decay time
     */
    public void removeExpiredViolations(long decayTimeMs) {
        violations.values().forEach(list -> 
            list.removeIf(v -> v.hasExpired(decayTimeMs))
        );
        recalculateCertainty();
    }
    
    /**
     * Calculate overall cheat certainty percentage (0-100)
     * Uses weighted algorithm considering:
     * - Number of violations
     * - Severity of violations
     * - Recency of violations
     * - Variety of cheat types detected
     * - False positive history
     */
    public void recalculateCertainty() {
        List<Violation> allViolations = getAllViolations();
        
        if (allViolations.isEmpty()) {
            overallCertainty = 0.0;
            return;
        }
        
        // Filter out flagged false positives
        List<Violation> validViolations = new ArrayList<>();
        for (Violation v : allViolations) {
            if (!v.isFlaggedAsFalsePositive()) {
                validViolations.add(v);
            }
        }
        
        if (validViolations.isEmpty()) {
            overallCertainty = 0.0;
            return;
        }
        
        double certainty = 0.0;
        long currentTime = System.currentTimeMillis();
        
        // Factor 1: Average severity of recent violations (weighted by recency)
        double totalWeightedSeverity = 0.0;
        double totalWeight = 0.0;
        
        for (Violation v : validViolations) {
            long ageMs = currentTime - v.getTimestamp();
            double recencyWeight = Math.exp(-ageMs / 60000.0); // Exponential decay over 1 minute
            double weight = recencyWeight * v.getCheatType().getBaseWeight();
            
            totalWeightedSeverity += v.getSeverity() * weight;
            totalWeight += weight;
        }
        
        double avgSeverity = totalWeight > 0 ? totalWeightedSeverity / totalWeight : 0;
        certainty += avgSeverity * 40.0; // Up to 40% from severity
        
        // Factor 2: Number of violations (diminishing returns)
        int violationCount = validViolations.size();
        double countFactor = Math.min(1.0, Math.log1p(violationCount) / 3.0);
        certainty += countFactor * 30.0; // Up to 30% from count
        
        // Factor 3: Variety of cheat types (more types = higher certainty)
        int uniqueTypes = (int) validViolations.stream()
            .map(Violation::getCheatType)
            .distinct()
            .count();
        double varietyFactor = Math.min(1.0, uniqueTypes / 5.0);
        certainty += varietyFactor * 20.0; // Up to 20% from variety
        
        // Factor 4: Recent activity (violations in last 30 seconds)
        long recentViolations = validViolations.stream()
            .filter(v -> v.getAgeSeconds() < 30)
            .count();
        double recentFactor = Math.min(1.0, recentViolations / 3.0);
        certainty += recentFactor * 10.0; // Up to 10% from recent activity
        
        // Apply false positive penalty
        double fpPenalty = Math.min(0.5, falsePositiveCount * 0.1); // Up to 50% reduction
        certainty *= (1.0 - fpPenalty);
        
        // Clamp to 0-100
        overallCertainty = Math.max(0.0, Math.min(100.0, certainty));
    }
    
    /**
     * Get the overall cheat certainty percentage (0-100)
     */
    public double getCertaintyPercentage() {
        return overallCertainty;
    }
    
    /**
     * Get certainty for a specific cheat type
     */
    public double getCertaintyForType(CheatType type) {
        List<Violation> typeViolations = getViolations(type);
        if (typeViolations.isEmpty()) {
            return 0.0;
        }
        
        double avgSeverity = typeViolations.stream()
            .filter(v -> !v.isFlaggedAsFalsePositive())
            .mapToDouble(Violation::getSeverity)
            .average()
            .orElse(0.0);
        
        double countFactor = Math.min(1.0, typeViolations.size() / 5.0);
        
        return Math.min(100.0, (avgSeverity * 60.0) + (countFactor * 40.0));
    }
    
    /**
     * Mark a violation as a potential false positive
     */
    public void markAsFalsePositive(Violation violation) {
        violation.setFlaggedAsFalsePositive(true);
        falsePositiveCount++;
        recalculateCertainty();
    }
    
    /**
     * Get the total number of violations
     */
    public int getTotalViolationCount() {
        return getAllViolations().size();
    }
    
    /**
     * Get the number of violations for a specific type
     */
    public int getViolationCount(CheatType type) {
        return getViolations(type).size();
    }
    
    /**
     * Get the time of the last violation
     */
    public long getLastViolationTime() {
        return lastViolationTime;
    }
    
    /**
     * Get false positive count
     */
    public int getFalsePositiveCount() {
        return falsePositiveCount;
    }
    
    /**
     * Clear all violations
     */
    public void clear() {
        violations.clear();
        overallCertainty = 0.0;
        falsePositiveCount = 0;
    }
    
    /**
     * Get summary statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("certainty", overallCertainty);
        stats.put("totalViolations", getTotalViolationCount());
        stats.put("falsePositives", falsePositiveCount);
        stats.put("uniqueTypes", violations.keySet().size());
        stats.put("lastViolation", lastViolationTime);
        
        Map<String, Integer> byType = new HashMap<>();
        for (Map.Entry<CheatType, List<Violation>> entry : violations.entrySet()) {
            byType.put(entry.getKey().getName(), entry.getValue().size());
        }
        stats.put("byType", byType);
        
        return stats;
    }
}
