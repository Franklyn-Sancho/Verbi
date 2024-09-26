package br.com.verbi.verbi.exception;

public class EmailCreationException extends RuntimeException {
    public EmailCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
