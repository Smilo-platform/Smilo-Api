package io.smilo.api.rest.service;

import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.rest.models.PostTransactionResult;

public interface TransactionService {
    PostTransactionResult putTransaction(Transaction transaction);
}
