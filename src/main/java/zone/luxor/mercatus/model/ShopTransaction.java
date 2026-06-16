package zone.luxor.mercatus.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ShopTransaction(
        long id,
        long shopId,
        UUID buyerUuid,
        UUID sellerUuid,
        String itemKey,
        int quantity,
        long priceMinor,
        long taxMinor,
        Instant createdAt
) {
    public ShopTransaction {
        Objects.requireNonNull(buyerUuid, "buyerUuid");
        Objects.requireNonNull(sellerUuid, "sellerUuid");
        Objects.requireNonNull(itemKey, "itemKey");
        Objects.requireNonNull(createdAt, "createdAt");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive.");
        }
        if (priceMinor <= 0L) {
            throw new IllegalArgumentException("priceMinor must be positive.");
        }
        if (taxMinor < 0L) {
            throw new IllegalArgumentException("taxMinor must not be negative.");
        }
    }
}
