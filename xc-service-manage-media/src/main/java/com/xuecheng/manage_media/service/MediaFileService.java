package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {

    //日志
    private static Logger logger = LoggerFactory.getLogger(MediaFileService.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    /**
     * 分页查询我的媒体资源列表
     * @param page 当前页
     * @param size 每页条数
     * @param queryMediaFileRequest 查询参数
     * @return
     */
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest){
        if(queryMediaFileRequest == null){
            //没有就新建
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        //条件值对象
        MediaFile mediaFile = new MediaFile();
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }

        //条件适配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                                                      .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains()) //tag字段模糊匹配
                                                      .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains()) //文件原始名称模糊匹配
                                                      .withMatcher("processStatus",ExampleMatcher.GenericPropertyMatchers.exact());//如果不设置匹配器默认精确匹配
        //定义example条件对象
        Example<MediaFile> example = Example.of(mediaFile,exampleMatcher);
        //分页条件查询
        if(page<=0){
            page = 1; //默认当前页为第一页
        }
        page = page - 1;
        if(size<=0){
            size = 10; //默认每一页10条数据
        }

        Pageable pageable = new PageRequest(page,size);
        //执行分页查询
        Page<MediaFile> all = mediaFileRepository.findAll(example, pageable);
        //获取总记录数
        long totalElements = all.getTotalElements();
        //获取数据列表
        List<MediaFile> content = all.getContent();

        //返回数据列表集
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(content);
        queryResult.setTotal(totalElements);

        //返回结果
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

}
