package com.raeyn.anticheat.punishment;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Progressive punishment manager using RaeYNCheat's escalating punishment system
 * Each violation increases the punishment level, with escalating durations
 */
public class PunishmentManager {
    
    private final Map<UUID, Integer> punishmentLevels = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastPunishmentTime = new ConcurrentHashMap<>();
    
    // Progressive punishment steps (in seconds, -1 = permanent ban, 0 = warning only)
    // Based on RaeYNCheat's system
    private List<Integer> punishmentSteps = new ArrayList<>();
    
    // Certainty threshold to trigger punishment
    private static final double PUNISHMENT_THRESHOLD = 95.0;
    
    public PunishmentManager() {
        // Load punishment steps from configuration
        loadPunishmentStepsFromConfig();
        
        RaeYNAntiCheat.getLogger().info("Progressive punishment system initialized (RaeYNCheat-style)");
        RaeYNAntiCheat.getLogger().info("Punishment steps: " + punishmentSteps.size() + " levels configured");
    }
    
    /**
     * Load punishment steps from configuration
     */
    private void loadPunishmentStepsFromConfig() {
        List<Integer> configSteps = RaeYNAntiCheat.getInstance().getConfigManager().getPunishmentSteps();
        
        if (configSteps != null && !configSteps.isEmpty()) {
            punishmentSteps = new ArrayList<>(configSteps);
        } else {
            // Fallback to default steps if config is empty
            punishmentSteps.add(0);      // 1st violation: Warning only
            punishmentSteps.add(60);     // 2nd violation: 1 minute
            punishmentSteps.add(300);    // 3rd violation: 5 minutes
            punishmentSteps.add(600);    // 4th violation: 10 minutes
            punishmentSteps.add(1800);   // 5th violation: 30 minutes
            punishmentSteps.add(3600);   // 6th violation: 1 hour
            punishmentSteps.add(7200);   // 7th violation: 2 hours
            punishmentSteps.add(14400);  // 8th violation: 4 hours
            punishmentSteps.add(28800);  // 9th violation: 8 hours
            punishmentSteps.add(86400);  // 10th violation: 24 hours
            punishmentSteps.add(-1);     // 11th+ violation: Permanent ban
        }
    }
    
    /**
     * Handle a violation and apply appropriate punishment based on progressive system
     */
    public void handleViolation(UUID playerId, double certainty, CheatType cheatType) {
        // Only punish if certainty is above threshold
        if (certainty < PUNISHMENT_THRESHOLD) {
            return;
        }
        
        // Don't spam punishments - cooldown of 10 seconds
        long lastPunishment = lastPunishmentTime.getOrDefault(playerId, 0L);
        if (System.currentTimeMillis() - lastPunishment < 10000) {
            return;
        }
        
        MinecraftServer server = getServer();
        if (server == null) return;
        
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) return;
        
        // Check bypass permission
        if (player.hasPermissions(4)) { // Op level 4 bypass
            return;
        }
        
        lastPunishmentTime.put(playerId, System.currentTimeMillis());
        
        // Get current violation count and increment
        int violationCount = punishmentLevels.getOrDefault(playerId, 0);
        int newViolationCount = violationCount + 1;
        punishmentLevels.put(playerId, newViolationCount);
        
        // Get punishment duration for this violation level
        int duration = getPunishmentDuration(newViolationCount);
        
        // Apply punishment based on duration
        if (duration == -1) {
            // Permanent ban
            applyBan(player, cheatType, true, 0, newViolationCount, certainty);
        } else if (duration > 0) {
            // Temporary ban
            applyBan(player, cheatType, false, duration, newViolationCount, certainty);
        } else {
            // Warning only (duration == 0)
            applyWarning(player, cheatType, certainty, newViolationCount);
        }
        
        RaeYNAntiCheat.getLogger().warn("Applied punishment to " + player.getName().getString() + 
            " (Violation #" + newViolationCount + ", Certainty: " + String.format("%.2f", certainty) + 
            "%, Type: " + cheatType.getName() + ", Duration: " + formatDuration(duration) + ")");
    }
    
    /**
     * Get punishment duration for a given violation count (progressive escalation)
     */
    private int getPunishmentDuration(int violationCount) {
        int index = violationCount - 1;
        if (index < 0) {
            return 0; // No punishment
        }
        if (index >= punishmentSteps.size()) {
            // If beyond configured steps, use last step (usually permanent ban)
            return punishmentSteps.get(punishmentSteps.size() - 1);
        }
        return punishmentSteps.get(index);
    }
    
    /**
     * Apply a warning to the player
     */
    private void applyWarning(ServerPlayer player, CheatType cheatType, double certainty, int violationCount) {
        Component message = Component.literal("§e[RaeYNAntiCheat] §cYou have been flagged for suspicious activity")
            .append(Component.literal("\n§7Detected: §c" + cheatType.getName()))
            .append(Component.literal("\n§7Certainty: §c" + String.format("%.1f", certainty) + "%"))
            .append(Component.literal("\n§7Violation: §e#" + violationCount))
            .append(Component.literal("\n§7Warning: §eContinued violations will result in bans"));
        
        player.sendSystemMessage(message);
        
        // Notify online operators
        notifyOperators("§e[AC] §c" + player.getName().getString() + " §7warned for §c" + 
            cheatType.getName() + " §7(#" + violationCount + ", " + String.format("%.1f", certainty) + "%)");
    }
    
    /**
     * Ban a player (temporary or permanent) - Progressive system
     */
    private void applyBan(ServerPlayer player, CheatType cheatType, boolean permanent, int durationSeconds, 
                         int violationCount, double certainty) {
        String playerName = player.getName().getString();
        UUID playerId = player.getUUID();
        
        Component banMessage;
        if (permanent) {
            banMessage = Component.literal("§c§lPERMANENTLY BANNED")
                .append(Component.literal("\n\n§7Reason: §c" + cheatType.getDescription()))
                .append(Component.literal("\n§7Detection: §cRaeYNAntiCheat"))
                .append(Component.literal("\n§7Violation: §e#" + violationCount))
                .append(Component.literal("\n§7Certainty: §c" + String.format("%.1f", certainty) + "%"))
                .append(Component.literal("\n\n§7This ban is permanent"));
            
            // Add to server ban list
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getPlayerList().getBans().add(
                    new net.minecraft.server.players.UserBanListEntry(
                        player.getGameProfile(),
                        null,
                        "RaeYNAntiCheat",
                        null,
                        "Cheating (" + violationCount + " violations): " + cheatType.getName()
                    )
                );
            }
            
            notifyOperators("§c§l[AC] " + playerName + " PERMANENTLY BANNED §7for §c" + 
                cheatType.getName() + " §7(#" + violationCount + ")");
        } else {
            long banUntil = System.currentTimeMillis() + (durationSeconds * 1000L);
            String duration = formatDuration(durationSeconds);
            
            banMessage = Component.literal("§c§lTEMPORARILY BANNED")
                .append(Component.literal("\n\n§7Reason: §c" + cheatType.getDescription()))
                .append(Component.literal("\n§7Detection: §cRaeYNAntiCheat"))
                .append(Component.literal("\n§7Duration: §e" + duration))
                .append(Component.literal("\n§7Violation: §e#" + violationCount))
                .append(Component.literal("\n§7Certainty: §c" + String.format("%.1f", certainty) + "%"))
                .append(Component.literal("\n\n§7You may reconnect after the ban expires"));
            
            // Add to server ban list with expiration
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getPlayerList().getBans().add(
                    new net.minecraft.server.players.UserBanListEntry(
                        player.getGameProfile(),
                        null,
                        "RaeYNAntiCheat",
                        new java.util.Date(banUntil),
                        "Cheating (" + violationCount + " violations): " + cheatType.getName()
                    )
                );
            }
            
            notifyOperators("§e[AC] §c" + playerName + " §7TEMP BANNED for §c" + 
                cheatType.getName() + " §7(" + duration + ", #" + violationCount + ")");
        }
        
        player.connection.disconnect(banMessage);
        
        // Log the punishment
        RaeYNAntiCheat.getInstance().getViolationLogger()
            .logPunishment(playerId, permanent ? "PERMANENT BAN" : "TEMP BAN (" + formatDuration(durationSeconds) + ")", 
                cheatType, certainty);
    }
    
    /**
     * Format duration in human-readable format
     */
    private String formatDuration(int seconds) {
        if (seconds == -1) return "PERMANENT";
        if (seconds == 0) return "WARNING";
        if (seconds < 60) return seconds + " seconds";
        if (seconds < 3600) return (seconds / 60) + " minutes";
        if (seconds < 86400) return (seconds / 3600) + " hours";
        return (seconds / 86400) + " days";
    }
    
    /**
     * Manually punish a player (for admin commands)
     * Similar to RaeYNCheat's /raeynpunish command
     */
    public void manualPunish(UUID playerId, CheatType cheatType) {
        MinecraftServer server = getServer();
        if (server == null) return;
        
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) return;
        
        // Increment violation count and apply punishment
        int violationCount = punishmentLevels.getOrDefault(playerId, 0) + 1;
        punishmentLevels.put(playerId, violationCount);
        
        int duration = getPunishmentDuration(violationCount);
        
        RaeYNAntiCheat.getLogger().info("Manual punishment applied to " + player.getName().getString() + 
            " (Violation #" + violationCount + ")");
        
        if (duration == -1) {
            applyBan(player, cheatType, true, 0, violationCount, 100.0);
        } else if (duration > 0) {
            applyBan(player, cheatType, false, duration, violationCount, 100.0);
        } else {
            applyWarning(player, cheatType, 100.0, violationCount);
        }
    }
    
    /**
     * Set custom punishment steps (for configuration)
     */
    public void setPunishmentSteps(List<Integer> steps) {
        if (steps != null && !steps.isEmpty()) {
            this.punishmentSteps = new ArrayList<>(steps);
            RaeYNAntiCheat.getLogger().info("Updated punishment steps: " + steps.size() + " levels");
        }
    }
    
    /**
     * Get punishment steps
     */
    public List<Integer> getPunishmentSteps() {
        return new ArrayList<>(punishmentSteps);
    }
    
    /**
     * Notify online operators
     */
    private void notifyOperators(String message) {
        MinecraftServer server = getServer();
        if (server == null) return;
        
        Component component = Component.literal(message);
        server.getPlayerList().getPlayers().forEach(player -> {
            if (player.hasPermissions(2)) {
                player.sendSystemMessage(component);
            }
        });
    }
    
    /**
     * Get the Minecraft server instance
     */
    private MinecraftServer getServer() {
        // This will be set by the event listeners
        return net.minecraft.server.MinecraftServer.getServer();
    }
    
    /**
     * Reset punishment level for a player
     */
    public void resetPunishmentLevel(UUID playerId) {
        punishmentLevels.remove(playerId);
        lastPunishmentTime.remove(playerId);
    }
    
    /**
     * Get punishment level for a player
     */
    public int getPunishmentLevel(UUID playerId) {
        return punishmentLevels.getOrDefault(playerId, 0);
    }
    
    /**
     * Reload configuration
     */
    public void reload() {
        loadPunishmentStepsFromConfig();
        RaeYNAntiCheat.getLogger().info("Punishment manager reloaded with " + punishmentSteps.size() + " steps");
    }
}
