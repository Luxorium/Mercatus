package dev.luxorium.mercatus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import dev.luxorium.mercatus.config.MercatusConfig;
import dev.luxorium.mercatus.model.ShopMode;

final class ShopPolicyTest {
    private final ShopPolicy policy = new ShopPolicy();

    @Test
    void acceptsValidSellShopCreation() {
        ShopResult<Void> result = policy.validateCreation(config(Set.of(), Set.of("world")), "world", ShopMode.SELL, "minecraft:diamond", 100L, 0);
        assertTrue(result.success());
    }

    @Test
    void rejectsMaxShopLimit() {
        ShopResult<Void> result = policy.validateCreation(config(Set.of(), Set.of()), "world", ShopMode.SELL, "minecraft:diamond", 100L, 25);
        assertEquals(ShopFailure.MAX_SHOPS_REACHED, result.failure());
    }

    @Test
    void rejectsBlockedItems() {
        ShopResult<Void> result = policy.validateCreation(config(Set.of("minecraft:bedrock"), Set.of()), "world", ShopMode.SELL, "minecraft:bedrock", 100L, 0);
        assertEquals(ShopFailure.BLOCKED_ITEM, result.failure());
    }

    @Test
    void rejectsDisabledBuyModeAndDisallowedWorld() {
        MercatusConfig config = new MercatusConfig(0L, 5.0D, 25, false, true, Set.of(), Set.of("market"), "mercatus.db", true, true, "");
        assertEquals(ShopFailure.WORLD_NOT_ALLOWED,
                policy.validateCreation(config, "world", ShopMode.BUY, "minecraft:diamond", 100L, 0).failure());
        assertEquals(ShopFailure.MODE_DISABLED,
                policy.validateCreation(config, "market", ShopMode.BUY, "minecraft:diamond", 100L, 0).failure());
    }

    private static MercatusConfig config(Set<String> blockedItems, Set<String> allowedWorlds) {
        return new MercatusConfig(0L, 5.0D, 25, true, true, blockedItems, allowedWorlds, "mercatus.db", true, true, "");
    }
}
