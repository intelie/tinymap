# TinyMap

Memory-Efficient Immutable HashMaps

This library provides a straightforward open-addressing ordered hash table implementation. That implementation, along
with an aggressive caching strategy (also provided here) can lead to incredibly low memory usage for semi-structured 
hashmaps.

That is very useful to represent small immutable events. 

## Usage

TinyMap is available through Maven Central repository, just add the following
dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>net.intelie.tinymap</groupId>
    <artifactId>tinymap</artifactId>
    <version>0.1</version>
</dependency>
```

### Building a new map (without caches)

```java
TinyMap<Object, Object> built = TinyMap.builder()
        .put("key1", "value1")
        .put("key2", TinyList.builder().add(42.0).add("subvalue").build())
        .build();
```

This map uses exactly 384 bytes in Java 8, considering all its object tree. This is already better than 
Guava's ImmutableMap (408 bytes) and LinkedHashMap (528 bytes).

### Optimizing existing map (with cache)

TinyMap can leverage aggressive caching to avoid representing same maps, keySets, or even Strings multiple times.

```java
ArrayList<Object> list = new ArrayList<>();

for (int i = 0; i < 1000; i++) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("key1", "value" + i);
    map.put("key2", i);
    map.put("key3", (double)(i/100));
    list.add(map);
}

TinyOptimizer optimizer = new TinyOptimizer(new ObjectCache());
TinyList<Object> tinyList = optimizer.optimizeList(list);
```

The optimized version uses almost 60% less memory than the pure Java version (137.19 KB vs 348.75 KB).

### Parsing JSON

TODO 