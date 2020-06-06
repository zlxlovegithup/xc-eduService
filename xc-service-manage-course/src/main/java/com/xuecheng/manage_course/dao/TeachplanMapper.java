package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeachplanMapper {
    /**
     * 根据courseId查询教学计划树
     * @param courseId
     * @return
     */
    public TeachplanNode selectList(String courseId);
}
