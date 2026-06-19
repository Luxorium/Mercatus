package dev.luxorium.mercatus.economy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
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

    @Test
    void standaloneProviderAllowsPositiveTransfersAndWithdrawals() {
        StandalonePaymentProvider provider = new StandalonePaymentProvider();
        assertFalse(provider.available());
        assertTrue(provider.transfer(UUID.randomUUID(), UUID.randomUUID(), 10L, "standalone").join().success());
        assertTrue(provider.withdraw(UUID.randomUUID(), 10L, "standalone").join().success());
    }

    @Test
    void providerResolutionHonorsRequiredFlagWhenAureusIsAbsent() {
        Server server = serverWithoutAureus();

        PaymentProvider strict = PaymentProviders.resolve(server, true, true, Logger.getLogger("test"));
        PaymentProvider standalone = PaymentProviders.resolve(server, true, false, Logger.getLogger("test"));

        assertInstanceOf(NoopPaymentProvider.class, strict);
        assertInstanceOf(StandalonePaymentProvider.class, standalone);
    }

    private static Server serverWithoutAureus() {
        PluginManager pluginManager = (PluginManager) Proxy.newProxyInstance(
                PluginManager.class.getClassLoader(),
                new Class<?>[]{PluginManager.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlugin" -> null;
                    case "toString" -> "PluginManagerWithoutAureus";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });

        return (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPluginManager" -> pluginManager;
                    case "toString" -> "ServerWithoutAureus";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
