package dev.luxorium.mercatus.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dev.luxorium.mercatus.model.BlockLocation;
import dev.luxorium.mercatus.model.Shop;
import dev.luxorium.mercatus.model.ShopMode;
import dev.luxorium.mercatus.model.ShopTransaction;

public final class MercatusStorage implements AutoCloseable {
    private final Path databasePath;
    private Connection connection;

    public MercatusStorage(Path databasePath) {
        this.databasePath = databasePath;
    }

    public synchronized void initialize() throws SQLException, IOException {
        Files.createDirectories(databasePath.getParent());
        connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS shops (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        owner_uuid TEXT NOT NULL,
                        owner_name TEXT NOT NULL,
                        world TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        mode TEXT NOT NULL,
                        item_key TEXT NOT NULL,
                        price_minor INTEGER NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        UNIQUE(world, x, y, z)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS shop_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        shop_id INTEGER NOT NULL,
                        buyer_uuid TEXT NOT NULL,
                        seller_uuid TEXT NOT NULL,
                        item_key TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        price_minor INTEGER NOT NULL,
                        tax_minor INTEGER NOT NULL,
                        created_at TEXT NOT NULL,
                        FOREIGN KEY(shop_id) REFERENCES shops(id) ON DELETE CASCADE
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_shops_owner_uuid ON shops(owner_uuid)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_shop_transactions_shop_id ON shop_transactions(shop_id)");
        }
    }

    public synchronized Shop createShop(UUID ownerUuid, String ownerName, BlockLocation location, ShopMode mode, String itemKey, long priceMinor)
            throws SQLException {
        Instant now = Instant.now();
        try (PreparedStatement statement = requireConnection().prepareStatement("""
                INSERT INTO shops(owner_uuid, owner_name, world, x, y, z, mode, item_key, price_minor, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, ownerUuid.toString());
            statement.setString(2, ownerName);
            statement.setString(3, location.world());
            statement.setInt(4, location.x());
            statement.setInt(5, location.y());
            statement.setInt(6, location.z());
            statement.setString(7, mode.name());
            statement.setString(8, itemKey);
            statement.setLong(9, priceMinor);
            statement.setString(10, now.toString());
            statement.setString(11, now.toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Shop(keys.getLong(1), ownerUuid, ownerName, location, mode, itemKey, priceMinor, now, now);
                }
            }
        }
        throw new SQLException("Shop insert did not return an id.");
    }

    public synchronized Optional<Shop> findShop(BlockLocation location) throws SQLException {
        try (PreparedStatement statement = requireConnection().prepareStatement("""
                SELECT * FROM shops WHERE world = ? AND x = ? AND y = ? AND z = ?
                """)) {
            statement.setString(1, location.world());
            statement.setInt(2, location.x());
            statement.setInt(3, location.y());
            statement.setInt(4, location.z());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(readShop(resultSet)) : Optional.empty();
            }
        }
    }

    public synchronized int countShops(UUID ownerUuid) throws SQLException {
        try (PreparedStatement statement = requireConnection().prepareStatement("SELECT COUNT(*) FROM shops WHERE owner_uuid = ?")) {
            statement.setString(1, ownerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public synchronized List<Shop> listShops(UUID ownerUuid) throws SQLException {
        try (PreparedStatement statement = requireConnection().prepareStatement("""
                SELECT * FROM shops WHERE owner_uuid = ? ORDER BY created_at DESC
                """)) {
            statement.setString(1, ownerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Shop> shops = new ArrayList<>();
                while (resultSet.next()) {
                    shops.add(readShop(resultSet));
                }
                return shops;
            }
        }
    }

    public synchronized List<Shop> listShopsByOwnerName(String ownerName) throws SQLException {
        try (PreparedStatement statement = requireConnection().prepareStatement("""
                SELECT * FROM shops WHERE lower(owner_name) = lower(?) ORDER BY created_at DESC
                """)) {
            statement.setString(1, ownerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Shop> shops = new ArrayList<>();
                while (resultSet.next()) {
                    shops.add(readShop(resultSet));
                }
                return shops;
            }
        }
    }

    public synchronized List<Shop> listAllShops(int limit) throws SQLException {
        try (PreparedStatement statement = requireConnection().prepareStatement("SELECT * FROM shops ORDER BY created_at DESC LIMIT ?")) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Shop> shops = new ArrayList<>();
                while (resultSet.next()) {
                    shops.add(readShop(resultSet));
                }
                return shops;
            }
        }
    }

    public synchronized boolean deleteShop(BlockLocation location) throws SQLException {
        try (PreparedStatement statement = requireConnection().prepareStatement("""
                DELETE FROM shops WHERE world = ? AND x = ? AND y = ? AND z = ?
                """)) {
            statement.setString(1, location.world());
            statement.setInt(2, location.x());
            statement.setInt(3, location.y());
            statement.setInt(4, location.z());
            return statement.executeUpdate() > 0;
        }
    }

    public synchronized ShopTransaction logTransaction(long shopId, UUID buyerUuid, UUID sellerUuid, String itemKey, int quantity, long priceMinor, long taxMinor)
            throws SQLException {
        Instant now = Instant.now();
        try (PreparedStatement statement = requireConnection().prepareStatement("""
                INSERT INTO shop_transactions(shop_id, buyer_uuid, seller_uuid, item_key, quantity, price_minor, tax_minor, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, shopId);
            statement.setString(2, buyerUuid.toString());
            statement.setString(3, sellerUuid.toString());
            statement.setString(4, itemKey);
            statement.setInt(5, quantity);
            statement.setLong(6, priceMinor);
            statement.setLong(7, taxMinor);
            statement.setString(8, now.toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new ShopTransaction(keys.getLong(1), shopId, buyerUuid, sellerUuid, itemKey, quantity, priceMinor, taxMinor, now);
                }
            }
        }
        throw new SQLException("Transaction insert did not return an id.");
    }

    @Override
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Shutdown path; nothing useful can be done here.
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Mercatus storage is not initialized.");
        }
        return connection;
    }

    private static Shop readShop(ResultSet resultSet) throws SQLException {
        return new Shop(
                resultSet.getLong("id"),
                UUID.fromString(resultSet.getString("owner_uuid")),
                resultSet.getString("owner_name"),
                new BlockLocation(
                        resultSet.getString("world"),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getInt("z")
                ),
                ShopMode.valueOf(resultSet.getString("mode")),
                resultSet.getString("item_key"),
                resultSet.getLong("price_minor"),
                Instant.parse(resultSet.getString("created_at")),
                Instant.parse(resultSet.getString("updated_at"))
        );
    }
}
