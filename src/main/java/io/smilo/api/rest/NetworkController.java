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

package io.smilo.api.rest;

import io.smilo.api.rest.models.Node;
import io.smilo.api.rest.service.NetworkServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class NetworkController {

    @Autowired
    NetworkServices networkServices;

    /**
      *  Retrieve Networks with nodes
     **/

    @GetMapping("/network")
    public Map<String, Object> listNetworks() {
        return networkServices.getAll();
    }

    /**
      *  Retrieve Single Network with nodes
     **/

    @GetMapping("/network/{network}")
    public Map<String, ArrayList<Node>> listNetwork(@PathVariable("network") String network) {
        return networkServices.getNetwork(network);
    }

}
