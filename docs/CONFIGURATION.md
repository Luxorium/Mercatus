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

## Storage Dependency

Mercatus bundles SQLite JDBC into the plugin jar intentionally. This keeps the
plugin self-contained and avoids requiring server owners to install a database
driver separately.
