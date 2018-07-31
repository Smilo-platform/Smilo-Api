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

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionDTO {

    private static final int VERSION = 1;

    private String assetId;
    @JsonSerialize(using = BigIntegerSerializer.class)
    private BigInteger inputAmount;
    private List<TransactionOutputDTO> transactionOutputs;
    private Long timestamp;
    private String inputAddress;
    @JsonSerialize(using = BigIntegerSerializer.class)
    private BigInteger fee;
    private String signatureData;
    private Long signatureIndex;
    private String dataHash;
    private long blockIndex = -1;

    public TransactionDTO () {
        // Make sonar happy :)
    }

    public int getVERSION() {
        return VERSION;
    }

    public long getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(long blockIndex) {
        this.blockIndex = blockIndex;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public BigInteger getInputAmount() {
        return inputAmount;
    }

    public void setInputAmount(BigInteger inputAmount) {
        this.inputAmount = inputAmount;
    }

    public List<TransactionOutputDTO> getTransactionOutputs() {
        return transactionOutputs;
    }

    public void setTransactionOutputs(List<TransactionOutputDTO> transactionOutputs) {
        this.transactionOutputs = transactionOutputs;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getInputAddress() {
        return inputAddress;
    }

    public void setInputAddress(String inputAddress) {
        this.inputAddress = inputAddress;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public Long getSignatureIndex() {
        return signatureIndex;
    }

    public void setSignatureIndex(Long signatureIndex) {
        this.signatureIndex = signatureIndex;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }

    public static TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAssetId(transaction.getAssetId());
        dto.setInputAmount(transaction.getInputAmount());
        dto.setTransactionOutputs(transaction.getTransactionOutputs().stream().map(TransactionOutputDTO::toDTO).collect(Collectors.toList()));
        dto.setTimestamp(transaction.getTimestamp());
        dto.setInputAddress(transaction.getInputAddress());
        dto.setFee(transaction.getFee());
        dto.setSignatureData(transaction.getSignatureData());
        dto.setSignatureIndex(transaction.getSignatureIndex());
        dto.setDataHash(transaction.getDataHash());
        return dto;
    }

    public static Transaction toTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setAssetId(transactionDTO.getAssetId());
        transaction.setInputAmount(transactionDTO.getInputAmount());
        transaction.setTransactionOutputs(transactionDTO.getTransactionOutputs().stream().map(TransactionOutputDTO::toTransactionOutput).collect(Collectors.toList()));
        transaction.setTimestamp(transactionDTO.getTimestamp());
        transaction.setInputAddress(transactionDTO.getInputAddress());
        transaction.setFee(transactionDTO.getFee());
        transaction.setSignatureData(transactionDTO.getSignatureData());
        transaction.setSignatureIndex(transactionDTO.getSignatureIndex());
        transaction.setDataHash(transactionDTO.getDataHash());
        return transaction;
    }
}

