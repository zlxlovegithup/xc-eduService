package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 查询页面信息dao
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
}
