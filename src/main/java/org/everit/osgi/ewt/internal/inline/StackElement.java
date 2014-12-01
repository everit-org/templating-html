package org.everit.osgi.ewt.internal.inline;

import java.io.Serializable;

public class StackElement implements Serializable {
    public StackElement next;

    public Object value;

    public StackElement(final StackElement next, final Object value) {
        this.next = next;
        this.value = value;
    }
}
