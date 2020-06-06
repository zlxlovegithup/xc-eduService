package com.xuecheng.manage_course.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true) //激活方法上的PreAuthorize注解
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    //公钥
    private static final String PUBLIC_KEY = "publickey.txt";

    //定义JwtTokenStore，使用jwt令牌
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter){
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    //定义JJwtAccessTokenConverter，使用jwt令牌
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        String pubKey = this.getPubKey();
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setVerifierKey(getPubKey());
        converter.setVerifierKey(pubKey);
        //不设置这个会出现 Cannot convert access token to JSON
        converter.setVerifier(new RsaVerifier(pubKey));
        return  converter;
    }

    /**
     * 获取非对称加密公钥 Key
     * @return 公钥 Key
     */
    private String getPubKey(){
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);
            return bufferReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Http安全配置，对每个到达系统的http请求链接进行校验
    @Override
    public void configure(HttpSecurity http) throws Exception{
        //所有请求必须认证通过
        http.authorizeRequests()
                //将下面配置的路径放行  http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000
                //通过上边的配置虽然可以访问swagger-ui，但是无法进行单元测试，除非去掉认证的配置或在上边配置中添加所有
                //请求均放行（"/**"）--> "/**"
                .antMatchers("/v2/api-docs","/swagger-resources/configuration/ui",
                        "/swagger-resources","/swagger-resources/configuration/security",
                        "/swagger-ui.html","/webjars/**","/course/coursepic/list/*","/cms/page/postPageQuick/**").permitAll()
                .anyRequest().authenticated();

    }
}
