package com.xuecheng.framework.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 此文件用于视频文件处理，步骤如下：
 * 1、生成mp4
 * 2、生成m3u8
 *
 */
public class HlsVideoUtil extends  VideoUtil {
    //ffmpeg执行命令的目录
    String ffmpeg_path = "D:\\ffmpeg-20200412-f1894c2-win64-static\\bin\\ffmpeg.exe";//ffmpeg的安装位置
    //视频文件存放的目录
    String video_path = "F:\\Ffmpeg_Video\\test1\\test1.avi";
    //需要转换的目标文件
    String m3u8_name = "test1.m3u8";
    //需要转换的目标文件存放目录
    String m3u8folder_path = "F:\\Ffmpeg_Video\\test1\\";
    public HlsVideoUtil(String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path){
        super(ffmpeg_path);
        this.ffmpeg_path = ffmpeg_path;
        this.video_path = video_path;
        this.m3u8_name = m3u8_name;
        this.m3u8folder_path = m3u8folder_path;
    }

    /**
     * 清理m3u8文件目录
     * @param m3u8_path
     */
    private void clear_m3u8(String m3u8_path){
        //删除原来已经生成的m3u8及ts文件
        File m3u8dir = new File(m3u8_path);
        if(!m3u8dir.exists()){
            m3u8dir.mkdirs();
        }
       /* if(m3u8dir.exists()&&m3u8_path.indexOf("/hls/")>=0){//在hls目录方可删除，以免错误删除
            String[] children = m3u8dir.list();
            //删除hls目录中的文件
            for (int i = 0; i < children.length; i++) {
                File file = new File(m3u8_path, children[i]);
                file.delete();
            }
        }else{
            m3u8dir.mkdirs();
        }*/
    }

    /**
     * 生成m3u8文件
     * @return 成功则返回success，失败返回控制台日志
     */
    public String generateM3u8(){
        //清理m3u8文件目录
        clear_m3u8(m3u8folder_path);
 /*
        //你需要提前将ffmpeg的运行命令配置到环境变量中
        ffmpeg.exe -i F:/Ffmpeg_Video/test1/test1.avi -hls_time 10 -hls_list_size 0 -hls_segment_filename F:/Ffmpeg_Video/test1/test1_%5d.ts
                   F:/Ffmpeg_Video/test1/test1.m3u8
         */
//        String m3u8_name = video_name.substring(0, video_name.lastIndexOf("."))+".m3u8";
        List<String> commend = new ArrayList<String>();
        commend.add(ffmpeg_path);
        commend.add("-i");
        commend.add(video_path);
        commend.add("-hls_time"); //-hls_time 设置每片的长度，单位为秒
        commend.add("10");

        commend.add("-hls_list_size");//-hls_list_size n: 保存的分片的数量，设置为0表示保存所有分片
        commend.add("0");

        commend.add("-hls_segment_filename");//-hls_segment_ﬁlename ：段文件的名称，%05d表示5位数字
//        commend.add("F:/Ffmpeg_Video/test1/test1_%05d.ts");
        commend.add(m3u8folder_path  + m3u8_name.substring(0,m3u8_name.lastIndexOf(".")) + "_%05d.ts");
//        commend.add("F:/Ffmpeg_Video/test1/test1.m3u8");
        commend.add(m3u8folder_path  + m3u8_name );
        String outstring = null;
        try {
            //ProcessBuilder类是java.lang包下的基础类，在使用时无需导入，可以直接使用。它主要用于创建和运行各类外部程序
            ProcessBuilder builder = new ProcessBuilder();
            //执行命令
            builder.command(commend);
            //将标准输入流和错误输入流合并，通过标准输入流程读取信息
            builder.redirectErrorStream(true);
            Process p = builder.start();
            outstring = waitFor(p);

        } catch (Exception ex) {

            ex.printStackTrace();

        }
        //通过查看视频时长判断是否成功
        Boolean check_video_time = check_video_time(video_path, m3u8folder_path + m3u8_name);
        //不相等
        if(!check_video_time){
            return outstring;
        }
        //通过查看m3u8列表判断是否成功
        List<String> ts_list = get_ts_list();
        if(ts_list == null){
            return outstring;
        }
        return "success";


    }



    /**
     * 检查视频处理是否完成
     * @return ts列表
     */
    public List<String> get_ts_list() {
//        String m3u8_name = video_name.substring(0, video_name.lastIndexOf("."))+".m3u8";
        List<String> fileList = new ArrayList<String>();
        List<String> tsList = new ArrayList<String>();
        //m3u8文件路径
        String m3u8file_path =m3u8folder_path + m3u8_name;
        BufferedReader br = null;
        String str = null;
        String bottomline = "";
        try {
            br = new BufferedReader(new FileReader(m3u8file_path));
            while ((str = br.readLine()) != null) {
                bottomline = str;
                if(bottomline.endsWith(".ts")){
                    tsList.add(bottomline);
                }
                //System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //如果并且只有当此字符串包含指定的字符序列的字符串值，则返回真值
        if (bottomline.contains("#EXT-X-ENDLIST")) {
//            fileList.add(hls_relativepath+m3u8_name);
            fileList.addAll(tsList);
            return fileList;
        }
        return null;

    }



    public static void main(String[] args) throws IOException {
        String ffmpeg_path = "D:\\ffmpeg-20200412-f1894c2-win64-static\\bin\\ffmpeg.exe";//ffmpeg的安装位置
        String video_path = "F:\\Ffmpeg_Video\\test1\\test1.avi";
        String m3u8_name = "test1.m3u8";
        String m3u8_path = "F:\\Ffmpeg_Video\\test1\\";
        HlsVideoUtil videoUtil = new HlsVideoUtil(ffmpeg_path,video_path,m3u8_name,m3u8_path);
        String s = videoUtil.generateM3u8();
        System.out.println(s);
        System.out.println(videoUtil.get_ts_list());
    }
}
