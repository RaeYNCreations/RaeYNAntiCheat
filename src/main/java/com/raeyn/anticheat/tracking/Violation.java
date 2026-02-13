package com.raeyn.anticheat.tracking;

import com.raeyn.anticheat.checks.CheatType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single violation record
 */
public class Violation {
    private final CheatType cheatType;
    private final long timestamp;
    private final double severity; // 0.0 - 1.0
    private final String details;
    private boolean flaggedAsFalsePositive;
    
    public Violation(CheatType cheatType, double severity, String details) {
        this.cheatType = cheatType;
        this.timestamp = System.currentTimeMillis();
        this.severity = Math.max(0.0, Math.min(1.0, severity));
        this.details = details;
        this.flaggedAsFalsePositive = false;
    }
    
    public CheatType getCheatType() {
        return cheatType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public double getSeverity() {
        return severity;
    }
    
    public String getDetails() {
        return details;
    }
    
    public boolean isFlaggedAsFalsePositive() {
        return flaggedAsFalsePositive;
    }
    
    public void setFlaggedAsFalsePositive(boolean flagged) {
        this.flaggedAsFalsePositive = flagged;
    }
    
    /**
     * Check if this violation has expired based on decay time
     */
    public boolean hasExpired(long decayTimeMs) {
        return System.currentTimeMillis() - timestamp > decayTimeMs;
    }
    
    /**
     * Get the age of this violation in seconds
     */
    public long getAgeSeconds() {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }
}
