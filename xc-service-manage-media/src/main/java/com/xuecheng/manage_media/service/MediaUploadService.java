package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    //日志
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadService.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;//上传的视频文件的存储目录

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 1 文件上传前注册
     * @param fileMd5 文件唯一标识
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype mimetype类型
     * @param fileExt 文件扩展名
     * @return
     */
    public ResponseResult register(String fileMd5,String fileName,Long fileSize,String mimetype,String fileExt){
        //检查文件是否上传
        /**
         * 测试用例:
         *      fileFolderPath:  F:/Ffmpeg_Video/video/c/5/c5c75d70f382e6016d2f506d134eee11/
         *      filePath:  F:/Ffmpeg_Video/video/c/5/c5c75d70f382e6016d2f506d134eee11/c5c75d70f382e6016d2f506d134eee11.avi
         *      file: F:\Ffmpeg_Video\video\c\5\c5c75d70f382e6016d2f506d134eee11\c5c75d70f382e6016d2f506d134eee11.avi
         */
        //1 检查文件在磁盘上是否存在
        //文件所属目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //文件是否在磁盘中存在
        boolean exists = file.exists();

        //2 检查文件信息在mongoDB是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        //如果文件存在则直接返回
        if(exists && optional.isPresent()){
            //文件已经存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //文件不存在时作一些准备工作，检查文件所在目录是否存在，如果不存在则创建
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            fileFolder.mkdirs();
        }

        //返回操作结果
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 得到文件所在目录
     * @param fileMd5
     */
    private String getFileFolderPath(String fileMd5) {
        //F:/Ffmpeg_Video/video/
        String fileFolderPath = uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
        return fileFolderPath;
    }

    /**
     * 根据fileMd5得到文件路径
     *      规则:
     *      一级目录: md5的第一个字符
     *      二级目录: md5的第二个字符
     *      三级目录: md5
     *      文件名: md5+文件拓展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件拓展名
     * @return 文件路径
     */
    private String getFilePath(String fileMd5, String fileExt) {
        String filePath = uploadPath + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
        return filePath;
    }

    /**
     * 得到目录文件的相对路径,路径中去掉根目录
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFileFolderRelativePath(String fileMd5,String fileExt){
        String filePath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
        return filePath;
    }

    /**
     * 2 检查块文件是否存在
     *  检查分块文件是否上传，已上传则返回true。
     *  未上传则检查上传路径是否存在，不存在则创建
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5,Integer chunk,Integer chunkSize){
        /**
         * 测试用例:
         *      chunkFileFolderPath:  F:/Ffmpeg_Video/video/c/5/c5c75d70f382e6016d2f506d134eee11/chunks/
         *      chunkFile:   F:\Ffmpeg_Video\video\c\a\ca948a2bfd834949ec502f126edbf3d2\chunks\0
         */
        //检查分块文件是否存在
        //得到分块文件的所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //块文件的文件名称以1,2,3,...序号进行命名,没有拓展名
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if(chunkFile.exists()){
            //分块文件在系统已存在!
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }else {
            //分块文件在系统不存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,false);

        }
    }

    /**
     * 得到块文件所在目录路径
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        //uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/"
        String fileChunkFolderPath = getFileFolderPath(fileMd5) + "chunks" + "/";
        return fileChunkFolderPath;
    }

    /**
     * 3 上传分块 (将分块文件上传到指定的路径。)
     * @param file
     * @param fileMd5
     * @param chunk 分块
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file,Integer chunk, String fileMd5){
        //检查你所要上传的分块文件
        if(file == null){
            //上传的分块文件为空!
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }

        //检查分块目录，如果不存在则要自动创建
        //得到分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        //如果不存在则要自动创建
        if(!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }

        //得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = file.getInputStream();
            fileOutputStream = new FileOutputStream(new File(chunkFilePath));
            //Copies bytes from an <code>InputStream</code> to an OutputStream
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("upload chunk file fail:{}",e.getMessage());
            //分块上传失败!
            ExceptionCast.cast(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        }finally {
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
        // 返回操作结果
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块文件
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5,String fileName,Long fileSize,String mimetype,String fileExt){
        //1、合并所有分块
        //得到分块文件的所属目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        //块目录不存在则创建
        if(!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        //分块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //创建合并文件
        File mergeFile = new File(getFilePath(fileMd5,fileExt));

        //执行合并
        mergeFile = this.mergeFile(fileList, mergeFile);
        if(mergeFile == null){
            //合并文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //2、校验文件的md5值是否和前端传入的md5一到
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if(!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //3、将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimetype(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        String mediaId = mediaFile.getFileId();
        //=================视频上传成功然后开始向消息队列发送视频处理消息=====================
        //向MQ发送视频处理消息
        sendProcessVideoMsg(mediaId);

        //返回操作状态码
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 校验文件
     * @param mergeFile
     * @param md5
     * @return
     */
    private boolean checkFileMd5(File mergeFile, String md5) {
        try {
            //创建文件输入流
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(inputStream);

            //和传入的md5比较
            if(md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 将分块文件合并
     * @param chunkFileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
            //如果合并文件已存在则删除，否则创建新文件
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                //创建一个新文件
                mergeFile.createNewFile();
            }

            //对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;

                }
            });

            //创建写文件对象
            /*RandomAccessFile(File file, String mode): 创建一个随机访问文件流读，随意写来，由 File参数指定的文件。
             *  mode只能为下列方式的一种:
             *      1、r :以只读的方式打开文本，也就意味着不能用write来操作文件
             *      2、w :读操作和写操作都是允许的
             *      3、rws : 每当进行写操作，同步的刷新到磁盘，刷新内容和元数据
             *      4、rwd : 每当进行写操作，同步的刷新到磁盘，刷新内容
             */
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            //遍历分块文件开始合并
            //读取文件缓冲区
            byte[] b = new byte[1024];
            for (File chunkFile:
                 chunkFileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                //读取分块文件
                while ((len = raf_read.read(b))!= -1){
                    //向合并文件中写入数据
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            //返回合并后的文件
            return mergeFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送视频处理消息
     * @param mediaId
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId){
        //查询数据库mediaFile
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        //如果不存在
        if(!optional.isPresent()){
            //操作失败
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //构建消息内容
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        //JSON.toJSONString则是将对象转化为Json字符串
        String jsonString = JSON.toJSONString(map);

        try {
            //向MQ发送视频处理消息
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            //处理失败
            return new ResponseResult(CommonCode.FAIL);
        }
        //操作成功
        return new ResponseResult(CommonCode.SUCCESS);
    }

}
