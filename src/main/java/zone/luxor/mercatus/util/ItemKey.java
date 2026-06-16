package zone.luxor.mercatus.util;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemKey {
    private static final Pattern KEY = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_/.-]+");

    private ItemKey() {
    }

    public static String from(ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "itemStack");
        return from(itemStack.getType());
    }

    public static String from(Material material) {
        Objects.requireNonNull(material, "material");
        return material.getKey().toString().toLowerCase(Locale.ROOT);
    }

    public static Optional<String> normalize(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }
        if (!KEY.matcher(normalized).matches()) {
            return Optional.empty();
        }
        return Optional.of(normalized);
    }
}
