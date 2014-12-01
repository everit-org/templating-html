package org.everit.osgi.ewt.internal.inline;

import static java.lang.String.valueOf;

public class ExecutionStack {

    private StackElement element;

    private int size = 0;

    public int deepCount() {
        int count = 0;

        if (element == null) {
            return 0;
        }
        else {
            count++;
        }

        StackElement element = this.element;
        while ((element = element.next) != null) {
            count++;
        }
        return count;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Object peek() {
        if (size == 0)
            return null;
        else
            return element.value;
    }

    public Object pop() {
        if (size == 0) {
            return null;
        }
        try {
            size--;
            return element.value;
        } finally {
            element = element.next;
            assert size == deepCount();
        }
    }

    public void push(final Object o) {
        size++;
        element = new StackElement(element, o);
        assert size == deepCount();

    }

    @Override
    public String toString() {
        StackElement el = element;

        if (element == null)
            return "<EMPTY>";

        StringBuilder appender = new StringBuilder().append("[");
        do {
            appender.append(valueOf(el.value));
            if (el.next != null)
                appender.append(", ");
        } while ((el = el.next) != null);

        appender.append("]");

        return appender.toString();
    }
}
