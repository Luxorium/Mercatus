# Installation

## Requirements

- Java 25 or newer.
- Paper or Folia 26.1.2.
- `Mercatus-0.1.2.jar`.
- Optional: Aureus 0.1.1 for paid shop actions.

## Steps

1. Download `Mercatus-0.1.2.jar` from the latest release.
2. Stop the server.
3. Place the jar in the server's `plugins/` directory.
4. Optionally place `Aureus-0.1.1.jar` in `plugins/`.
5. Start the server once to generate `plugins/Mercatus/config.yml`.
6. Review the configuration.
7. Run `/shop reload` after future configuration changes.

Mercatus loads without Aureus. By default, shop actions use standalone no-op
payments when Aureus is absent. Set `aureus.require-for-transactions: true` to
require a live economy provider for paid actions.
