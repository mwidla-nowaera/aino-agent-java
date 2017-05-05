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


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import io.aino.agents.core.config.AgentConfig;

import javax.ws.rs.core.HttpHeaders;

/**
 * Default implementation for {@link ApiClient}
 */
public class DefaultApiClient implements ApiClient {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final WebResource resource;
    private final AgentConfig agentConfig;

    public DefaultApiClient(final AgentConfig config) {
        this.agentConfig = config;
        URLConnectionClientHandler connection = HttpProxyFactory.getConnectionHandler(agentConfig);
        Client restClient = new Client(connection);
        resource = restClient.resource(agentConfig.getLogServiceUri());
    }

    @Override
    public ApiResponse send(final byte[] data) {
        return new ApiResponseImpl(buildRequest().post(ClientResponse.class, data));
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
