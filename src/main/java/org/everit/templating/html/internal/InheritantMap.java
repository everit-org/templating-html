package org.everit.templating.html.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.everit.templating.TemplateConstants;

public class InheritantMap<K, V> implements Map<K, V> {

    private final Map<K, V> internalMap;

    private final Map<K, V> parentMap;

    public InheritantMap(final Map<K, V> parentMap) {
        this.parentMap = parentMap;
        this.internalMap = new HashMap<K, V>();
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public boolean containsKey(final Object key) {
        boolean result = internalMap.containsKey(key);
        if (!result && parentMap != null) {
            return parentMap.containsKey(key);
        }
        return result;
    }

    @Override
    public boolean containsValue(final Object value) {
        boolean result = internalMap.containsValue(value);
        if (!result && parentMap != null) {
            return parentMap.containsValue(value);
        }
        return result;
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        InheritantMap<K, V> other = (InheritantMap<K, V>) obj;
        if (internalMap == null) {
            if (other.internalMap != null) {
                return false;
            }
        } else if (!internalMap.equals(other.internalMap)) {
            return false;
        }
        if (parentMap == null) {
            if (other.parentMap != null) {
                return false;
            }
        } else if (!parentMap.equals(other.parentMap)) {
            return false;
        }
        return true;
    }

    @Override
    public V get(final Object key) {

        V result = internalMap.get(key);

        if (result != null) {
            return result;
        }

        if (!internalMap.containsKey(key)) {
            return parentMap.get(key);
        }
        return null;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((internalMap == null) ? 0 : internalMap.hashCode());
        result = prime * result + ((parentMap == null) ? 0 : parentMap.hashCode());
        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = internalMap.isEmpty();
        if (result && parentMap != null) {
            return parentMap.isEmpty();
        }
        return result;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(final K key, final V value) {
        if (TemplateConstants.VAR_TEMPLATE_CONTEXT.equals(key)) {
            throw new ReservedWordException("'" + TemplateConstants.VAR_TEMPLATE_CONTEXT + "' is a reserved word");
        }

        if (parentMap != null && parentMap.containsKey(key)) {
            return parentMap.put(key, value);
        } else {
            return internalMap.put(key, value);
        }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Same as {@link #put(Object, Object)} but without checking reserved words.
     *
     * @param key
     *            The key.
     * @param value
     *            The value.
     * @return The previous value if existed.
     */
    V putInternal(final K key, final V value) {
        return internalMap.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        return internalMap.remove(key);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

}
