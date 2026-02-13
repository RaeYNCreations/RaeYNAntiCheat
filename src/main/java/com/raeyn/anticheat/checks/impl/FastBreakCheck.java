package com.raeyn.anticheat.checks.impl;

import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;

/**
 * Detects too-fast block breaking
 */
public class FastBreakCheck extends AbstractCheck {
    
    public FastBreakCheck() {
        super(CheatType.FASTBREAK);
    }
    
    @Override
    public double check(ServerPlayer player, Object... data) {
        // TODO: Implement actual detection logic
        // This is a stub implementation
        return 0.0;
    }
}
