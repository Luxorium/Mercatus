package dev.luxorium.mercatus.model;

import java.util.Objects;
import org.bukkit.Location;

public record BlockLocation(String world, int x, int y, int z) {
    public BlockLocation {
        Objects.requireNonNull(world, "world");
    }

    public static BlockLocation from(Location location) {
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Location must have a world.");
        }
        return new BlockLocation(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public String display() {
        return world + " " + x + " " + y + " " + z;
    }
}
