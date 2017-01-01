/*
 *  Copyright 2017 Aino.io
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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionDataBufferTest {

    private AgentConfig config;

    @Before
    public void setUp() {
        config = new AgentConfig();
        config.getApplications().addEntry("app1", "Application 1");
        config.getApplications().addEntry("app2", "Application 2");
    }

    @Test
    public void testBufferReturnsSingleTransactionWhenSizeThresholdIsZero() throws IOException {
        TransactionDataBuffer buffer = new TransactionDataBuffer(0);
        assertSingleTransactionsAreReturned(buffer);
    }

    @Test
    public void testBufferReturnsSingleTransactionWhenSizeThresholdIsOne() throws IOException {
        TransactionDataBuffer buffer = new TransactionDataBuffer(1);
        assertSingleTransactionsAreReturned(buffer);
    }

    @Test
    public void testBufferReturnsAllTransactionsWhenSizeThresholdGreaterThanOne() throws IOException {
        TransactionDataBuffer buffer = new TransactionDataBuffer(2);
        buffer.addTransaction(transactionWrapper());
        buffer.addTransaction(transactionWrapper());
        assertNotNull("Should have received data to send", buffer.getDataToSend());
        assertEquals("No more transactions should exist", 0, buffer.getSize());
    }

    private void assertSingleTransactionsAreReturned(TransactionDataBuffer buffer) throws IOException {
        buffer.addTransaction(transactionWrapper());
        buffer.addTransaction(transactionWrapper());
        assertNotNull("Should have received data to send", buffer.getDataToSend());
        assertEquals("Should have returned one transaction", 1, buffer.getSize());
        assertNotNull("Should have received data to send", buffer.getDataToSend());
        assertEquals("No more transactions should exist", 0, buffer.getSize());
    }

    private TransactionSerializable transactionWrapper() {
        return TransactionSerializable.from(simpleTransation());
    }

    private Transaction simpleTransation() {
        Transaction transaction = new Transaction(config);
        transaction.setFromKey("app1");
        transaction.setToKey("app2");
        transaction.setStatus("success");

        return transaction;
    }
}
