/*
 * Copyright (c) 2018 Smilo Platform B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.smilo.api.address;

import io.smilo.api.block.data.transaction.Transaction;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
//TODO: transactional?
public class AddressManager {

    private static final Logger LOGGER = Logger.getLogger(AddressManager.class);
    private final AddressStore addressStore;
    private final AddressUtility addressUtility;

    public AddressManager(AddressStore addressStore, AddressUtility addressUtility) {
        this.addressStore = addressStore;
        this.addressUtility = addressUtility;
    }

    /**
     * This method executes a given transaction String of the format InputAddress;InputAmount;OutputAddress1;OutputAmount1;OutputAddress2;OutputAmount2...;SignatureData;SignatureIndex
     *
     * @param transaction transaction to execute
     *
     * @return boolean Whether execution of the transaction was successful
     */
    // TODO: throw exception instead of boolean for validation
    // TODO: validations might not be required here if we isValid during parsing
    public boolean executeTransaction(Transaction transaction) {
        if (!transaction.hasContent()) {
            return false;
        }

        try {
            boolean valid = checkValidity(transaction);
            if(!valid) {
                return false;
            }

            //Looks like everything is correct--transaction should be executed correctly
            Address inputAccount = addressStore.findOrCreate(transaction.getInputAddress());
            inputAccount.decrementBalance(transaction.getAssetId(), -transaction.getInputAmount());

            transaction.getTransactionOutputs().forEach(txOutput -> {
                Address outputAccount = addressStore.findOrCreate(txOutput.getOutputAddress());
                outputAccount.incrementBalance(transaction.getAssetId(), txOutput.getOutputAmount());
            });
            adjustAddressSignatureCount(transaction.getInputAddress(), 1);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Error executing transaction!");
            return false;
        }
    }

    /**
     * This method reverse-executes a given transaction String of the format InputAddress;InputAmount;OutputAddress1;OutputAmount1;OutputAddress2;OutputAmount2...;SignatureData;SignatureIndex Used
     * primarily when a blockchain fork is resolved, and transactions have to be reversed that existed in the now-forked block(s).
     *
     * @param transaction String-formatted transaction to execute
     *
     * @return boolean Whether execution of the transaction was successful
     */
    public boolean reverseTransaction(Transaction transaction) {
        try {
            boolean valid = checkValidity(transaction);
            if(!valid) {
                return false;
            }
            //Looks like everything is correct--transaction should be reversed correctly
            Address inputAccount = addressStore.findOrCreate(transaction.getInputAddress());
            inputAccount.incrementBalance(transaction.getAssetId(), transaction.getInputAmount());

            transaction.getTransactionOutputs().forEach(txOutput -> {
                Address outputAccount = addressStore.findOrCreate(txOutput.getOutputAddress());
                outputAccount.decrementBalance(transaction.getAssetId(), txOutput.getOutputAmount());
            });
            adjustAddressSignatureCount(transaction.getInputAddress(), - 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the last-used signature index of an address.
     *
     * @param address Account to retrieve the latest index for
     *
     * @return int Last signature index used by address
     */
    public long getAddressSignatureCount(String address) {
        Address result = addressStore.getByAddress(address);
        if (result == null) return -1;
        return result.getSignatureCount();
    }

    /**
     * Adjusts an address's signature count.
     *
     * @param address Account to adjust
     * @param adjustment Amount to adjust address's signature count by. This can be negative.
     *
     * @return boolean Whether the adjustment was successful
     */
    public boolean adjustAddressSignatureCount(String address, long adjustment) {
        long oldCount = getAddressSignatureCount(address);
        if (oldCount + adjustment < 0) //Adjustment is negative with an absolute value larger than oldBalance
        {
            return false;
        }
        return updateAddressSignatureCount(address, oldCount + adjustment);
    }

    /**
     * Updates an address's signature count.
     *
     * @param address Account to update
     * @param newCount New signature index to use
     *
     * @return boolean Whether the adjustment was successful
     */
    private boolean updateAddressSignatureCount(String address, long newCount) {
        try {
            Address account = addressStore.findOrCreate(address);
            account.setSignatureCount(newCount);
            addressStore.writeToFile(account);
        } catch (Exception e) {
            LOGGER.error("Unable to update address signature count", e);
            return false;
        }
        return true;
    }

    /**
     * Returns the address balance for a given address.
     *
     * @param address Account to check balance of
     *
     * @return long Balance of address
     */
    public long getAddressBalance(String address) {
        Address result = addressStore.getByAddress(address);
        if (result == null) return 0L;
        return (long)result.getBalance("000x00123");
    }

    /**
     * Adjusts the balance of an address by a given adjustment, which can be positive or negative.
     *
     * @param address Account to adjust the balance of
     * @param adjustment Amount to adjust account balance by
     *
     * @return boolean Whether the adjustment was successful
     */
    public boolean adjustAddressBalance(String address, long adjustment) {
        long oldBalance = (long)addressStore.findOrCreate(address).getBalance("000x00123");
        if (oldBalance + adjustment < 0) //Adjustment is negative with an absolute value larger than oldBalance
        {
            return false;
        }
        return updateAddressBalance(address, oldBalance + adjustment);
    }

    /**
     * Updates the balance of an address to a new amount
     *
     * @param address Account to set the balance of
     * @param newAmount New amount to set as the balance of address
     *
     * @return boolean Whether setting the new balance was successful
     */
    public boolean updateAddressBalance(String address, long newAmount) {
        try {
            Address account = addressStore.findOrCreate(address);
            account.setBalance("000x00123", newAmount);
            addressStore.writeToFile(account);
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
        return true;
    }

    private boolean checkValidity(Transaction transaction) {
        String inputAddress = transaction.getInputAddress();
        long signatureIndex = transaction.getSignatureIndex();
        long inputAmount = transaction.getInputAmount();
        long outputTotal = transaction.getOutputTotal();

        //The signature is valid, however it isn't using the expected signatureIndex. Blocked to ensure a compromised Lamport key from a previous transaction can't be used.
        if (getAddressSignatureCount(inputAddress) + 1 != signatureIndex) return false;

        //Incorrect sending address
        if (!addressUtility.isAddressFormattedCorrectly(inputAddress)) return false;

        //inputAddress has an insufficient balance
        if (getAddressBalance(inputAddress) < inputAmount) return false; //Insufficient balance

        boolean addressesAreValid = transaction.getTransactionOutputs().stream()
                .allMatch(txOutput -> addressUtility.isAddressFormattedCorrectly(txOutput.getOutputAddress()));
        if (!addressesAreValid) return false;

        if (inputAmount < outputTotal) return false;
        return true;
    }

}