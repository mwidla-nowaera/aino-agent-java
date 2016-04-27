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

package io.aino.agents.core.config;

/**
 * Class for holding agent configuration.
 */
public class AgentConfig {

    private final ServiceConfig loggerService = new ServiceConfig();
    private final KeyNameListConfig operations = new KeyNameListConfig();
    private final KeyNameListConfig applications = new KeyNameListConfig();
    private final KeyNameListConfig idTypes = new KeyNameListConfig();
    private final KeyNameListConfig payloadTypes = new KeyNameListConfig();

    public enum KeyNameElementType {
        OPERATIONS,
        APPLICATIONS,
        IDTYPES,
        PAYLOADTYPES
    }

    /**
     * Get the configured URL to aino.io API.
     *
     * @return aino.io API URL
     */
    public String getLogServiceUri() { return this.loggerService.getAddressUri(); }

    /**
     * Sets the URL to aino.io API.
     *
     * @param logServiceUri URL of aino.io API
     */
    public void setLogServiceUri(String logServiceUri) {
        this.loggerService.setAddressUri(logServiceUri);
    }

    /**
     * Gets the configured API key.
     *
     * @return apikey
     */
    public String getApiKey() { return this.loggerService.getAddressApiKey(); }

    /**
     * Sets the API key used for authentication to aino.io API.
     *
     * @param apiKey apikey
     */
    public void setApiKey(String apiKey) {
        this.loggerService.setAddressApiKey(apiKey);
    }

    /**
     * Gets the interval of data sending.
     *
     * @return interval in milliseconds
     */
    public int getSendInterval() { return this.loggerService.getSendInterval(); }

    /**
     * Sets the interval for data sending.
     *
     * @param sendInterval interval in milliseconds
     */
    public void setSendInterval(int sendInterval) { this.loggerService.setSendInterval(sendInterval); }

    /**
     * Gets the size threshold for sending.
     * If log message count in send buffer is over the threshold, starts sending data to aino.io API
     * (even if {@link #getSendInterval()} has not yet passed).
     *
     * @return number of log entries
     */
    public int getSizeThreshold() { return this.loggerService.getSendSizeThreshold(); }

    /**
     * Sets the size threshold of send buffer.
     *
     * @param maxSize threshold
     * @see #getSizeThreshold() for more information on size threshold.
     */
    public void setSizeThreshold(int maxSize) { this.loggerService.setSendSizeThreshold(maxSize); }

    /**
     * Checks if logging to aino.io is enabled.
     *
     * @return true if logging is enabled
     */
    public boolean isEnabled() {
        return this.loggerService.isEnabled();
    }

    /**
     * Sets logging on/off.
     *
     * @param val true to enable, false to disable
     */
    public void setEnabled(boolean val) { this.loggerService.setEnabled(val); }

    /**
     * Checks if gzipping the request is enabled.
     *
     * @return is gzip enabled
     */
    public boolean isGzipEnabled() {
        return this.loggerService.isGzipEnabled();
    }

    /**
     * Sets whether data should be gzipped when sending it to the API.
     *
     * @param val should the data be gzipped
     */
    public void setGzipEnabled(boolean val) { this.loggerService.setGzipEnabled(val); }

    /**
     * Get the operations defined.
     *
     * @return KeyNameListConfig containing operations
     */
    public KeyNameListConfig getOperations() {
        return this.operations;
    }

    /**
     * Get the id types defined.
     *
     * @return KeyNameListConfig containing operations
     */
    public KeyNameListConfig getIdTypes() {
        return this.idTypes;
    }

    /**
     * Get the applications defined.
     *
     * @return KeyNameListConfig containing id types
     */
    public KeyNameListConfig getApplications() {
        return this.applications;
    }

    /**
     * Get the payload types defined.
     *
     * @return KeyNameListConfig containing operations
     */
    public KeyNameListConfig getPayloadTypes() {
        return this.payloadTypes;
    }

    /**
     * Get defined configuration based on type.
     *
     * @param type type of the configuration.
     * @return KeyNameListConfig containing configured elements
     */
    public KeyNameListConfig get(KeyNameElementType type) {
        switch (type) {
            case OPERATIONS:
                return this.operations;
            case APPLICATIONS:
                return this.applications;
            case IDTYPES:
                return this.idTypes;
            case PAYLOADTYPES:
                return this.payloadTypes;
            default:
                throw new RuntimeException("Invalid KeyNameElement");
        }
    }
}
