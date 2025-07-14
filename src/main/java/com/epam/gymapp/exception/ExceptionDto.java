package com.epam.gymapp.exception;


public class ExceptionDto {

    public ExceptionDto() {
    }

    public ExceptionDto(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
}
