package com.xuecheng.framework.domain.cms;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@ToString
@Document(collection = "cms_config") //是Spring Data mongodb提供的注解，最终CMS的开发会使用Mongodb数据库
public class CmsConfig {

    @Id
    private String id;
    private String name;
    private List<CmsConfigModel> model;

}
