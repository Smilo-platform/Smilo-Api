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

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class provides all methods necessary to use an address after it has been generated.
 */
@Component
//TODO: Either remove all hash functions here or in the MerkleTreeGenerator and LamportThread: unnecessary duplication
public class AddressUtility {

    private static final Logger LOGGER = Logger.getLogger(AddressUtility.class);
    private static final String CS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; //Character set used in Lamport Private Key Parts
    private static final Base32 base32 = new Base32();
    private static final Base64 base64 = new Base64();
    private MessageDigest md;
    private MessageDigest md512;

    private final Function<String, String> hashShort;
    private final Function<String, String> hash512;

    /**
     * Constructor readies the MessageDigest md object to compute SHA-256 hashes, and ensures existance of address folder for storing the Merkle Trees. Also checks for availability of SHA1PRNG.
     */
    public AddressUtility() {
        try {
            SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception e) {
            LOGGER.error("CRITICAL ERROR: NO SHA1PRNG SUPPORT! EXITING APPLICATION", e);
        }
        try {
            md = MessageDigest.getInstance("SHA-256");
            md512 = MessageDigest.getInstance("SHA-512");
        } catch (Exception e) {
            LOGGER.error("CRITICAL ERROR: NO SHA-256 SUPPORT! EXITING APPLICATION", e);
            System.exit(-1);
        }

        hashShort = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return SHA256Short(s);
            }
        };
        hash512 = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return SHA512(s);
            }
        };
    }

    /**
     * This method will verify that the supplied address signed the supplied message to generate the supplied signature.
     *
     * @param message   The message of which to verify the signature
     * @param signature The signature to verify
     * @param address   The address to check the signature against
     * @param index     The index of the Lamport Keypair used (position on bottom of Merkle tree)
     * @return boolean Whether the message was signed by the provided address using the provided index
     */
    public boolean verifyMerkleSignature(String message, String signature, String address, long index) {
        try {
            String lamportSignature = signature.substring(0, signature.indexOf(","));
            String merkleAuthPath = signature.substring(signature.indexOf(",") + 1);

            String[] lamportPublicKey = buildPublicKeyFromLamportSignature(message, lamportSignature);


            String leafStart = getLeafNode(lamportPublicKey);

            //Split on : in order to get the auth paths into a String array
            String[] merkleAuthPathComponents = merkleAuthPath.split(":");

            //Address matches, so signature is legitimate!
            if (checkMerkelPathAuthentication(address, index, leafStart, merkleAuthPathComponents)) {
                return true;
            }

        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("Failure: Incorrect MerkleSignature");
        } catch (NullPointerException e) {
            LOGGER.error("Failure: No MerkleSignature available");
        }catch (Exception e) {
            LOGGER.error("Failed to verify MerkleSignature", e);
        }
        //Fell through at some point, likely the address didn't match
        return false;
    }

    private String[] buildPublicKeyFromLamportSignature(String message, String lamportSignature) {
        //Holds 100 pairs, each pair containing one public and one private Lamport Key part
        String[] lamportSignaturePairs = lamportSignature.split("::");
        String[] lamportPublicKey = new String[200];

        //Lamport Signatures work with binary, so we need a binary string representing the hash of the message we want verify the signature of
        //Smilo Lamport Signatures sign the first 100 bytes of the hash. To generate a message colliding with the signature, one would need on average 2^99 tries
        final String binaryToCheck = SHA256Binary(message).substring(0, 100);

        int i;
        for (i = 0; i < 99; i++) {
            String [] items = applyHashOnPrivateKey(binaryToCheck.charAt(i), lamportSignaturePairs[i].split(":"), hashShort);
            lamportPublicKey[2*i] = items[0];
            lamportPublicKey[2*i+1] = items[1];
        }

        String [] items = applyHashOnPrivateKey(binaryToCheck.charAt(i), lamportSignaturePairs[i].split(":"), hash512);
        lamportPublicKey[2*i] = items[0];
        lamportPublicKey[2*i+1] = items[1];

        return lamportPublicKey;
    }

    private String[] applyHashOnPrivateKey(char binary, String[] pair, Function<String,String> hash) {
        if (binary == '1') {
            pair[1] = hash.apply(pair[1]);
        } else {
            pair[0] = hash.apply(pair[0]);
        }
        return pair;
    }

    private boolean checkMerkelPathAuthentication(String address, long index, String leafStart, String[] merkleAuthPathComponents) {
        //This position variable will store where on the tree we are. Important for order of concatenation: rollingHash first, or Component first
        long position = index;
        //This rollingHash will contain the hash as we calculate up the hash tree
        String rollingHash;
        if (position % 2 == 0) { //Even; rollingHash goes first
            rollingHash = SHA256(leafStart + merkleAuthPathComponents[0]);
        } else { //Odd; path component should go first
            rollingHash = SHA256(merkleAuthPathComponents[0] + leafStart);
        }
        position /= 2;
        for (int i = 1; i < merkleAuthPathComponents.length - 1; i++) { //Go to merkleAuthPathComponents.length - 1 because the final hash is returned in base32 and is truncated
            //Combine the current hash with the next component, which visually would lie on the same Merkle Tree layer
            if (position % 2 == 0) { //Even; rollingHash goes first
                rollingHash = SHA256(rollingHash + merkleAuthPathComponents[i]);
            } else { //Odd; path component should go first
                rollingHash = SHA256(merkleAuthPathComponents[i] + rollingHash);
            }
            LOGGER.debug("rollingHash: " + rollingHash + " and auth component: " + merkleAuthPathComponents[i]);
            position /= 2;
        }
        //Final hash, done differently for formatting of address (base32, set length of 32 characters for the top of the Merkle Tree)
        if (position % 2 == 0) { //Even; rollingHash goes first
            rollingHash = SHA256ReturnBase32(rollingHash + merkleAuthPathComponents[merkleAuthPathComponents.length - 1]);
        } else { //Odd; path component should go first
            rollingHash = SHA256ReturnBase32(merkleAuthPathComponents[merkleAuthPathComponents.length - 1] + rollingHash);
        }

        if (address.substring(2, address.length() - 4).equals(rollingHash)) { //Remove the prefix and hash suffix
            return true;
        }
        return false;
    }

    private String getLeafNode(String[] lamportPublicKey) {
        String lamportPublicSignatureFull = "";
        //Populate full String to hash to get first leaf component
        for (int i = 0; i < lamportPublicKey.length; i++) {
            lamportPublicSignatureFull += lamportPublicKey[i];
        }
        LOGGER.debug("lmpSig: " + lamportPublicSignatureFull);
        //First leaf component; bottom layer of Merkle Tree
        return SHA256(lamportPublicSignatureFull);
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
     * This SHA512 function returns the full-length SHA512 hash of the String toHash. SHA512 is used for the last 2 elements of the Lamport Signature, in order to require any attacker to break one
     * SHA512 hash if they were to crack a Lamport Public Key.
     *
     * @param toHash The String to hash using SHA512
     * @return String the 128-character base64 String resulting from hashing toHash
     */
    private String SHA512(String toHash) {
        try {
            return base64.encodeAsString(md512.digest(toHash.getBytes(UTF_8)));
        } catch (Exception e) {
            LOGGER.error("Unable to create SHA512 to hash", e);
        }
        return null;
    }

    /**
     * This SHA256 function returns a 16-character, base64 String. The String is shortened to reduce space on the smiloChain, and is sufficiently long for security purposes.
     *
     * @param toHash The String to hash using SHA256
     * @return String The 16-character base64 String resulting from hashing toHash and truncating
     */
    private String SHA256Short(String toHash) //Each hash is shortened to 16 characters based on a 64-character charset. 64^16=79,228,162,514,264,337,593,543,950,336 (Aka more than enough for Lamport)
    {
        try {
            return base64.encodeAsString(md.digest(toHash.getBytes(UTF_8))).substring(0, 16);
        } catch (Exception e) {
            LOGGER.error("Unable to create SHA256Short hash", e);
        }
        return null;
    }

    /**
     * This SHA256 function returns a 256-character binary String representing the full SHA256 hash of the String toHash This binary String is useful when signing a message with a Lamport Signature.
     *
     * @param toHash The String to hash using SHA256
     * @return String The binary String representing the entire SHA256 hash of toHash
     */
    private String SHA256Binary(String toHash) {
        try {
            byte[] messageHash = md.digest(toHash.getBytes(UTF_8));
            return new BigInteger(1, messageHash).toString(2);
        } catch (Exception e) {
            LOGGER.error("Unable to create SHA256 binary hash", e);
        }
        return null;
    }

    /**
     * This SHA256 function returns a base64 String repesenting the full SHA256 hash of the String toHash The full-length SHA256 hashes are used for the non-Lamport and non-Address levels of the
     * Merkle Tree
     *
     * @param toHash The String to hash using SHA256
     * @return String the base64 String representing the entire SHA256 hash of toHash
     */
    private String SHA256(String toHash) {
        try {
            return base64.encodeAsString(md.digest(toHash.getBytes(UTF_8)));
        } catch (Exception e) {
            LOGGER.error("Unable to create SHA256 hash", e);
        }
        return null;
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
