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

import io.aino.agents.core.config.InvalidAgentConfigException;
import io.aino.agents.core.overloadchecker.SenderOverloadCheckerTask;
import io.aino.agents.core.overloadchecker.ThreadAmountObserver;
import io.aino.agents.core.config.AgentConfig;
import io.aino.agents.core.config.AgentConfigBuilder;
import io.aino.agents.core.validators.ApplicationValidator;
import io.aino.agents.core.validators.TransactionValidator;
import io.aino.agents.core.validators.OperationValidator;
import io.aino.agents.core.validators.IdTypeValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Main class. Used for creating the agent and logging to aino.io.
 */
public class Agent implements ThreadAmountObserver {

    private static final Log log = LogFactory.getLog(Agent.class);

    private TransactionDataBuffer dataBuffer;
    private Map<Thread, Sender> senderThreads;
    private final AgentConfig agentConfig;
    private List<TransactionValidator> validators;
    private Timer overloadCheckerTimer;

    private final int MAX_THREAD_AMOUNT = 5;

    private Agent(AgentConfig config) {
        agentConfig = config;

        senderThreads = new HashMap<Thread, Sender>();

        dataBuffer = new TransactionDataBuffer();
        Sender sender = new Sender(agentConfig, dataBuffer);
        senderThreads.put(new Thread(sender), sender);

        validators = new ArrayList<TransactionValidator>();
        validators.add(new OperationValidator(agentConfig));
        validators.add(new IdTypeValidator(agentConfig));
        validators.add(new ApplicationValidator(agentConfig));

        overloadCheckerTimer = new Timer(true);
        overloadCheckerTimer.schedule(new SenderOverloadCheckerTask(this, dataBuffer, agentConfig), 5000, 5000);

        if(isEnabled()) {
            log.info("Aino logger is enabled, starting sender thread.");
            for (Map.Entry<Thread, Sender> thread: senderThreads.entrySet()) {
                thread.getKey().start();
            }
        }
        log.info("Aino logger initialized.");
    }

    private void stop() {
        overloadCheckerTimer.cancel();

        for(Map.Entry<Thread, Sender> thread : senderThreads.entrySet()) {
            thread.getValue().stop();
        }

        for(Map.Entry<Thread, Sender> thread : senderThreads.entrySet()) {
            try {
                thread.getKey().join();
            } catch (InterruptedException ignored) { }
        }

        senderThreads.clear();
    }

    /**
     * Returns new entry.
     * Returned Transaction should be populated with applications, operations, etc before passing
     * to {@link #addTransaction(Transaction)}.
     *
     * @return new Transaction for logging
     */
    public Transaction newTransaction() {
        return new Transaction(agentConfig);
    }

    /**
     * Adds log entry to be sent to aino.io.
     *
     * @param entry log entry to be sent
     */
    public void addTransaction(Transaction entry) {
        if(!isEnabled()) {
            return;
        }
        validateTransaction(entry);
        TransactionSerializable les = TransactionSerializable.from(entry);
        dataBuffer.addTransaction(les);
        log.debug("Added log entry.");
    }

    private void validateTransaction(Transaction trans) {
        for(TransactionValidator validator : this.validators) {
            validator.validate(trans);
        }
    }

    /**
     * Checks if this agent is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return agentConfig.isEnabled();
    }

    /**
     * Checks if application key is configured to this agent.
     * @param applicationKey key of the application
     * @return true if key was found
     */
    public boolean applicationExists(String applicationKey) {
        return this.agentConfig.getApplications().entryExists(applicationKey);
    }

    /**
     * Checks if operation key is configured to this agent.
     *
     * @param operationKey operation key to check
     * @return true if key was found
     */
    public boolean operationExists(String operationKey) {
        return this.agentConfig.getOperations().entryExists(operationKey);
    }

    /**
     * Checks if payload type key is configured to this agent.
     *
     * @param payloadTypeKey payload type key to check
     * @return true if key was found
     */
    public boolean payloadTypeExists(String payloadTypeKey) {
        return this.agentConfig.getPayloadTypes().entryExists(payloadTypeKey);
    }

    /**
     * Gets the configration object for this agent.
     *
     * @return configuration object
     */
    public AgentConfig getAgentConfig() {
        return this.agentConfig;
    }

    /**
     * Gets factory for creating agent.
     *
     * @return LoggerFactory
     */
    public static LoggerFactory getFactory() {
        return new LoggerFactory();
    }

    @Override
    public void increaseThreads() {
        log.info("increaseThreads() called.");
        if(MAX_THREAD_AMOUNT <= senderThreads.size())
            return;

        Sender sender = new Sender(agentConfig, dataBuffer);
        Thread thread = new Thread(sender);
        senderThreads.put(thread, sender);

        thread.start();
        log.info("Added new sender thread to Aino.io logger core.");
    }

    @Override
    public void decreaseThreads() {

    }

    /**
     * Factory class for constructing {@link Agent} agent.
     */
    public static class LoggerFactory {
        private AgentConfigBuilder builder;

        /**
         * Sets the configuration builder this factory should use.
         *
         * @param builder builder to use
         * @return this factory
         */
        public LoggerFactory setConfigurationBuilder(AgentConfigBuilder builder) {
            this.builder = builder;
            return this;
        }

        /**
         * Builds the logger agent.
         *
         * @return configured logger agent
         */
        public Agent build() {
            AgentConfig agentConfig;

            if(null == builder){
                throw new InvalidAgentConfigException("No builder specified!");
            } else {
                agentConfig = builder.build();
            }

            return new Agent(agentConfig);
        }

    }
}
