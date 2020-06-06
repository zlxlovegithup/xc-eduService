package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "媒体资源管理接口",description = "媒体资源管理接口,提供文件注册,上传,分块检查,合并文件等功能")
public interface MediaUploadControllerApi {

    @ApiOperation("文件上传注册")
    public ResponseResult register(String fileMd5, //文件唯一标识
                                   String fileName,
                                   Long fileSize,
                                   String mimeType,
                                   String fileExt); //文件扩展名

    @ApiOperation("分块检查")
    public CheckChunkResult checkchunk(String fileMd5,
                                       Integer chunk,
                                       Integer chunkSize);

    @ApiOperation("上传分块")
    public ResponseResult uploadchunk(MultipartFile file,
                                      Integer chunk,
                                      String fileMd5);

    @ApiOperation("合并分块")
    public ResponseResult mergechunks(String fileMd5,  //文件唯一标识
                                      String fileName,
                                      Long fileSize,
                                      String mimeType,
                                      String fileExt);

}
