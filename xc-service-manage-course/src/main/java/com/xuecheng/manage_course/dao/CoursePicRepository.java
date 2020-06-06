package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 课程图片操作Dao
 */
public interface CoursePicRepository extends JpaRepository<CoursePic,String> {
     //自定义删除课程图片的方法,deleteById方法中没有返回值,注意: 返回值为long类型而不是boolean类型
     long deleteByCourseid(String courseid);
}
