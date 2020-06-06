package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常捕获类
 * @ControllerAdvice //是Spring3.2提供的新注解,它是一个Controller增强器,
 *                   // 可对controller中被 @RequestMapping注解的方法加一些逻辑处理。最常用的就是异常处理
 *                   //需要配合@ExceptionHandler使用。
 *                   //当将异常抛到controller时,可以对异常进行统一处理,规定返回的json格式或是跳转到一个错误页面
 */
@ControllerAdvice
public class ExceptionCatch {

    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);
    //定义map，配置异常类型所对应的错误代码
    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //定义map的builder对象，去构建ImmutableMap
    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    //静态代码块，在虚拟机加载类的时候就会加载执行，而且只执行一次;
    static {
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }
    /**
     * 捕获自定义的CustomException异常
     * @ExceptionHandler(CustomException.class): 异常处理，用于全局处理控制器里的异常
     * @param e
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody //将java对象转为json格式的数据。 后端-->前端
    public ResponseResult customException(CustomException e){
        //打印异常日志
        LOGGER.error("catch exception : {}",e.getMessage());

        //获取异常代码
        ResultCode resultCode = e.getResultCode();
        //封装响应结果
        ResponseResult responseResult = new ResponseResult(resultCode);
        //返回结果
        return responseResult;
    }

    /**
     * 捕获Exception异常
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody //将java对象转为json格式的数据。 后端-->前端
    public ResponseResult exception(Exception exception){
        //记录日志
        LOGGER.error("catch exception:{}",exception.getMessage());
        exception.printStackTrace();
        if(EXCEPTIONS == null){
            EXCEPTIONS = builder.build();//EXCEPTIONS构建成功
        }
        //final关键字:对于成员变量来说，一旦使用final关键字，也是一样不能改变
        //从EXCEPTIONS中找异常类型所对应的错误代码，如果找到了将错误代码响应给用户，如果找不到给用户响应99999异常
        final ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        final ResponseResult responseResult;
        if(resultCode !=null ){
            //CommonCode(success=false, code=10003, message=非法参数!)
            //INVALID_PARAM(false,10003,"非法参数!")
            responseResult = new ResponseResult(resultCode);
        }else{
            //返回99999异常
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }

}
