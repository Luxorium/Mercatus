# Mercatus

[![Release](https://img.shields.io/github/v/release/Luxorium/Mercatus?sort=semver&display_name=tag&style=flat-square&label=release&color=blue&cacheSeconds=3600)](https://github.com/Luxorium/Mercatus/releases/latest)
[![Build](https://github.com/Luxorium/Mercatus/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Luxorium/Mercatus/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/Luxorium/Mercatus?style=flat-square&label=license&color=green)](https://github.com/Luxorium/Mercatus/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/java-25%2B-orange?style=flat-square)](https://adoptium.net/)

Mercatus is a Folia-native player shops and trading plugin for Paper/Folia
26.1.2 servers. It can run standalone, and it integrates with Aureus when
Aureus is installed.

## Highlights

- Player-created chest buy and sell shops
- Shop ownership by UUID and last known name
- SQLite storage with WAL mode
- Shop transaction logging
- Configurable creation fee, transaction tax, max shops, worlds, and blocked items
- Optional Aureus integration through a soft dependency and reflective API link

## Compatibility

| Requirement | Version |
|-------------|---------|
| Java | 25 or newer |
| Server | Paper or Folia 26.1.2 |
| Mercatus | 0.1.0 |
| Aureus | Optional |

## Installation

1. Download `Mercatus-0.1.0.jar` from the latest release.
2. Place the jar in your server's `plugins/` directory.
3. Optionally install Aureus for paid shop actions.
4. Restart the server.
5. Edit `plugins/Mercatus/config.yml` as needed.
6. Run `/shop reload` after configuration changes.

When Aureus is not installed, Mercatus loads safely with a no-op payment
provider. Paid shop actions fail instead of creating money.

## Build From Source

```bash
./gradlew clean build
```

The compiled plugin is written to:

```text
build/libs/Mercatus-0.1.0.jar
```

## Commands And Permissions

| Command | Permission |
|---------|------------|
| `/mercatus` | `mercatus.command.shop` |
| `/shop create sell <price>` | `mercatus.shop.create` |
| `/shop create buy <price>` | `mercatus.shop.create` |
| `/shop remove` | `mercatus.shop.remove.own` |
| `/shop info` | `mercatus.shop.info` |
| `/shop list` | `mercatus.shop.list` |
| `/shop limits` | `mercatus.command.shop` |
| `/shop reload` | `mercatus.admin.reload` |
| `/shop admin info` | `mercatus.admin.info` |
| `/shop admin remove` | `mercatus.admin.remove` |
| `/shop admin list <player>` | `mercatus.admin.list` |

## Configuration

`config.yml` controls creation fees, transaction taxes, shop caps, enabled
worlds, blocked items, storage file name, messages, and Aureus integration.

Prices are minor-unit whole numbers so Mercatus can interoperate with economy
providers without floating point currency loss.

## Data Storage

Runtime data is stored in SQLite under the server's plugin data directory:

| Data | Path |
|------|------|
| Database | `plugins/Mercatus/mercatus.db` |

## Folia Notes

Mercatus avoids legacy Bukkit scheduler APIs. Database work runs asynchronously,
and chest or inventory access stays in command, entity, or region scheduler
contexts.

## License

Mercatus is licensed under the [MIT License](LICENSE).
