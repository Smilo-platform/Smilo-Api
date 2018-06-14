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

import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lmdbjava.Env.create;

public class LMDBStore implements Store {

    private final ThreadLocal<Txn<ByteBuffer>> transaction = new ThreadLocal<>();

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
                .setMapSize(10_485_760)
                // LMDB also needs to know how many DBs (Dbi) we want to store in this Env.
                .setMaxDbs(5)
                // Now let's open the Env. The same path can be concurrently opened and
                // used in different processes, but do not open the same path twice in
                // the same process at the same time.
                .open(path);

    }

    public void initializeCollection(String collectionName) {
        getDatabase(collectionName);
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
        if (transaction.get() != null) {
            getDatabase(collection).put(transaction.get(), key, value);
        } else {
            getDatabase(collection).put(key, value);
        }
    }

    @Override
    // TODO: refactor
    public byte[] get(String collection, ByteBuffer key) {
        final ByteBuffer fetchedVal;
        if (transaction.get() != null) {
            getDatabase(collection).get(transaction.get(), key);
            fetchedVal = transaction.get().val();
        } else {
            try (Txn<ByteBuffer> txn = env.txnRead()) {
                getDatabase(collection).get(txn, key);
                fetchedVal = txn.val();
            }
        }
        //TODO: Make this beautiful
        byte[] bytes = new byte[fetchedVal.remaining()];

        for (int i = 0; i < fetchedVal.remaining(); i++) {
            bytes[i] = fetchedVal.get(i);
        }
        return bytes;
    }

    @Override
    public void delete(String collection, ByteBuffer key) {
        if (transaction.get() != null) {
            getDatabase(collection).delete(transaction.get(), key);
        } else {
            getDatabase(collection).delete(key);
        }
    }

    @Override
    public void startTransaction() {
        if (transaction.get() == null) {
            transaction.set(env.txnWrite());
        } else {
            throw new IllegalStateException("Transaction already active on this thread!");
        }
    }

    @Override
    public void commitTransaction() {
        transaction.get().commit();
        transaction.get().close();
        transaction.remove();
    }

    @Override
    public void rollback() {
        transaction.get().abort();
        transaction.get().close();
        transaction.remove();
    }

}
