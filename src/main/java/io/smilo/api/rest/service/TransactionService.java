package io.smilo.api.rest.service;

import io.smilo.api.block.data.transaction.Transaction;

public interface TransactionService {
    Transaction putTransaction(Transaction transaction);
}
