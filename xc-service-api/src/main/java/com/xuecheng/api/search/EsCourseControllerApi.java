package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

@Api(value = "课程搜索",description = "课程搜索",tags= {"课程搜索"})
public interface EsCourseControllerApi {

    @ApiOperation("课程搜索")
    public QueryResponseResult<CoursePub> list(int page,  //当前页码
                                               int size,  //每页显示的条数
                                               CourseSearchParam courseSearchParam); //搜索的条件(关键字,一级分类,二级分类,难度等级,价格区间,排序字段,过滤字段)

    @ApiOperation("根据id查询课程信息")
    public Map<String,CoursePub> getall(String id);

    @ApiOperation("根据课程计划id查询课程媒资信息")
    public TeachplanMediaPub getmedia(String id);
}
