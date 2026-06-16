package zone.luxor.mercatus.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ItemKeyTest {
    @Test
    void normalizesStableItemKeys() {
        assertEquals("minecraft:emerald", ItemKey.normalize("Emerald").orElseThrow());
        assertEquals("minecraft:diamond", ItemKey.normalize("minecraft:DIAMOND").orElseThrow());
        assertTrue(ItemKey.normalize("not a key").isEmpty());
    }
}
