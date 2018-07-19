package io.smilo.api.rest.models;

public class PostTransactionResult {
    private boolean succeeded;
    private String error;

    public PostTransactionResult(boolean succeeded, String error) {
        this.succeeded = succeeded;
        this.error = error;
    }

    public boolean getSucceeded() {
        return this.succeeded;
    }

    public String getError() {
        return this.error;
    }

}
