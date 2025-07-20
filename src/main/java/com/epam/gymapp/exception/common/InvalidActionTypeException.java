package com.epam.gymapp.exception.common;

public class InvalidActionTypeException extends RuntimeException {
    public InvalidActionTypeException(String type) {
        super("Unknown action type: " + type);
    }
}