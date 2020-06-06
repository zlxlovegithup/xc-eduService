package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemService.class);

    //配置虚拟机中的tracker_server服务器，可以有多个
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;

    //配置连接超时时间
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;

    //网络超时时间
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;

    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    @Autowired
    FileSystemRepository fileSystemRepository;

    /**
     * 加载fdfs的配置文件
     */
    private void initFdfsConfig(){
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);
        } catch (Exception e) {
            e.printStackTrace();
            //初始化文件系统出错
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

    /**
     * 上传文件
     * @param file 用于上传文件的类
     * @param filetag  //业务标签
     *                 //文件标签，由于文件系统服务是公共服务，文件系统服务会为使用文件系统服务的子系统分配文件标签， 用于标识此文件来自哪个系统
     * @param businesskey//业务key
     *                   //文件系统服务为其它子系统提供的一个业务标识字段，各子系统根据自己的需求去使用，比如：课程管理会在此字段中存储课程id用于标识该图片属于哪个课程。
     * @param metadata //文件元信息
     * @return
     */
    public UploadFileResult upload(MultipartFile file,
                                   String filetag,
                                   String businesskey,
                                   String metadata){
        if(file == null){
            //上传文件为空
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }

        //1第一大步:  上传文件到fdfs服务器
        String fileId = fdfs_upload(file);

        //2第二大步:   将文件信息存储到MongoDB数据库中(存储到数据库xc_fs的集合filesystem中)
        //创建文件信息对象
        FileSystem fileSystem = new FileSystem();

        //文件id
        fileSystem.setFileId(fileId);
        //文件在文件系统中的路径
        fileSystem.setFilePath(fileId);
        //业务标识
        fileSystem.setBusinesskey(businesskey);
        //标签
        fileSystem.setFiletag(filetag);
        //元数据
        if(StringUtils.isNotEmpty(metadata)){
            //idea中抓取异常的try/catch的快捷方式 快捷键: ctrl+alt+t
            try {
                //map: {"name","test001"}
                Map map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件名称
        fileSystem.setFileName(file.getOriginalFilename());
        //文件大小
        fileSystem.setFileSize(file.getSize());
        //文件类型
        fileSystem.setFileType(file.getContentType());

        //将文件信息存储到MongoDBS数据库中
        fileSystemRepository.save(fileSystem);

        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    /**
     * 上传文件到fdfs服务器,返回文件id
     * @param file
     * @return
     */
    private String fdfs_upload(MultipartFile file) {
        try {
            //加载fdfs配置文件
            initFdfsConfig();

            //创建tracker client
            TrackerClient trackerClient = new TrackerClient();
            //获取tracker server
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storage server
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storage client
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);

            //上传文件
            //上传字节
            byte[] bytes = file.getBytes();
            //获取文件原始名称 originalFilename:row.jpg
            String originalFilename = file.getOriginalFilename();
            //获取文件拓展名 extName  jpg
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //获取文件id
            String fileId = storageClient1.upload_file1(bytes, extName, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
