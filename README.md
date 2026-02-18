![Logo](https://i.ibb.co/201Mczsg/Simple-Broadcast.png)

Simple Minecraft plugin for Paper/Spigot that sends automatic messages to the server chat or action bar

## Features

- All configuration is read from `config.yml`
- **Chat or action bar:** each message can be shown in **chat** (with prefix) or **action bar** (no prefix)
- **Optional sound:** each message can play a Minecraft sound when sent (or no sound if omitted or set to `none`)
- Configurable prefix for chat messages (`prefix`)
- Configurable interval in seconds (`interval`)
- Sequential or random sending (`random-send`)
- Minecraft color codes supported using `&` (chat and action bar)
- Safe handling when the message list is empty
- **reload** command to reload configuration without restarting the server
- **toggle** command to pause or resume message broadcasting
- All plugin messages (reload, toggle, errors, usage) are configurable in `config.yml`, including the plugin name prefix

## Commands & Permissions

| Command | Aliases | Description |
|---------|---------|-------------|
| `/simplebroadcast reload` | `/sb reload` | Reloads the plugin configuration |
| `/simplebroadcast toggle` | `/sb toggle` | Pauses or resumes automatic message broadcasting |

- **reload**: requires permission **`sb.reload`** (default: op)
- **toggle**: requires permission **`sb.toggle`** (default: op)
- Works in-game and from the server console

## Requirements

- Java 21 (LTS).
- Paper or Spigot server on the latest supported version (tested with `api-version: "1.21"`)
- Maven 3.6+ to build the project

## Build

From the project root (`simple-broadcast`), run:

```bash
mvn clean package
```

The plugin JAR will be generated at:

`target/SimpleBroadcast-1.1.1.jar`

## Installation

1. Copy the built JAR to your Paper/Spigot server `plugins` folder
2. Start or restart the server
3. The `config.yml` file will be created automatically in `plugins/SimpleBroadcast/` if it does not exist

## Configuration

Example configuration (defaults):

```yaml
check-updates: true
prefix: "&e[!] &r"
interval: 60
random-send: false

messages:
  - text: "&fThis is the &e&lfirst &fexample message"
    display: chat
    sound: "entity.experience_orb.pickup"
  - text: "&e&lAction bar example message"
    display: action-bar
    sound: none
  - text: "&fThis is the &e&lthird &fexample message"

plugin-messages:
  prefix: "&7[&6Broadcasts&7]"
  toggle-paused: "&eMessage broadcasting paused"
  toggle-resumed: "&aMessage broadcasting resumed"
  toggle-no-permission: "&cYou do not have permission to run this command. (sb.toggle)"
  reload-success: "&aConfiguration reloaded successfully"
  reload-no-permission: "&cYou do not have permission to run this command. (sb.reload)"
  usage: "&7Usage: &f/<command> <reload|toggle>"
```

- **check-updates**: If `true`, checks Modrinth for updates on startup and notifies console and operators
- **prefix**: Prefix added at the start of each **chat** message (ignored for action bar)
- **interval**: Interval in seconds between each message
- **random-send**: `false` = sequential; `true` = random, avoiding repeating the last message
- **messages**: List of messages. Each has **`text`** (required, supports `&` colors). Optional: **`display`** (`chat` or `action-bar`, default `chat`); **`sound`** (Minecraft sound key, or `none` / omit for no sound). Action bar messages do not show the prefix.
- **plugin-messages**: Messages shown for commands. `<command>` in `usage` is replaced with the command used (`/simplebroadcast` or `/sb`)

## Internal behaviour

- The plugin schedules a repeating task using Bukkit's scheduler
- On enable (`onEnable`), it saves the default config, loads it, and starts the task
- On disable (`onDisable`), it cancels the task
- Each run of the task:
  - Reads the message list from the configuration
  - If it is empty, it does nothing and returns silently
  - Applies `&` color codes via `ChatColor.translateAlternateColorCodes`
  - If display is **chat**: broadcasts to the whole server with the configured prefix
  - If display is **action-bar**: sends to each online player's action bar (no prefix)
  - If the message has a **sound** set (and not `none`), plays that sound for all online players
