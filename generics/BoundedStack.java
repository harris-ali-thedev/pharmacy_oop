package com.pharmacy.generics;

import java.util.EmptyStackException;

/**
 * Generic bounded Stack implementation.
 * Demonstrates: Generics with bounded capacity, Exception handling
 *
 * @param <T> element type
 */
public class BoundedStack<T> {

    private final Object[] elements;
    private int top = -1;

    public BoundedStack(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        this.elements = new Object[capacity];
    }

    public void push(T item) {
        if (isFull()) throw new IllegalStateException("Stack is full (capacity=" + elements.length + ")");
        elements[++top] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T item = (T) elements[top];
        elements[top--] = null;
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return (T) elements[top];
    }

    public boolean isEmpty()  { return top == -1; }
    public boolean isFull()   { return top == elements.length - 1; }
    public int     size()     { return top + 1; }
    public int     capacity() { return elements.length; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i <= top; i++) {
            if (i > 0) sb.append(", ");
            sb.append(elements[i]);
        }
        return sb.append("]").toString();
    }
}
