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

import java.nio.ByteBuffer;
import java.util.Map;

public interface Store {

    void put(String collection, ByteBuffer key, ByteBuffer value);

    byte[] get(String collection, ByteBuffer key);

    Map<String,String> getAll(String collection);

    byte[] last(String collection);

    void initializeCollection(String collectionName);

    void clear(String collectionName);

    Long getEntries(String collectionName);

}
