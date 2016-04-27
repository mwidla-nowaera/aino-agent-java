/*
 *  Copyright 2016 Aino.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.aino.agents.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This class exists for convenient JSON serialization (by Jackson) of log
 * messages.
 * Should not be used directly. Use {@link Transaction} instead.
 */
public class TransactionSerializable {

    /**
     * Creates TransactionSerializable from Transaction.
     *
     * @param entry log entry to create from
     * @return created TransactionSerializable
     */
    public static TransactionSerializable from(Transaction entry) {
        TransactionSerializable obj = new TransactionSerializable();

        for(Transaction.Field field : Transaction.Field.values()) {
            obj.addField(field.getFieldName(), entry.getFieldValue(field));
        }

        for(Map.Entry<String, List<String>> idList : entry.getIds().entrySet()) {
            IdList list = obj.addIdType(entry.getIdTypeName(idList.getKey()));
            list.addIds(idList.getValue());
        }

        obj.setMetadata(entry.getMetadata());

        return obj;
    }

    /**
     *
     */
    public class IdList {
        private String idType;
        private final List<String> values = new LinkedList<String>();

        private IdList() {
        }

        public String getIdType() {
            return idType;
        }

        public void setIdType(String idType) {
            this.idType = idType;
        }

        public void addId(String id) {
            values.add(id);
        }

        public void addIds(List<String> ids) {
            values.addAll(ids);
        }

        public List<String> getValues() {
            return values;
        }
    }

    private int size;

    private final Map<String, Object> fields = new LinkedHashMap<String, Object>();

    private final Map<String, IdList> idLists = new LinkedHashMap<String, IdList>();

    /**
     * Constructor.
     * Sets timestamp.
     */
    public TransactionSerializable() {
        fields.put("timestamp", new Long(System.currentTimeMillis()));
        fields.put("ids", new ArrayList<IdList>(2));
    }

    /**
     * Add new id type.
     *
     * @param idType id type to add
     * @return list for id type
     */
    public IdList addIdType(String idType) {
        IdList idList = new IdList();
        idList.setIdType(idType);

        if (idLists.containsKey(idType)) {
            throw new RuntimeException("Duplicate IdList in a TransactionSerializable.");
        }

        idLists.put(idType, idList);

        @SuppressWarnings("unchecked")
        List<IdList> idsField = (List<IdList>) fields.get("ids");
        idsField.add(idList);

        return idList;
    }

    /**
     * Adds metadata.
     *
     * @param data metadata to add
     */
    public void setMetadata(List<NameValuePair> data) {
        fields.put("metadata", data);
    }

    private Object getField(String key) {
        Object value = fields.get(key);
        if (value == null) {
            value = "";
        }
        return value;
    }

    private String getFieldAsString(String key) {
        String value = (String) fields.get(key);
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * Adds field.
     *
     * @param name name of the field
     * @param value value of the field
     */
    public void addField(String name, Object value) {
        fields.put(name, value);
    }

    /**
     * Gets 'from' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getFrom() {
        return getFieldAsString("from");
    }

    /**
     * Gets 'to' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getTo() {
        return getFieldAsString("to");
    }

    /**
     * Gets 'message' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getMessage() {
        return getFieldAsString("message");
    }

    /**
     * Gets 'operation' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getOperation() {
        return getFieldAsString("operation");
    }

    /**
     * Gets 'flowId' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getFlowId() {
        return getFieldAsString("flowId");
    }

    /**
     * Gets 'timestamp' field.
     * Used for serialization.
     *
     * @return field value
     */
    public Long getTimestamp() {
        return (Long) getField("timestamp");
    }

    /**
     * Gets 'status' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getStatus() {
        return getFieldAsString("status");
    }

    /**
     * Gets 'payloadType' field.
     * Used for serialization.
     *
     * @return field value
     */
    public String getPayloadType() {
        return getFieldAsString("payloadType");
    }


    /**
     * Gets 'ids' field.
     * Used for serialization.
     *
     * @return field value as {@code List<IdList>}
     */
    @SuppressWarnings("unchecked")
    public List<IdList> getIds() {
        return (List<IdList>) getField("ids");
    }

    /**
     * Gets 'metadata' field.
     * Used for serialization.
     *
     * @return field values as {@code List<NameValuePair>}
     */
    @SuppressWarnings("unchecked")
    public List<NameValuePair> getMetadata() {
        return (List<NameValuePair>) getField("metadata");
    }

    @JsonIgnore
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
