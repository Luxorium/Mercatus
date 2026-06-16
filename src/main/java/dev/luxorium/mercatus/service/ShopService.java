package dev.luxorium.mercatus.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import dev.luxorium.mercatus.config.MercatusConfig;
import dev.luxorium.mercatus.economy.PaymentProvider;
import dev.luxorium.mercatus.economy.PaymentResult;
import dev.luxorium.mercatus.model.BlockLocation;
import dev.luxorium.mercatus.model.Shop;
import dev.luxorium.mercatus.model.ShopMode;
import dev.luxorium.mercatus.model.ShopTransaction;
import dev.luxorium.mercatus.storage.MercatusStorage;
import dev.luxorium.mercatus.util.TaxCalculator;

public final class ShopService {
    private final MercatusStorage storage;
    private final Executor asyncExecutor;
    private final ShopPolicy policy;
    private final Logger logger;
    private volatile MercatusConfig config;
    private volatile PaymentProvider paymentProvider;

    public ShopService(
            MercatusStorage storage,
            Executor asyncExecutor,
            ShopPolicy policy,
            MercatusConfig config,
            PaymentProvider paymentProvider,
            Logger logger
    ) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.asyncExecutor = Objects.requireNonNull(asyncExecutor, "asyncExecutor");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.config = Objects.requireNonNull(config, "config");
        this.paymentProvider = Objects.requireNonNull(paymentProvider, "paymentProvider");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void updateConfig(MercatusConfig config, PaymentProvider paymentProvider) {
        this.config = Objects.requireNonNull(config, "config");
        this.paymentProvider = Objects.requireNonNull(paymentProvider, "paymentProvider");
    }

    public MercatusConfig config() {
        return config;
    }

    public PaymentProvider paymentProvider() {
        return paymentProvider;
    }

    public CompletableFuture<ShopResult<Shop>> createShop(
            UUID ownerUuid,
            String ownerName,
            BlockLocation location,
            ShopMode mode,
            String itemKey,
            long priceMinor
    ) {
        MercatusConfig snapshot = config;
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (storage.findShop(location).isPresent()) {
                    return ShopResult.<Shop>failure(ShopFailure.SHOP_EXISTS, "A shop already exists there.");
                }
                int existing = storage.countShops(ownerUuid);
                ShopResult<Void> validation = policy.validateCreation(snapshot, location.world(), mode, itemKey, priceMinor, existing);
                if (!validation.success()) {
                    return ShopResult.<Shop>failure(validation.failure(), validation.message());
                }
                return ShopResult.<Shop>success(null, "Validated.");
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to validate shop creation.", exception);
                return ShopResult.<Shop>failure(ShopFailure.STORAGE_ERROR, "Storage error.");
            }
        }, asyncExecutor).thenCompose(validation -> {
            if (!validation.success()) {
                return CompletableFuture.completedFuture(validation);
            }
            return paymentProvider.withdraw(ownerUuid, snapshot.shopCreationFeeMinor(), "Mercatus shop creation fee")
                    .thenCompose(payment -> {
                        if (!payment.success()) {
                            return CompletableFuture.completedFuture(ShopResult.<Shop>failure(ShopFailure.PAYMENT_FAILED, payment.message()));
                        }
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                Shop shop = storage.createShop(ownerUuid, ownerName, location, mode, itemKey, priceMinor);
                                return ShopResult.success(shop, "Shop created.");
                            } catch (SQLException exception) {
                                logger.log(Level.WARNING, "Failed to create shop.", exception);
                                return ShopResult.<Shop>failure(ShopFailure.STORAGE_ERROR, "Storage error.");
                            }
                        }, asyncExecutor);
                    });
        });
    }

    public CompletableFuture<ShopResult<Shop>> findShop(BlockLocation location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return storage.findShop(location)
                        .map(shop -> ShopResult.success(shop, "Shop found."))
                        .orElseGet(() -> ShopResult.failure(ShopFailure.SHOP_NOT_FOUND, "No shop was found there."));
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to find shop.", exception);
                return ShopResult.failure(ShopFailure.STORAGE_ERROR, "Storage error.");
            }
        }, asyncExecutor);
    }

    public CompletableFuture<ShopResult<Boolean>> removeShop(UUID actorUuid, BlockLocation location, boolean admin) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Shop shop = storage.findShop(location).orElse(null);
                if (shop == null) {
                    return ShopResult.<Boolean>failure(ShopFailure.SHOP_NOT_FOUND, "No shop was found there.");
                }
                if (!admin && !shop.ownerUuid().equals(actorUuid)) {
                    return ShopResult.<Boolean>failure(ShopFailure.NOT_OWNER, "You do not own this shop.");
                }
                return storage.deleteShop(location)
                        ? ShopResult.success(Boolean.TRUE, "Shop removed.")
                        : ShopResult.<Boolean>failure(ShopFailure.SHOP_NOT_FOUND, "No shop was found there.");
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to remove shop.", exception);
                return ShopResult.failure(ShopFailure.STORAGE_ERROR, "Storage error.");
            }
        }, asyncExecutor);
    }

    public CompletableFuture<List<Shop>> listShops(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return storage.listShops(ownerUuid);
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to list shops.", exception);
                return List.of();
            }
        }, asyncExecutor);
    }

    public CompletableFuture<List<Shop>> listShopsByOwnerName(String ownerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return storage.listShopsByOwnerName(ownerName);
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to list shops by owner name.", exception);
                return List.of();
            }
        }, asyncExecutor);
    }

    public CompletableFuture<List<Shop>> listAllShops(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return storage.listAllShops(limit);
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to list all shops.", exception);
                return List.of();
            }
        }, asyncExecutor);
    }

    public CompletableFuture<ShopResult<ShopTransaction>> completeSale(Shop shop, UUID buyerUuid, int quantity) {
        MercatusConfig snapshot = config;
        long total = Math.multiplyExact(shop.priceMinor(), quantity);
        long tax = TaxCalculator.taxMinor(total, snapshot.transactionTaxPercent());
        long sellerReceives = Math.max(0L, total - tax);
        return paymentProvider.transfer(buyerUuid, shop.ownerUuid(), sellerReceives, "Mercatus shop purchase")
                .thenCompose(payment -> afterSalePayment(shop, buyerUuid, shop.ownerUuid(), buyerUuid, quantity, total, tax, payment));
    }

    public CompletableFuture<ShopResult<ShopTransaction>> completeBuyOrder(Shop shop, UUID sellerUuid, int quantity) {
        MercatusConfig snapshot = config;
        long total = Math.multiplyExact(shop.priceMinor(), quantity);
        long tax = TaxCalculator.taxMinor(total, snapshot.transactionTaxPercent());
        long sellerReceives = Math.max(0L, total - tax);
        return paymentProvider.transfer(shop.ownerUuid(), sellerUuid, sellerReceives, "Mercatus shop buy order")
                .thenCompose(payment -> afterSalePayment(shop, shop.ownerUuid(), sellerUuid, shop.ownerUuid(), quantity, total, tax, payment));
    }

    private CompletableFuture<ShopResult<ShopTransaction>> afterSalePayment(
            Shop shop,
            UUID buyerUuid,
            UUID sellerUuid,
            UUID taxSourceUuid,
            int quantity,
            long total,
            long tax,
            PaymentResult payment
    ) {
        if (!payment.success()) {
            return CompletableFuture.completedFuture(ShopResult.failure(ShopFailure.PAYMENT_FAILED, payment.message()));
        }
        return paymentProvider.withdraw(taxSourceUuid, tax, "Mercatus transaction tax")
                .thenCompose(taxPayment -> {
                    if (!taxPayment.success()) {
                        return CompletableFuture.completedFuture(ShopResult.failure(ShopFailure.PAYMENT_FAILED, taxPayment.message()));
                    }
                    return CompletableFuture.supplyAsync(() -> {
                        try {
                            ShopTransaction transaction = storage.logTransaction(
                                    shop.id(),
                                    buyerUuid,
                                    sellerUuid,
                                    shop.itemKey(),
                                    quantity,
                                    total,
                                    tax
                            );
                            return ShopResult.success(transaction, "Transaction logged.");
                        } catch (SQLException exception) {
                            logger.log(Level.WARNING, "Failed to log shop transaction.", exception);
                            return ShopResult.<ShopTransaction>failure(ShopFailure.STORAGE_ERROR, "Storage error.");
                        }
                    }, asyncExecutor);
                });
    }
}
