package net.intelie.tinymap;

import net.intelie.introspective.ObjectSizer;
import net.intelie.introspective.reflect.ReflectionCache;
import net.intelie.introspective.util.IdentityVisitedSet;

import java.lang.ref.WeakReference;
import java.util.Locale;

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
            if (!sizer.type().equals(WeakReference.class))
                total += sizer.bytes();
            else
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
}
