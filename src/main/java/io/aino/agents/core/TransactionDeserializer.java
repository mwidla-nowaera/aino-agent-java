package io.aino.agents.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDeserializer extends JsonDeserializer<Transaction> {

    @Override
    public Transaction deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();

        Transaction transaction = new Transaction();
        transaction.setToKey(node.get("to").asText());
        transaction.setFromKey(node.get("from").asText());
        transaction.setOperationKey(node.get("operation").asText());
        transaction.setPayloadTypeKey(node.get("payloadType").asText());
        transaction.setMessage(node.get("message").asText());
        transaction.setStatus(node.get("status").asText());
        transaction.setFlowId(node.get("flowId").asText());
        transaction.setTimestamp(node.get("timestamp").asLong());

        JsonNode idsNode = node.get("ids");
        if (idsNode != null && idsNode.isArray()) {
            Map<String, List<String>> idsMap = new HashMap<>();
            for (JsonNode idNode : idsNode) {
                String idType = idNode.get("idType").asText();
                List<String> values = mapper.convertValue(idNode.get("values"), new TypeReference<List<String>>() {});
                idsMap.put(idType, values);
            }
            transaction.setIds(idsMap);
        }
        JsonNode metadataNode = node.get("metadata");
        List<NameValuePair> metadata = new ArrayList<>();
        if (metadataNode.isArray()) {
            for (JsonNode metadataItem : metadataNode) {
                String key = metadataItem.get("name").asText();
                String value = metadataItem.get("value").asText();
                metadata.add(new NameValuePair(key, value));
            }
        }
        transaction.setMetadata(metadata);

        return transaction;
    }
}
