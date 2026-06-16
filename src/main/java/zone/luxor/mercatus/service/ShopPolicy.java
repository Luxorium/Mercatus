package zone.luxor.mercatus.service;

import java.util.Set;
import zone.luxor.mercatus.config.MercatusConfig;
import zone.luxor.mercatus.model.ShopMode;

public final class ShopPolicy {
    public ShopResult<Void> validateCreation(
            MercatusConfig config,
            String world,
            ShopMode mode,
            String itemKey,
            long priceMinor,
            int existingShopCount
    ) {
        if (priceMinor <= 0L) {
            return ShopResult.failure(ShopFailure.INVALID_PRICE, "Price must be greater than zero.");
        }
        if (!config.allowedWorlds().isEmpty() && !config.allowedWorlds().contains(world)) {
            return ShopResult.failure(ShopFailure.WORLD_NOT_ALLOWED, "Shops are not allowed in this world.");
        }
        if (mode == ShopMode.BUY && !config.allowBuyShops()) {
            return ShopResult.failure(ShopFailure.MODE_DISABLED, "Buy shops are disabled.");
        }
        if (mode == ShopMode.SELL && !config.allowSellShops()) {
            return ShopResult.failure(ShopFailure.MODE_DISABLED, "Sell shops are disabled.");
        }
        if (isBlocked(config.blockedItems(), itemKey)) {
            return ShopResult.failure(ShopFailure.BLOCKED_ITEM, "That item cannot be used in shops.");
        }
        if (existingShopCount >= config.maxShopsPerPlayer()) {
            return ShopResult.failure(ShopFailure.MAX_SHOPS_REACHED, "You have reached your shop limit.");
        }
        return ShopResult.success(null, "OK");
    }

    public boolean isBlocked(Set<String> blockedItems, String itemKey) {
        return blockedItems.contains(itemKey);
    }
}
