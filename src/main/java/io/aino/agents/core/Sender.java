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

import com.sun.jersey.api.client.ClientHandlerException;
import io.aino.agents.core.config.AgentConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

/**
 * Class doing the message sending to aino.io.
 */
public class Sender implements Runnable, TransactionDataObserver {
    private static final Log log = LogFactory.getLog(Sender.class);

    private enum Action {
        RETRY, SEND, NONE
    }

    private AtomicBoolean continueLoop = new AtomicBoolean(true);
    private SenderStatus status = new SenderStatus();

    private final AgentConfig agentConfig;
    private final TransactionDataBuffer transactionDataBuffer;
    private final ApiClient client;
    private String stringToSend;

    /**
     * Constructor.
     *
     * @param config agent configuration
     * @param dataBuffer databuffer to use
     * @param client the Aino.io API client to use
     */
    public Sender(AgentConfig config, TransactionDataBuffer dataBuffer, ApiClient client) {
        agentConfig = config;
        this.client = client;
        transactionDataBuffer = dataBuffer;
        transactionDataBuffer.addLogDataSizeObserver(this);
    }

    /**
     * Stop sending gracefully.
     * If sending is in progress, continue until it is done.
     */
    public void stop() {
        continueLoop.set(false);
    }

    @Override
    public void logDataAdded(int newSize) {
        synchronized (this) {
            if (newSize >= agentConfig.getSizeThreshold()) {
                notify();
            }
        }
    }

    @Override
    public void run() {
        status.initialStatus();

        try {
            while (transactionDataBuffer.containsData() || continueLoop.get()) {
                switch(action()) {
                    case RETRY: retry(); break;
                    case SEND: send(); break;
                    case NONE: default: sleep(); break;
                }
            }
        } catch (InterruptedException ignored) {
            // Thread has been interrupted. Stop processing.
        }
    }

    private Action action() {
        if(status.retryLastSend) {
            return Action.RETRY;
        }

        if(transactionDataBuffer.containsData()){
            return Action.SEND;
        }

        return Action.NONE;
    }

    private void retry() throws InterruptedException {
        performRequest();
        sleep();
    }

    private void send() throws InterruptedException {
        sendLogData();
        sleep();
    }

    private void sleep() throws InterruptedException {
        // If sleep interval is very short, this will generate a ridiculous amount of log messages. Enable only in dire need of debugging.
        // log.trace(new StringBuilder("Sleeping for a maximum of ").append(agentConfig.getSendInterval()).append(" ms."));

        synchronized (this) {
            wait(agentConfig.getSendInterval());
        }
    }

    private void sendLogData() {
        try {
            stringToSend = transactionDataBuffer.getDataToSend();
            performRequest();
        } catch (IOException e) {
            log.error("Failed to send LogEntries because the JSON serialization failed.", e);
        }
    }

    private void performRequest() {
        try {
            status.retryCount++;
            log.debug("Attempting to resend log entries (retry " + status.retryCount + ").");

            ApiResponse response = client.send(getRequestContent());

            status.responseStatus(response);
        } catch (ClientHandlerException e) {
            status.exceptionStatus();
        } finally {
            status.continuationStatus();
        }
    }



    private byte[] getRequestContent() {
        if(!agentConfig.isGzipEnabled()) {
            return stringToSend.getBytes();
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipStream = new GZIPOutputStream(baos);
            gzipStream.write(stringToSend.getBytes());

            gzipStream.finish();
            baos.flush();
            byte[] returnBytes = baos.toByteArray();
            baos.close();
            gzipStream.close();

            return returnBytes;
        } catch (IOException e) {
            throw new AgentCoreException("Failed to compress Aino log message using gzip.");
        }
    }
}
