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

import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionDTO;
import io.smilo.api.rest.models.PostTransactionResult;
import io.smilo.api.rest.models.TransactionList;
import io.smilo.api.rest.service.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TxController {
    private final Logger LOGGER = Logger.getLogger(TxController.class);
    private final String COLLECTION_NAME = "TestCollection";

    @Autowired
    TransactionService transactionService;

    @GetMapping("/tx")
    @ResponseBody
    public TransactionList respondAllTxs(@RequestParam(value = "skip", required = true, defaultValue = "0") long skip,
                                         @RequestParam(value = "take", required = true, defaultValue = "32") long take,
                                         @RequestParam(value = "isdescending", required = true, defaultValue = "false") boolean isDescending) {
        return transactionService.getAll(skip, take, isDescending);
    }

    @RequestMapping(path = "/tx/{tx}")
    @ResponseBody
    public ResponseEntity<TransactionDTO> respondTx(@PathVariable("tx") String tx) {
        TransactionDTO transaction = transactionService.get(tx);

        return new ResponseEntity<>(transaction, transaction == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(path = "/tx", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<PostTransactionResult> respondPost(@RequestBody TransactionDTO transaction) {
        PostTransactionResult result = this.transactionService.putTransaction(transaction);

        return new ResponseEntity<>(result, result.getSucceeded() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}