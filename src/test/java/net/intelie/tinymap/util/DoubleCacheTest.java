package net.intelie.tinymap.util;

import net.intelie.tinymap.util.DoubleCache;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleCacheTest {
    @Test
    public void testCacheHit() {
        DoubleCache cache = new DoubleCache();
        Double cached1 = cache.get(Double.parseDouble("123.456"));
        Double cached2 = cache.get(Double.parseDouble("123.456"));

        assertThat(cached1).isSameAs(cached2);
    }

    @Test
    public void testCacheSecondHit() {
        DoubleCache cache = new DoubleCache();
        Double original1 = Double.longBitsToDouble(0xF00F000000000000L);
        Double original2 = Double.longBitsToDouble(0x00000000F00F0000L);

        Double cached1 = cache.get(original1);
        Double cached2 = cache.get(original2);
        Double cached1b = cache.get(Double.longBitsToDouble(0xF00F000000000000L));

        assertThat(cached1).isNotEqualTo(cached2);
        assertThat(cached1.hashCode()).isEqualTo(cached2.hashCode());

        assertThat(cached1).isSameAs(cached1b);
    }


    @Test
    public void testSmallCache() {
        DoubleCache cache = new DoubleCache();
        Double original1 = Double.parseDouble("123");
        Double cached1 = cache.get(original1);
        Double cached2 = cache.get(original1);

        assertThat(cached1).isEqualTo(original1)
                .isNotSameAs(original1)
                .isSameAs(cached2);
    }
}