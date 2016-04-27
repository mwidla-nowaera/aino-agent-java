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

import io.aino.agents.core.config.AgentConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Class for creating log entries.
 *
 * Once log entry is created with desired data, it should be passed to {@link Agent} for sending.
 */
public class Transaction {

    private String toKey;
    private String fromKey;
    private String operationKey;
    private String payloadTypeKey;
    private String message;
    private String status;
    private String flowId;
    private Map<String, List<String>> ids;
    private Long timestamp;
    private List<NameValuePair> metadata;
    private AgentConfig config;

    public enum Field {
        TO ("to"),
        FROM ("from"),
        OPERATION ("operation"),
        MESSAGE ("message"),
        STATUS ("status"),
        TIMESTAMP ("timestamp"),
        PAYLOADTYPE ("payloadType"),
        FLOWID ("flowId");

        private final String fieldName;

        Field(String n) {
            this.fieldName = n;
        }

        public String getFieldName() {
            return this.fieldName;
        }

    }

    /**
     * Gets value of field.
     *
     * @param field field type to get the value for
     * @return value of the field
     */
    public Object getFieldValue(Field field) {
        switch (field) {
            case TO:
                return this.config.getApplications().getEntry(this.getToKey());
            case FROM:
                return this.config.getApplications().getEntry(this.getFromKey());
            case OPERATION:
                return this.config.getOperations().getEntry(this.getOperationKey());
            case MESSAGE:
                return this.getMessage();
            case STATUS:
                return this.getStatus();
            case TIMESTAMP:
                return this.timestamp;
            case PAYLOADTYPE:
                return this.config.getPayloadTypes().getEntry(this.getPayloadTypeKey());
            case FLOWID:
                return this.getFlowId();
            default:
                throw new AgentCoreException("Invalid field [" + field.fieldName + "]!");
        }
    }

    /**
     * Constructor.
     * AgentConfig is used to get human readable values for keys.
     *
     * @param config agent configuration
     */
    public Transaction(AgentConfig config) {
        this.ids = new HashMap<String, List<String>>();
        this.timestamp = new Long(System.currentTimeMillis());
        this.metadata =  new ArrayList<NameValuePair>(2);
        this.config = config;

    }

    /**
     * Gets 'to application' key.
     *
     * @return key
     */
    public String getToKey() {
        return toKey;
    }

    /**
     * Sets 'to application' key.
     * @param toKey key
     */
    public void setToKey(String toKey) {
        this.toKey = toKey;
    }

    /**
     * Gets 'from application' key.
     *
     * @return key
     */
    public String getFromKey() {
        return fromKey;
    }

    /**
     * Sets 'from application' key.
     *
     * @param fromKey key
     */
    public void setFromKey(String fromKey) {
        this.fromKey = fromKey;
    }

    /**
     * Gets 'operation' key.
     *
     * @return key
     */
    public String getOperationKey() {
        return operationKey;
    }

    /**
     * Sets 'operation' key.
     *
     * @param operationKey key
     */
    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }

    /**
     * Gets 'payload type' key.
     *
     * @return key
     */
    public String getPayloadTypeKey() {
        return payloadTypeKey;
    }

    /**
     * Sets 'payload type' key.
     *
     * @param payloadTypeKey key
     */
    public void setPayloadTypeKey(String payloadTypeKey) {
        this.payloadTypeKey = payloadTypeKey;
    }

    /**
     * Gets message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets status.
     * Possible values are 'failure', 'success', 'unknown'.
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     * Possible values are 'failure', 'success', 'unknown'.
     *
     * @param status "failure", "success", "unknown"
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the flow id.
     * Also known as correlation id.
     *
     * @return flow id
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Sets the flow id.
     * Also known as correlation id.
     *
     * @param flowId flow id
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * Gets the timestamp this entry was created.
     *
     * @return timestamp
     */
    public Long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Gets all ids passed to this entry.
     *
     * @return ids
     */
    public Map<String, List<String>> getIds() {
        return ids;
    }

    /**
     * Gets id type name for key.
     *
     * @param key key of id type
     * @return name corresponding to key
     */
    public String getIdTypeName(String key) {
        return this.config.getIdTypes().getEntry(key);
    }

    /**
     * Gets all ids in this entry based on type key.
     *
     * @param typeKey key
     * @return all ids
     */
    public List<String> getIdsByType(String typeKey) {
        return this.ids.get(typeKey);
    }

    /**
     * Adds id type key.
     *
     * @param typeKey key to add
     * @return list for ids
     */
    public List<String> addIdTypeKey(String typeKey) {
        List<String> newIDs = new ArrayList<String>();
        this.ids.put(typeKey, newIDs);
        return newIDs;
    }

    /**
     * Adds ids for type key.
     *
     * @param typeKey key of id type
     * @param ids ids to add for typeKey
     * @return list of ids
     */
    public List<String> addIdsByTypeKey(String typeKey, List<String> ids) {
        List<String> list = this.getIdsByType(typeKey);
        if(list == null) {
            list = new ArrayList<String>();
            this.ids.put(typeKey, list);
        }
        list.addAll(ids);

        return list;
    }

    /**
     * Adds metadata to this entry.
     *
     * @param key key of metadata
     * @param value value of metadata
     */
    public void addMetadata(String key, String value) {
        NameValuePair toRemove = null;
        for(NameValuePair nvp : this.metadata) {
            if(StringUtils.equals(nvp.getName(), key)) {
                toRemove = nvp;
            }
        }
        if(toRemove != null) this.metadata.remove(toRemove);
        
        this.metadata.add(new NameValuePair(key, value));
    }

    /**
     * Gets all metadata from this entry.
     *
     * @return all metadata
     */
    public List<NameValuePair> getMetadata() {
        return this.metadata;
    }
}
