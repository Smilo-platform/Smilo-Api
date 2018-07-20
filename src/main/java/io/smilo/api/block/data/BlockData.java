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

package io.smilo.api.block.data;

import io.smilo.api.block.Content;

public abstract class BlockData extends Content {

    private String inputAddress;
    private Long fee;
    private String signatureData;
    private Long signatureIndex;
    private String dataHash;

    protected BlockData () {}

    protected BlockData(Long timestamp, String inputAddress, Long fee, String signatureData, Long signatureIndex, String dataHash) {
        super(timestamp);
        this.inputAddress = inputAddress;
        this.fee = fee;
        this.signatureData = signatureData;
        this.signatureIndex = signatureIndex;
        this.dataHash = dataHash;
    }

    public String getInputAddress() {
        return inputAddress;
    }

    public Long getFee() {
        return fee;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public Long getSignatureIndex() {
        return signatureIndex;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setInputAddress(String inputAddress) {
        this.inputAddress = inputAddress;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public void setSignatureIndex(Long signatureIndex) {
        this.signatureIndex = signatureIndex;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }



}

