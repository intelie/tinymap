package net.intelie.tinymap;

import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class StringCacheTest {

    @Test
    public void testCacheHit() {
        ObjectCache cache = new ObjectCache();
        StringBuilder original = new StringBuilder("abcde");
        String cached1 = cache.get(original);
        String cached2 = cache.get(original);

        assertThat(original.toString()).isEqualTo(cached1).isNotSameAs(cached1);
        assertThat(original.toString()).isEqualTo(cached2).isNotSameAs(cached2);

        assertThat(cached1).isSameAs(cached2);
    }

    @Test
    public void testCacheSecondHit() {
        ObjectCache cache = new ObjectCache();
        StringBuilder original1 = new StringBuilder("FB");
        StringBuilder original2 = new StringBuilder("Ea");

        String cached1 = cache.get(original1);
        String cached2 = cache.get(original2);
        String cached1b = cache.get(original1);

        assertThat(original1.toString()).isEqualTo(cached1).isNotSameAs(cached1);
        assertThat(original2.toString()).isEqualTo(cached2).isNotSameAs(cached2);

        assertThat(cached1).isNotEqualTo(cached2);
        assertThat(cached1.hashCode()).isEqualTo(cached2.hashCode());

        assertThat(cached1).isSameAs(cached1b);
    }

    @Test
    public void testSpecialCases() {
        ObjectCache cache = new ObjectCache();

        String original1 = IntStream.range(0, 1025).mapToObj(x -> "x").collect(Collectors.joining());
        String original2 = IntStream.range(0, 1025).mapToObj(x -> "x").collect(Collectors.joining());
        String cached1 = cache.get(original1);
        String cached2 = cache.get(original2);

        assertThat(cached1).isSameAs(cached2);
    }

    @Test
    public void testEmptyStrings() {
        ObjectCache cache = new ObjectCache();

        assertThat(cache.get("")).isSameAs(cache.get(""));
        assertThat(cache.get((CharSequence) null)).isEqualTo(null);
    }
}