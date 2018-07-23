package io.smilo.api.rest.models;

import io.smilo.api.block.data.transaction.Transaction;

import java.util.List;

public class TransactionList {
    private final List<Transaction> transactions;
    private final long skip;
    private final long take;
    private final long totalCount;

    public TransactionList(List<Transaction> transactions, long skip, long take, long count) {
        this.transactions = transactions;
        this.skip = skip;
        this.take = take;
        this.totalCount = count;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getSkip() {
        return skip;
    }

    public long getTake() {
        return take;
    }
}
