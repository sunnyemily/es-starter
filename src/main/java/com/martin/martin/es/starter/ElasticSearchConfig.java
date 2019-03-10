package com.martin.martin.es.starter;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author sunhuazhong
 * @create 2019/3/2 下午3:10
 **/
@Configuration
public class ElasticSearchConfig {

    @Bean
    public TransportClient client() throws UnknownHostException {
        InetSocketTransportAddress address = new InetSocketTransportAddress(
                InetAddress.getByName("localhost"),
                9300
        );

        Settings settings = Settings.builder()
                .put("cluster.name", "martin")
                .build();

        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(address);

        return client;
    }
}
