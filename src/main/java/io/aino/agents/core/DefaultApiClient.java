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


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import io.aino.agents.core.config.AgentConfig;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation for {@link ApiClient}
 */
public class DefaultApiClient implements ApiClient {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final ElasticsearchTransport est;
    private final ElasticsearchClient esClient;

    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    private final String elasticSearchUrl ;

    private final WebResource resource;
    private final AgentConfig agentConfig;

    public DefaultApiClient(final AgentConfig config) {
        elasticSearchUrl = config.getElasticSearchUri();

        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(config.getElasticSearchUsername(), config.getElasticSearchPassword())
        );

        HttpHost host = new HttpHost(elasticSearchUrl,config.getElasticSearchPort(),"https");

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

        this.est = new RestClientTransport(restClient2,new JacksonJsonpMapper());
        this.esClient = new ElasticsearchClient(est);

        this.agentConfig = config;
        URLConnectionClientHandler connection = HttpProxyFactory.getConnectionHandler(agentConfig);
        Client restClient = new Client(connection);
        resource = restClient.resource(agentConfig.getLogServiceUri());
    }

    @Override
    public ApiResponse send(final byte[] data, final List<TransactionSerializable> transactions) {
        BulkRequest.Builder req =  new BulkRequest.Builder();
        System.out.println(transactions);

        transactions.forEach(
                elem -> req.operations(
                        op -> op
                                .index(
                                        ind -> ind.index(agentConfig.getElasticSearchIndexName())
                                                .id(UUID.randomUUID().toString())
                                                .document(elem)
                                )
                )
        );
        try {
            System.out.println(esClient.bulk(req.build()));
            return new ApiResponseImpl(buildRequest().post(ClientResponse.class, data));
        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
