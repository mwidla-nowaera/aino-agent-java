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
import io.aino.agents.core.config.FileConfigBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SenderTest {
    private static AgentConfig validConfig;

    @Mock
    private ApiClient apiClient;

    private ApiResponse apiResponse = new ApiResponse() {
        @Override
        public int getStatus() {
            return HttpStatus.SC_ACCEPTED;
        }

        @Override
        public String getPayload() {
            return "OK";
        }
    };

    @BeforeClass
    public static void initConfigs() throws FileNotFoundException {
        validConfig = new FileConfigBuilder(new File("src/test/resources/validConfig.xml")).build();
        validConfig.setSendInterval(100);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRemainingDataIsSentOnShutdown() throws IOException, InterruptedException {
        final int trxCount = 10;
        TransactionDataBuffer dataBuffer = initDataBuffer(trxCount);
        when(apiClient.send(any(byte[].class))).thenReturn(apiResponse);
        Sender sender = new Sender(validConfig, dataBuffer, apiClient);
        new Thread(sender).start();
        sender.stop();
        Thread.sleep(1500l);
        verify(apiClient, times(trxCount)).send(any(byte[].class));
    }

    private TransactionDataBuffer initDataBuffer(int trxCount) {
        TransactionDataBuffer dataBuffer = new TransactionDataBuffer(1);
        for (int i = 0; i < trxCount; i++) {

            Transaction transaction = new Transaction(validConfig);
            transaction.setFromKey("app01");
            transaction.setToKey("app02");
            transaction.setStatus("success");
            dataBuffer.addTransaction(TransactionSerializable.from(transaction));
        }

        return dataBuffer;
    }
}
