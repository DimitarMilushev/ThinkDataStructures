package com.allendowney.thinkdast.utils;


import java.util.List;
import java.util.Map;

public class StreamsUtility{
    public static <K, V> void mapEntry(Map<K, V> map, Map.Entry<K, V> entry) {
        map.put(entry.getKey(), entry.getValue());
    }
}
