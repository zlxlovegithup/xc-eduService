package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component //使用@Component标识为bean
public class LoginFilter extends ZuulFilter {

    //日志对象
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    @Autowired
    AuthService authService;

    /**
     * filterType：返回字符串代表过滤器的类型，如下
     *      pre：请求在被路由之前
     *      执行 routing：在路由请求时调用
     *      post：在routing和errror过滤器之后调用
     *      error：处理请求时发生错误调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0; //int值来定义过滤器的执行顺序，数值越小优先级越高
    }

    @Override
    public boolean shouldFilter() {
        return true; //true: 该过滤器需要执行
    }

    /**
     * 过滤器的业务逻辑
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
//        HttpServletResponse response = requestContext.getResponse();
        //请求对象request
        HttpServletRequest request = requestContext.getRequest();

        //1  查询身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if(StringUtils.isEmpty(access_token)){
            //拒绝访问
            this.access_denied();
            return null;
        }
        //2  从redis中校验身份令牌是否过期
        long expire = authService.getExpire(access_token);
        if(expire<=0){
            //拒绝访问
            access_denied();
            return null;
        }
        //3 查询jwt令牌
        String jwt = authService.getJwtFromHeader(request);
        if(jwt == null){
            //拒绝访问
            access_denied();
            return null;
        }

        return null;
    }

    /**
     * 拒绝访问
     */
    private void access_denied() {
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        requestContext.setSendZuulResponse(false);//拒绝访问
        //设置响应内容
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED); //此操作需要登陆系统
        //转换为字符串
        String responseResultString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(responseResultString);
        //设置状态码
        requestContext.setResponseStatusCode(200);
        HttpServletResponse response = requestContext.getResponse();
        response.setContentType("application/json;charset=UTF-8");
    }
}
