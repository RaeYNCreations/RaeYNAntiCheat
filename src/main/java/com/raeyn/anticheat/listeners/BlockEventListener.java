package com.raeyn.anticheat.listeners;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.Check;
import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Listener for block interaction events
 * Detects X-Ray, Nuker, Scaffold, FastBreak, FastPlace
 */
public class BlockEventListener {
    
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        
        // Check XRay (ore mining patterns)
        Check xrayCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.XRAY);
        if (xrayCheck != null && xrayCheck.isEnabled()) {
            double violation = xrayCheck.check(player, event.getPos(), event.getState());
            if (violation > 0) {
                xrayCheck.onViolation(player, violation, "Detected suspicious ore mining pattern");
            }
        }
        
        // Check Nuker
        Check nukerCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.NUKER);
        if (nukerCheck != null && nukerCheck.isEnabled()) {
            double violation = nukerCheck.check(player, event.getPos());
            if (violation > 0) {
                nukerCheck.onViolation(player, violation, "Detected rapid block breaking");
            }
        }
        
        // Check FastBreak
        Check fastBreakCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.FASTBREAK);
        if (fastBreakCheck != null && fastBreakCheck.isEnabled()) {
            double violation = fastBreakCheck.check(player, event.getState());
            if (violation > 0) {
                fastBreakCheck.onViolation(player, violation, "Detected too-fast block breaking");
            }
        }
    }
    
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Check Scaffold
        Check scaffoldCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.SCAFFOLD);
        if (scaffoldCheck != null && scaffoldCheck.isEnabled()) {
            double violation = scaffoldCheck.check(player, event.getPos());
            if (violation > 0) {
                scaffoldCheck.onViolation(player, violation, "Detected impossible block placement");
            }
        }
        
        // Check FastPlace
        Check fastPlaceCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.FASTPLACE);
        if (fastPlaceCheck != null && fastPlaceCheck.isEnabled()) {
            double violation = fastPlaceCheck.check(player, event.getPos());
            if (violation > 0) {
                fastPlaceCheck.onViolation(player, violation, "Detected too-fast block placing");
            }
        }
    }
}
