package com.raeyn.anticheat.checks.impl;

import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;

/**
 * Detects unauthorized flying
 */
public class FlyCheck extends AbstractCheck {
    
    public FlyCheck() {
        super(CheatType.FLY);
    }
    
    @Override
    public double check(ServerPlayer player, Object... data) {
        // TODO: Implement actual detection logic
        // This is a stub implementation
        return 0.0;
    }
}
