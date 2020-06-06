package com.xuecheng.test.freemaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FreemakerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(FreemakerTestApplication.class,args);
    }

    /**
     * 将RestTemplate对象注入到容器中
     * @return
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
