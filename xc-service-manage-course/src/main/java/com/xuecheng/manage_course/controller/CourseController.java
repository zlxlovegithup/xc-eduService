package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {

    @Autowired
    CourseService courseService;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    /**
     * 查询课程计划
     * @param courseId
     * @return
     */
    @PreAuthorize("hasAuthority('course_teachplan_list')")
    @GetMapping("/teachplan/list/{courseId}")
    @Override
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    /**
     * 向数据库中添加节点
     * @param teachplan
     * @RequestBody @RequestBody主要用来接收前端传递给后端的json字符串中的数据的并且将其转换为java对象(请求体中的数据的);
     *                GET方式无请求体，所以使用@RequestBody接收数据时，前端不能使用GET方式提交数据，而是用POST方式进行提交。
     * @return
     */
    @PostMapping("/teachplan/add")
    @Override
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    /**
     * 课程的分页查询
     * @param page 当前页
     * @param size 当前页的多少
     * @param courseListRequest
     * @return
     */
    @PreAuthorize("hasAuthority('course_find_list')") //有'course_find_list'权限时才可以进行访问
    @GetMapping("/coursebase/list/{page}/{size}")
    @Override
    public QueryResponseResult<CourseInfo> findCourseList(
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            CourseListRequest courseListRequest) {
        //先使用静态数据进行测试
//        String companyId = "1";
        //工具类XcOauth2Util:  将解析的JWT内容封装成UserJwt对象返回
        //调用工具类XcOauth2Util取出用户信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        //从Header中拿到jwt令牌
        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
        //取出companyId
        String companyId = userJwt.getCompanyId();
        return courseService.findCourseList(companyId,page,size,courseListRequest);
    }

    /**
     * 添加课程
     * @RequestBody: 将前台接受到的json数据转为java对象
     * @param courseBase
     * @return
     */
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    /**
     * 根据课程id查询课程
     * @param courseId
     * @return
     * @throws RuntimeException
     */
    @PreAuthorize("hasAuthority('course_get_baseinfo')") //指定查询课程基本信息方法需要拥有course_get_baseinfo权限
    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) throws RuntimeException {
       return courseService.getCoursebaseById(courseId);
    }

    /**
     * 先根据课程id查询课程,然后再修改课程
     * @param id
     * @param courseBase
     * @return
     */
    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCoursebase(id,courseBase);
    }

    /**
     * 查询课程营销信息
     * @param courseid
     * @return
     */
    @Override
    @GetMapping("/coursemarket/get/{courseid}")
    public CourseMarket getCourseMarketById(@PathVariable("courseid") String courseid) {
        return courseService.getCourseMarketById(courseid);
    }

    /**
     * 更新课程营销信息
     * @param courseid
     * @param courseMarket
     * @return
     */
    @Override
    @PostMapping("/coursemarket/update/{courseid}")
    public ResponseResult updateCourseMarket(@PathVariable("courseid") String courseid, @RequestBody CourseMarket courseMarket) {
        CourseMarket courseMarket_update = courseService.updateCourseMarket(courseid, courseMarket);
        if(courseMarket_update != null){
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    /**
     * 保存课程图片
     *  /coursepic/add?courseId=xxx&pic=xxx
     * @param courseId
     * @param pic
     * @return
     */
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
        return courseService.addCoursePic(courseId,pic);
    }

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePic(courseId);
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    /**
     * 课程预览
     * @param courseId
     * @return
     */
    @Override
    @GetMapping("/courseview/{courseId}")
    public CourseView courseView(@PathVariable("courseId") String courseId) {
        return courseService.getCourseView(courseId);
    }

    /**
     * 添加页面信息(包含了课程基础信息,课程图片,课程营销,课程计划)
     * @param id
     * @return
     */
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    /**
     * 一键发布课程
     * @param courseId
     * @return
     */
    @Override
    @PostMapping("/publish/{courseId}")
    public CoursePublishResult publish(@PathVariable("courseId") String courseId){
        return courseService.publish(courseId);
    }

    /**
     * 保存媒资
     * @param teachplanMedia
     * @return
     */
    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }

}
