package net.intelie.tinymap;

import net.intelie.introspective.ObjectSizer;
import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.introspective.util.IdentityVisitedSet;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SizeUtils {
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
        while (sizer.moveNext()) {
            total += sizer.bytes();

            if (WeakReference.class.isAssignableFrom(sizer.type()))
                sizer.skipChildren();
        }
        return total;
    }

    public static long sizeNoStrings(Object obj) {
        ObjectSizer sizer = new ObjectSizer();
        sizer.resetTo(obj);
        long total = 0;
        while (sizer.moveNext()) {
            if (!sizer.type().equals(String.class))
                total += sizer.bytes();
        }
        return total;
    }

    public static void dump(Object obj) {
        ObjectSizer sizer = new ObjectSizer();
        sizer.resetTo(obj);

        Map<Class, AtomicLong> counts = new HashMap<>();
        Map<Class, AtomicLong> total = new HashMap<>();
        Map<Object, AtomicLong> doubleCount = new HashMap<>();

        while (sizer.moveNext()) {
            counts.computeIfAbsent(sizer.type(), x -> new AtomicLong()).incrementAndGet();
            total.computeIfAbsent(sizer.type(), x -> new AtomicLong()).addAndGet(sizer.bytes());
            doubleCount.computeIfAbsent(sizer.current(), x -> new AtomicLong()).incrementAndGet();
            if (WeakReference.class.isAssignableFrom(sizer.type()))
                sizer.skipChildren();
        }
        System.out.println("histogram:");
        total.entrySet().stream().sorted(Comparator.comparing(x -> -x.getValue().get())).forEach(entry -> {
            System.out.printf("  %6d %10s %s\n", counts.get(entry.getKey()).get(), SizeUtils.formatBytes(entry.getValue().get()), entry.getKey().getCanonicalName());
        });

        System.out.println("duplicates:");
        doubleCount.entrySet().stream()
                .filter(x -> x.getValue().get() > 1)
                .sorted((Comparator.comparing(x -> -x.getValue().get())))
                .limit(20)
                .forEach(entry -> {
                    System.out.println("  " + entry.getKey() + "   \t" + entry.getValue().get());
                });


    }
}
