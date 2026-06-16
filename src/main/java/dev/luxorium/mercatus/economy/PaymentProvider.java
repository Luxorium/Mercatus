package dev.luxorium.mercatus.economy;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PaymentProvider {
    String name();

    boolean available();

    CompletableFuture<PaymentResult> transfer(UUID sourceUuid, UUID targetUuid, long amountMinor, String reason);

    CompletableFuture<PaymentResult> withdraw(UUID sourceUuid, long amountMinor, String reason);
}
