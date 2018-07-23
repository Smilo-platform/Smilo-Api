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

package io.smilo.api.db;

import org.lmdbjava.*;
import org.springframework.boot.liquibase.CommonsLoggingLiquibaseLogger;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lmdbjava.Env.create;

public class LMDBStore implements Store {

    private final Map<String, Dbi<ByteBuffer>> dbs = new HashMap<>();
    private final Env<ByteBuffer> env;

    public LMDBStore(String folder) {
        final File path = new File(folder);

        if (!path.exists()) {
            path.mkdirs();
        }

        // We always need an Env. An Env owns a physical on-disk storage file. One
        // Env can store many different databases (ie sorted maps).
        // TODO: review env settings
        this.env = create()
                // LMDB also needs to know how large our DB might be. Over-estimating is OK.
                //.setMapSize(1_048_576 * 1_024L * 1_024L) // 1 TB? // TODO enable 1TB
                .setMapSize(1_048_576 * 1_024L)
                // LMDB also needs to know how many DBs (Dbi) we want to store in this Env.
                .setMaxDbs(10)
                .setMaxReaders(100)
                // Now let's open the Env. The same path can be concurrently opened and
                // used in different processes, but do not open the same path twice in
                // the same process at the same time.
                .open(path);

    }

    @Override
    public void clear(String collectionName) {
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            getDatabase(collectionName).drop(txn);
            txn.commit();
        }
    }

    @Override
    public Long getEntries(String collectionName) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return getDatabase(collectionName).stat(txn).entries;
        }
    }

    private Dbi<ByteBuffer> getDatabase(String collectionName) {
        if (!dbs.containsKey(collectionName)) {
            // We need a Dbi for each DB. A Dbi roughly equates to a sorted map. The
            // MDB_CREATE flag causes the DB to be created if it doesn't already exist.
            Dbi<ByteBuffer> db = env.openDbi(collectionName, DbiFlags.MDB_CREATE);
            dbs.put(collectionName, db);
        }

        return dbs.get(collectionName);
    }

    @Override
    public void put(String collection, ByteBuffer key, ByteBuffer value) {
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            getDatabase(collection).put(txn, key, value);
            txn.commit();
        }
    }

    @Override
    public byte[] get(String collection, ByteBuffer key) {
        final ByteBuffer fetchedVal;
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            getDatabase(collection).get(txn, key);
            fetchedVal = txn.val();
        }
        if(fetchedVal == null) return null;

        return toByteArray(fetchedVal);
    }

    @Override
    public List<byte[]> getArray(String collection, String key, long skip, long take, boolean isDescending) {
        List<byte[]> result = new ArrayList<>();

        try(Txn<ByteBuffer> txn = env.txnRead()) {
            final Dbi<ByteBuffer> db = getDatabase(collection);

            // First retrieve count
            ByteBuffer countBuffer = db.get(txn, toByteBuffer(key));
            if (countBuffer != null) {
                // Key exists
                long count = countBuffer.getLong();

                long startIndex = isDescending ? (count - 1) - skip : skip;
                long endIndex = isDescending ? count - skip - take : skip + take;
                long indexIncrement = isDescending ? -1 : 1;
                for (long i = startIndex; i != endIndex && i >= 0; i += indexIncrement) {
                    String retrieveKey = key + i;

                    ByteBuffer valueBuffer = db.get(txn, toByteBuffer(retrieveKey));

                    if (valueBuffer != null) {
                        result.add(toByteArray(valueBuffer));
                    } else {
                        // Value could not be found, it means we found the end of the array
                        break;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void addToArray(String collection, String key, ByteBuffer value) {
        try(Txn<ByteBuffer> readTxn = env.txnRead()) {
            Dbi<ByteBuffer> db = getDatabase(collection);

            ByteBuffer arrayLengthBuffer = db.get(readTxn, toByteBuffer(key));

            long arrayLength;
            if(arrayLengthBuffer != null) {
                arrayLength = arrayLengthBuffer.getLong();
            }
            else {
                // Array does not exist yet
                arrayLength = 0;
            }

            long nextElementId = arrayLength + 1;

            // Update array count
            ByteBuffer arrayHeaderBuffer = ByteBuffer.allocateDirect(64);
            arrayHeaderBuffer.putLong(nextElementId);
            arrayHeaderBuffer.flip();

            db.put(toByteBuffer(key), arrayHeaderBuffer);

            // Write element
            db.put(toByteBuffer(key + arrayLength), value);
        }
    }

    @Override
    public byte[] last(String collection) {
        final ByteBuffer fetchedVal;
        try (Txn<ByteBuffer> txn = env.txnRead()) {

            final Cursor<ByteBuffer> cursor = getDatabase(collection).openCursor(txn);
            cursor.seek(SeekOp.MDB_LAST);
            try {
                getDatabase(collection).get(txn, cursor.key());
                fetchedVal = txn.val();
            } catch(Exception e) {
                return null;
            }
        }

        return toByteArray(fetchedVal);
    }

    @Override
    public Map<String,String> getAll(String collection) {
        try (Txn<ByteBuffer> txn = env.txnRead();
             CursorIterator<ByteBuffer> cursor = getDatabase(collection).iterate(txn)) {

            Map<String, String> result = new HashMap<>();
            cursor.forEachRemaining(x -> result.put(StandardCharsets.UTF_8.decode(x.key()).toString(), StandardCharsets.UTF_8.decode(x.val()).toString()));

            return result;
        }
    }

    @Override
    public List<byte[]> getAll(String collection, long skip, long take, boolean isDescending) {
        List<byte[]> result = new ArrayList<>();

        try(Txn<ByteBuffer> txn = env.txnRead()) {
            Dbi<ByteBuffer> db = getDatabase(collection);

            // Get the amount of elements
            long count = db.stat(txn).entries;

            // Do some quick checks to prevent reading 'out of bounds'.
            long baseIndex = isDescending ? count - 1 : 0;
            long startIndex = isDescending ? baseIndex - skip : baseIndex + skip;
            long endIndex = isDescending ? startIndex - take : startIndex + take;

            if(isDescending) {
                if(startIndex < 0)
                    return result; // Nothing to read
                if(endIndex < 0) {
                    // Adjust take
                    take += endIndex + 1;
                }
            }
            else {
                if(startIndex >= count)
                    return result; // Nothing to read
                if(endIndex >= count) {
                    // Adjust take
                    take -= (endIndex - count);
                }
            }

            Cursor<ByteBuffer> cursor = getDatabase(collection).openCursor(txn);

            // Move to back if reading in descending order
            cursor.seek(isDescending ? SeekOp.MDB_LAST : SeekOp.MDB_FIRST);

            // Skip
            for (long i = 0; i < skip; i++) {
                if (isDescending)
                    cursor.prev();
                else
                    cursor.next();
            }

            // Take
            for (long i = 0; i < take; i++) {
                result.add(toByteArray(cursor.val()));

                if (isDescending)
                    cursor.prev();
                else
                    cursor.next();
            }
        }

        return result;
    }

    @Override
    public void initializeCollection(String collectionName) {
        getDatabase(collectionName);
    }

    private byte[] toByteArray(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];

        buffer.get(bytes);

        return bytes;
    }

    private ByteBuffer toByteBuffer(String value) {
        byte[] bytes = value.getBytes();

        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);

        buffer.put(bytes).flip();

        return buffer;
    }

}
