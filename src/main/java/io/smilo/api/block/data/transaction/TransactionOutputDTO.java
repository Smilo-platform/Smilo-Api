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

package io.smilo.api.block.data.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.smilo.api.rest.serializers.BigIntegerSerializer;
import io.smilo.commons.block.data.transaction.TransactionOutput;

import java.math.BigInteger;

public class TransactionOutputDTO {

    private String outputAddress;
    @JsonSerialize(using = BigIntegerSerializer.class)
    private BigInteger outputAmount;

    public TransactionOutputDTO() {
        // Make sonar happy.. :)
    }

    public void setOutputAddress(String outputAddress) {
        this.outputAddress = outputAddress;
    }

    public void setOutputAmount(BigInteger outputAmount) {
        this.outputAmount = outputAmount;
    }

    public String getOutputAddress() {
        return outputAddress;
    }

    public BigInteger getOutputAmount() {
        return outputAmount;
    }

    public static TransactionOutputDTO toDTO(TransactionOutput transactionOutput) {
        TransactionOutputDTO dto = new TransactionOutputDTO();
        dto.setOutputAddress(transactionOutput.getOutputAddress());
        dto.setOutputAmount(transactionOutput.getOutputAmount());
        return dto;
    }

    public static TransactionOutput toTransactionOutput(TransactionOutputDTO transactionOutputDTO) {
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setOutputAddress(transactionOutputDTO.getOutputAddress());
        transactionOutput.setOutputAmount(transactionOutputDTO.getOutputAmount());
        return transactionOutput;
    }
}

