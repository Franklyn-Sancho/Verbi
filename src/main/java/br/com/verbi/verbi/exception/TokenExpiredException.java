package br.com.verbi.verbi.exception;

// TokenExpiredException.java
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}