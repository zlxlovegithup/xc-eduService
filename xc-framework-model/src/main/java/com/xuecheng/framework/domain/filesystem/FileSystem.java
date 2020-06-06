package com.xuecheng.framework.domain.filesystem;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Created by mrt on 2018/2/5.
 */
@Data
@ToString
@Document(collection = "filesystem")
public class FileSystem {

    @Id
    //fastDFS返回文件的ID
    private String fileId;
    //文件请求路径 fastDFS浏览文件URL
    private String filePath;
    //文件大小
    private long fileSize;
    //文件名称
    private String fileName;
    //文件类型
    private String fileType;
    //图片宽度
    private int fileWidth;
    //图片高度
    private int fileHeight;
    //用户id，用于授权
    private String userId;
    //业务key
    //文件系统服务为其它子系统提供的一个业务标识字段，各子系统根据自己的需求去使用，比如：课程管理会在此字段中存储课程id用于标识该图片属于哪个课程。
    private String businesskey;
    //业务标签
    //文件标签，由于文件系统服务是公共服务，文件系统服务会为使用文件系统服务的子系统分配文件标签， 用于标识此文件来自哪个系统。
    private String filetag;
    //文件元信息
    private Map metadata;

}
