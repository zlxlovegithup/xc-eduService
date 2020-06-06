package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.sun.jersey.core.util.Base64;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    //日志类
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    //token存储到redis的过期时间
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 登录(认证)
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username,String password,String clientId,String clientSecret){
        //申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            //请求令牌错误！
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //将token存储到redis中
        //取出用户身份令牌 "jti"
        String access_token = authToken.getAccess_token();
        //存储到redis中的内容
        String content = JSON.toJSONString(authToken);
        //将令牌存储到redis
        boolean saveTokenResult = saveToken(access_token, content, tokenValiditySeconds);
        if(!saveTokenResult){
            //存储令牌redis中失败
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    /**
     * 存储令牌到redis中
     * @param access_token 用户身份令牌
     * @param content 令牌内容,包含"access_token","refresh_token","jti"
     * @param tokenValiditySeconds
     * @return
     */
    private boolean saveToken(String access_token, String content, int tokenValiditySeconds) {
        //令牌名称
        String name = "user_token: "+access_token;
        //保存到令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,tokenValiditySeconds, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name, TimeUnit.SECONDS);
        return expire>0;
    }

    /**
     * 申请令牌(用户名密码认证)
     * @param username 用户名
     * @param password 密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //选中认证服务的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if(serviceInstance == null){
            LOGGER.error("choose an auth instance fail");
            //认证服务器未找到
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        //获取申请令牌的url : http://127.0.0.1:40400/auth/oauth/token
        String jwtUrl = serviceInstance.getUri().toString() + "/auth/oauth/token";

        //请求的内容分两部分
        //1、定义body 包括：grant_type、username、passowrd
        MultiValueMap<String,String> formData = new LinkedMultiValueMap<>();
        //设置授权方式为账号密码授权
        formData.add("grant_type","password");
        //账号
        formData.add("username",username);
        //密码
        formData.add("password",password);
        //2、headers信息，包括了http basic认证信息
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        String httpbasic = httpbasic(clientId, clientSecret);
        headers.add("Authorization",httpbasic);
        //指定restTemplate当遇到400或者401响应时候也不要抛出异常,也要正常返回值
        //由于restTemplate收到400或401的错误会抛出异常，而spring security针对账号不存在及密码错误会返回400及
        //       401，所以在代码中控制针对400或401的响应不要抛出异常
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        Map map = null;
        try {
            //http请求spring security的申请令牌接口
            ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(jwtUrl, HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(formData, headers), Map.class);
            //获取令牌body部分
            map = mapResponseEntity.getBody();
            //取出错误描述
            String error_description = (String) map.get("error_description");
            //不为空
            if(StringUtils.isNotEmpty(error_description)){
                if(error_description.indexOf("UserDetailsService returned null, which is an interface contract violation")>=0){
                    //账号不存在
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }else if(error_description.equals("坏的凭证")){
                    //账号名或者密码错误
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
        } catch (RestClientException e) {
            e.printStackTrace();
            LOGGER.error("request oauth_token_password error: {}",e.getMessage());
            e.printStackTrace();
            //获取spring security返回的错误信息

            //请求授权码令牌错误！
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        if(map == null || map.get("access_token") ==null ||
           map.get("refresh_token") == null || map.get("jti") == null){ //jti是jwt令牌的唯一标识作为用户身份令牌
            //请求授权码令牌错误！
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        //jwt令牌
        String jwt_token = (String) map.get("access_token");
        //刷新令牌
        String refresh_token = (String) map.get("refresh_token");
        //用户身份令牌
        String access_token = (String) map.get("jti");
        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);
        authToken.setJwt_token(jwt_token);
        return authToken;
    }

    /**
     * 获取httpbasic认证串
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return
     */
    private String httpbasic(String clientId, String clientSecret) {
        //将客户端和密码进行拼接,按照"客户端id:客户端密码"
        String string = clientId + ":" +clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }

    /**
     * 拿身份令牌从redis中查询jwt令牌
     * @param token
     * @return
     */
    public AuthToken getUserToken(String token){

        String userToken = "user_token: " + token;
        //获取user_token(jwt令牌)
        //从redis中取到令牌信息
        String userTokenString = stringRedisTemplate.opsForValue().get(userToken);
        //转成对象
        if(userToken!=null){
            AuthToken authToken = null;
            try {
                //
                authToken = JSON.parseObject(userTokenString, AuthToken.class);
            } catch (Exception e) {
                LOGGER.error("getUserToken from redis and execute JSON.parseObject error{}",e.getMessage());
                e.printStackTrace();
            }
            //将jwt令牌返回
            return authToken;
        }
        return null;
    }

    /**
     * 从redis中删除令牌
     * @param access_token
     * @return
     */
    public boolean delToken(String access_token){
        String name = "user_token: "+access_token;
        //从redis中删除令牌
        stringRedisTemplate.delete(name);
        return true;
    }
}
