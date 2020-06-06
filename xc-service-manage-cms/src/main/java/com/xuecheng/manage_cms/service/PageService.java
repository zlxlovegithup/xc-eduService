package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;
    /**
     * 页面列表分页查询
     * @param page 当前页码
     * @param size 页面显示个数
     * @param queryPageRequest 查询条件
     * @return 页面列表
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        if(queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }
        if(page <= 0){
            page=1;
        }
        page=page-1;//为了适应mongodb的接口将页码减1
        if(size <= 0){
            size = 20;
        }
        //分页对象
        Pageable pageable = new PageRequest(page,size);

        //构建条件查询器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        //条件匹配器
        /**
         * ExampleMatcher.GenericPropertyMatchers.contains();  //包含
         * ExampleMatcher.GenericPropertyMatchers.endsWith(); //以某某结尾
         * ExampleMatcher.GenericPropertyMatchers.caseSensitive();//严格区分大小写
         * ExampleMatcher.GenericPropertyMatchers.exact(); //精确匹配
         * ExampleMatcher.GenericPropertyMatchers.ignoreCase();//忽略大小写
         * ExampleMatcher.GenericPropertyMatchers.regex();//正则表达式匹配
         * ExampleMatcher.GenericPropertyMatchers.startsWith();//以某某开始
         * ExampleMatcher.GenericPropertyMatchers.storeDefaultMatching();//默认规则
         */
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains())//包含
                                       .withMatcher("pageName",ExampleMatcher.GenericPropertyMatchers.contains());
        //设置条件查询
        CmsPage cmsPage = new CmsPage();
        //设置站点id为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置页面id为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageId())){
            cmsPage.setPageId(queryPageRequest.getPageId());
        }
        //设置页面名称为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageName())){
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        //设置模板id为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        //创建条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //分页查询 指定条件进行插叙
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult<CmsPage> cmsPageQueryResult = new QueryResult<CmsPage>();
        cmsPageQueryResult.setList(all.getContent()); //数据列表
        cmsPageQueryResult.setTotal(all.getTotalElements());//数据总记录数
        //返回结果
        return new QueryResponseResult(CommonCode.SUCCESS,cmsPageQueryResult);
    }

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage){
        //校验cmsPage是否为空值
        if(cmsPage == null){
            //抛出异常,非法参数异常
            //...
        }
        //查询页面是否存在CmsCode
        //校验页面名称、站点Id、页面webpath的唯一性
        //根据页面名称、站点Id、页面webpath去cms_page集合，如果查到说明此页面已经存在，如果查询不到再继续添加
        CmsPage byPageNameAndSiteIdAndPageWebPath = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        //如果查询的页面已经存在
        if(byPageNameAndSiteIdAndPageWebPath!=null){
            //抛出异常,已经存在相同的页面
            //页面已经存在
            //抛出异常，异常内容就是页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //如果不存在
        //调用dao新增页面
        cmsPage.setPageId(null);//添加页面主键由spring data 自动生成
        CmsPage save = cmsPageRepository.save(cmsPage);//新增页面
        //返回结果
        //CmsPageResult封装了实体类CmsPage以及操作是否成功,操作代码,提示信息等等
        CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS,save);
        return cmsPageResult;
    }

    /**
     * 根据id查询页面
     * @param id
     * @return
     */
    public CmsPage getById(String id){
        //根据SpringData提供的findById方法完成根据主键查询
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        //如果查询到了数据 直接将数据进行返回
        if(optional.isPresent()){
            return optional.get();
        }
        //返回空值
        return null;
    }

    /**
     * 更新页面信息
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id,CmsPage cmsPage){
        //根据id查询页面信息
        CmsPage cmspage1 = this.getById(id);
        if(cmspage1 != null){
            //将模板进行更新
            //更新模板id
            cmspage1.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点id
            cmspage1.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmspage1.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cmspage1.setPageName(cmsPage.getPageName());
            //更新访问路径
            cmspage1.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cmspage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            cmspage1.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage save = cmsPageRepository.save(cmspage1);
            if(save!=null){
                //CmsPageResult封装了实体类CmsPage以及操作是否成功,操作代码,提示信息等等
                CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS,save);
                return cmsPageResult;
            }
        }
        //返回失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    /**
     * 根据id删除页面
     * @param id
     * @return
     */
    public ResponseResult delete(String id){
        //根据id查询页面
        CmsPage byId = this.getById(id);
        if(byId!=null){
            //删除页面
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 页面静态化
     *      步骤:
     *      1、填写页面DataUrl 在编辑cms页面信息界面填写DataUrl，将此字段保存到cms_page集合中。
     *      2、静态化程序获取页面的DataUrl
     *      3、静态化程序远程请求DataUrl获取数据模型。
     *      4、静态化程序获取页面的模板信息
     *      5、执行页面静态化
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId){
        //第一大步:  获取页面模型数据  (位于集合cms_config中)  pageId = 5e6df21a083c89ba84a0442e
        Map model = this.getModelByPageId(pageId);
        if(model == null){
            //根据页面的数据url获取不到数据
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //第二大步: 获取页面 cms_page
        String templateContent = getTemplateByPageId(pageId);
        if(StringUtils.isEmpty(templateContent)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //第三大步: 执行静态化
        String html = generateHtml(templateContent, model);
        if(StringUtils.isEmpty(html)){
            //生成的静态html为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    /**
     * 获取页面模型数据
     *  集合cms_config存放的就是静态化页面所需要的模板
     * @param pageId
     */
    private Map getModelByPageId(String pageId) {
        //查询页面信息 pageId = 5e6df21a083c89ba84a0442e
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            //页面不存在 抛出异常("你请求的页面不存在!")
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataUrl  dataUrl = http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            //从页面信息中找不到获取数据的url
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //根据dataUrl远程请求页面静态化所需要的数据   id -> 5a791725dd573c3574ee333f
        //集合cms_config存放的是页面静态化所需要的数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        //将数据存入map集合
        Map body = forEntity.getBody();
        return body;
    }

    /**
     * 获取页面模板
     * @param pageId
     */
    private String getTemplateByPageId(String pageId) {
        //查询页面信息
        CmsPage cmspage = this.getById(pageId);
        if(cmspage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //查询页面模板id
        String templateId = cmspage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //根据模板id找到模板页面
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        //如果不为空
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //取出模板id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据模板id取出模板文件内容  (模板文件内容位于集合fs.files中)
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //输出流的内容
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 页面静态化
     * @param templateContent 页面内容
     * @param model 模板
     */
    private String generateHtml(String templateContent, Map model) {
        //生成配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            //获取模板
            Template template = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发布页面
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId){
        //执行静态化 请输入测试页面的: 测试发布pageId = 5e6df21a083c89ba84a0442e  正式发布pageid:5e8956c3083c8978f499a95a
        String pageHtml = this.getPageHtml(pageId);
        if(StringUtils.isEmpty(pageHtml)){
            //生成的静态html为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        //保存静态文件到GridFS服务器上
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);

        //发送消息(发送消息到MQ队列)
        sendPostPage(pageId);

        //返回发送成功操作码
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送页面发布消息
     * @param pageId
     */
    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            //你请求的页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("pageId",pageId);
        //消息内容
        String msg = JSON.toJSONString(msgMap);
        //获取站点id(siteId)作为routingKey (它指定交换机发送消息到哪个队列上)
        String siteId = cmsPage.getSiteId();
        //发布消息 将pageId发送到交换机中(指定routingKey为siteId)
        //参数1:交换机  参数2:路由key  参数3:向交换机发送的消息
        this.rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,msg);
    }

    /**
     * 保存静态页面内容
     * @param pageId
     * @param content
     * @return
     */
    private CmsPage saveHtml(String pageId,String content){
        //查询页面
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            //你请求的页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();

        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if(StringUtils.isNotEmpty(htmlFileId)){
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }

        //保存html文件到GridFs
        InputStream inputStream = IOUtils.toInputStream(content);
        ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());

        //文件id  objectId = 5e72421d083c8983308a5a37 位于fs.files集合中
        String fileId = objectId.toString();

        //将文件id存储到cmsPage中
        cmsPage.setHtmlFileId(fileId);
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    /**
     * 添加课程详情页面,如果页面已经存在就更新页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //检查页面是否存在,根据页面名称,站点Id，pageWebPath查询
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(one != null){
            //有页面则更新页面
            return this.update(one.getPageId(),cmsPage);
        }else{
            //没有页面则新增页面
            return this.add(cmsPage);
        }
    }

    /**
     * 页面的一键发布(先保存到MongoDB数据库中然后再进行发布)
     * @param cmsPage
     * @return
     * 1:  将CmsPage保存到MongoDB数据库中         -------CMS服务
     * 2:  发布页面
     *      a):执行页面静态化                     -------CMS服务
     *      b):将静态化了的文件存储到GridFS服务器上 -------CMS服务
     *      c):向MQ发送消息                       -------CMS服务
     *      d):由MQ向CMSClient服务发送消息         -------MQ
     *      e):CMSClient接收到了MQ发送过来的消息，去查询页面的文件 --------CMSClient服务
     *      f):去GridFS下载静态文件                             --------CMSClient服务
     * 3:   返回页面的发布结果                     --------CMS服务
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage){
        //1 将CmsPage保存到MongoDB数据库中
        //添加课程详情页面(将课程存储到MongoDB数据库)
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
            //保存失败
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        //获取保存好了的课程页面
        CmsPage cmsPage1 = save.getCmsPage();
        //获取将要发布的课程页面的pageId
        String pageId = cmsPage1.getPageId();

        //2 发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()){
            //发送失败
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }

        //得到页面的URL
        //页面URL=站点域名+站点webpath+页面webpath+页面名称
        //获取站点id
        String siteId = cmsPage1.getSiteId();

        //查询站点信息
        CmsSite cmsSite = findCmsSiteById(siteId);
        String siteDomain = cmsSite.getSiteDomain(); //站点域名
        String siteWebPath = cmsSite.getSiteWebPath();//站点web路径
        String pageWebPath = cmsPage1.getPageWebPath();//页面web路径
        String pageName = cmsPage1.getPageName(); //页面名称

        //拼接页面的web访问路径
        String pageUrl = siteDomain + siteWebPath +pageWebPath + pageName;

        //3: 返回页面的发布结果
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 根据站点id查询站点信息
     * @param siteId
     * @return
     */
    private CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
