package com.pharmacy.generics;

/**
 * Generic result wrapper for service operations.
 * Demonstrates: Generics, Immutability, Static factory methods
 *
 * @param <T> the type of data returned on success
 */
public final class Result<T> {

    private final boolean success;
    private final T       data;
    private final String  errorMessage;

    private Result(boolean success, T data, String errorMessage) {
        this.success      = success;
        this.data         = data;
        this.errorMessage = errorMessage;
    }

    /** Create a successful result carrying data. */
    public static <T> Result<T> ok(T data) {
        return new Result<>(true, data, null);
    }

    /** Create a successful result with no data. */
    public static <T> Result<T> ok() {
        return new Result<>(true, null, null);
    }

    /** Create a failed result with an error message. */
    public static <T> Result<T> fail(String message) {
        return new Result<>(false, null, message);
    }

    public boolean isSuccess()          { return success; }
    public boolean isFailure()          { return !success; }
    public T       getData()            { return data; }
    public String  getErrorMessage()    { return errorMessage; }

    @Override
    public String toString() {
        return success ? "Result.ok(" + data + ")" : "Result.fail(" + errorMessage + ")";
    }
}
