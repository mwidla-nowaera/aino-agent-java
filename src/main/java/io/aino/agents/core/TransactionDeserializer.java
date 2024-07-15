package io.aino.agents.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

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
        Map<String, List<String>> ids = new HashMap<>();
        if (idsNode.isArray() && idsNode.size() == 0) {
            transaction.setIds(ids);
        } else {
            ids = jp.getCodec().treeToValue(idsNode, Map.class);
            transaction.setIds(ids);
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

