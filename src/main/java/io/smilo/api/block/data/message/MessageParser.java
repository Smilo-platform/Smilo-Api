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

package io.smilo.api.block.data.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.api.address.AddressUtility;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.Parser;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MessageParser extends BlockDataParser implements Parser<Message> {

    private static final Logger LOGGER = Logger.getLogger(MessageParser.class);

    private final ObjectMapper dataMapper;
    private final AddressUtility addressUtility;

    public MessageParser(ObjectMapper dataMapper,
                         AddressUtility addressUtility) {
        this.dataMapper = dataMapper;
        this.addressUtility = addressUtility;
    }

    @Override
    public boolean isValid(Message message) {
        boolean isValid = true;
        try {
            if (!addressUtility.isAddressFormattedCorrectly(message.getInputAddress())) {
                LOGGER.error("Error validating message: input address " + message.getInputAddress() + " is misformatted.");
                isValid = false;
            }

            for (String outputAddress : message.getOutputAddresses()) {
                if (!addressUtility.isAddressFormattedCorrectly(outputAddress)) {
                    LOGGER.error("Error validating message: output address " + outputAddress + " is misformatted.");
                    isValid = false;
                }
            }

            //TODO: Check if message content is empty?
        } catch (Exception e) {
            // Likely an error parsing a Long or performing some String manipulation task. Maybe array bounds exceptions.
            LOGGER.error("Could not validate message ", e);
            isValid = false;

        }
        return isValid;
    }

    @Override
    public Message deserialize(byte[] raw) {
        Message message = null;
        try {
            MessageDTO dto = dataMapper.readValue(raw, MessageDTO.class);
            message = MessageDTO.toMessage(dto);
        } catch (IOException ex) {
            LOGGER.error("Unable to deserialize message", ex);
        }
        return message;
    }

    @Override
    public byte[] serialize(Message message) {
        byte[] bytes = null;
        try {
            bytes = dataMapper.writeValueAsBytes(MessageDTO.toDTO(message));
        } catch (IOException ex) {
            LOGGER.error("Unable to serialize message", ex);
        }
        return bytes;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Message.class.isAssignableFrom(clazz);
    }
}
