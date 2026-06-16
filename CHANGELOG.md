# Changelog

All notable changes to Mercatus will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed

- Standardized repository documentation, GitHub metadata, and CI configuration.
- Standardized on `plugin.yml` as the single Paper/Folia plugin descriptor.

## [0.1.0] - 2026-06-16

### Added

- Initial Java 25 Gradle project for Mercatus.
- Folia/Paper plugin descriptor.
- SQLite storage with WAL mode for shops and transactions.
- Chest shop creation, ownership, listing, info, removal, and admin commands.
- Sell-shop and buy-shop interaction foundations.
- Optional Aureus payment bridge and no-op fallback provider.
- Configurable shop fees, transaction tax, shop limit, blocked items, and allowed worlds.
- Unit tests for shop validation, item keys, payment fallback, tax calculation, max shops, and blocked items.
