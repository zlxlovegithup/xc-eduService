package com.xuecheng.manage_media_process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFfmpeg {

    @Test
    public void testProcessBuilder(){
        //ProcessBuilder类是java.lang包下的基础类，在使用时无需导入，可以直接使用。它主要用于创建和运行各类外部程序
        ProcessBuilder processBuilder = new ProcessBuilder();
//        processBuilder.command("ping","127.0.0.1");
        processBuilder.command("ipconfig");
        //将标准输入流和错误输入流合并，通过标准输入流读取信息
        processBuilder.redirectErrorStream(true);

        try {
            //启动进程
            Process start = processBuilder.start();
            //获取输入流
            InputStream inputStream = start.getInputStream();

            //转为字符输入流
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"gbk");

            int len = -1;
            //缓冲区大小(字符数组)
            char[] c = new char[1024];
            StringBuffer ouputString = new StringBuffer();
            //读取进程输入流中的内容
            while ((len = inputStreamReader.read(c))!=-1){
                String s = new String(c,0,len);
                ouputString.append(s);
                System.out.println(s);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testFFmpeg(){
        //ProcessBuilder类是java.lang包下的基础类，在使用时无需导入，可以直接使用。它主要用于创建和运行各类外部程序
        ProcessBuilder processBuilder = new ProcessBuilder();

        //定义命令内容
        List<String> command = new ArrayList<>();
        command.add("D:\\ffmpeg-20200412-f1894c2-win64-static\\bin\\ffmpeg.exe");
        command.add("-i");

        command.add("F:\\Ffmpeg_Video\\solr.avi");
        command.add("-y");//覆盖输出文件

        command.add("-c:v");//-c:v 视频编码为x264 ，x264编码是H264的一种开源编码格式。
        command.add("libx264");

        command.add("-s");//-s 设置分辨率
        command.add("1280x720");

        command.add("-pix_fmt"); //-pix_fmt yuv420p：设置像素采样方式，主流的采样方式有三种，YUV4:4:4，YUV4:2:2，YUV4:2:0，它的作用是 根据采样方式来从码流中还原每个像素点的YUV（亮度信息与色彩信息）值。
        command.add("yuv420p");

        command.add("-b:a");//-b 设置码率，-b:a和-b:v分别表示音频的码率和视频的码率，-b表示音频加视频的总码率。码率对一个视频质量有 很大的作用，后边会介绍。
        command.add("63k");
        command.add("-b:v");
        command.add("753k");

        command.add("-r");//-r：帧率，表示每秒更新图像画面的次数，通常大于24肉眼就没有连贯与停顿的感觉了
        command.add("18");

        command.add("F:\\Ffmpeg_Video\\solr.mp4");

        //执行命令
        processBuilder.command(command);

        //将标准输入流和错误输入流合并，通过标准输入流读取信息
        processBuilder.redirectErrorStream(true);

        try {
            //启动进程
            Process start = processBuilder.start();
            //获取输入流
            InputStream inputStream = start.getInputStream();
            //转为字符输入流
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"gbk");

            int len = -1;
            char[] c = new char[1024];
            StringBuffer outputString = new StringBuffer();
            //读取进程输入流中的内容
            while ((len = inputStreamReader.read(c))!=-1){
                String s = new String(c,0,len);
                outputString.append(s);
                System.out.println(s);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
