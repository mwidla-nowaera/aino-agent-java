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

import io.aino.agents.core.AgentCoreException;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for holding message sending related configuration.
 */
class ServiceConfig {

    private boolean enabled = false;
    private boolean gzipEnabled = false;
    private String addressUri;
    private String addressApiKey;
    private int sendInterval;
    private int sendSizeThreshold;
    private String proxyHost = null;
    private int proxyPort = 0;

    /**
     * Checks if the agent is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets agent state to enabled or disabled.
     *
     * @param enabled
     * @throws AgentCoreException if addressUri or apiKey is not set
     */
    public void setEnabled(boolean enabled) {
        if(enabled && StringUtils.isAnyBlank(this.addressApiKey, this.addressUri)) {
            throw new AgentCoreException("Cannot set logger to enabled, because address uri or apikey is missing.");
        }
        this.enabled = enabled;
    }

    /**
     * Gets the address of aino.io API.
     *
     * @return aino.io API address
     */
    String getAddressUri() {
        return addressUri;
    }

    /**
     * Sets the address of aino.io API.
     *
     * @param addressUri aino.io API address
     */
    void setAddressUri(String addressUri) {
        this.addressUri = addressUri;
    }

    /**
     * Gets the API key for aino.io API.
     *
     * @return api key
     */
    String getAddressApiKey() {
        return addressApiKey;
    }

    /**
     * Sets the API key for aino.io API.
     *
     * @param addressApiKey api key
     */
    void setAddressApiKey(String addressApiKey) {
        this.addressApiKey = addressApiKey;
    }

    /**
     * Gets the send interval of messages.
     *
     * @return send interval in milliseconds
     */
    int getSendInterval() {
        return sendInterval;
    }

    /**
     * Sets the send interval of messages.
     *
     * @param sendInterval send interval in milliseconds
     */
    void setSendInterval(int sendInterval) {
        this.sendInterval = sendInterval;
    }

    /**
     * Gets the size threshold of message queue.
     *
     * @return size threshold
     */
    int getSendSizeThreshold() {
        return sendSizeThreshold;
    }

    /**
     * Sets the size threshold of message queue.
     *
     * @param sendSizeThreshold size threshold
     */
    void setSendSizeThreshold(int sendSizeThreshold) {
        this.sendSizeThreshold = sendSizeThreshold;
    }

    /**
     * Checks if gzipping is enabled.
     *
     * @return true if enabled
     */
    boolean isGzipEnabled() {
        return gzipEnabled;
    }

    /**
     * Sets gzipping to enabled or disabled.
     *
     * @param gzipEnabled true to enable
     */
    void setGzipEnabled(boolean gzipEnabled) {
        this.gzipEnabled = gzipEnabled;
    }

    /**
     * Gets the address of an HTTP(S) proxy.
     *
     * @return HTTP(S) proxy address
     */
    String getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets the address of an HTTP(S) proxy.
     *
     * @param proxyHost HTTP(S) proxy address
     */
    void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Gets the port of an HTTP(S) proxy.
     *
     * @return HTTP(S) proxy port
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets the port of an HTTP(S) proxy.
     *
     * @param proxyPort HTTP(S) proxy port
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Checks if an HTTP(S) proxy is defined
     * @return true if defined
     */
    public boolean isProxyDefined() {
        return StringUtils.isNotBlank(getProxyHost());
    }
}
