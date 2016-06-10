package io.aino.agents.core;

import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import io.aino.agents.core.config.AgentConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by mystes-am on 8.6.2016.
 */
public class HttpProxyFactory implements HttpURLConnectionFactory {

    Proxy proxy;

    private HttpProxyFactory(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection(this.proxy);
    }

    public static URLConnectionClientHandler getConnectionHandler(AgentConfig agentConfig){
        if(agentConfig.isProxyDefined()){
            HttpProxyFactory factory = new HttpProxyFactory(getProxiedConnection(agentConfig));
            return new URLConnectionClientHandler(factory);
        }
        return new URLConnectionClientHandler(new HttpProxyFactory(Proxy.NO_PROXY));
    }

    private static Proxy getProxiedConnection(AgentConfig agentConfig){
        InetSocketAddress proxyAddress = new InetSocketAddress(agentConfig.getProxyHost(), agentConfig.getProxyPort());
        return new Proxy(Proxy.Type.HTTP, proxyAddress);
    }
}
