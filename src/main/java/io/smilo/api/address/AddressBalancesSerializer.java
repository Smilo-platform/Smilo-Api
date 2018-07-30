package io.smilo.api.address;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public class AddressBalancesSerializer extends JsonSerializer<Map<String, BigInteger>> {
    @Override
    public void serialize(Map<String, BigInteger> stringBigIntegerMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        for(Map.Entry<String, BigInteger> pair : stringBigIntegerMap.entrySet()) {
            jsonGenerator.writeFieldName(pair.getKey());
            jsonGenerator.writeString(pair.getValue().toString());
        }

        jsonGenerator.writeEndObject();
    }
}
