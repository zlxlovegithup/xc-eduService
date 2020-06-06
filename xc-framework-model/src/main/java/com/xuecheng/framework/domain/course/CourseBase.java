package com.xuecheng.framework.domain.course;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Data
@ToString
@Entity
@Table(name="course_base")
//@GenericGenerator(name = "jpa-assigned", strategy = "assigned")
//使用JPA的UUID主键生成策略
//注意@GenericGenerator(name = "jpa-uuid", strategy = "uuid") 和 @GeneratedValue(generator = "jpa-uuid") 两个注解是生成策略核心注解。
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class CourseBase implements Serializable {
    private static final long serialVersionUID = -916357110051689486L;
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 32)
    private String id;
    private String name;
    private String users;
    private String mt;
    private String st;
    private String grade;
    private String studymodel;
    private String teachmode;
    private String description;
    private String status;
    @Column(name="company_id")
    private String companyId;
    @Column(name="user_id")
    private String userId;

}
