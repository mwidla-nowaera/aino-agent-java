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

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for {@link Sender}.
 * Checks HTTP status codes etc.
 * Does logging.
 */
class SenderStatus {
    private static final Log log = LogFactory.getLog(SenderStatus.class);

    private static final int MAX_RETRIES = 4;
    boolean retryLastSend = false;
    int retryCount = 0;

    private boolean lastSendSuccessful;
    private int lastResponseStatus;
    private String lastResponse;

    private void createLogMessagesForStatus() {
        if (this.lastSendSuccessful && log.isDebugEnabled()) {
            log.debug(buildStatusLogMessage());
        }
        if(!this.lastSendSuccessful && log.isErrorEnabled()){
            log.error(buildStatusLogMessage());
        }
    }

    private String buildStatusLogMessage() {
        if(this.lastSendSuccessful) {
            return new StringBuilder("Succeeded in sending LogEntries. HTTP status code: ").append(this.lastResponseStatus).toString();
        }

        if(this.retryCount == SenderStatus.MAX_RETRIES) {
            return new StringBuilder("Failed to send LogEntries after ").append(this.retryCount + 1).append(" tries. Discarding the entries.").toString();
        }

        if(-1 == this.lastResponseStatus) {
            return "Failed to send LogEntries. Connection timed out.";
        }

        StringBuilder sb = new StringBuilder("Failed to send LogEntries.");
        sb.append(" HTTP status code: ").append(this.lastResponseStatus);
        sb.append(" Response body: ").append(this.lastResponse);
        return sb.toString();
    }

    private void handleResponseStatus() {
        if (isHttpStatus2xx()) {
            this.lastSendSuccessful = true;
            return;
        }

        if(isHttpStatus4xx()){
            // A malformed request. It will not get correct by retrying.
            this.lastSendSuccessful = false;
            this.retryLastSend = false;
            return;
        }

        this.lastSendSuccessful = false;
        this.retryLastSend = true;
    }

    private boolean isHttpStatus2xx() {
        return this.isInInclusiveRange(200, 299, this.lastResponseStatus);
    }

    private boolean isHttpStatus4xx() {
        return this.isInInclusiveRange(400, 499, this.lastResponseStatus);
    }

    private boolean isInInclusiveRange(int lowerBound, int upperBound, int value){
        if(value < lowerBound) {
            return false;
        }
        if(value > upperBound){
            return false;
        }
        return true;
    }

    /**
     * Sets the initial state.
     */
    void initialStatus() {
        this.lastSendSuccessful = false;
        this.lastResponseStatus = 0;
        this.lastResponse = null;
    }

    /**
     * Updates state from response.
     *
     * @param response response to update the state from
     */
    void responseStatus(ClientResponse response) {
        this.lastResponseStatus = response.getStatus();
        this.lastResponse = response.getEntity(String.class);
        this.handleResponseStatus();
    }

    /**
     * Sets status to error.
     * Enables retries.
     */
    void exceptionStatus() {
        this.lastSendSuccessful = false;
        this.lastResponseStatus = -1;
        this.retryLastSend = true;
    }

    /**
     * Logs sending status.
     * If last send was successful or max retries tried, reset some internal variables.
     */
    void continuationStatus() {
        this.createLogMessagesForStatus();
        if (this.lastSendSuccessful || this.retryCount == this.MAX_RETRIES) {
            this.retryLastSend = false;
            this.retryCount = 0;
        }
    }
}