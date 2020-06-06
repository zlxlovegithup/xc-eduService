package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AuthController implements AuthControllerApi {

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    @Autowired
    AuthService authService;

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        //校验账号是否输入
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            //请输入账号
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        //校验密码是否输入
        if(StringUtils.isEmpty(loginRequest.getPassword())){
            //请输入密码
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //进行登录
        AuthToken authToken = authService.login(loginRequest.getUsername(), loginRequest.getPassword(), clientId, clientSecret);
        //将用户身份令牌jti写入cookie
        //访问token
        String access_token = authToken.getAccess_token();
        //将访问令牌(用户身份令牌jti)存储到cookie
        saveCookie(access_token);
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    /**
     * 访问令牌(用户身份令牌jti)存储到cookie
     * @param access_token
     */
    private void saveCookie(String access_token) {
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
        //添加cookie 认证令牌，最后一个参数设置为false，表示允许浏览器获取
        CookieUtil.addCookie(response,cookieDomain,"/","uid",access_token,cookieMaxAge,false);
    }

    /**
     * 退出登录
     * @return
     */
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //取出cookie中的身份令牌
        String uid = getTokenFromCookie();
        //删除redis中的token
        authService.delToken(uid);
        //清除cookie
        clearCookie(uid);
        //操作成功
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 清除cookie
     * @param
     */
    private void clearCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //在Spring API中提供了一个非常便捷的工具类RequestContextHolder，能够在Controller中获取request对象和response对象，使用方法如下
        //设置cookie
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,0,false);
    }

    /**
     * 获取令牌
     * @return
     */
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt(){
        //获取coookie中的用户身份令牌
        String access_token = getTokenFromCookie();
        if(access_token == null){
            //获取用户身份令牌失败
            return new JwtResult(CommonCode.FAIL,null);
        }
        //拿身份令牌从redis中查询jwt令牌
        AuthToken userToken = authService.getUserToken(access_token);
        if(userToken!=null){
            //将jwt令牌返回给用户
            String jwt_token = userToken.getJwt_token();
            //操作成功
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }
        return null;
    }

    /**
     * 取出cookie中的身份令牌
     * @return
     */
    private String getTokenFromCookie() {
        //在Spring API中提供了一个非常便捷的工具类RequestContextHolder，能够在Controller中获取request对象和response对象，使用方法如下
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //根据cookie名称读取cookie
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if(map!=null && map.get("uid")!=null){
            String access_token = map.get("uid");
            return access_token;
        }
        return null;
    }
}
