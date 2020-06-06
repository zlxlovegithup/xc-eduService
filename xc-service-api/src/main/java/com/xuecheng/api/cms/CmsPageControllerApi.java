package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 分页查询接口
 *   @Api修饰整个类，描述Controller的作用
 */
@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {
    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @param queryPageRequest 你所需要的查询条件(例如:站点id,页面id,页面名称,页面别名,模板id...)
     * @return 封装了操作状态(操作是否成功),操作代码,提示信息
     *
     * @ApiOperation：描述一个类的一个方法，或者说一个接口
     * @ApiImplicitParams：多个请求参数
     */
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams(  //@ApiImplicitParams：多个请求参数
            {@ApiImplicitParam(name="page",  //@ApiImplicitParam：一个请求参数
                    value="页 码",
                    required=true,
                    paramType="path",
                    dataType="int"),
             @ApiImplicitParam(name="size",
                    value="每页记录数",
                    required=true,
                    paramType="path",
                    dataType="int")
            })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    @ApiOperation("新增页面")
    public CmsPageResult add(CmsPage cmsPage);

    /**
     * 通过id查询页面
     * @param id
     * @return
     */
    @ApiOperation("通过ID查询页面")
    public CmsPage findById(String id);

    /**
     * 修改页面
     * @param id
     * @param cmsPage
     * @return
     */
    @ApiOperation("修改页面")
    public CmsPageResult edit(String id,CmsPage cmsPage);

    /**
     * 删除页面
     * @param id
     * @return
     */
    @ApiOperation("通过id删除页面")
    public ResponseResult deleteById(String id);

    /**
     * 管理员通过 cms系统发布“页面发布”的消费，cms系统作为页面发布的生产方。
     * 需求如下：
     * 1、管理员进入管理界面点击“页面发布”，前端请求cms页面发布接口。
     * 2、cms页面发布接口执行页面静态化，并将静态化页面存储至GridFS中。
     * 3、静态化成功后，向消息队列发送页面发布的消息。
     *      1） 获取页面的信息及页面所属站点ID。
     *      2） 设置消息内容为页面ID。（采用json格式，方便日后扩展）
     *      3） 发送消息给ex_cms_postpage交换机，并将站点ID作为routingKey。
     */
    /**
     * 发布页面接口
     * @param pageId
     * @return
     */
    @ApiOperation("发布页面")
    public ResponseResult post(String pageId);

    @ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
