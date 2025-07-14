package com.epam.gymapp.util;

public class TransactionContext {
    private static final ThreadLocal<String> transactionId = new ThreadLocal<>();

    public static void setTransactionId(String id) {
        transactionId.set(id);
    }

    public static String getTransactionId() {
        return transactionId.get();
    }

    public static void clearTransactionId() {
        transactionId.remove();
    }
}