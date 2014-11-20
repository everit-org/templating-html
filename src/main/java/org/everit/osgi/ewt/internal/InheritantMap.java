/**
 * This file is part of Everit - Web Templating.
 *
 * Everit - Web Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Web Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Web Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.ewt.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class InheritantMap<K, V> implements Map<K, V> {

    private Map<K, V> internalMap;

    private final Map<K, V> parentMap;

    public InheritantMap(Map<K, V> parentMap) {
        this.parentMap = parentMap;
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        boolean result = internalMap.containsKey(key);
        if (!result && parentMap != null) {
            return parentMap.containsKey(key);
        }
        return result;
    }

    @Override
    public boolean containsValue(Object value) {
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
    public boolean equals(Object obj) {
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
    public V get(Object key) {
        return internalMap.get(key);
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
    public V put(K key, V value) {
        return internalMap.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        internalMap.putAll(m);
    }

    @Override
    public V remove(Object key) {
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
