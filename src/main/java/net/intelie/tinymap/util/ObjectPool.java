package net.intelie.tinymap.util;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ObjectPool<T> {
    private final ArrayDeque<Ref> queue = new ArrayDeque<>();
    private final Function<Ref, T> factory;
    private final int maxRetries;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final List<T> strong;
    //keeping a strong reference to minPoolSize elements to avoid their collection

    public ObjectPool(Function<Ref, T> factory) {
        this(factory, 1, 5);
    }

    public ObjectPool(Function<Ref, T> factory, int minPoolSize, int maxRetries) {
        this.maxRetries = maxRetries;
        this.factory = factory;
        this.strong = initMinPool(minPoolSize);
    }

    private List<T> initMinPool(int minPoolSize) {
        List<T> strong = new ArrayList<>();
        for (int i = 0; i < minPoolSize; i++) {
            try (Ref ref = new Ref()) {
                strong.add(ref.obj());
            }
        }
        return strong;
    }

    public Ref acquire() {
        Ref ref = null;

        for (int i = 0; i < maxRetries; i++) {
            synchronized (queue) {
                ref = queue.poll();
            }

            //either empty pool or valid deref'd object
            if (ref == null || ref.materialize()) break;

            ref = null;
        }

        if (ref == null)
            ref = new Ref();

        return ref;
    }

    public class Ref implements AutoCloseable {
        private final SoftReference<T> ref;
        private T obj;

        private Ref() {
            this.ref = new SoftReference<>(this.obj = factory.apply(this));
        }

        private boolean materialize() {
            this.obj = ref.get();
            return this.obj != null;
        }

        public T obj() {
            return obj;
        }

        @Override
        public void close() {
            if (obj == null) return;
            obj = null;
            synchronized (queue) {
                queue.offer(this);
            }
        }
    }
}
