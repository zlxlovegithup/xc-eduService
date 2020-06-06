package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileSystemControllerApi {

    /**
     * 上传文件接口
     * @param multipartFile
     * @param filetag     //业务标签
     *                    //文件标签，由于文件系统服务是公共服务，文件系统服务会为使用文件系统服务的子系统分配文件标签，
     *                      用于标识此文件来自哪个系统。
     * @param businesskey //业务key
     *                    //文件系统服务为其它子系统提供的一个业务标识字段，各子系统根据自己的需求去使用，
     *                      比如：课程管理会在此字段中存储课程id用于标识该图片属于哪个课程。
     * @param metadata    //文件元信息
     * @return
     */
    public UploadFileResult upload(
            MultipartFile multipartFile,
            String filetag,
            String businesskey,
            String metadata
    );
}
