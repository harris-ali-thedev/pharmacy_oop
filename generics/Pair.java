package com.pharmacy.generics;

/**
 * Generic immutable key-value pair.
 * Demonstrates: Multiple type parameters, Generics
 *
 * @param <A> first element type
 * @param <B> second element type
 */
public final class Pair<A, B> {

    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first  = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> of(A a, B b) { return new Pair<>(a, b); }

    public A getFirst()  { return first; }
    public B getSecond() { return second; }

    @Override
    public String toString() { return "(" + first + ", " + second + ")"; }
}
