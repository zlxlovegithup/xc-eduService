package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.request.RequestData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 1: 定义请求模型QueryPageRequest，此模型作为查询条件类型
 *    为后期扩展需求，请求类型统一继承RequestData类型
 * 2: 响应结果类型，分页查询统一使用QueryResponseResult
 */
@Data //自动生成setter/getter equals 构造方法等
public class QueryPageRequest extends RequestData {
    //站点id
    @ApiModelProperty("站点id")
    private String siteId;
    //页面id
    @ApiModelProperty("页面id")
    private String pageId;
    //页面名称
    @ApiModelProperty("页面名称")
    private String pageName;
    //页面别名
    @ApiModelProperty("页面别名")
    private String pageAliase;
    //模板id
    @ApiModelProperty("模板id")
    private String templateId;
    //...
}
