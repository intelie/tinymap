package net.intelie.tinymap;

public interface ObjectCache {
    Double get(double value);

    String get(CharSequence cs);

    <B extends CacheableBuilder<B, T>, T> T get(B builder);

    <B, T> T get(B builder, CacheAdapter<B, T> adapter);
}
