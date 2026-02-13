package com.raeyn.anticheat.checks.impl;

import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;

/**
 * Detects multi-entity attacks
 */
public class KillAuraCheck extends AbstractCheck {
    
    public KillAuraCheck() {
        super(CheatType.KILLAURA);
    }
    
    @Override
    public double check(ServerPlayer player, Object... data) {
        // TODO: Implement actual detection logic
        // This is a stub implementation
        return 0.0;
    }
}
