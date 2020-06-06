package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.LearningCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

@Service
public class LearningService {

    @Autowired
    CourseSearchClient courseSearchClient;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;

    /**
     * 根据课程id和课程计划id获取课程视频(课程媒资)播放地址
     * @param courseId
     * @param teachplanId
     * @return
     */
    public GetMediaResult getMedia(String courseId,String teachplanId){
        //校验学生的学习权限..(后面将实现)
        //根据课程计划id查询课程媒资(调用查询服务进行查询)
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getMedia(teachplanId);
        if(teachplanMediaPub == null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())){
            //获取视频播放地址出错 
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        //返回成功代码以及课程媒资地址
        return new GetMediaResult(CommonCode.SUCCESS,teachplanMediaPub.getMediaUrl());
    }

    /**
     *  学生选课
     *  实现思路:
     *      向xc_learning_course添加记录，为保证不重复添加选课，先查询历史任务表，如果从历史任务表查询不到任务说
     *      明此任务还没有处理，此时则添加选课并添加历史任务。
     * @param userId
     * @param courseId
     * @param valid
     * @param startTime
     * @param endTime
     * @param xcTask
     * @return
     */
    @Transactional
    public ResponseResult addCourse(String userId,
                                    String courseId,
                                    String valid,
                                    Date startTime,
                                    Date endTime,
                                    XcTask xcTask){
        if(StringUtils.isEmpty(courseId)){
            //获取课程媒资错误!
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        if(StringUtils.isEmpty(userId)){
            //获取的用户不存在!
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_USERISNULL);
        }
        if(xcTask == null || StringUtils.isEmpty(xcTask.getId())){
            //获取的任务不存在!
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_TASKISNULL);
        }
        //根据任务id查询历史任务
        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
        if(optional.isPresent()){
            //操作成功！
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //根据用户和课程查询选课记录，用于判断是否添加选课
        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findXcLearningCourseByUserIdAndCourseId(userId,courseId);
        //没有选课记录则添加
        if(xcLearningCourse == null){
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        } else {//有选课记录则更新日期
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        }
        //查询出历史记录
        Optional<XcTaskHis> XcTaskHisOptional = xcTaskHisRepository.findById(xcTask.getId());
        //向历史任务表播入记录
        if(!XcTaskHisOptional.isPresent()){
            //添加历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            //赋值
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
        }
        //返回操作结果
        return new ResponseResult(CommonCode.SUCCESS);
    }

}
