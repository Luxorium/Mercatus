# Installation

## Requirements

- Java 25 or newer.
- Paper or Folia 26.1.2.
- `Mercatus-0.1.0.jar`.
- Optional: Aureus 0.1.0 for paid shop actions.

## Steps

1. Download `Mercatus-0.1.0.jar` from the latest release.
2. Stop the server.
3. Place the jar in the server's `plugins/` directory.
4. Optionally place `Aureus-0.1.0.jar` in `plugins/`.
5. Start the server once to generate `plugins/Mercatus/config.yml`.
6. Review the configuration.
7. Run `/shop reload` after future configuration changes.

Mercatus loads without Aureus. When Aureus is absent, paid shop actions fail
cleanly instead of creating or moving money.
