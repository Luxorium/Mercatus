package zone.luxor.mercatus.config;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.configuration.file.FileConfiguration;
import zone.luxor.mercatus.util.ItemKey;

public record MercatusConfig(
        long shopCreationFeeMinor,
        double transactionTaxPercent,
        int maxShopsPerPlayer,
        boolean allowBuyShops,
        boolean allowSellShops,
        Set<String> blockedItems,
        Set<String> allowedWorlds,
        String storageFile,
        boolean aureusEnabled,
        boolean aureusRequiredForTransactions,
        String messagePrefix
) {
    public static MercatusConfig from(FileConfiguration configuration) {
        return new MercatusConfig(
                configuration.getLong("shop-creation-fee", 0L),
                configuration.getDouble("transaction-tax-percent", 5.0D),
                configuration.getInt("max-shops-per-player", 25),
                configuration.getBoolean("allow-buy-shops", true),
                configuration.getBoolean("allow-sell-shops", true),
                normalizedSet(configuration, "blocked-items"),
                configuration.getStringList("allowed-worlds").stream().collect(Collectors.toUnmodifiableSet()),
                configuration.getString("storage.file", "mercatus.db"),
                configuration.getBoolean("aureus.enabled", true),
                configuration.getBoolean("aureus.require-for-transactions", true),
                configuration.getString("messages.prefix", "Mercatus: ")
        );
    }

    private static Set<String> normalizedSet(FileConfiguration configuration, String path) {
        return configuration.getStringList(path).stream()
                .map(ItemKey::normalize)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }
}
