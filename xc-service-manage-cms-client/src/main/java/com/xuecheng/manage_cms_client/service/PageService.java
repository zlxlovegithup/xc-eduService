package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 保存页面静态文件到服务器物理路径
     * @param pageId
     */
    public void savePageToServerPath(String pageId){
        //根据页面id获取页面信息 (接收到了生产者发送过来的消息pageId)
        CmsPage cmsPage = this.getCmsPageById(pageId);
        //得到html的文件id，从cmsPage中获取htmlFileId内容
        String htmlFileId = cmsPage.getHtmlFileId();

        //从gridFS中查询html文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if(inputStream == null){
            LOGGER.error("getFileById InputStream is null ,htmlFileId:{}",htmlFileId);
            return;
        }

        //查询页面所属站点
        CmsSite cmsSite = this.getCmsSiteById(cmsPage.getSiteId());

        //得到站点的物理路径
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        //得到页面的物理路径
        String pagePhysicalPath = cmsPage.getPagePhysicalPath();
        //得到页面名称
        String pageName = cmsPage.getPageName();

        //获取页面物理路径: 页面物理路径=站点物理路径+页面物理路径+页面名称
        //pagePath = E:/workspace_xcEdu/xcEduUI01/xc-ui-pc-static-portal/course/detail/4028e581617f945f01617f9dabc40000
        String pagePath = sitePhysicalPath + pagePhysicalPath + pageName;

        FileOutputStream fileOutputStream = null;
        try {
            //将html文件保存到服务器物理路径上
            fileOutputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件id从GridFS中查询文件内容
     * @param htmlFileId
     * @return
     */
    private InputStream getFileById(String htmlFileId) {
        //得到文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据站点id获取站点
     * @param siteId
     * @return
     */
    private CmsSite getCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(!optional.isPresent()){
            //请求的站点不存在
            ExceptionCast.cast(CmsCode.CMS_SITE_NOTEXISTS);
            return null;
        }
        CmsSite cmsSite = optional.get();
        return cmsSite;
    }

    /**
     * 根据页面id获取页面信息
     * @param pageId
     * @return
     */
    private CmsPage getCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            //你请求的页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
            return null;
        }
        //取出页面物理路径
        CmsPage cmsPage = optional.get();
        return cmsPage;
    }

}
