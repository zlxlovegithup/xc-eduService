package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data JPA,里面整合了mongodb常用操作方法,
 * 新建的dao接口只需要继承MongoRepository类并指定实体类和id类型就可以.
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {
}
