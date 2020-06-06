package com.xuecheng.learning.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 此接口会生成代理对象，调用搜索服务：(使用到了FeignClient技术)
 */
@FeignClient(value = XcServiceList.XC_SERVICE_SEARCH)
public interface CourseSearchClient {
    //根据课程计划id查询课程媒资信息
    @GetMapping("/search/course/getmedia/{teachplanId}")
    public TeachplanMediaPub getMedia(@PathVariable("teachplanId") String teachplanId);
}
