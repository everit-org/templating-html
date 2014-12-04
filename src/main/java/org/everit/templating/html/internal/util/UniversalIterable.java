/**
 * This file is part of Everit - HTML Templating.
 *
 * Everit - HTML Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTML Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTML Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.templating.html.internal.util;

import java.util.Iterator;

public class UniversalIterable<T> implements Iterable<T> {

    private static abstract class AbstractArrayIterator<T> extends AbstractIterator<T> {
        private int cursor = 0;

        private final int length;

        public AbstractArrayIterator(final int length) {
            this.length = length;
        }

        public abstract T element(int index);

        @Override
        public boolean hasNext() {
            return cursor < length;
        }

        @Override
        public T next() {
            return element(cursor++);
        }

    }

    private static abstract class AbstractIterator<T> implements Iterator<T> {
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class BooleanArrayIterator extends AbstractArrayIterator<Boolean> {

        private final boolean[] array;

        public BooleanArrayIterator(final boolean[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Boolean element(final int index) {
            return array[index];
        }

    }

    private static class ByteArrayIterator extends AbstractArrayIterator<Byte> {

        private final byte[] array;

        public ByteArrayIterator(final byte[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Byte element(final int index) {
            return array[index];
        }

    }

    private static class CharArrayIterator extends AbstractArrayIterator<Character> {

        private final char[] array;

        public CharArrayIterator(final char[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Character element(final int index) {
            return array[index];
        }

    }

    private static enum CollectionTypes {
        BOOLEAN_ARRAY, BYTE_ARRAY, CHAR_ARRAY, DOUBLE_ARRAY, FLOAT_ARRAY, INT_ARRAY, INTEGER, ITERABLE, LONG_ARRAY,
        OBJECT_ARRAY, SHORT_ARRAY
    }

    private static class DoubleArrayIterator extends AbstractArrayIterator<Double> {

        private final double[] array;

        public DoubleArrayIterator(final double[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Double element(final int index) {
            return array[index];
        }
    }

    private static class FloatArrayIterator extends AbstractArrayIterator<Float> {

        private final float[] array;

        public FloatArrayIterator(final float[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Float element(final int index) {
            return array[index];
        }
    }

    private static class IntArrayIterator extends AbstractArrayIterator<Integer> {

        private final int[] array;

        public IntArrayIterator(final int[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Integer element(final int index) {
            return array[index];
        }
    }

    private static class IntegerIterator extends AbstractIterator<Integer> {

        private int cursor = 0;

        private final int n;

        public IntegerIterator(final Integer n) {
            this.n = n;
        }

        @Override
        public boolean hasNext() {
            return cursor < n;
        }

        @Override
        public Integer next() {
            return cursor++;
        }

    }

    private static class LongArrayIterator extends AbstractArrayIterator<Long> {

        private final long[] array;

        public LongArrayIterator(final long[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Long element(final int index) {
            return array[index];
        }
    }

    private static class ObjectArrayIterator extends AbstractArrayIterator<Object> {

        private final Object[] array;

        public ObjectArrayIterator(final Object[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Object element(final int index) {
            return array[index];
        }

    }

    private static class ShortArrayIterator extends AbstractArrayIterator<Short> {

        private final short[] array;

        public ShortArrayIterator(final short[] array) {
            super(array.length);
            this.array = array;
        }

        @Override
        public Short element(final int index) {
            return array[index];
        }
    }

    private final Object collection;

    private final CollectionTypes collectionType;

    public UniversalIterable(final Object collection) {
        if (collection == null) {
            throw new NullPointerException("Collection must not be null");
        }
        this.collection = collection;
        if (collection instanceof Iterable) {
            collectionType = CollectionTypes.ITERABLE;
        } else if (collection instanceof boolean[]) {
            collectionType = CollectionTypes.BOOLEAN_ARRAY;
        } else if (collection instanceof byte[]) {
            collectionType = CollectionTypes.BYTE_ARRAY;
        } else if (collection instanceof char[]) {
            collectionType = CollectionTypes.CHAR_ARRAY;
        } else if (collection instanceof double[]) {
            collectionType = CollectionTypes.DOUBLE_ARRAY;
        } else if (collection instanceof float[]) {
            collectionType = CollectionTypes.FLOAT_ARRAY;
        } else if (collection instanceof int[]) {
            collectionType = CollectionTypes.INT_ARRAY;
        } else if (collection instanceof long[]) {
            collectionType = CollectionTypes.LONG_ARRAY;
        } else if (collection instanceof short[]) {
            collectionType = CollectionTypes.SHORT_ARRAY;
        } else if (collection instanceof Object[]) {
            collectionType = CollectionTypes.OBJECT_ARRAY;
        } else if (collection instanceof Integer) {
            collectionType = CollectionTypes.INTEGER;
        } else {
            throw new IllegalArgumentException(
                    "Unrecognized type of collection (Iterable, Array and number are accepted): "
                            + collection.getClass());
        }
    }

    @Override
    public Iterator<T> iterator() {
        Object result = null;

        switch (collectionType) {
        case BOOLEAN_ARRAY:
            result = new BooleanArrayIterator((boolean[]) collection);
            break;
        case BYTE_ARRAY:
            result = new ByteArrayIterator((byte[]) collection);
            break;
        case CHAR_ARRAY:
            result = new CharArrayIterator((char[]) collection);
            break;
        case DOUBLE_ARRAY:
            result = new DoubleArrayIterator((double[]) collection);
            break;
        case FLOAT_ARRAY:
            result = new FloatArrayIterator((float[]) collection);
            break;
        case INT_ARRAY:
            result = new IntArrayIterator((int[]) collection);
            break;
        case LONG_ARRAY:
            result = new LongArrayIterator((long[]) collection);
            break;
        case OBJECT_ARRAY:
            result = new ObjectArrayIterator((Object[]) collection);
            break;
        case SHORT_ARRAY:
            result = new ShortArrayIterator((short[]) collection);
            break;
        case ITERABLE:
            @SuppressWarnings("unchecked")
            Iterable<T> iterable = (Iterable<T>) collection;
            result = iterable.iterator();
            break;
        case INTEGER:
            result = new IntegerIterator((Integer) collection);
            break;
        }

        @SuppressWarnings("unchecked")
        Iterator<T> typedResult = (Iterator<T>) result;
        return typedResult;
    }

}
