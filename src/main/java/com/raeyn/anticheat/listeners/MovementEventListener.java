package com.raeyn.anticheat.listeners;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.Check;
import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Listener for movement-related events
 * Detects Fly, Speed, NoFall, Phase, Jesus cheats
 */
public class MovementEventListener {
    
    private int tickCounter = 0;
    
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Run checks at configured interval
        tickCounter++;
        int checkInterval = RaeYNAntiCheat.getInstance().getConfigManager().getCheckInterval();
        
        if (tickCounter % checkInterval != 0) return;
        
        // Run movement checks
        Check flyCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.FLY);
        if (flyCheck != null && flyCheck.isEnabled()) {
            double violation = flyCheck.check(player);
            if (violation > 0) {
                flyCheck.onViolation(player, violation, "Detected unauthorized flying");
            }
        }
        
        Check speedCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.SPEED);
        if (speedCheck != null && speedCheck.isEnabled()) {
            double violation = speedCheck.check(player);
            if (violation > 0) {
                speedCheck.onViolation(player, violation, "Detected speed hacking");
            }
        }
        
        Check phaseCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.PHASE);
        if (phaseCheck != null && phaseCheck.isEnabled()) {
            double violation = phaseCheck.check(player);
            if (violation > 0) {
                phaseCheck.onViolation(player, violation, "Detected phasing through blocks");
            }
        }
        
        Check jesusCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.JESUS);
        if (jesusCheck != null && jesusCheck.isEnabled()) {
            double violation = jesusCheck.check(player);
            if (violation > 0) {
                jesusCheck.onViolation(player, violation, "Detected walking on water/lava");
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Check NoFall
        Check noFallCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.NOFALL);
        if (noFallCheck != null && noFallCheck.isEnabled()) {
            double violation = noFallCheck.check(player, event.getDistance());
            if (violation > 0) {
                noFallCheck.onViolation(player, violation, "Detected NoFall hack (distance: " + event.getDistance() + ")");
            }
        }
    }
}
