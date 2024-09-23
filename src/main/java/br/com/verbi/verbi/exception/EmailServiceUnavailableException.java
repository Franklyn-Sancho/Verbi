package br.com.verbi.verbi.exception;

public class EmailServiceUnavailableException extends RuntimeException {
    public EmailServiceUnavailableException(String message) {
        super(message);
    }
}
