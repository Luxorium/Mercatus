package dev.luxorium.mercatus.economy;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NoopPaymentProvider implements PaymentProvider {
    @Override
    public String name() {
        return "none";
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public CompletableFuture<PaymentResult> transfer(UUID sourceUuid, UUID targetUuid, long amountMinor, String reason) {
        return CompletableFuture.completedFuture(PaymentResult.failure("No payment provider is available."));
    }

    @Override
    public CompletableFuture<PaymentResult> withdraw(UUID sourceUuid, long amountMinor, String reason) {
        if (amountMinor == 0L) {
            return CompletableFuture.completedFuture(PaymentResult.ok());
        }
        return CompletableFuture.completedFuture(PaymentResult.failure("No payment provider is available."));
    }
}
