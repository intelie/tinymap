package net.intelie.tinymap.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultDoubleCacheTest {
    @Test
    public void testCacheHit() {
        DefaultDoubleCache cache = new DefaultDoubleCache();
        Double cached1 = cache.get(Double.parseDouble("123.456"));
        Double cached2 = cache.get((Double)Double.parseDouble("123.456"));

        assertThat(cached1).isSameAs(cached2);
    }

    @Test
    public void testNegativeZero() {
        DefaultDoubleCache cache = new DefaultDoubleCache();
        Double original1 = Double.parseDouble("0.0");
        Double original2 = Double.parseDouble("-0.0");

        Double cached1 = cache.get(original1);
        Double cached2 = cache.get(original2);

        assertThat(cached1).isNotEqualTo(cached2);
        assertThat(cached1.hashCode()).isNotEqualTo(cached2.hashCode());
    }


    @Test
    public void testSmallCache() {
        DefaultDoubleCache cache = new DefaultDoubleCache();
        Double original1 = Double.parseDouble("123");
        Double cached1 = cache.get(original1);
        Double cached2 = cache.get(original1);

        assertThat(cached1).isEqualTo(original1)
                .isNotSameAs(original1)
                .isSameAs(cached2);
    }
}