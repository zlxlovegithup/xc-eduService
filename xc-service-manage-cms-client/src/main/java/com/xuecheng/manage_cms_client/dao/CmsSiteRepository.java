package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 查询站点信息dao
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {
}
