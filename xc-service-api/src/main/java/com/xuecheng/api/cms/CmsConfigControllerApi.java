package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * cms配置管理接口
 * 轮播图信息、精品推荐等信息存储在MongoDB的cms_conﬁg集合中
 */
@Api(value = "cms配置管理接口",description = "cms配置管理接口，提供数据模型的管理、查询接口") //@Api: 修饰整个类，描述Controller的作用
public interface CmsConfigControllerApi {

    /**
     * 根据id查询CMS配置信息
     * @param id 主键
     * @return
     */
    @ApiOperation("根据id查询CMS配置信息") // @ApiOperation：描述一个类的一个方法，或者说一个接口
    public CmsConfig getmodel(String id);

}
