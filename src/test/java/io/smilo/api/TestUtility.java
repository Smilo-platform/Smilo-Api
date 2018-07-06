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

package io.smilo.api;

import io.smilo.api.address.AddressManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TestUtility {

    @Value("${WALLET_FILE:wallet_test.keys}")
    private String walletFileName;

    @Value("${DB_FOLDER:database_test}")
    private String databaseDirName;

    @Autowired
    private AddressManager addressManager;

    /**
     * Initialize the environment. Some classes need to be reset to their original state after every test.
     */
    public void initialize() {
        // TODO: incomplete; block store needs to be regenerated as well
        cleanUp();
        createFolders();
    }

    /**
     * Clean up the mess we made while trying to break the application with our tests
     */
    public void cleanUp() {
        deleteDir(new File(walletFileName));
        deleteDir(new File(databaseDirName));
    }

    private void createFolders() {
        new File(walletFileName);
        new File(databaseDirName).mkdirs();
    }

    private void deleteDir(File file) {
        if (file.exists()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteDir(f);
                }
            }
            file.delete();
        }
    }

}
