package zone.luxor.mercatus.economy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class PaymentProviderTest {
    @Test
    void noopProviderAllowsZeroWithdrawOnly() {
        NoopPaymentProvider provider = new NoopPaymentProvider();
        assertFalse(provider.available());
        assertTrue(provider.withdraw(UUID.randomUUID(), 0L, "free").join().success());
        assertFalse(provider.withdraw(UUID.randomUUID(), 1L, "fee").join().success());
    }

    @Test
    void noopProviderRefusesTransfers() {
        NoopPaymentProvider provider = new NoopPaymentProvider();
        assertFalse(provider.transfer(UUID.randomUUID(), UUID.randomUUID(), 10L, "test").join().success());
    }
}
