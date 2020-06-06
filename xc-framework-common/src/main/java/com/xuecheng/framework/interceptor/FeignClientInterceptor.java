package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * feign拦截器实现远程调用携带JWT
 */
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            //使用RequestContextHolder工具获取request相关变量
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(requestAttributes != null){
                //取出request
                HttpServletRequest request = requestAttributes.getRequest();
                //获取头名称
                Enumeration<String> headerNames = request.getHeaderNames();
                if(headerNames != null){
                    String name = headerNames.nextElement();
                    String values = request.getHeader(name);
                    if(name.equals("authorization")){
                        //System.out.println("name="+name+"values="+values);
                        //将header向下传递
                        requestTemplate.header(name,values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
