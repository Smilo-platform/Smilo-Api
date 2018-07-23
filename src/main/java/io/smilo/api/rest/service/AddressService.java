package io.smilo.api.rest.service;

import io.smilo.api.address.Address;
import io.smilo.api.rest.models.TransactionList;

public interface AddressService {
    Address getAddress(String address);

    TransactionList getTransactionsForAddress(String address, long skip, long take, boolean isDescending);
}
