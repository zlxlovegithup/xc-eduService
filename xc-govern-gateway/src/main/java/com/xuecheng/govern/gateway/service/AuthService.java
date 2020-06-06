package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 从cookie查询用户身份令牌是否存在，不存在则拒绝访问
     * @param request
     * @return
     */
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    /**
     * 从http header查询jwt令牌是否存在，不存在则拒绝访问
     * @param request
     * @return
     */
    public String getJwtFromHeader(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒绝访问
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            //拒绝访问
            return null;
        }
        //取到jwt令牌
//        return authorization;
        String jwt = authorization.substring(7);
        return jwt;
    }

    /**
     * 从Redis查询user_token令牌是否过期，过期则拒绝访问
     * @param access_token
     * @return
     */
    public long getExpire(String access_token){
        //token在redis中的key
        String key = "user_token: "+access_token;
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire;
    }
    
}
