package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {
    //日志类
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;

    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    @Autowired
    MediaFileRepository mediaFileRepository;

    /**
     * 接受视频处理消息进行视频处理
     * @param msg
     */
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",
                    containerFactory ="customContainerFactory" )
    public void receiveMediaProcessTask(String msg){
        //1、解析消息内容，得到mediaId
        //msg是前台发送过来的视频处理消息
        //JSON.parseObject，是将Json字符串转化为相应的对象
        Map map = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive media process task msg :{}",msg);
        //解析消息
        String mediaId = (String) map.get("mediaId");

        //2、拿mediaId从数据库查询文件信息
        //获取媒体资源文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        //如果不存在
        if(!optional.isPresent()){
            return;
        }
        //取出值
        MediaFile mediaFile = optional.get();
        
        //媒体资源文件类型
        String fileType = mediaFile.getFileType();
        //暂时处理的文件只有avi的视频文件
        if(fileType == null || !fileType.equals("avi")){
            mediaFile.setProcessStatus("303004");//处理状态为无需处理
            //保存到数据库
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            //不是avi的文件此将处理状态标识为未处理
            mediaFile.setProcessStatus("303001");
            //保存到数据库
            mediaFileRepository.save(mediaFile);
        }

        //3、使用工具类将avi文件生成mp4
        //生成.mp4文件
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        //mp4文件名称为: 文件Id+".mp4"后缀
        String mp4_name = mediaFile.getFileId() + ".mp4";
        //mp4文件的存放目录
        //例如: F:/Ffmpeg_Video/video/c/5/c5c75d70f382e6016d2f506d134eee11/
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        //创建工具类对象
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        //视频编码，生成mp4文件
        String result = mp4VideoUtil.generateMp4();
        if(result==null || !result.equals("success")){
            //操作失败写入处理日志
            mediaFile.setProcessStatus("303003");
            //存放视频处理结果的类
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            //将视频处理结果设置进入MediaFile实体类
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            //保存到数据库
            mediaFileRepository.save(mediaFile);
            return;
        }

        //4、将mp4生成m3u8和ts文件
        //mp4视频文件路径
        //F:/Ffmpeg_Video/video + c/5/c5c75d70f382e6016d2f506d134eee11/ +c5c75d70f382e6016d2f506d134eee11.mp4
        String mp4_video_path = serverPath + mediaFile.getFilePath() + mp4_name;
        //m3u8_name文件名称
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        //m3u8文件所在目录
        String m3u8folder_path = serverPath + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4_video_path,m3u8_name,m3u8folder_path);
        //生成m3u8和ts文件
        String tsResult = hlsVideoUtil.generateM3u8();
        if(tsResult == null || !tsResult.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            //定义mediaFileProcess_m3u8
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            //记录失败原因
            mediaFileProcess_m3u8.setErrormsg(tsResult);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //处理成功
        //获取ts文件列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //处理状态为处理成功
        mediaFile.setProcessStatus("303002");
        //定义mediaFileProcess_m3u8 (记录处理结果的类)
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        //保存fileUrl（此url就是视频播放的相对路径）
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);
        //保存到数据库
        mediaFileRepository.save(mediaFile);
    }
}
