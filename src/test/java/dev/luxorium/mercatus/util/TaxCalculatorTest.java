package dev.luxorium.mercatus.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TaxCalculatorTest {
    @Test
    void calculatesTaxByRoundingDownMinorUnits() {
        assertEquals(5L, TaxCalculator.taxMinor(101L, 5.0D));
        assertEquals(96L, TaxCalculator.sellerReceivesMinor(101L, 5.0D));
    }

    @Test
    void zeroOrNegativeTaxDoesNotCharge() {
        assertEquals(0L, TaxCalculator.taxMinor(100L, 0.0D));
        assertEquals(0L, TaxCalculator.taxMinor(100L, -1.0D));
    }
}
