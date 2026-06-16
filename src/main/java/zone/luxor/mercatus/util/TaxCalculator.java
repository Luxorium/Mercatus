package zone.luxor.mercatus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TaxCalculator {
    private TaxCalculator() {
    }

    public static long taxMinor(long priceMinor, double taxPercent) {
        if (priceMinor <= 0L || taxPercent <= 0.0D) {
            return 0L;
        }
        return BigDecimal.valueOf(priceMinor)
                .multiply(BigDecimal.valueOf(taxPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .longValueExact();
    }

    public static long sellerReceivesMinor(long priceMinor, double taxPercent) {
        return Math.max(0L, priceMinor - taxMinor(priceMinor, taxPercent));
    }
}
