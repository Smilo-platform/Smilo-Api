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

package io.smilo.api.block;

import io.smilo.api.block.data.transaction.TransactionDTO;
import io.smilo.commons.block.Block;

import java.util.List;
import java.util.stream.Collectors;

public class BlockDTO {

    private static final int VERSION = 1;

    private long timestamp;
    private long blockNum;
    private String previousBlockHash;
    private String ledgerHash;
    private List<TransactionDTO> transactions;
    private String blockHash;
    private String nodeSignature;
    private long nodeSignatureIndex;
    private String redeemAddress;

    public BlockDTO() {
        // Make the sonar scanner happy!
    }

    public static int getVERSION() {
        return VERSION;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(long blockNum) {
        this.blockNum = blockNum;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public String getLedgerHash() {
        return ledgerHash;
    }

    public void setLedgerHash(String ledgerHash) {
        this.ledgerHash = ledgerHash;
    }

    public void setTransactions(List<TransactionDTO> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionDTO> getTransactions() {
        return transactions;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getNodeSignature() {
        return nodeSignature;
    }

    public void setNodeSignature(String nodeSignature) {
        this.nodeSignature = nodeSignature;
    }

    public long getNodeSignatureIndex() {
        return nodeSignatureIndex;
    }

    public void setNodeSignatureIndex(long nodeSignatureIndex) {
        this.nodeSignatureIndex = nodeSignatureIndex;
    }

    public String getRedeemAddress() {
        return redeemAddress;
    }

    public void setRedeemAddress(String redeemAddress) {
        this.redeemAddress = redeemAddress;
    }

    public static BlockDTO toDTO(Block block) {
        BlockDTO dto = new BlockDTO();
        if (block == null) {
            return dto;
        }
        dto.setTimestamp(block.getTimestamp());
        dto.setBlockNum(block.getBlockNum());
        dto.setPreviousBlockHash(block.getPreviousBlockHash());
        dto.setLedgerHash(block.getLedgerHash());
        dto.setTransactions(block.getTransactions().stream().map(TransactionDTO::toDTO).collect(Collectors.toList()));
        dto.setNodeSignature(block.getNodeSignature());
        dto.setNodeSignatureIndex(block.getNodeSignatureIndex());
        dto.setRedeemAddress(block.getRedeemAddress());
        dto.setBlockHash(block.getBlockHash());
        return dto;
    }

    public static Block toBlock(BlockDTO blockDTO) {
        Block block = new Block();
        if(blockDTO == null) {
            return block;
        }
        block.setTimestamp(blockDTO.getTimestamp());
        block.setBlockNum(blockDTO.getBlockNum());
        block.setPreviousBlockHash(blockDTO.getPreviousBlockHash());
        block.setLedgerHash(blockDTO.getLedgerHash());
        block.setTransactions(blockDTO.getTransactions().stream().map(TransactionDTO::toTransaction).collect(Collectors.toList()));
        block.setNodeSignature(blockDTO.getNodeSignature());
        block.setNodeSignatureIndex(blockDTO.getNodeSignatureIndex());
        block.setRedeemAddress(blockDTO.getRedeemAddress());
        block.setBlockHash(blockDTO.getBlockHash());
        return block;
    }
}
