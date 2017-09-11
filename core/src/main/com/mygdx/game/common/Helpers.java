package com.mygdx.game.common;

import java.util.AbstractMap;
import java.util.Map.Entry;

public class Helpers {

    public static <K, V> Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }

}
