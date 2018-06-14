package io.smilo.api.rest.models;

public class Status {

    private final String status;
    private final Long height;

    public Status(Long height, String status) {
        this.height = height;
        this.status = status;
    }

    public long getHeight() {
        return height;
    }

    public String getStatus() {
        return status;
    }
}
