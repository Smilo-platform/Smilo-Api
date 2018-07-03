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

package io.smilo.api.block;

import io.smilo.api.block.data.Parser;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParserProvider  {

    private static final Logger LOGGER = Logger.getLogger(ParserProvider.class);

    private final List<Parser> providers;

    public ParserProvider(List<Parser> providers) {
        this.providers = providers;
    }

    public Parser getParser(Class<?> clazz) {
        return providers.stream().filter(p -> p.supports(clazz)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unable to find matching parser!"));
    }
}
