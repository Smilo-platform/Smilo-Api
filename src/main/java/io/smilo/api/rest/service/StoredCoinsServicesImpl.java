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

package io.smilo.api.rest.service;

import io.smilo.api.rest.models.StoredCoin;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("storedCoinsServices")
public class StoredCoinsServicesImpl implements StoredCoinsServices {

    @Override
    public List<StoredCoin> findByPublicKey(String publicKey) {
        List<StoredCoin> storedCoins = new ArrayList<StoredCoin>();

        if (publicKey.equals("ETm9QUJLVdJkTqRojTNqswmeAQGaofojJJ")){
            storedCoins.add(new StoredCoin("XSM", 5712.00));
            storedCoins.add(new StoredCoin("XSP",234.00));

        } else if (publicKey.equals("ELsKCchf9rcGsufjRR62PG5Fn5dFinfgeN")) {
            storedCoins.add(new StoredCoin("XSM", 8122.00));
            storedCoins.add(new StoredCoin("XSP",634.00));

        } else if (publicKey.equals("EZ7tP3CBdBKrB9MaBgZNHyDcTg5TFRRpaY")) {
            storedCoins.add(new StoredCoin("XSM", 168234.00));
            storedCoins.add(new StoredCoin("XSP",2993.00));

        } else {
            storedCoins.add(new StoredCoin("XSM", 0.00));
            storedCoins.add(new StoredCoin("XSP",0.00));

        }
        return storedCoins;
    }
}
