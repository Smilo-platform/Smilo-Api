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

import io.smilo.api.rest.models.Balance;
import io.smilo.api.rest.models.StoredCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("balanceServices")
public class BalanceServicesImpl implements BalanceServices {

    @Autowired
    StoredCoinsServices storedCoinsServices;

    private static String publicKey;
    private static List<Balance> balances;

    @Override
    public Balance findByPublicKey(String publicKey) {
        List<StoredCoin> storedCoins = new ArrayList<>();

        storedCoins = storedCoinsServices.findByPublicKey(publicKey);
        Balance balances = new Balance(publicKey, (ArrayList<StoredCoin>) storedCoins);
        return balances;
    }
}
