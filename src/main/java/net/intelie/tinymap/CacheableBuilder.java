package net.intelie.tinymap;

public interface CacheableBuilder<B, T> {
    T build();

    CacheAdapter<B, T> adapter();
}
