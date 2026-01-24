package com.example.notificationdispatcher.exception;

public class PermanentFailureException extends RuntimeException{

    public PermanentFailureException(String message) {
        super(message);
    }
}
