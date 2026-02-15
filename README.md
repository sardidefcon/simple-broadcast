![Logo](https://i.ibb.co/201Mczsg/Simple-Broadcast.png)

Simple Minecraft plugin for Paper/Spigot that sends automatic messages to the server chat

## Features

- All configuration is read from `config.yml`
- Configurable prefix for broadcast messages (`prefix`)
- Configurable interval in seconds (`interval`)
- Sequential or random sending (`random-send`)
- Minecraft color codes supported using `&`
- Safe handling when the message list is empty
- **reload** command to reload configuration without restarting the server
- **toggle** command to pause or resume message broadcasting
- All plugin messages (reload, toggle, errors, usage) are configurable in `config.yml`, including the plugin name prefix

## Commands & Permissions

| Command | Aliases | Description |
|---------|---------|-------------|
| `/simplebroadcast reload` | `/sb reload` | Reloads the plugin configuration |
| `/simplebroadcast toggle` | `/sb toggle` | Pauses or resumes automatic message broadcasting |

- **reload**: requires permission **`sp.reload`** (default: op)
- **toggle**: requires permission **`sp.toggle`** (default: op)
- Works in-game and from the server console

## Requirements

- Java 21 (LTS).
- Paper or Spigot server on the latest supported version (tested with `api-version: "1.21"`)
- Gradle installed on your system (this project only includes `build.gradle`, not the wrapper)

## Build

From the project root (`simplebroadcast`), run:

```bash
gradle build
```

The plugin JAR will be generated at:

`build/libs/SimpleBroadcast-1.0.0.jar`

## Installation

1. Copy the built JAR to your Paper/Spigot server `plugins` folder
2. Start or restart the server
3. The `config.yml` file will be created automatically in `plugins/SimpleBroadcast/` if it does not exist

## Configuration

Example configuration (defaults):

```yaml
prefix: "&e[!]"
interval: 60
random-send: false
messages:
  - "&fThis is the &e&lfirst &r&a example message."
  - "&fThis is the &e&lsecond &r&b example message."

plugin-messages:
  prefix: "&7[&aSimpleBroadcast&7]"
  reload-success: "&aConfiguration reloaded successfully."
  reload-no-permission: "&cYou do not have permission to run this command. (sp.reload)"
  toggle-resumed: "&aMessage broadcasting resumed."
  toggle-paused: "&eMessage broadcasting paused."
  toggle-no-permission: "&cYou do not have permission to run this command. (sp.toggle)"
  usage: "&7Usage: &f/<command> <reload|toggle>"
```

- **prefix**: Prefix added at the start of each broadcast message
- **interval**: Interval in seconds between each message
- **random-send**:
  - `false`: Messages are sent in order; when the list ends, it starts again from the beginning
  - `true`: A random message is chosen, avoiding repeating the last one
- **messages**: List of messages to broadcast
- **plugin-messages**: Messages shown by the plugin for commands. Use `&` for color codes. `<command>` in `usage` is replaced with the command used (`/simplebroadcast` or `/sb`)

## Internal behaviour

- The plugin schedules a repeating task using Bukkit's scheduler
- On enable (`onEnable`), it saves the default config, loads it, and starts the task
- On disable (`onDisable`), it cancels the task
- Each run of the task:
  - Reads the message list from the configuration
  - If it is empty, it does nothing and returns silently
  - Applies `&` color codes via `ChatColor.translateAlternateColorCodes`
  - Broadcasts the message to the whole server with the configured prefix
