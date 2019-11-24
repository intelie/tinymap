package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;


//AUTO-GENERATED SOURCE. See GenerateClasses
class TinyMapGenerated {
    public static <K, V> TinyMap<K, V> createUnsafe(TinySet<K> keys, Object[] values) {
        switch (values.length) {
            case 1:
                return new Size1<>(keys, values[0]);
            case 2:
                return new Size2<>(keys, values[0], values[1]);
            case 3:
                return new Size3<>(keys, values[0], values[1], values[2]);
            case 4:
                return new Size4<>(keys, values[0], values[1], values[2], values[3]);
            case 5:
                return new Size5<>(keys, values[0], values[1], values[2], values[3], values[4]);
            case 6:
                return new Size6<>(keys, values[0], values[1], values[2], values[3], values[4], values[5]);
            case 7:
                return new Size7<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
            case 8:
                return new Size8<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
            case 9:
                return new Size9<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
            case 10:
                return new Size10<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9]);
            case 11:
                return new Size11<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10]);
            case 12:
                return new Size12<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11]);
            case 13:
                return new Size13<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12]);
            case 14:
                return new Size14<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13]);
            case 15:
                return new Size15<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14]);
            case 16:
                return new Size16<>(keys, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14], values[15]);
            default:
                return new SizeAny<>(keys, values);
        }
    }

    public static class Size1<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;

        public Size1(TinySet<K> keys, Object v0) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 1, "keys and values must have same size");
            this.v0 = v0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size2<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;

        public Size2(TinySet<K> keys, Object v0, Object v1) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 2, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size3<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;

        public Size3(TinySet<K> keys, Object v0, Object v1, Object v2) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 3, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size4<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;

        public Size4(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 4, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size5<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;

        public Size5(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 5, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size6<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;

        public Size6(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 6, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size7<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;

        public Size7(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 7, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size8<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;

        public Size8(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 8, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size9<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;

        public Size9(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 9, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size10<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;

        public Size10(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 10, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size11<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;
        private final Object v10;

        public Size11(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 11, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                case 10:
                    return (V) v10;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size12<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;
        private final Object v10;
        private final Object v11;

        public Size12(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10, Object v11) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 12, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
            this.v11 = v11;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                case 10:
                    return (V) v10;
                case 11:
                    return (V) v11;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size13<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;
        private final Object v10;
        private final Object v11;
        private final Object v12;

        public Size13(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10, Object v11, Object v12) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 13, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
            this.v11 = v11;
            this.v12 = v12;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                case 10:
                    return (V) v10;
                case 11:
                    return (V) v11;
                case 12:
                    return (V) v12;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size14<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;
        private final Object v10;
        private final Object v11;
        private final Object v12;
        private final Object v13;

        public Size14(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10, Object v11, Object v12, Object v13) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 14, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
            this.v11 = v11;
            this.v12 = v12;
            this.v13 = v13;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                case 10:
                    return (V) v10;
                case 11:
                    return (V) v11;
                case 12:
                    return (V) v12;
                case 13:
                    return (V) v13;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size15<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;
        private final Object v10;
        private final Object v11;
        private final Object v12;
        private final Object v13;
        private final Object v14;

        public Size15(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10, Object v11, Object v12, Object v13, Object v14) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 15, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
            this.v11 = v11;
            this.v12 = v12;
            this.v13 = v13;
            this.v14 = v14;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                case 10:
                    return (V) v10;
                case 11:
                    return (V) v11;
                case 12:
                    return (V) v12;
                case 13:
                    return (V) v13;
                case 14:
                    return (V) v14;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }

    public static class Size16<K, V> extends TinyMap<K, V> implements Serializable {
        private final Object v0;
        private final Object v1;
        private final Object v2;
        private final Object v3;
        private final Object v4;
        private final Object v5;
        private final Object v6;
        private final Object v7;
        private final Object v8;
        private final Object v9;
        private final Object v10;
        private final Object v11;
        private final Object v12;
        private final Object v13;
        private final Object v14;
        private final Object v15;

        public Size16(TinySet<K> keys, Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10, Object v11, Object v12, Object v13, Object v14, Object v15) {
            super(keys);
            Preconditions.checkArgument(keys.size() == 16, "keys and values must have same size");
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
            this.v9 = v9;
            this.v10 = v10;
            this.v11 = v11;
            this.v12 = v12;
            this.v13 = v13;
            this.v14 = v14;
            this.v15 = v15;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            switch (index) {
                case 0:
                    return (V) v0;
                case 1:
                    return (V) v1;
                case 2:
                    return (V) v2;
                case 3:
                    return (V) v3;
                case 4:
                    return (V) v4;
                case 5:
                    return (V) v5;
                case 6:
                    return (V) v6;
                case 7:
                    return (V) v7;
                case 8:
                    return (V) v8;
                case 9:
                    return (V) v9;
                case 10:
                    return (V) v10;
                case 11:
                    return (V) v11;
                case 12:
                    return (V) v12;
                case 13:
                    return (V) v13;
                case 14:
                    return (V) v14;
                case 15:
                    return (V) v15;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
        }
    }


    public static class SizeAny<K, V> extends TinyMap<K, V> {
        public final Object[] values;

        public SizeAny(TinySet<K> keys, Object[] values) {
            super(keys);
            Preconditions.checkArgument(keys.size() == values.length, "keys and values must have same size");
            this.values = values;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V getValueAt(int index) {
            return (V) values[index];
        }
    }
}


