package com.xuecheng.framework.domain.course;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by admin on 2018/2/10.
 */
@Data
@ToString
@Entity
@Table(name="course_pub")
@GenericGenerator(name = "jpa-assigned", strategy = "assigned")
public class CoursePub implements Serializable {
    private static final long serialVersionUID = -916357110051689487L;
    @Id
    @GeneratedValue(generator = "jpa-assigned")
    @Column(length = 32)
    private String id;      //课程id
    private String name;    //课程名称
    private String users;   //适用人群
    private String mt;      //大分类(一级分类)
    private String st;      //小分类(二级分类)
    private String grade;   //课程等级
    private String studymodel;  //学习模式
    private String teachmode;   //教育模式
    private String description; //课程介绍
    private String pic;         //图片
    private Date timestamp;     //时间戳(logstash更新课程时使用)
    private String charge;      //收费规则(对应数据字典)
    private String valid;       //有效性(对应数据字典)
    private String qq;          //咨询QQ
    private Double price;        //价格
    private Double price_old;    //原价格
    private String expires;     //过期时间
    private String teachplan;   //课程计划
    @Column(name="pub_time")
    private String pubTime;//课程发布时间
}
