# Folia Safety

Mercatus is Folia-native.

## Rules

- Do not add `BukkitScheduler`, `BukkitRunnable`, or `runTask*` usage.
- Do not touch chests, inventories, blocks, worlds, entities, or players from async storage callbacks.
- Keep database work asynchronous.
- Keep optional economy integration behind `PaymentProvider`.
- Use region-safe contexts for block and inventory interactions.

## Review Checklist

- Storage changes do not block region threads.
- Shop interaction changes do not assume a single global main thread.
- Optional Aureus integration remains optional and does not create a hard dependency.
