package io.smilo.api.rest.service;

import io.smilo.api.address.Address;
import io.smilo.api.address.AddressDTO;
import io.smilo.api.rest.models.TransactionList;

public interface AddressService {
    AddressDTO getAddress(String address);

    TransactionList getTransactionsForAddress(String address, long skip, long take, boolean isDescending);
}
