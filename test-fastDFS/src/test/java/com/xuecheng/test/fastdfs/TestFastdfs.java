package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestFastdfs {

    /**
     * 测试文件上传
     */
    @Test
    public void testUpload(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //超时时间
            int g_network_timeout = ClientGlobal.g_network_timeout;
            System.out.println("network_timeout="+g_network_timeout+"ms");

            //创建客户端
            TrackerClient trackerClient = new TrackerClient();
            //获取tracker服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            if(trackerServer == null){
                System.out.println("无法连接到tracker服务器!");
                return;
            }

            //获取一个storage server
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            if(storeStorage == null){
                System.out.println("无法连接到storage服务器");
                return;
            }

            //创建一个storage存储客户端
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);

            //文件的元信息
            NameValuePair[] meta_list = null; //new NameValuePair[0]

            //需要上传的文件的位置
            String item = "F:\\testFastdfs\\uploadFile\\cat.jpg";
            String fileid;
            fileid = storageClient1.upload_file1(item,"png",meta_list);

            System.out.println("Upload local file " + item + " ok, fileid=" + fileid);
            //fileid=group1/M00/00/00/wKjTj16Ap7qAVa5jAABHwOVM56Y851.png
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试查询文件
     */
    @Test
    public void testQueryFile(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");

            //创建TrackerClient
            TrackerClient trackerClient = new TrackerClient();

            //获取TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //定义storageServer
            StorageServer storageServer = null;

            //创建StorageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
            //获取文件在远程StorageServer上的位置 group+name
            FileInfo fileInfo = storageClient1.query_file_info("group1", "M00/00/00/wKjTj16Ap7qAVa5jAABHwOVM56Y851.png");
            System.out.println(fileInfo); //source_ip_addr = 192.168.211.143, file_size = 18368, create_timestamp = 2020-03-29 21:50:50, crc32 = -447944794
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试下载文件
     */
    @Test
    public void testDownloadFile(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");

            //创建客户端
            TrackerClient trackerClient = new TrackerClient();
            //获取tracker服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取一个storage server
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建一个storage存储客户端
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);

            //下载文件
            byte[] result = storageClient1.download_file1("group1/M00/00/00/wKjTj16Ap7qAVa5jAABHwOVM56Y851.png");

            //下载之后的文件的存储的位置 注意:需要写上下载之后的文件的名字
            File file = new File("F:\\testFastdfs\\downloadFile\\cat1.jpg");

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(result);
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

}
