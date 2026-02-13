package com.raeyn.anticheat.logging;

import com.raeyn.anticheat.RaeYNAntiCheat;
import com.raeyn.anticheat.checks.CheatType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive logging system for violations
 * Logs to both console and file with rotation support
 */
public class ViolationLogger {
    
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final Path logDirectory;
    
    private boolean consoleLoggingEnabled = true;
    private boolean fileLoggingEnabled = true;
    
    public ViolationLogger() {
        // Create logs directory
        this.logDirectory = Paths.get("logs", "raeynantiacheat");
        try {
            Files.createDirectories(logDirectory);
        } catch (IOException e) {
            RaeYNAntiCheat.getLogger().error("Failed to create log directory", e);
        }
        
        RaeYNAntiCheat.getLogger().info("Violation logger initialized at: " + logDirectory.toAbsolutePath());
    }
    
    /**
     * Log a violation
     */
    public void logViolation(UUID playerId, CheatType cheatType, double severity, double certainty, String details) {
        String playerName = getPlayerName(playerId);
        String timestamp = timeFormat.format(new Date());
        
        String logMessage = String.format("[%s] [%s] [%s] Severity: %.2f, Certainty: %.2f%% - %s",
            timestamp,
            playerName,
            cheatType.getName(),
            severity,
            certainty,
            details
        );
        
        // Log to console if enabled
        if (consoleLoggingEnabled) {
            if (certainty >= 95.0) {
                RaeYNAntiCheat.getLogger().warn(logMessage);
            } else if (certainty >= 75.0) {
                RaeYNAntiCheat.getLogger().warn(logMessage);
            } else {
                RaeYNAntiCheat.getLogger().info(logMessage);
            }
        }
        
        // Log to file asynchronously if enabled
        if (fileLoggingEnabled) {
            logExecutor.submit(() -> writeToFile(logMessage));
        }
    }
    
    /**
     * Log a punishment action
     */
    public void logPunishment(UUID playerId, String action, CheatType cheatType, double certainty) {
        String playerName = getPlayerName(playerId);
        String timestamp = timeFormat.format(new Date());
        
        String logMessage = String.format("[%s] [PUNISHMENT] %s - %s for %s (Certainty: %.2f%%)",
            timestamp,
            playerName,
            action,
            cheatType.getName(),
            certainty
        );
        
        RaeYNAntiCheat.getLogger().warn(logMessage);
        
        if (fileLoggingEnabled) {
            logExecutor.submit(() -> writeToFile(logMessage));
        }
    }
    
    /**
     * Log a general event
     */
    public void logEvent(String event) {
        String timestamp = timeFormat.format(new Date());
        String logMessage = String.format("[%s] [EVENT] %s", timestamp, event);
        
        RaeYNAntiCheat.getLogger().info(logMessage);
        
        if (fileLoggingEnabled) {
            logExecutor.submit(() -> writeToFile(logMessage));
        }
    }
    
    /**
     * Write a log message to file
     */
    private void writeToFile(String message) {
        try {
            String date = dateFormat.format(new Date());
            Path logFile = logDirectory.resolve("violations-" + date + ".log");
            
            try (BufferedWriter writer = Files.newBufferedWriter(logFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
                writer.write(message);
                writer.newLine();
            }
        } catch (IOException e) {
            RaeYNAntiCheat.getLogger().error("Failed to write to log file", e);
        }
    }
    
    /**
     * Get player name from UUID
     */
    private String getPlayerName(UUID playerId) {
        try {
            net.minecraft.server.MinecraftServer server = net.minecraft.server.MinecraftServer.getServer();
            if (server != null) {
                net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player != null) {
                    return player.getName().getString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return playerId.toString();
    }
    
    /**
     * Enable/disable console logging
     */
    public void setConsoleLogging(boolean enabled) {
        this.consoleLoggingEnabled = enabled;
    }
    
    /**
     * Enable/disable file logging
     */
    public void setFileLogging(boolean enabled) {
        this.fileLoggingEnabled = enabled;
    }
    
    /**
     * Clean up old log files (keep only last N days)
     */
    public void cleanOldLogs(int retentionDays) {
        logExecutor.submit(() -> {
            try {
                long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
                
                Files.list(logDirectory)
                    .filter(path -> path.toString().endsWith(".log"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            RaeYNAntiCheat.getLogger().info("Deleted old log file: " + path.getFileName());
                        } catch (IOException e) {
                            RaeYNAntiCheat.getLogger().error("Failed to delete old log: " + path, e);
                        }
                    });
            } catch (IOException e) {
                RaeYNAntiCheat.getLogger().error("Failed to clean old logs", e);
            }
        });
    }
    
    /**
     * Shutdown the logger
     */
    public void shutdown() {
        logExecutor.shutdown();
        try {
            if (!logExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                logExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
