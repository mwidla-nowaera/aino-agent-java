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

import io.aino.agents.core.config.ClasspathResourceConfigBuilder;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class AgentIntegrationTest {

    private HttpClient client = new HttpClient();
    private static final String API_URL = "http://localhost:8808/api/1.0";
    private static final String AINO_CONFIG = "validConfig.xml";
    private static final String AINO_CONFIG_WITH_LONG_INTERVAL = "validConfigWithIntervalAndSize.xml";
    private static final String AINO_CONFIG_WITH_PROXY = "validConfigWithProxy.xml";

    private Agent ainoAgent = getAinoLogger();
    private Agent slowAinoAgent = getSlowAinoLogger();
    private Agent proxiedAinoAgent = getProxiedAinoLogger();

    private static Agent getAinoLogger() {
        Agent.LoggerFactory ainoLoggerFactory = new Agent.LoggerFactory();
        return ainoLoggerFactory.setConfigurationBuilder(new ClasspathResourceConfigBuilder(AINO_CONFIG)).build();
    }

    private static Agent getSlowAinoLogger() {
        Agent.LoggerFactory ainoLoggerFactory = new Agent.LoggerFactory();
        return ainoLoggerFactory.setConfigurationBuilder(new ClasspathResourceConfigBuilder(AINO_CONFIG_WITH_LONG_INTERVAL)).build();
    }

    private static Agent getProxiedAinoLogger() {
        Agent.LoggerFactory ainoLoggerFactory = new Agent.LoggerFactory();
        return ainoLoggerFactory.setConfigurationBuilder(new ClasspathResourceConfigBuilder(AINO_CONFIG_WITH_PROXY)).build();
    }

    /*************************************************/

    private boolean isAinoMockApiUp(){
        try {
            boolean reachable = isReachable("127.0.0.1", 8808, 1000);
            System.out.println("Aino Mock API reachable: " + reachable);
            return reachable;
        } catch (IOException e) {
            System.out.println("Aino Mock API is probably not running, I/O exception!");
            return false;
        }
    }

    private boolean isProxyUp(){
        try {
            boolean reachable = isReachable("127.0.0.1", 8080, 1000);
            System.out.println("Proxy reachable: " + reachable);
            return reachable;
        } catch (IOException e) {
            System.out.println("Proxy is probably not running, I/O exception!");
            return false;
        }
    }

    private boolean isReachable(String host, int port, int timeout) throws IOException {
        SocketAddress proxyAddress = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        try{
            socket.connect(proxyAddress, timeout);
            //System.out.println("Connected to: " + host + ":" + port);
            return socket.isConnected();
        } catch (IOException e) {
            System.out.println("Could not connect to: " + host + ":" + port);
            return false;
        } finally {
            if(socket.isConnected()){
                //System.out.println("Closing connection to: " + host + ":" + port);
                socket.close();
                //System.out.println("Disconnected: " + host + ":" + port);
            }
        }
    }

    /*************************************************/

    @Before
    public void setup() throws Exception {
        if(!isAinoMockApiUp()){
            System.out.println("--------------------------------------------");
            System.out.println(" SKIPPING TEST - AINO MOCK API IS NOT UP!!! ");
            System.out.println("--------------------------------------------");
        }
        assumeTrue(isAinoMockApiUp());
        HttpMethod get = new GetMethod(API_URL + "/test/clear");
        client.executeMethod(get);
    }

    @Test
    public void ainoMockApiIsRunningTest() throws Exception {
        HttpMethod get = new GetMethod(API_URL + "/ping");

        int statusCode = client.executeMethod(get);

        assertEquals(200, statusCode);
        assertEquals("pong", get.getResponseBodyAsString().trim());
    }

    @Test
    public void loggerSendsDataToMockApiTest() throws Exception {
        assertLoggerSendsDataToMockApi(ainoAgent);
    }

    @Test
    public void loggerSendsDataToMockApiThroughProxyTest() throws Exception {
        if(!isProxyUp()) {
            System.out.println("------------------------------------------------------------------------");
            System.out.println(" SKIPPING loggerSendsDataToMockApiThroughProxyTest - PROXY IS NOT UP!!! ");
            System.out.println("------------------------------------------------------------------------");
        }
        assumeTrue("Test proxy needs to be running first.", isProxyUp());
        //TODO Test could be improved by implementing an automatic test proxy
        //instead of starting one manually. E.g. something like below
        // TestProxy proxy = new TestProxy("127.0.0.1", 8080, 8088);
        //proxy.start();
        assertLoggerSendsDataToMockApi(proxiedAinoAgent);
        //proxy.stop();
    }

    @Test
    public void loggerDoesNotSendDataToMockApiThroughProxyWhenProxyIsOfflineTest() throws Exception {
        if((isProxyUp())) {
            System.out.println("------------------------------------------------------------------------");
            System.out.println(" SKIPPING loggerSendsDataToMockApiThroughProxyTest - PROXY _IS_ UP!!!   ");
            System.out.println("------------------------------------------------------------------------");
        }
        assumeFalse("Test proxy should not be up while running this test.", isProxyUp());
        initializeBasicTransaction(proxiedAinoAgent);

        Thread.sleep(2000); // :(
        HttpMethod get = new GetMethod(API_URL + "/test/readTransactions");
        int statusCode = client.executeMethod(get);

        JsonNode transactions = parseJsonFromResponseBody(get).findPath("transactions");
        assertNotNull("JsonNode 'transactions' should not be null", transactions);
        assertEquals("There should be no transactions due to proxy failure", 0, transactions.size());
    }

    public void assertLoggerSendsDataToMockApi(Agent agent) throws Exception {
        initializeBasicTransaction(agent);

        Thread.sleep(2000); // :(
        HttpMethod get = new GetMethod(API_URL + "/test/readTransactions");
        int statusCode = client.executeMethod(get);

        JsonNode transactions = parseJsonFromResponseBody(get).findPath("transactions");
        assertNotNull("JsonNode 'transactions' should not be null", transactions);
        assertEquals("There should be exactly 1 transaction", 1, transactions.size());

        JsonNode operationNode = transactions.get(0).get("operation");
        assertEquals("Create", operationNode.asText());
        assertEquals(200, statusCode);
    }

    private Transaction initializeBasicTransaction(Agent agent){
        Transaction tle = new Transaction(agent.getAgentConfig());
        tle.setFromKey("app01");
        tle.setOperationKey("create");
        tle.setToKey("esb");
        agent.addTransaction(tle);
        return tle;
    }

    @Test
    public void loggerSendsDataToMockApiWithDelay() throws Exception {
        Transaction tle = new Transaction(slowAinoAgent.getAgentConfig());
        tle.setOperationKey("create");
        tle.setFromKey("app01");
        tle.setToKey("esb");

        // Wait for logger to do one send iteration.
        Thread.sleep(100);

        slowAinoAgent.addTransaction(tle);

        HttpMethod get = new GetMethod(API_URL + "/test/readTransactions");
        int statusCode = client.executeMethod(get);
        assertEquals("Status code should be 200", 200, statusCode);

        JsonNode transactions = parseJsonFromResponseBody(get).findPath("transactions");
        assertNotNull("'transactions' should not be null", transactions );
        assertEquals("There should be 0 transactions", 0, transactions.size());

        Thread.sleep(5000); // logger send interval is 5000
        statusCode = client.executeMethod(get);

        assertEquals("Status code should be 200", 200, statusCode);

        transactions = parseJsonFromResponseBody(get).findPath("transactions");
        assertNotNull("'transactions' should not be null", transactions );
        assertEquals("There should be 1 transactions", 1, transactions.size());

    }

    @Test
    public void loggerSendsCorrectPayloadType() throws Exception {
        Transaction tle = new Transaction(slowAinoAgent.getAgentConfig());
        tle.setOperationKey("update");
        tle.setFromKey("app01");
        tle.setToKey("esb");
        tle.setPayloadTypeKey("subInterface01");

        ainoAgent.addTransaction(tle);

        Thread.sleep(2000);

        HttpMethod get = new GetMethod(API_URL + "/test/readTransactions");

        int statusCode = client.executeMethod(get);

        assertEquals("Status code should be 200", 200, statusCode);

        JsonNode transactions = parseJsonFromResponseBody(get).findPath("transactions");

        assertNotNull("'transactions' should not be null", transactions );
        assertEquals("There should be 1 transactions", 1, transactions.size());
        assertEquals("operation should be 'Update'", "Update", transactions.findPath("operation").getTextValue());

        JsonNode payloadTypeNode = parseJsonFromResponseBody(get).findPath("payloadType");
        assertEquals("payloadType should be 'Interface 1", "Interface 1", payloadTypeNode.getTextValue());
    }

    private JsonNode parseJsonFromResponseBody(HttpMethod method) throws IOException {
        JsonParser jsonParser = new JsonFactory().createJsonParser(method.getResponseBody());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(jsonParser);
    }
}
