package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {
    /**
     * 从Teachplan_media表中根据课程id查询媒资信息
     * @param courseId
     * @return
     */
    List<TeachplanMedia> findByCourseId(String courseId);
}
