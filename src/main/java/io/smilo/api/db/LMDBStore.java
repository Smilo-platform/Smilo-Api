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

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
                .setMapSize(1_048_576 * 1_024L * 1_024L) // 1 TB?
                // LMDB also needs to know how many DBs (Dbi) we want to store in this Env.
                .setMaxDbs(5)
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
        byte[] bytes = new byte[fetchedVal.remaining()];

        fetchedVal.get(bytes);
        return bytes;
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
        byte[] bytes = new byte[fetchedVal.remaining()];
        for (int i = 0; i < fetchedVal.remaining(); i++) {
            bytes[i] = fetchedVal.get(i);
        }
        return bytes;
    }

    @Override
    public Map<String,String> getAll(String collection) {
        try (Txn<ByteBuffer> txn = env.txnRead();
             CursorIterator<ByteBuffer> cursor = getDatabase(collection).iterate(txn)) {

            Map<String, String> result = new HashMap<>();
            cursor.forEachRemaining(x -> { result.put(StandardCharsets.UTF_8.decode(x.key()).toString(), StandardCharsets.UTF_8.decode(x.val()).toString()); });

            return result;
        }
    }

    @Override
    public void initializeCollection(String collectionName) {
        getDatabase(collectionName);
    }

}
