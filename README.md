# RaeYNAntiCheat

**Server-Side ONLY Anti-Cheat for NeoForge 1.21.1**

An extension to [RaeYNCheat](https://github.com/RaeYNCreations/RaeYNCheat) to cover server-side only in-game anti-cheat after boot protection is finished. Takes over after player logs into server where RaeYNCheat left off.

## Features

### Core Features
- ✅ **Server-Side ONLY** - No client installation required
- ✅ **Comprehensive Cheat Detection** - Covers 25 types of common cheats
- ✅ **Extremely High Performance** - Low tick impact, async processing where possible
- ✅ **Not Super Aggressive, But Powerful** - Balanced detection to minimize false positives
- ✅ **False Positive Tracker** - Automatic detection and mitigation of false positives
- ✅ **Percentage-Based Certainty** - 0-100% certainty calculation with multiple factors
- ✅ **Progressive Ban System** - RaeYNCheat-style escalating punishments for 95%+ certainty
- ✅ **Comprehensive Logging** - File and console logging with rotation
- ✅ **RaeYNCheat Integration** - Optional soft dependency, coordinates with boot protection

### Cheat Detection (25 Types)

#### Movement Cheats (5)
- **Fly** - Unauthorized flying
- **Speed** - Moving faster than normal
- **NoFall** - Avoiding fall damage
- **Phase** - Moving through solid blocks
- **Jesus** - Walking on water/lava

#### Combat Cheats (6)
- **KillAura** - Attacking multiple entities simultaneously
- **Reach** - Hitting from too far away
- **Criticals** - Forcing critical hits
- **AutoClicker** - Automated clicking
- **Velocity** - Reducing knockback
- **AntiKnockback** - Negating knockback completely

#### Block Interaction Cheats (5)
- **X-Ray** - Seeing through blocks to find ores (pattern analysis)
- **Nuker** - Breaking blocks too quickly in radius
- **Scaffold** - Impossible block placements
- **FastBreak** - Breaking blocks too fast
- **FastPlace** - Placing blocks too fast

#### Player State Cheats (4)
- **Infinite Health** - God mode / taking no damage
- **Regeneration** - Abnormally fast health regeneration
- **OP Guard** - Preventing de-op / permission manipulation
- **Freecam** - Camera separated from player body

#### Inventory Cheats (3)
- **ChestStealer** - Stealing items from chests too fast
- **InventoryMove** - Moving while in inventory screen
- **AutoArmor** - Automatically equipping armor

#### Other Cheats (2)
- **FastBow** - Shooting arrows without full draw
- **Blink** - Packet manipulation / teleportation

**Note**: Timer check was excluded to avoid naming conflicts.

### Progressive Punishment System

Based on RaeYNCheat's progressive system with **customizable escalating punishments**:

**Default Steps** (fully customizable):
1. Warning only
2. 1 minute ban
3. 5 minutes ban
4. 10 minutes ban
5. 30 minutes ban
6. 1 hour ban
7. 2 hours ban
8. 4 hours ban
9. 8 hours ban
10. 24 hours ban
11. **Permanent ban**

Each violation increases the punishment level. Steps can be modified via config or commands.

## Commands

All commands are under the `/raeyn cheat` branch:

```
/raeyn cheat reload                     - Reload configuration
/raeyn cheat stats [player]             - View statistics (global or player-specific)
/raeyn cheat check <player>             - Check player's cheat certainty percentage
/raeyn cheat clear <player>             - Clear all violations for a player
/raeyn cheat punish <player>            - Manually punish a player (progressive system)
/raeyn cheat steps                      - View current punishment steps
/raeyn cheat steps add <duration>       - Add a new punishment step
/raeyn cheat steps remove <index>       - Remove a punishment step
/raeyn cheat steps set <index> <duration> - Modify an existing step
/raeyn cheat info                       - Display mod information
/raeyn cheat help                       - Show command help
```

**Duration Format**: 
- `0` = Warning only
- Positive integer = Ban duration in seconds
- `-1` = Permanent ban

**Examples**:
```
/raeyn cheat check PlayerName          # Check certainty
/raeyn cheat stats PlayerName          # View detailed stats
/raeyn cheat steps add 7200            # Add 2-hour ban step
/raeyn cheat steps set 5 3600          # Change 5th step to 1 hour
/raeyn cheat clear PlayerName          # Clear violations
```

## Configuration

Configuration file: `config/raeynantiacheat-server.toml`

### Key Configuration Options

```toml
[general]
enabled = true
debugMode = false

[performance]
checkInterval = 1              # Ticks between checks (1-20)
asyncProcessing = true         # Use async for non-critical checks
maxChecksPerTick = 3          # Max checks per player per tick
adaptiveThresholds = true     # Adjust based on server TPS
minTpsThreshold = 18.0        # Reduce strictness below this TPS

[violationTracking]
decayTime = 300               # Violation history in seconds
falsePositiveDetection = true # Enable FP detection

[thresholds]
alertThreshold = 50.0         # Alert admins at this certainty
punishmentThreshold = 95.0    # Begin punishment at this certainty

[punishments]
enabled = true
certaintyThreshold = 95.0     # Certainty required to trigger
steps = [0, 60, 300, 600, 1800, 3600, 7200, 14400, 28800, 86400, -1]

[logging]
console = true
file = true
retentionDays = 30
alertsEnabled = true
alertsMinCertainty = 60.0
```

## How It Works

### Certainty Calculation

The certainty system uses a weighted algorithm considering:

1. **Severity of Violations** (40%) - Weighted by recency and cheat type severity
2. **Number of Violations** (30%) - Diminishing returns to prevent spam-based false positives
3. **Variety of Cheat Types** (20%) - More unique cheat types = higher certainty
4. **Recent Activity** (10%) - Violations in last 30 seconds boost certainty

**False Positive Mitigation**:
- Low-severity single violations are flagged
- Server TPS is considered for timing-based checks
- False positive history reduces certainty
- Adaptive thresholds based on server performance

### Integration with RaeYNCheat

When RaeYNCheat is present (soft dependency):
- Automatically detects and integrates
- Coordinates punishment systems
- Takes over after boot protection completes
- Can factor RaeYNCheat violations into certainty

When standalone:
- Fully functional without RaeYNCheat
- All features work independently
- No boot protection (handled by RaeYNCheat)

## Installation

### Server Installation

1. Download `RaeYNAntiCheat-1.0.0.jar`
2. Place in server `mods` folder
3. (Optional) Install RaeYNCheat for boot protection integration
4. Start server - configuration file will be generated
5. Customize `config/raeynantiacheat-server.toml` as needed

### Client Installation

**NOT REQUIRED** - This mod is server-side only. Clients do not need to install anything.

## Building from Source

```bash
git clone https://github.com/RaeYNCreations/RaeYNAntiCheat.git
cd RaeYNAntiCheat
./gradlew build
```

The built JAR will be in `build/libs/`

## Performance

- **Low Tick Impact** - Configurable check intervals and async processing
- **Optimized Data Structures** - Concurrent collections for thread-safety
- **Automatic Cleanup** - Expired violations are automatically removed
- **Adaptive Thresholds** - Reduces strictness when server TPS drops
- **Efficient Logging** - Async file I/O with automatic rotation

## Permissions

- `raeynantiacheat.bypass` - Bypass all checks (not recommended)
- `raeynantiacheat.admin` - Access to all commands (Op level 2+)
- `raeynantiacheat.moderate` - Check and stats commands (Op level 2+)
- `raeynantiacheat.manage` - Punishment and clear commands (Op level 3+)
- `raeynantiacheat.configure` - Reload and step management (Op level 4+)

## Logging

Logs are stored in `logs/raeynantiacheat/violations-YYYY-MM-DD.log`

**Log Format**:
```
[HH:mm:ss] [PlayerName] [CheatType] Severity: X.XX, Certainty: XX.XX% - Details
[HH:mm:ss] [PUNISHMENT] PlayerName - ACTION for CheatType (Certainty: XX.XX%)
[HH:mm:ss] [EVENT] Event description
```

**Automatic Rotation**:
- Daily log files
- Configurable retention (default 30 days)
- Old logs automatically deleted

## License

MIT License - See LICENSE file for details

## Credits

- **RaeYNCreations** - Original author
- **RaeYNCheat** - Companion mod for boot protection
- **NeoForge** - Mod loader platform

## Support

For issues, feature requests, or questions:
- Open an issue on GitHub
- Check existing issues first
- Provide server logs and configuration when reporting bugs
