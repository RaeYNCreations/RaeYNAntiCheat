package com.raeyn.anticheat.checks.impl;

import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;

/**
 * Detects instant bow shots
 */
public class FastBowCheck extends AbstractCheck {
    
    public FastBowCheck() {
        super(CheatType.FASTBOW);
    }
    
    @Override
    public double check(ServerPlayer player, Object... data) {
        // TODO: Implement actual detection logic
        // This is a stub implementation
        return 0.0;
    }
}
