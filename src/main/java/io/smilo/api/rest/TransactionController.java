package io.smilo.api.rest;

import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionDTO;
import io.smilo.api.rest.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @RequestMapping(path = "/transaction", method = RequestMethod.POST)
    @ResponseBody
    public TransactionDTO respondPost(@RequestBody TransactionDTO transactionDTO) {
        return TransactionDTO.toDTO(
                this.transactionService.putTransaction(
                    TransactionDTO.toTransaction(transactionDTO)
            )
        );
    }
}
