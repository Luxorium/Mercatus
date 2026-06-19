# Configuration

Mercatus writes its default configuration to `plugins/Mercatus/config.yml` on
first startup.

## Key Areas

| Area | Description |
| ---- | ----------- |
| Creation fee | Minor-unit amount charged when a shop is created. |
| Transaction tax | Percent removed from each completed transaction. |
| Shop limits | Maximum shops per player. |
| Shop modes | Toggles for buy shops and sell shops. |
| Blocked items | Item keys that cannot be traded. |
| Allowed worlds | Empty allows all worlds; otherwise only listed worlds are allowed. |
| Storage | SQLite database file name under the plugin data folder. |
| Aureus | Optional payment provider integration settings. |

Prices are minor-unit whole numbers so Mercatus can interoperate with economy
providers without floating point currency loss.

When `aureus.require-for-transactions` is `false`, Mercatus remains fully
usable without Aureus by treating payment operations as standalone no-ops. When
it is `true`, paid shop actions fail cleanly until Aureus is installed and
enabled.

## Options

| Key | Default | Description |
| --- | ------- | ----------- |
| `shop-creation-fee` | `0` | Minor-unit amount charged when a shop is created. |
| `transaction-tax-percent` | `5.0` | Percent removed from completed transactions. |
| `max-shops-per-player` | `25` | Maximum shops one player may own. |
| `allow-buy-shops` | `true` | Enables player buy shops. |
| `allow-sell-shops` | `true` | Enables player sell shops. |
| `blocked-items` | `minecraft:bedrock`, `minecraft:command_block` | Item keys blocked from shops. |
| `allowed-worlds` | `[]` | Empty allows all worlds; otherwise only listed worlds may contain shops. |
| `storage.file` | `mercatus.db` | SQLite file under `plugins/Mercatus/`. |
| `aureus.enabled` | `true` | Enables optional Aureus provider detection. |
| `aureus.require-for-transactions` | `false` | Requires a live payment provider for paid actions when true. |
| `messages.prefix` | `<gold>Mercatus</gold><gray>:</gray> ` | MiniMessage command prefix. |
| `messages.economy-unavailable` | `Payments are unavailable because Aureus is not installed.` | Message used when payment enforcement blocks an action. |
| `messages.shop-created` | `Shop created.` | Shop creation success message. |
| `messages.shop-removed` | `Shop removed.` | Shop removal success message. |
| `messages.shop-not-found` | `No Mercatus shop was found there.` | Missing targeted shop message. |
| `messages.invalid-price` | `Price must be a positive whole minor-unit amount.` | Invalid price message. |
| `messages.blocked-item` | `That item cannot be sold in Mercatus shops.` | Blocked item message. |
| `messages.max-shops` | `You have reached your shop limit.` | Shop limit message. |
| `messages.no-permission` | `You do not have permission.` | Permission failure message. |

## Storage Dependency

Mercatus bundles SQLite JDBC into the plugin jar intentionally. This keeps the
plugin self-contained and avoids requiring server owners to install a database
driver separately.
