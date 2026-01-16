package Noteboard._helpers;

import java.time.Instant;

public class ApiError {

    public String message;
    public String error;
    public Instant timestamp;

    public ApiError() {
    }

    public ApiError(String message, String error) {
        this.message = message;
        this.error = error;
        this.timestamp = Instant.now();
    }
}
