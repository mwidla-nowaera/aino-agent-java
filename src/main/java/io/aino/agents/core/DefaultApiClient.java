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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import io.aino.agents.core.config.AgentConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation for {@link ApiClient}
 */
public class DefaultApiClient implements ApiClient {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Log log = LogFactory.getLog(DefaultApiClient.class);
    private OpenSearchClient esClient;

    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    private final String elasticSearchUrl;
    private final Integer elasticSearchPort;

    private WebResource resource;
    private final AgentConfig agentConfig;

    public DefaultApiClient(final AgentConfig config) {
        this.agentConfig = config;
        URLConnectionClientHandler connection = HttpProxyFactory.getConnectionHandler(agentConfig);
        Client restClient = new Client(connection);
        resource = restClient.resource(agentConfig.getLogServiceUri());

        this.elasticSearchUrl = config.getElasticSearchUri();
        this.elasticSearchPort = config.getElasticSearchPort();

        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(config.getElasticSearchUsername(), config.getElasticSearchPassword())
        );
        setupOpensearchConnection();
    }

    public void setupOpensearchConnection() {
        if (esClient != null){
            try {
                esClient.info();
                return;
            } catch(OpenSearchException | IOException ex){
                log.error("Error to connect with opensearch: "+ex);
            }
        }

        HttpHost host = new HttpHost(elasticSearchUrl,elasticSearchPort,"https");
        RestClient restClient2 = RestClient
                .builder( host )
                .setHttpClientConfigCallback(
                        httpAsyncClientBuilder -> httpAsyncClientBuilder
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .addInterceptorLast(
                                        (HttpResponseInterceptor)
                                                (response,request) -> {
                                                    response.addHeader("X-Elastic-Product","Elasticsearch");
                                                }
                                )
                )
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Content-type","application/json")
                })
                .build();

        OpenSearchTransport transport = new RestClientTransport(restClient2,new JacksonJsonpMapper());
        esClient = new OpenSearchClient(transport);
    }

    @Override
    public ApiResponse send(final byte[] data, final String stringToSend) throws JsonProcessingException {
        setupOpensearchConnection();
        BulkRequest.Builder req =  new BulkRequest.Builder();
        ObjectMapper mapper = new ObjectMapper();
        TransactionWrapper transactionWrapper = mapper.readValue(stringToSend, TransactionWrapper.class);
        List<Transaction> transactions = transactionWrapper.getTransactions();
        String finalIndex = agentConfig.getElasticSearchIndexName();
        LocalDate date = LocalDate.now();
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        transactions.forEach(
                elem -> req.operations(
                        op -> op
                                .index(
                                        ind -> ind.index(String.format(finalIndex +"-%s",formattedDate))
                                                .id(UUID.randomUUID().toString())
                                                .document(elem)
                                )
                )
        );
        try {
            //esClient.bulk();
            //BulkResponse response = esClient.bulk(req.build());
            return new OpensearchApiResponseImpl(esClient.bulk(req.build()));
        } catch (IOException e) {
            return new OpensearchApiResponseImpl(500);
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

    private static final class ApiResponseImpl implements ApiResponse {
        private final ClientResponse response;

        ApiResponseImpl(ClientResponse response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.getStatus();
        }

        @Override
        public String getPayload() {
            return response.getEntity(String.class);
        }
    }

    private static final class OpensearchApiResponseImpl implements ApiResponse {
        private final Integer status;
        private final String payload;


        OpensearchApiResponseImpl(BulkResponse response){
            if (response.errors()){
                this.status = response.items().get(0).status();
            } else {
                this.status = 200;
            }
            this.payload = "test";
        }

        OpensearchApiResponseImpl(Integer status){
            this.status = status;
            this.payload = "Error while connecting to Opensearch";
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public String getPayload() {
            return payload;
        }
    }
}
