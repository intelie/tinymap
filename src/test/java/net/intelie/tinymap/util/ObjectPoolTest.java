package net.intelie.tinymap.util;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectPoolTest {

    @Test
    public void crazyTest() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(1000);
        ObjectPool<Integer> pool = new ObjectPool<>(x -> counter.incrementAndGet(), 0, 5);

        Thread t1 = startThread(pool);
        Thread t2 = startThread(pool);
        Thread t3 = startThread(pool);
        Thread t4 = startThread(pool);

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        int last = counter.get();
        assertThat(last).isLessThan(1100);

        forceCollectSoftReferences();

        try (ObjectPool<Integer>.Ref ref = pool.acquire()) {
            assertThat(ref.obj()).isEqualTo(last + 1);
        }

    }

    @Test
    public void crazyTestMinPool() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(1000);
        ObjectPool<Integer> pool = new ObjectPool<>(x -> counter.incrementAndGet());

        Thread t1 = startThread(pool);
        Thread t2 = startThread(pool);
        Thread t3 = startThread(pool);
        Thread t4 = startThread(pool);

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        int last = counter.get();
        assertThat(last).isLessThan(1100);

        forceCollectSoftReferences();

        try (ObjectPool<Integer>.Ref ref = pool.acquire()) {
            assertThat(ref.obj()).isEqualTo(1001);
        }

    }

    private void forceCollectSoftReferences() {
        try {
            int max = (int) Math.min(Runtime.getRuntime().maxMemory() / 8, Integer.MAX_VALUE);
            long[] ignored = new long[max];
        } catch (OutOfMemoryError e) {
            // Ignore
        }
    }

    Thread startThread(ObjectPool<Integer> pool) {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try (ObjectPool<Integer>.Ref ref = pool.acquire()) {
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
                }
            }
        });
        thread.start();
        return thread;
    }
}