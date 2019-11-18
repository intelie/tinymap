package net.intelie.tinymap;

import net.intelie.introspective.ObjectSizer;
import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.introspective.util.IdentityVisitedSet;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TestSizeUtils {
    public static String formatBytes(double value) {
        String[] NAMES = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int name = 0;
        while (Math.abs(value) >= 1024) {
            value /= 1024;
            name++;
        }

        return String.format(Locale.US,
                name == 0 ? "%,.0f" : "%,.2f", value) + " " + NAMES[name];
    }

    public static String formattedSize(Object obj) {
        return formatBytes(size(obj));
    }

    public static String formattedSizeNoStrings(Object obj) {
        return formatBytes(sizeNoStrings(obj));
    }

    public static long size(Object obj) {
        ObjectSizer sizer = new ObjectSizer();
        sizer.resetTo(obj);
        long total = 0;
        long skipped = 0;
        while (sizer.moveNext()) {
            total += sizer.bytes();

            if (WeakReference.class.isAssignableFrom(sizer.type())) {
                sizer.skipChildren();
                skipped++;
            }
        }
        assert sizer.skipped() == skipped;
        return total;
    }

    public static long sizeNoStrings(Object obj) {
        ObjectSizer sizer = new ObjectSizer(new ReflectionCache(), new IdentityVisitedSet(), 1 << 20);
        sizer.resetTo(obj);
        long total = 0;
        while (sizer.moveNext()) {
            if (!sizer.type().equals(String.class))
                total += sizer.bytes();
        }
        assert sizer.skipped() == 0;
        return total;
    }

    public static void dump(Object obj) {
        ObjectSizer sizer = new ObjectSizer();
        sizer.resetTo(obj);

        Map<Class, AtomicLong> counts = new HashMap<>();
        Map<Class, AtomicLong> total = new HashMap<>();
        Map<UniqueWrapper, AtomicLong> doubleCount = new HashMap<>();
        long skipped = 0;
        long totalCount = 0, uniqueCount = 0, totalBytes = 0, uniqueBytes = 0;

        Map<Integer, AtomicLong> mapCounts = new HashMap<>();

        while (sizer.moveNext()) {
            totalCount++;
            totalBytes += sizer.bytes();

            counts.computeIfAbsent(sizer.type(), x -> new AtomicLong()).incrementAndGet();
            total.computeIfAbsent(sizer.type(), x -> new AtomicLong()).addAndGet(sizer.bytes());

            if (doubleCount.computeIfAbsent(new UniqueWrapper(sizer.current()), x -> new AtomicLong()).incrementAndGet() == 1) {
                uniqueCount++;
                uniqueBytes += sizer.bytes();
            }

            if (sizer.current() instanceof Map)
                mapCounts.computeIfAbsent(((Map) sizer.current()).size(), x -> new AtomicLong()).incrementAndGet();

            if (WeakReference.class.isAssignableFrom(sizer.type())) {
                sizer.skipChildren();
                skipped++;
            }
        }
        System.out.println(mapCounts);
        System.out.printf("total     %10d %10s\n", totalCount, formatBytes(totalBytes));
        System.out.printf("unique    %10d %10s\n", uniqueCount, formatBytes(uniqueBytes));
        System.out.printf("duplicate %10d %10s\n", (totalCount - uniqueCount), formatBytes(totalBytes - uniqueBytes));

        System.out.println("histogram:");
        total.entrySet().stream().sorted(Comparator.comparing(x -> -x.getValue().get())).forEach(entry -> {
            System.out.printf("  %6d %10s %s\n", counts.get(entry.getKey()).get(), TestSizeUtils.formatBytes(entry.getValue().get()), entry.getKey().getCanonicalName());
        });
        assert sizer.skipped() == skipped;

        sizer.clear();

        System.out.println("top 10 duplicates: ");

        doubleCount.entrySet().stream()
                .filter(x -> x.getValue().get() > 1)
                .sorted((Comparator.comparing(x -> -x.getValue().get())))
                .limit(10)
                .forEach(entry -> {
                    System.out.println("  " + entry.getKey().obj + "   \t" + entry.getValue().get());
                });

    }

    private static class UniqueWrapper {
        private final Object obj;

        public UniqueWrapper(Object obj) {
            this.obj = obj;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof UniqueWrapper)) return false;
            Object that = ((UniqueWrapper) o).obj;
            if (that == null) return obj == null;
            if (obj == that) return true;
            Class<?> clazz = obj.getClass();
            if (!clazz.isAssignableFrom(that.getClass())) return false;
            if (byte[].class.equals(clazz))
                return Arrays.equals((byte[]) obj, (byte[]) obj);
            if (short[].class.equals(clazz))
                return Arrays.equals((short[]) obj, (short[]) obj);
            if (int[].class.equals(clazz))
                return Arrays.equals((int[]) obj, (int[]) obj);
            if (long[].class.equals(clazz))
                return Arrays.equals((long[]) obj, (long[]) obj);
            if (float[].class.equals(clazz))
                return Arrays.equals((float[]) obj, (float[]) obj);
            if (double[].class.equals(clazz))
                return Arrays.equals((double[]) obj, (double[]) obj);
            if (char[].class.equals(clazz))
                return Arrays.equals((char[]) obj, (char[]) obj);
            if (boolean[].class.equals(clazz))
                return Arrays.equals((boolean[]) obj, (boolean[]) obj);
            return Objects.equals(obj, that);
        }

        @Override
        public int hashCode() {
            Class<?> clazz = obj.getClass();
            if (byte[].class.equals(clazz))
                return Arrays.hashCode((byte[]) obj);
            if (short[].class.equals(clazz))
                return Arrays.hashCode((short[]) obj);
            if (int[].class.equals(clazz))
                return Arrays.hashCode((int[]) obj);
            if (long[].class.equals(clazz))
                return Arrays.hashCode((long[]) obj);
            if (float[].class.equals(clazz))
                return Arrays.hashCode((float[]) obj);
            if (double[].class.equals(clazz))
                return Arrays.hashCode((double[]) obj);
            if (char[].class.equals(clazz))
                return Arrays.hashCode((char[]) obj);
            if (boolean[].class.equals(clazz))
                return Arrays.hashCode((boolean[]) obj);
            return Objects.hashCode(obj);
        }
    }
}
