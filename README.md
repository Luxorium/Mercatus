# Mercatus

[![Release](https://img.shields.io/github/v/release/Luxorium/Mercatus?sort=semver&display_name=tag&style=flat-square&label=release&color=blue&cacheSeconds=3600)](https://github.com/Luxorium/Mercatus/releases/latest)
[![Build](https://github.com/Luxorium/Mercatus/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Luxorium/Mercatus/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/Luxorium/Mercatus?style=flat-square&label=license&color=green)](https://github.com/Luxorium/Mercatus/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/java-25%2B-orange?style=flat-square)](https://adoptium.net/)
[![Paper/Folia](https://img.shields.io/badge/Paper%2FFolia-26.1.2-blueviolet?style=flat-square)](https://papermc.io/)

Mercatus is a Folia-native player shops and trading plugin for Paper/Folia
26.1.2 Minecraft servers.

## Ecosystem Role

Mercatus provides player shops and trading for the Luxorium plugin ecosystem. It
can run by itself with paid actions disabled, and it integrates with Aureus when
Aureus is installed for shop fees, taxes, and player-to-player shop payments.
Munera provides contracts and buy orders, while Civitas provides essentials,
administration, and quality-of-life commands.

## Highlights

- Player-created chest buy and sell shops.
- Shop ownership by UUID and last known player name.
- SQLite storage with WAL mode and bundled SQLite JDBC.
- Configurable creation fees, transaction taxes, shop caps, worlds, and blocked items.
- Shop transaction logging for economy review.
- Optional Aureus integration with a standalone no-op fallback.

## Compatibility

| Requirement | Version |
| ----------- | ------- |
| Java | 25 or newer |
| Server | Paper/Folia 26.1.2 |
| Mercatus | 0.1.2 |
| Optional integrations | Aureus 0.1.1 |

## Installation

1. Download `Mercatus-0.1.2.jar` from the latest release.
2. Place the jar in your server's `plugins/` directory.
3. Optionally install Aureus for paid shop actions.
4. Restart the server.
5. Edit `plugins/Mercatus/config.yml` as needed.
6. Run `/shop reload` after configuration changes.

## Build From Source

```bash
./gradlew clean build
```

The compiled plugin jar is written to:

```text
build/libs/Mercatus-0.1.2.jar
```

## Commands and Permissions

| Command | Permission | Description |
| ------- | ---------- | ----------- |
| `/mercatus` | `mercatus.command.shop` | Show plugin and payment provider status. |
| `/shop create sell <price>` | `mercatus.shop.create` | Create a sell shop. |
| `/shop create buy <price>` | `mercatus.shop.create` | Create a buy shop. |
| `/shop remove` | `mercatus.shop.remove.own` | Remove your targeted shop. |
| `/shop info` | `mercatus.shop.info` | Inspect the targeted shop. |
| `/shop list` | `mercatus.shop.list` | List your shops. |
| `/shop limits` | `mercatus.command.shop` | Show shop count and limits. |
| `/shop reload` | `mercatus.admin.reload` | Reload configuration. |
| `/shop admin info` | `mercatus.admin.info` | Inspect any targeted shop. |
| `/shop admin remove` | `mercatus.admin.remove` | Remove any targeted shop. |
| `/shop admin list <player>` | `mercatus.admin.list` | List shops by owner. |

## Configuration

`config.yml` controls creation fees, transaction taxes, shop caps, enabled
worlds, blocked items, storage file name, messages, and Aureus integration. See
[Configuration](docs/CONFIGURATION.md) for the full reference.

## Data Storage

| Data | Path |
| ---- | ---- |
| Configuration | `plugins/Mercatus/config.yml` |
| SQLite database | `plugins/Mercatus/mercatus.db` |

## Folia Safety

Mercatus is Folia-native. It must avoid legacy `BukkitScheduler`,
`BukkitRunnable`, and `runTask*` APIs, and it must avoid unsafe cross-thread
world, entity, player, or inventory access. Database work runs asynchronously,
while chest and inventory work stays in safe command or region contexts.

## Documentation

- [Installation](docs/INSTALLATION.md)
- [Commands](docs/COMMANDS.md)
- [Configuration](docs/CONFIGURATION.md)
- [Folia Safety](docs/FOLIA_SAFETY.md)
- [Ecosystem](docs/ECOSYSTEM.md)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

Mercatus is licensed under the [MIT License](LICENSE).
