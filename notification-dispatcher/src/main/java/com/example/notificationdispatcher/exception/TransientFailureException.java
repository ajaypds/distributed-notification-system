package com.example.notificationdispatcher.exception;

public class TransientFailureException extends RuntimeException{

    public TransientFailureException(String message, Throwable cause) {
        super(message, cause);

    }


}
