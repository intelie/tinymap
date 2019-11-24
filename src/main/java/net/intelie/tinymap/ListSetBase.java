package net.intelie.tinymap;

import java.util.Objects;
import java.util.Set;

public abstract class ListSetBase<T> extends ListCollectionBase<T> implements ListSet<T> {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Set) || size() != ((Set) o).size()) return false;

        for (Object obj : ((Set) o))
            if (getIndex(obj) < 0)
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i))
                hash += Objects.hashCode(getEntryAt(i));
        return hash;
    }
}
