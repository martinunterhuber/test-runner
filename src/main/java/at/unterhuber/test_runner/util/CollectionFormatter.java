package at.unterhuber.test_runner.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class CollectionFormatter {
    public static <E> String toLineSeparatedString(Collection<E> c) {
        Iterator<E> it = c.iterator();
        if (!it.hasNext()) {
            return "[]";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append('[').append('\n');

            while (true) {
                E e = it.next();
                sb.append(' ');
                sb.append(e == c ? "(this Collection)" : e);
                if (!it.hasNext()) {
                    return sb.append('\n').append(']').toString();
                }

                sb.append('\n');
            }
        }
    }

    public static <K, V> String toLineSeparatedString(Map<K, V> m) {
        Iterator<Map.Entry<K, V>> i = m.entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append('{').append('\n');

            while (true) {
                Map.Entry<K, V> e = (Map.Entry) i.next();
                K key = e.getKey();
                V value = e.getValue();
                sb.append(' ');
                sb.append(key == m ? "(this Map)" : key);
                sb.append('=');
                sb.append(value == m ? "(this Map)" : value);
                if (!i.hasNext()) {
                    return sb.append('\n').append('}').toString();
                }

                sb.append('\n');
            }
        }
    }
}
