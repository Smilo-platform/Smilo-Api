/*
 * Copyright (c) 2018 Smilo Platform B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smilo.api.block.data.transaction;

import io.smilo.commons.block.data.transaction.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionBuilder {

    public TransactionBuildCommand empty() {
        return new TransactionBuildCommand();
    }

    public class TransactionBuildCommand {

        private final Transaction transaction;

        public TransactionBuildCommand() {
            this.transaction = new Transaction();
        }

        public Transaction construct() {
            return transaction;
        }

    }

}
