package com.raeyn.anticheat.checks.impl;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.Check;
import com.raeyn.anticheat.checks.CheatType;

/**
 * Abstract base class for all cheat checks
 * Provides common functionality
 */
public abstract class AbstractCheck implements Check {
    
    protected final CheatType cheatType;
    
    /**
     * Constructs a new check with the specified cheat type
     * @param cheatType the type of cheat this check detects
     */
    public AbstractCheck(CheatType cheatType) {
        this.cheatType = cheatType;
    }
    
    @Override
    public CheatType getCheatType() {
        return cheatType;
    }
    
    @Override
    public boolean isEnabled() {
        // Check configuration
        return RaeYNAntiCheat.getInstance().getConfigManager().isEnabled();
    }
    
    @Override
    public int getSensitivity() {
        // Default sensitivity is 5 (1-10 scale)
        return 5;
    }
    
    /**
     * Utility method to check if a value exceeds a threshold
     */
    protected double calculateViolationLevel(double actual, double expected, double threshold) {
        if (actual <= expected) return 0.0;
        
        double excess = actual - expected;
        double normalizedExcess = excess / threshold;
        
        return Math.min(1.0, normalizedExcess);
    }
}
