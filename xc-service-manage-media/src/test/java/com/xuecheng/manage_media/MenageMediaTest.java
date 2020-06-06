package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MenageMediaTest {
    /**
     * 测试文件分块方法
     * @throws IOException
     */
    @Test
    public void testChunk() throws IOException {
        //1 源文件
        File sourceFile = new File("F:/Ffmpeg_Video/lucene.mp4");
        //File sourceFile2 = new File("");

        //2 目标文件
        String chunkPath = "F:/Ffmpeg_Video/chunk/chunk";
        File chunkFolder = new File(chunkPath);

        if(!chunkFolder.exists()){
            chunkFolder.mkdirs();
        }
        //设置分块大小
        long chunkSize = 1024*1024*1; //1M
        //设置分块数量
        //ceil(double a) : 返回最小（接近负无穷大）double值是大于或等于的说法，等于一个数学整数。
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 /chunkSize);
        long chunkNum2 = (long) Math.ceil(6*1.0/5); //1.2-->2
        long chunkNum3 = (long) Math.ceil(12*1.0/5); //2.4-->3
        if(chunkNum<=0){
            chunkNum = 1;
        }
        //缓冲区大小
        byte[] b = new byte[1024];

        //3 使用RandomAccessFile访问文件
        /*RandomAccessFile(File file, String mode): 创建一个随机访问文件流读，随意写来，由 File参数指定的文件。
         *  mode只能为下列方式的一种:
         *      1、r :以只读的方式打开文本，也就意味着不能用write来操作文件
         *      2、w :读操作和写操作都是允许的
         *      3、rws : 每当进行写操作，同步的刷新到磁盘，刷新内容和元数据
         *      4、rwd : 每当进行写操作，同步的刷新到磁盘，刷新内容
         */
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //进行分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File file = new File(chunkPath + i);
            //自动创建一个新的空文件命名的抽象路径名的当且仅当该文件不存在
            boolean newFile = file.createNewFile();
            if(newFile){
                //向文件中写入数据
                RandomAccessFile raf_write = new RandomAccessFile(file,"rw");
                int len = -1;
                //一直去读文件
                while((len = raf_read.read(b))!=-1){
                    //写文件到缓冲区
                    raf_write.write(b,0,len);
                    if(file.length()>chunkSize){
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }

    @Test
    public void testMerge() throws IOException {

        //块文件目录
        File chunkFolder = new File("F:/Ffmpeg_Video/chunk/");

        //合并文件
        File mergeFile = new File("F:/Ffmpeg_Video/lucene1.mp4");
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        //创建新的合并文件
        mergeFile.createNewFile();

        //用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");

        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        //分块列表
        File[] fileArray = chunkFolder.listFiles();
        //转换为集合，便于排序
        //Arrays.asList(fileArray): 返回由指定数组支持的一个固定大小的列表
        List<File> fileList = new ArrayList<>(Arrays.asList(fileArray));

        //从小到大排序
        //指定列表为升序排序，根据其元素的 natural ordering
        Collections.sort(fileList, new Comparator<File>() {
            //比较其两个顺序的参数。返回一个负整数、零或一个正整数作为第一个参数小于、等于或大于第二个参数
            @Override
            public int compare(File o1, File o2) {
                String fileName1 = o1.getName().substring(5);
                String fileName2 = o2.getName().substring(5);
                if(Integer.parseInt(fileName1)<Integer.parseInt(fileName2)){
                    return -1;
                }
                return 1;
            }
        });
        //合并列表
        for (File chunkFile:
             fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            //读了这个文件 len字节到字节数组数据
            while ((len = raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
