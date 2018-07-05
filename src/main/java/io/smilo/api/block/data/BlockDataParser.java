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

import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.Base64;

public abstract class BlockDataParser {

    private static final Logger LOGGER = Logger.getLogger(BlockDataParser.class);

    protected String generateDataHash(byte[] blockData) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return DatatypeConverter.printHexBinary(md.digest(blockData));
        } catch (Exception e) {
            LOGGER.error("Oops: " + e);
            return null;
        }
    }

    public static String encode(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    public static byte[] decode(String toDecode) {
        return Base64.getDecoder().decode(toDecode);
    }
}
