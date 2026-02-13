package com.raeyn.anticheat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.CheatType;
import com.raeyn.anticheat.tracking.PlayerViolationData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main command handler for /raeyn cheat
 * Provides administration and monitoring commands with customizable punishment steps
 */
public class RaeYNCheatCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("raeyn")
                .then(Commands.literal("cheat")
                    // /raeyn cheat reload - Reload configuration
                    .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(4))
                        .executes(RaeYNCheatCommand::reload))
                    
                    // /raeyn cheat stats [player] - View statistics
                    .then(Commands.literal("stats")
                        .requires(source -> source.hasPermission(2))
                        .executes(RaeYNCheatCommand::statsGlobal)
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(RaeYNCheatCommand::statsPlayer)))
                    
                    // /raeyn cheat check <player> - Check player's certainty
                    .then(Commands.literal("check")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(RaeYNCheatCommand::check)))
                    
                    // /raeyn cheat clear <player> - Clear violations
                    .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(3))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(RaeYNCheatCommand::clear)))
                    
                    // /raeyn cheat punish <player> - Manually punish player
                    .then(Commands.literal("punish")
                        .requires(source -> source.hasPermission(3))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(RaeYNCheatCommand::punish)))
                    
                    // /raeyn cheat steps - Punishment step management
                    .then(Commands.literal("steps")
                        .requires(source -> source.hasPermission(2))
                        .executes(RaeYNCheatCommand::viewSteps)
                        
                        // /raeyn cheat steps add <duration> - Add step
                        .then(Commands.literal("add")
                            .requires(source -> source.hasPermission(4))
                            .then(Commands.argument("duration", IntegerArgumentType.integer(-1))
                                .executes(RaeYNCheatCommand::addStep)))
                        
                        // /raeyn cheat steps remove <index> - Remove step
                        .then(Commands.literal("remove")
                            .requires(source -> source.hasPermission(4))
                            .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(RaeYNCheatCommand::removeStep)))
                        
                        // /raeyn cheat steps set <index> <duration> - Modify step
                        .then(Commands.literal("set")
                            .requires(source -> source.hasPermission(4))
                            .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .then(Commands.argument("duration", IntegerArgumentType.integer(-1))
                                    .executes(RaeYNCheatCommand::setStep)))))
                    
                    // /raeyn cheat info - Display mod information
                    .then(Commands.literal("info")
                        .executes(RaeYNCheatCommand::info))
                    
                    // /raeyn cheat help - Display help
                    .then(Commands.literal("help")
                        .executes(RaeYNCheatCommand::help)))
        );
    }
    
    private static int reload(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> 
            Component.literal("§e[RaeYNAntiCheat] §7Reloading configuration..."), false);
        
        RaeYNAntiCheat.getInstance().reload();
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e[RaeYNAntiCheat] §aConfiguration reloaded successfully!"), true);
        
        return 1;
    }
    
    private static int statsGlobal(CommandContext<CommandSourceStack> context) {
        Map<String, Object> stats = RaeYNAntiCheat.getInstance().getViolationTracker().getGlobalStatistics();
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e========== RaeYNAntiCheat Statistics =========="), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Tracked Players: §a" + stats.get("trackedPlayers")), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Total Violations: §c" + stats.get("totalViolations")), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7False Positives: §e" + stats.get("totalFalsePositives")), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Average Certainty: §6" + 
                String.format("%.2f", stats.get("avgCertainty")) + "%"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Players >50%%: §e" + stats.get("playersAbove50")), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Players >75%%: §6" + stats.get("playersAbove75")), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Players >95%%: §c" + stats.get("playersAbove95")), false);
        
        if (RaeYNAntiCheat.getInstance().isRaeYNCheatPresent()) {
            String integrationStatus = RaeYNAntiCheat.getInstance().getRaeYNCheatIntegration().getIntegrationStatus();
            context.getSource().sendSuccess(() -> 
                Component.literal("§7Integration: " + integrationStatus), false);
        }
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e============================================="), false);
        
        return 1;
    }
    
    private static int statsPlayer(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            UUID playerId = player.getUUID();
            
            PlayerViolationData data = RaeYNAntiCheat.getInstance().getViolationTracker().getPlayerData(playerId);
            Map<String, Object> stats = data.getStatistics();
            
            context.getSource().sendSuccess(() -> 
                Component.literal("§e===== " + player.getName().getString() + " Statistics ====="), false);
            context.getSource().sendSuccess(() -> 
                Component.literal("§7Certainty: §c" + String.format("%.2f", stats.get("certainty")) + "%"), false);
            context.getSource().sendSuccess(() -> 
                Component.literal("§7Total Violations: §e" + stats.get("totalViolations")), false);
            context.getSource().sendSuccess(() -> 
                Component.literal("§7False Positives: §a" + stats.get("falsePositives")), false);
            context.getSource().sendSuccess(() -> 
                Component.literal("§7Unique Cheat Types: §6" + stats.get("uniqueTypes")), false);
            
            int punishmentLevel = RaeYNAntiCheat.getInstance().getPunishmentManager().getPunishmentLevel(playerId);
            context.getSource().sendSuccess(() -> 
                Component.literal("§7Punishment Level: §c" + punishmentLevel), false);
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> byType = (Map<String, Integer>) stats.get("byType");
            if (!byType.isEmpty()) {
                context.getSource().sendSuccess(() -> 
                    Component.literal("§7Violations by Type:"), false);
                byType.forEach((type, count) -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("  §e" + type + ": §c" + count), false);
                });
            }
            
            context.getSource().sendSuccess(() -> 
                Component.literal("§e====================================="), false);
            
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cPlayer not found"));
            return 0;
        }
        
        return 1;
    }
    
    private static int check(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            UUID playerId = player.getUUID();
            
            double certainty = RaeYNAntiCheat.getInstance().getViolationTracker().getCertainty(playerId);
            int punishmentLevel = RaeYNAntiCheat.getInstance().getPunishmentManager().getPunishmentLevel(playerId);
            
            String certaintyColor = certainty >= 95.0 ? "§c" : certainty >= 75.0 ? "§6" : certainty >= 50.0 ? "§e" : "§a";
            
            context.getSource().sendSuccess(() -> 
                Component.literal("§e[Check] §7" + player.getName().getString() + 
                    " - Certainty: " + certaintyColor + String.format("%.2f", certainty) + "% " +
                    "§7(Level: §c" + punishmentLevel + "§7)"), true);
            
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cPlayer not found"));
            return 0;
        }
        
        return 1;
    }
    
    private static int clear(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            UUID playerId = player.getUUID();
            
            RaeYNAntiCheat.getInstance().getViolationTracker().clearViolations(playerId);
            RaeYNAntiCheat.getInstance().getPunishmentManager().resetPunishmentLevel(playerId);
            
            context.getSource().sendSuccess(() -> 
                Component.literal("§e[RaeYNAntiCheat] §aCleared all violations for " + player.getName().getString()), true);
            
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cPlayer not found"));
            return 0;
        }
        
        return 1;
    }
    
    private static int punish(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            UUID playerId = player.getUUID();
            
            RaeYNAntiCheat.getInstance().getPunishmentManager().manualPunish(playerId, CheatType.KILLAURA);
            
            context.getSource().sendSuccess(() -> 
                Component.literal("§e[RaeYNAntiCheat] §cManually punished " + player.getName().getString()), true);
            
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cPlayer not found"));
            return 0;
        }
        
        return 1;
    }
    
    private static int viewSteps(CommandContext<CommandSourceStack> context) {
        List<Integer> steps = RaeYNAntiCheat.getInstance().getPunishmentManager().getPunishmentSteps();
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e===== Punishment Steps (Customizable) ====="), false);
        
        for (int i = 0; i < steps.size(); i++) {
            int step = i + 1;
            int duration = steps.get(i);
            String formatted = formatDuration(duration);
            
            context.getSource().sendSuccess(() -> 
                Component.literal("§7" + step + ". §e" + formatted), false);
        }
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e=========================================="), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Use §e/raeyn cheat steps add/remove/set §7to customize"), false);
        
        return 1;
    }
    
    private static int addStep(CommandContext<CommandSourceStack> context) {
        int duration = IntegerArgumentType.getInteger(context, "duration");
        
        List<Integer> steps = RaeYNAntiCheat.getInstance().getPunishmentManager().getPunishmentSteps();
        if (steps.size() >= 30) {
            context.getSource().sendFailure(Component.literal("§cMaximum 30 punishment steps allowed"));
            return 0;
        }
        
        steps.add(duration);
        RaeYNAntiCheat.getInstance().getPunishmentManager().setPunishmentSteps(steps);
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e[RaeYNAntiCheat] §aAdded punishment step: " + formatDuration(duration)), true);
        
        return 1;
    }
    
    private static int removeStep(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index") - 1;
        
        List<Integer> steps = RaeYNAntiCheat.getInstance().getPunishmentManager().getPunishmentSteps();
        if (index < 0 || index >= steps.size()) {
            context.getSource().sendFailure(Component.literal("§cInvalid step index"));
            return 0;
        }
        
        int removed = steps.remove(index);
        RaeYNAntiCheat.getInstance().getPunishmentManager().setPunishmentSteps(steps);
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e[RaeYNAntiCheat] §aRemoved punishment step: " + formatDuration(removed)), true);
        
        return 1;
    }
    
    private static int setStep(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index") - 1;
        int duration = IntegerArgumentType.getInteger(context, "duration");
        
        List<Integer> steps = RaeYNAntiCheat.getInstance().getPunishmentManager().getPunishmentSteps();
        if (index < 0 || index >= steps.size()) {
            context.getSource().sendFailure(Component.literal("§cInvalid step index"));
            return 0;
        }
        
        steps.set(index, duration);
        RaeYNAntiCheat.getInstance().getPunishmentManager().setPunishmentSteps(steps);
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e[RaeYNAntiCheat] §aUpdated step " + (index + 1) + " to: " + formatDuration(duration)), true);
        
        return 1;
    }
    
    private static int info(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> 
            Component.literal("§e========== RaeYNAntiCheat =========="), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Version: §a1.0.0"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Platform: §aNeoForge 1.21.1 (Server-Side Only)"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Cheat Types: §e25 detection modules"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Punishment: §6Progressive (RaeYNCheat-style, Customizable)"), false);
        
        if (RaeYNAntiCheat.getInstance().isRaeYNCheatPresent()) {
            context.getSource().sendSuccess(() -> 
                Component.literal("§7RaeYNCheat: §aIntegrated"), false);
        } else {
            context.getSource().sendSuccess(() -> 
                Component.literal("§7RaeYNCheat: §eStandalone Mode"), false);
        }
        
        context.getSource().sendSuccess(() -> 
            Component.literal("§e===================================="), false);
        
        return 1;
    }
    
    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> 
            Component.literal("§e===== RaeYNAntiCheat Commands ====="), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat reload §f- Reload configuration"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat stats [player] §f- View statistics"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat check <player> §f- Check player certainty"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat clear <player> §f- Clear violations"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat punish <player> §f- Manually punish"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat steps §f- View punishment steps"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat steps add <duration> §f- Add step"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat steps remove <index> §f- Remove step"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat steps set <index> <duration> §f- Modify step"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat info §f- Mod information"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7/raeyn cheat help §f- Show this help"), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§e===================================="), false);
        context.getSource().sendSuccess(() -> 
            Component.literal("§7Note: Punishment steps are customizable!"), false);
        
        return 1;
    }
    
    private static String formatDuration(int seconds) {
        if (seconds == -1) return "PERMANENT BAN";
        if (seconds == 0) return "WARNING";
        if (seconds < 60) return seconds + " seconds";
        if (seconds < 3600) return (seconds / 60) + " minutes";
        if (seconds < 86400) return (seconds / 3600) + " hours";
        return (seconds / 86400) + " days";
    }
}
