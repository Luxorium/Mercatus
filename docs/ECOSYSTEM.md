# Luxorium Ecosystem

Mercatus is one plugin in the Luxorium plugin ecosystem:

| Plugin | Role | Dependency Model |
| ------ | ---- | ---------------- |
| Civitas | Essentials, administration, and quality-of-life commands | Standalone |
| Aureus | Economy core | Standalone |
| Mercatus | Player shops and trading | Standalone with optional Aureus integration |
| Munera | Contracts, jobs, quests, and buy orders | Standalone with optional Aureus integration |

Mercatus should remain usable without Aureus. When Aureus is available,
Mercatus may use it for shop fees, taxes, and player-to-player shop payments.

## Descriptor Standard

Luxorium plugins use `src/main/resources/plugin.yml` as the single plugin
descriptor. Paper/Folia 26.1.2 supports this descriptor with `folia-supported:
true`, and using one descriptor avoids duplicated metadata drift.
