package zone.luxor.mercatus.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Shop(
        long id,
        UUID ownerUuid,
        String ownerName,
        BlockLocation location,
        ShopMode mode,
        String itemKey,
        long priceMinor,
        Instant createdAt,
        Instant updatedAt
) {
    public Shop {
        Objects.requireNonNull(ownerUuid, "ownerUuid");
        Objects.requireNonNull(ownerName, "ownerName");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(itemKey, "itemKey");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (priceMinor <= 0L) {
            throw new IllegalArgumentException("priceMinor must be positive.");
        }
    }
}
