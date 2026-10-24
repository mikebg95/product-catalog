package dev.michaelgoldman.catalog.application.port;

public class PriceSourceUnavailableException extends RuntimeException {
    public PriceSourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
