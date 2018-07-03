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

package io.smilo.api.address;

import io.smilo.api.db.Store;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class provides all methods necessary to use an address after it has been generated.
 */
@Component
//TODO: Either remove all hash functions here or in the MerkleTreeGenerator and LamportThread: unnecessary duplication
public class AddressUtility {

    private static final Logger LOGGER = Logger.getLogger(AddressUtility.class);
    private static final Base32 base32 = new Base32();
    private MessageDigest md;

    /**
     * Constructor readies the MessageDigest md object to compute SHA-256 hashes.
     */
    public AddressUtility() {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            LOGGER.error("CRITICAL ERROR: NO SHA-256 SUPPORT! EXITING APPLICATION", e);
            System.exit(-1);
        }
    }

    /**
     * This method checks an address to ensure proper formatting. Smilo address format: Prefix + TreeRoot + VerificationHash Prefix can be S1, S2, S3, S4, or S5. And P1, P2, P3, P4 and P5 for private
     * addresses. S1 means 14 layer, S2 means 15 layer, S3 means 16 layer, S4 means 17 layer, S5 means 18 layer. TreeRoot is an all-caps Base32 32-character-long SHA256 hash that represents the top of
     * the Merkle Tree for the respective address. VerificationHash is the first four digits of the Base32 SHA256 hash of TreeRoot, also in caps.
     *
     * @param address The address to test for validity
     * @return boolean Whether the address is formatted correctly
     */
    public boolean isAddressFormattedCorrectly(String address) {
        try {
            String prefix = address.substring(0, 2); //Prefix is 2 characters long
            if (!prefix.equals("S1") && !prefix.equals("S2") && !prefix.equals("S3") && !prefix.equals("S4") && !prefix.equals("S5")) {
                return false;
            }
            LOGGER.trace("Address has correct prefix");
            String treeRoot = address.substring(2, 34); //32 characters long. Should be all-caps Base32
            String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"; //Normal Base32 character set. All upper case! Omission of 1 is normal. :)
            for (int i = 0; i < treeRoot.length(); i++) {
                if (!characterSet.contains(treeRoot.charAt(i) + "")) {
                    LOGGER.error("Address not valid!");
                    return false;
                }
            }
            LOGGER.trace("Address has correct characterset");
            String givenEnding = address.substring(34); //Characters 34 to 37 should be all that's left. Remember we start counting at 0.
            String correctEnding = SHA256ReturnBase32(prefix + treeRoot).substring(0, 4); //First four characters of Base32-formatted SHA256 of treeRoot
            if (!correctEnding.equals(givenEnding)) {
                LOGGER.debug("Address has incorrect ending, should be " + correctEnding + " instead of " + givenEnding);
                return false;
            }
            return true; //We didn't return false for a failure, it must be valid!
        } catch (Exception e) { //Not printing exceptions or logging them on purpose. Any time an address too short is passed in, this will snag it.
            return false;
        }
    }

    /**
     * Used for the generation of an address. Base32 is more practical for real-world addresses, due to a more convenient ASCII charset. Shortened to 32 characters, as that provides
     * 32^32=1,461,501,637,330,902,918,203,684,832,716,283,019,655,932,542,976 possible addresses.
     *
     * @param toHash The String to hash using SHA256
     * @return String the base32-encoded String representing the entire SHA256 hash of toHash
     */
    private String SHA256ReturnBase32(String toHash) {
        try {
            return base32.encodeAsString(md.digest(toHash.getBytes(UTF_8))).substring(0, 32);
        } catch (Exception e) {
            LOGGER.error("Unable to create base32 encoded SHA256 hash", e);
        }
        return null;
    }
}
