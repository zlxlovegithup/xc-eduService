package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Component //使用@Component标识为bean
public class LoginFilterTest extends ZuulFilter {
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
        return 2; //int值来定义过滤器的执行顺序，数值越小优先级越高
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
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        HttpServletRequest request = requestContext.getRequest();

        //取出头部信息Authorization
        String authorization = request.getHeader("Authorization");
        //如果没有设置Authorization则发出提示: 此操作需要登陆系统
        if(StringUtils.isEmpty(authorization)){
            requestContext.setSendZuulResponse(false);//拒绝访问
            requestContext.setResponseStatusCode(200);// 设置响应状态码
            ResponseResult unauthenticated = new ResponseResult(CommonCode.UNAUTHENTICATED); //此操作需要登陆系统
            String jsonString = JSON.toJSONString(unauthenticated);
            requestContext.setResponseBody(jsonString);
            requestContext.getResponse().setContentType("application/json;charset=UTF-8");
            return null;
        }
        return null;
    }
}
