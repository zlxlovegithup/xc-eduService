package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CourseService courseService;

    @Test
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper(){
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);
    }

    @Test
    public void testAddTeachplan(){
        Teachplan teachplan = new Teachplan();
        teachplan.setCourseid("4028e581617f945f01617f9dabc40000");
        teachplan.setParentid("1"); //设置为大章节(一级节点)
        teachplan.setPname("Bootstrap开发框架test01");
        teachplan.setPtype("1");
        teachplan.setTimelength(120.00);
        teachplan.setOrderby(1);
        teachplan.setDescription("这是一个前端框架Bootstrap的课程");
        teachplan.setStatus("0");
        ResponseResult responseResult = courseService.addTeachplan(teachplan);
        System.out.println(responseResult);
    }

    @Test
    public void testDao(){
        //在调用dao的service方法中设置分页参数：PageHelper.startPage(page, size)，分页参数会设置在ThreadLocal中
        //PageHelper在mybatis执行sql前进行拦截，从ThreadLocal取出分页参数，修改当前执行的sql语句，添加分页 sql。
        PageHelper.startPage(2,1);
        CourseListRequest courseListRequest = new CourseListRequest();
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> result = courseListPage.getResult();
        System.out.println(courseListPage);
    }

}
