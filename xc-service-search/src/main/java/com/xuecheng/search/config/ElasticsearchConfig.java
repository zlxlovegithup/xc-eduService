package com.xuecheng.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    //配置主机
    @Value("${xuecheng.elasticsearch.hostlist}")
    private String hostlist;

    /**
     * 创建高版本的ES客户端
     * @return
     */
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        //解析hostlist配置信息
        String[] split = hostlist.split(",");
        //创建HttpHost数组,在这个数组中存放Elasticsearch主机和端口的配置信息
        HttpHost[] httpHostsArray = new HttpHost[split.length];
        for (int i = 0; i < split.length ; i++) {
            String item = split[i];
            //存放IP地址,端口,协议  127.0.0.1   9200   http
            httpHostsArray[i] = new HttpHost(item.split(":")[0],Integer.parseInt(item.split(":")[1]),"http");
        }
        //创建RestHighLevelClient客户端
        return new RestHighLevelClient(RestClient.builder(httpHostsArray));
    }

    /**
     * 创建低版本的ES客户端
     * @return
     */
    //本项目主要使用RestHighLevelClient，对于低级的客户端暂时不使用
    @Bean
    public RestClient restClient(){
        //解析hostlist配置信息
        String[] split = hostlist.split(",");
        //创建HttpHost数组，其中存放es主机和端口的配置信息
        HttpHost[] httpHostsArray = new HttpHost[split.length];
        for (int i = 0; i < split.length ; i++) {
            String item = split[i];
            //存放IP地址,端口,协议  127.0.0.1   9200   http
            httpHostsArray[i] = new HttpHost(item.split(":")[0],Integer.parseInt(item.split(":")[1]),"http");
        }
        //创建RestClient客户端(低版本的客户端)
        return RestClient.builder(httpHostsArray).build();
    }
}
