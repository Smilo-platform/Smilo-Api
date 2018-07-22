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

package io.smilo.api.cache;

import io.smilo.api.block.Block;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BlockCache {
    private static final Map<Long, Block> blocks = new LinkedHashMap<>();

    public Map<Long, Block> getBlocks(){
        return blocks;
    }

    public Boolean isDuplicate(Block block){
        if(blocks.containsKey(block.getBlockNum())){
            return true;
        }
        return false;
    }

    public void addBlock(Block block){
        // Store latest 100 blocks
        if (!isDuplicate(block)){
            blocks.put(block.getBlockNum(), block);
            while(blocks.size() > 100){
                blocks.remove(blocks.entrySet().iterator().next().getKey());
            }
        }
    }

}

