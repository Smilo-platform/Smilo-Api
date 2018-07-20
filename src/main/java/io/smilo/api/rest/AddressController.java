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

package io.smilo.api.rest;

import io.smilo.api.address.Address;
import io.smilo.api.address.AddressStore;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionAddressStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AddressController {

    @Autowired
    AddressStore addressStore;

    @Autowired
    TransactionAddressStore transactionAddressStore;

    @GetMapping("/address/{address}")
    public Address listAddress(@PathVariable("address") String address) {
        Address response = addressStore.getByAddress(address);

        if (response == null) throw new AddressNotFoundException();
        return response;
    }

    @GetMapping("/address/tx/{address}")
    @ResponseBody
    public List<Transaction> getTransactions(@PathVariable String address,
                                             @RequestParam(value = "skip", required = false, defaultValue = "0") long skip,
                                             @RequestParam(value = "take", required = false, defaultValue = "32") long take,
                                             @RequestParam(value = "isdescending", required = false, defaultValue = "false") boolean isDescending) {
        return this.transactionAddressStore.getTransactionsForAddress(address, skip, take, isDescending);
    }


    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Address not found")  // 404
    public class AddressNotFoundException extends RuntimeException {
        // ...
    }
}
