package com.xuecheng.auth;

import com.sun.jersey.core.util.Base64;
import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testClient(){
        //采用客户端负载均衡，从eureka获取认证服务的ip 和端口
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        String authUrl = uri + "/auth/oauth/token"; //authUrl: http://127.0.0.1:40400/auth/oauth/token
        //URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType
        // url就是 申请令牌的url /oauth/token
        //method http的方法类型
        //requestEntity请求内容
        //responseType，将响应的结果生成的类型

        //请求的内容分两部分
        //1、header信息，包括了http basic认证信息
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        String httpbasic = httpbasic("XcWebApp", "XcWebApp");
        //"Basic WGNXZWJBcHA6WGNXZWJBcHA="
        headers.add("Authorization",httpbasic);

        //2、包括：grant_type、username、passowrd
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

        HttpEntity<MultiValueMap<String,String>> multiValueMapHttpEntity = new HttpEntity<MultiValueMap<String,String>>(body,headers);
        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException{
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //远程调用申请令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        Map body1 = exchange.getBody();
        /**
         * access_token -> eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU4NzgzODAzMCwianRpIjoiYTE3Y2FiN2ItNWM5ZS00NTg1LTk5MjQtYzc4MmU3YzQ3Y2VmIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.LpZY0Pg-7KsAg3m4ItiQUeQhtdndBM_5M3TXaKukAtCuP7LbYMa66bgy47jfGX-MlBWLwatsqFK4iLrgcH8BFogC4TOr39Ac9fAJO6wcInBMKL8kx5RTeCrSmH0AL44iSl_pxa3xTozq1y1eY4voTNLky0oo6aL5uDNkOwlkKgwZsgdywojkf-Zj2V8KAQ9DL4cMCJP7xVu2p5ipfMYMSSo1UtQrnBI9XEV_N7ltwyZWDlM_gSGEWG3Y4HBCp6b9QLX0MmVDm2SdtzVgJuwMYLZrupYfjr5HF0AXqddBY9DW9plqauGMKIqlBgHSzcBeDAfxCn53R_3SdtUFvJBZcw
         * token_type -> bearer
         * refresh_token -> eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiJhMTdjYWI3Yi01YzllLTQ1ODUtOTkyNC1jNzgyZTdjNDdjZWYiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU4NzgzODAzMCwianRpIjoiMzRlNzE2NTItZDZhOC00MDAwLThkNWUtN2Y1ZGRmNjBmOWU2IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.WtKWcO6hzx2AYTaiaJWBn_SFDk_BQP0HvJYppq0U1LjTJlgZTGfn1sz7ggojVV1Ccr4sPo2MYUkqj3cVfr2SM5OZQG4y4MwAOh-jViixDruMjhwLwSI7UP9kYFVifr8YhHfpyG3Jzx9J5O1trSq6_HzAEIawaatS1Pqia_N93h3qoWYWH7OS4oXOi4ywlh7wDbWq_vloniS0ZKHhNdaZBNQbSD_Clyc5Lv1w_JJ2HnwfUBb0JeGTZe04ZLMH_kUwTCe7Z37KFly--mxAT23Yb23bicVXxsLdmooL9beN62cL7tsEndSC90CsaiKuUTENCz4UEcZUOjoxaiuYg7dRdg
         * expires_in -> {Integer@13743}43199
         * scope -> app
         * jti -> a17cab7b-5c9e-4585-9924-c782e7c47cef
         */
        System.out.println("令牌: "+body1);
    }

    private String httpbasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
}
