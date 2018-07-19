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

package io.smilo.api.cache;

import io.smilo.api.block.data.transaction.Transaction;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BlockDataCache {

    private static final Map<String, Transaction> transactions = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(BlockDataCache.class);

    public Map<String, Transaction> getTransactions(){
        return transactions;
    }

    public Boolean isDuplicate(Transaction transaction){
        if(transactions.containsKey(transaction.getDataHash())){
            return true;
        }
        return false;
    }

    public void addTransaction(Transaction transaction){
        // Store latest 100 blocks
        transactions.put(transaction.getDataHash(), transaction);
//        while(transactions.size() >= 101){
//            // delete iets TODO!
//        }
    }
}
