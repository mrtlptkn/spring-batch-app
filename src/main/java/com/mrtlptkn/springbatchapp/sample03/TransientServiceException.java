package com.mrtlptkn.springbatchapp.sample03;

public class TransientServiceException extends RuntimeException {
    public TransientServiceException(String message) {
        super(message);
    }
}
