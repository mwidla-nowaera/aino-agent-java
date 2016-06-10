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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import io.aino.agents.core.config.AgentConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

/**
 * Class doing the message sending to aino.io.
 */
public class Sender implements Runnable, TransactionDataObserver {
    private static final Log log = LogFactory.getLog(Sender.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private enum Action {
        RETRY, SEND, NONE
    }

    private AtomicBoolean continueLoop = new AtomicBoolean(true);
    private SenderStatus status = new SenderStatus();

    private AgentConfig agentConfig;
    private TransactionDataBuffer transactionDataBuffer;
    private WebResource resource;
    private String stringToSend;

    /**
     * Constructor.
     *
     * @param config agent configuration
     * @param dataBuffer databuffer to use
     */
    public Sender(AgentConfig config, TransactionDataBuffer dataBuffer) {
        agentConfig = config;
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
        URLConnectionClientHandler connection = HttpProxyFactory.getConnectionHandler(agentConfig);
        Client restClient = new Client(connection);
        resource = restClient.resource(agentConfig.getLogServiceUri());

        status.initialStatus();

        try {
            while (continueLoop.get()) {
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

            ClientResponse response = buildRequest().post(ClientResponse.class, getRequestContent());

            status.responseStatus(response);
        } catch (ClientHandlerException e) {
            status.exceptionStatus();
        } finally {
            status.continuationStatus();
        }
    }

    private WebResource.Builder buildRequest() {
        WebResource.Builder builder = resource.accept("text/plain").type("application/json")
                .header(AUTHORIZATION_HEADER, "apikey " + agentConfig.getApiKey());

        if(agentConfig.isGzipEnabled()) {
            builder.header(HttpHeaders.CONTENT_ENCODING, "gzip");
            builder.header(HttpHeaders.ACCEPT_ENCODING, "gzip");
        }

        return builder;
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
