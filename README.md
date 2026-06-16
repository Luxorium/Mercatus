# Mercatus

Mercatus is the Folia-native player shops, market stalls, trading, and shop transaction plugin for the Luxor Zone SMP server network. It is part of the Luxorium plugin suite and can run standalone, while integrating cleanly with Aureus when Aureus is installed.

Mercatus 0.1.0 focuses on chest shops. Auction house support is intentionally not included in this foundation.

## Economy Model

Mercatus circulates money; it does not create money.

Shop payments move existing money from buyer to seller. Creation fees and transaction taxes remove money from the economy. When Aureus is installed and enabled, Mercatus uses Aureus for transfers and withdrawals. When Aureus is missing, Mercatus loads safely with a no-op payment provider, but paid shop actions fail instead of minting currency.

## Features

- Player-created chest buy and sell shops
- Shop ownership by UUID and last known name
- SQLite storage with WAL mode
- Shop transaction logging
- Configurable creation fee, tax, max shops, worlds, and blocked items
- Admin shop inspection, removal, and listing
- Folia-native async storage and region-safe block interaction patterns

## Commands

- `/mercatus` - show plugin and payment provider status
- `/shop create sell <price>` - create a sell shop while looking at a chest and holding the item
- `/shop create buy <price>` - create a buy shop while looking at a chest and holding the item
- `/shop remove` - remove your targeted shop
- `/shop info` - inspect the targeted shop
- `/shop list` - list your shops
- `/shop limits` - show your shop count and configured limit
- `/shop reload` - reload configuration
- `/shop admin info` - inspect any targeted shop
- `/shop admin remove` - remove any targeted shop
- `/shop admin list <player>` - list shops by owner name

Prices are minor-unit whole numbers so Mercatus can interoperate with economy providers without floating point currency loss.

## Permissions

- `mercatus.command.shop`
- `mercatus.shop.create`
- `mercatus.shop.remove.own`
- `mercatus.shop.info`
- `mercatus.shop.list`
- `mercatus.admin.reload`
- `mercatus.admin.info`
- `mercatus.admin.remove`
- `mercatus.admin.list`

## Example Shop Setup

1. Place a chest in an allowed world.
2. Put stock in the chest for a sell shop, or leave space for a buy shop.
3. Hold the item the shop trades.
4. Look at the chest.
5. Run `/shop create sell 250` or `/shop create buy 250`.
6. Other players right-click the chest to trade one item at the configured price.

## Configuration

`config.yml` includes:

- `shop-creation-fee` - minor units removed from the creator when a shop is created
- `transaction-tax-percent` - percent removed from each transaction
- `max-shops-per-player` - per-owner shop cap
- `allow-buy-shops` and `allow-sell-shops` - mode toggles
- `blocked-items` - item keys that cannot be traded
- `allowed-worlds` - empty means all worlds; otherwise only listed worlds are allowed
- `storage.file` - SQLite database file under the plugin data folder
- `aureus.enabled` - whether Mercatus should try to link Aureus
- `aureus.require-for-transactions` - reserved compatibility flag for stricter deployments
- `messages` - basic message text

## Folia Notes

Mercatus does not use `BukkitScheduler`. Storage work runs through Folia async scheduling. Chest and inventory access happens from command, entity, or region scheduler contexts. Async database continuations do not touch Bukkit player, world, inventory, or entity state.

## Build

Requirements:

- Java 25
- Network access for first Gradle dependency resolution

Build and test:

```bash
./gradlew build
```

The build produces `build/libs/Mercatus-0.1.0.jar`.
