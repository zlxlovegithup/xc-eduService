package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data JPA,里面整合了mongodb常用操作方法,
 * 新建的dao接口只需要继承MongoRepository类并指定实体类和id类型就可以.
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    /**
     * 根据页面名称,站点id,访问路径查询页面(此方法用于校验页面是否存在)
     * @param pageName 页面名称
     * @param siteId  站点id
     * @param pageWebPath  访问路径
     * @return
     */
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);
}
