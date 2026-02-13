package com.raeyn.anticheat.listeners;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.commands.RaeYNCheatCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Server event listener for command registration and lifecycle events
 */
public class ServerEventListener {
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RaeYNCheatCommand.register(event.getDispatcher());
        RaeYNAntiCheat.getLogger().info("Registered /raeyn cheat commands");
    }
    
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        RaeYNAntiCheat.getLogger().info("Server started - RaeYNAntiCheat is active");
    }
    
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        RaeYNAntiCheat.getLogger().info("Server stopped - RaeYNAntiCheat shutting down");
    }
}
