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

import io.smilo.api.rest.models.Price;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("priceServices")
public class PriceServicesImpl implements PriceServices {

    private static List<Price> prices;

    static{
        prices = populateDummyPrices();
    }

    public List<Price> findAllPrices() {
        return prices;
    }

    public Price findByCurrencyFrom(String name) {
        for(Price price : prices){
            if(price.getCurrencyFrom().equalsIgnoreCase(name)){
                return price;
            }
        }
        return null;
    }

    private static List<Price> populateDummyPrices(){
        List<Price> prices = new ArrayList<>();
        prices.add(new Price("XSM","USD",0.25));
        prices.add(new Price("XSM","ETH",0.05));
        prices.add(new Price("XSM","BTC",0.005));
        prices.add(new Price("XSP","XSM",0.2));
        return prices;
    }

}
