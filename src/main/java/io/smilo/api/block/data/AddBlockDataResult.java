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

import io.smilo.commons.block.AddResultType;
import io.smilo.commons.block.data.BlockData;

public class AddBlockDataResult {
    private final AddResultType type;
    private final BlockData blockData;
    private final String message;

    public AddBlockDataResult(BlockData blockData, AddResultType type, String message) {
        this.type = type;
        this.message = message;
        this.blockData = blockData;
    }

    public AddResultType getType() {
        return type;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public String getMessage() {
        return message;
    }
}
