package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJWT {

    /**
     * 生成jwt令牌
     */
    @Test
    public void testCreateJwt(){
        //证书文件
        String key_location = "xc.keystore";
        //密钥库密码
        String keystore_password = "xuechengkeystore";
        //访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);
        //密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,keystore_password.toCharArray());
        //密钥的密码,此密码和别名需要匹配
        String keypassword = "xuecheng";
        //密钥别名
        String alias = "xckey";
        //密钥对（密钥和公钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypassword.toCharArray());
        //私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey)keyPair.getPrivate();
        //定义payload信息
        Map<String,Object> tokenMap = new HashMap<>();
        tokenMap.put("id","123");
        tokenMap.put("name","itcast");
        tokenMap.put("roles","r01,r02");
        tokenMap.put("ext","1");
        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(aPrivate));
        //取出jwt令牌
        String token = jwt.getEncoded();
        System.out.println("token="+token);
    }

    @Test
    public void testVerify(){
        //jwt令牌
        //eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHQiOiIxIiwicm9sZXMiOiJyMDEscjAyIiwibmFtZSI6Iml0Y2FzdCIsImlkIjoiMTIzIn0.EoXOMtw8Hot744PbZ0obvpbQNMnqMGl40LPSnDNtiPFWX8AJll6CtIeRTgVIS7x49bzXfrZRx1z_QCbAkUjcXEYLlz33Davitab8yZ_sjk9yjCJs7A2_K3GZTRajTCJJC4o9rzI0EpiB9FbI_x4ZlOUpty_jhnwJRs6JlqBQIxMlM2oqPvP50HgzScKbDT4C5PlV-cgXHEUPh56MSjJCpguIwTuggp6R7sRNX636VBgOdGJolleaeVldE0KvtmwFLw53SChC7d7Ak6boC4_hynjgWrQWNpfeIRQqF-caSSrHcJ-hZWd5_igVzFpmq9bC53HU60dauSJXC8gct_j9xA
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6bnVsbCwiaWQiOiI0OSIsImV4cCI6MTU4ODQ0Mjk4NSwiYXV0aG9yaXRpZXMiOlsieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9iYXNlIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9kZWwiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX3BsYW4iLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlIiwiY291cnNlX2ZpbmRfbGlzdCIsInhjX3RlYWNobWFuYWdlciIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfbWFya2V0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wdWJsaXNoIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9hZGQiXSwianRpIjoiYjc4YzI4NjAtOTU1Ni00NTAyLTkwYjYtZTdiYzVkOWVhMTRiIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.mVow0thL7Cc1vy3rei3p9531mE19fpgXDXEp8GqUH2eLF5tnS3ebIjoPfjgvaLItCLUIzX28bWa8Eb8-G6V3H_E6HKeBGJROh4KWJrJrpy1wTQlUxTRWnhUUsMEeBlSJd5OkI7G5x4vICXxmNT-KrmRBFvSlcLJK_6XZp66lSg5vusvK-rBsLFKD0KFVJwnFJEZSWOcRKGgv4KOldYa08XbH8mZuBFk6xGsDv7elcuZ5ll7qVg0xHuT7kjx5F1r6OSgsd-gxWHGzoih8jkgeoYctas44HK6pzDbyBfwgyFWA3lyST2W_DYtupYQn-2AhUEa2I46J3Xjj57KQDP6i2A";
        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4FXQNol+kMRUAk58cB1kSOf4/SKS1xNqnzPr4RpCskaxXyIKjZlJ0y1JF/85ff20BMP9EOGpQuYvm5JJzAKh+4JuyA7EM2q7nJCtYskL1lmM5NJDrkPokLl/vlFEIjkP3Cxwch6R7TJGTaccYyJG6Bv2vsdIPJ2SSfd3zGDuDyiu1AXV0lQFk4iQtLviCdIE4nZwK01ahPnes3oFdEnW8BCCRHJHRfOMbuSHDFW2OZjhKn+msP7C3VHxt2Gr0vbVPLnn/zgPBN493HW9ow3cW6/ddMLpXMtCwFnaK+a3jWCAuufV+YLl8u3EWUEUd0m+yDfzBT4oM4OCXMNMeXiLBwIDAQAB-----END PUBLIC KEY-----";
        //校验jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        //获取jwt原始内容
        String claims = jwt.getClaims();
        System.out.println("claims: "+claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println("jwt令牌:"+encoded);
    }

}
