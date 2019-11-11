package net.intelie.tinymap.util;

import net.intelie.tinymap.CacheAdapter;
import net.intelie.tinymap.ObjectCache;

public class StringCacheAdapter implements CacheAdapter<CharSequence, String> {
    @Override
    public int contentHashCode(CharSequence cs) {
        int length = cs.length();
        int hash = 0;
        for (int i = 0; i < length; i++)
            hash = 31 * hash + cs.charAt(i);
        return hash;
    }

    @Override
    public String contentEquals(CharSequence cs, Object cached) {
        if (!(cached instanceof String)) return null;
        String str = (String) cached;
        return str.contentEquals(cs) ? str : null;
    }

    @Override
    public String build(CharSequence cs, ObjectCache parent) {
        return cs.toString();
    }
}
