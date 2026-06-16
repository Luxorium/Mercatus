package zone.luxor.mercatus.model;

import java.util.Locale;
import java.util.Optional;

public enum ShopMode {
    BUY,
    SELL;

    public static Optional<ShopMode> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(input.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
