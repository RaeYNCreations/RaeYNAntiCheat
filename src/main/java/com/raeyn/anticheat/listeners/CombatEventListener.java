package com.raeyn.anticheat.listeners;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.Check;
import com.raeyn.anticheat.checks.CheatType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

/**
 * Listener for combat-related events
 * Detects KillAura, Reach, Criticals, AutoClicker, Velocity, AntiKnockback
 */
public class CombatEventListener {
    
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Check KillAura
        Check killAuraCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.KILLAURA);
        if (killAuraCheck != null && killAuraCheck.isEnabled()) {
            double violation = killAuraCheck.check(player, event.getTarget());
            if (violation > 0) {
                killAuraCheck.onViolation(player, violation, "Detected KillAura/multi-target attack");
            }
        }
        
        // Check Reach
        Check reachCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.REACH);
        if (reachCheck != null && reachCheck.isEnabled()) {
            double violation = reachCheck.check(player, event.getTarget());
            if (violation > 0) {
                reachCheck.onViolation(player, violation, "Detected extended reach");
            }
        }
        
        // Check AutoClicker
        Check autoClickerCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.AUTOCLICKER);
        if (autoClickerCheck != null && autoClickerCheck.isEnabled()) {
            double violation = autoClickerCheck.check(player);
            if (violation > 0) {
                autoClickerCheck.onViolation(player, violation, "Detected auto-clicker");
            }
        }
    }
    
    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Check Criticals
        Check criticalsCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.CRITICALS);
        if (criticalsCheck != null && criticalsCheck.isEnabled()) {
            double violation = criticalsCheck.check(player, event.isVanillaCritical());
            if (violation > 0) {
                criticalsCheck.onViolation(player, violation, "Detected forced critical hits");
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Check Infinite Health (taking damage but health doesn't decrease)
        Check infiniteHealthCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.INFINITE_HEALTH);
        if (infiniteHealthCheck != null && infiniteHealthCheck.isEnabled()) {
            double violation = infiniteHealthCheck.check(player, event.getNewDamage());
            if (violation > 0) {
                infiniteHealthCheck.onViolation(player, violation, "Detected god mode/infinite health");
            }
        }
        
        // Check Velocity/AntiKnockback
        Check velocityCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.VELOCITY);
        if (velocityCheck != null && velocityCheck.isEnabled()) {
            double violation = velocityCheck.check(player, event.getSource());
            if (violation > 0) {
                velocityCheck.onViolation(player, violation, "Detected velocity/knockback modification");
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Check abnormal Regeneration
        Check regenCheck = RaeYNAntiCheat.getInstance().getCheckManager().getCheck(CheatType.REGENERATION);
        if (regenCheck != null && regenCheck.isEnabled()) {
            double violation = regenCheck.check(player, event.getAmount());
            if (violation > 0) {
                regenCheck.onViolation(player, violation, "Detected abnormal health regeneration");
            }
        }
    }
}
