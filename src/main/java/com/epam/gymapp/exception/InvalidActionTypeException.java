package com.epam.gymapp.exception;

public class InvalidActionTypeException extends RuntimeException {
    public InvalidActionTypeException(String type) {
        super("Unknown action type: " + type);
    }
}