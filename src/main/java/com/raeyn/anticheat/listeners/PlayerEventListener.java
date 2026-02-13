package com.raeyn.anticheat.listeners;

import com.raeyn.anticheat.RaeYNAntiCheat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Listener for player connection events
 * Handles login and logout for violation tracking
 */
public class PlayerEventListener {
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            RaeYNAntiCheat.getLogger().info("Player logged in: " + player.getName().getString() + 
                " - Starting anti-cheat monitoring");
            
            // Check if RaeYNCheat is present and if player passed boot protection
            if (RaeYNAntiCheat.getInstance().isRaeYNCheatPresent()) {
                boolean passed = RaeYNAntiCheat.getInstance().getRaeYNCheatIntegration()
                    .hasPassedBootProtection(player);
                if (passed) {
                    RaeYNAntiCheat.getLogger().debug("Player " + player.getName().getString() + 
                        " has passed RaeYNCheat boot protection");
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            RaeYNAntiCheat.getLogger().info("Player logged out: " + player.getName().getString());
            
            // Keep violation data for a while in case player reconnects
            // Cleanup will happen automatically via the tracker's cleanup task
        }
    }
}
