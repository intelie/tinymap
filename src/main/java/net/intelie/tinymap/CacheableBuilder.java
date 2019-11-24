package net.intelie.tinymap;

public interface CacheableBuilder<B, T> {
    T build();

    void compact();

    CacheAdapter<B, T> adapter();
}
