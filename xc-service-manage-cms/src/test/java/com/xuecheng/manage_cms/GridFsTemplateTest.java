package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTemplateTest {

    @Autowired
    GridFsTemplate gridFsTemplate;

    /**
     * 测试存储文件
     * @throws FileNotFoundException
     *  文件存储成功得到一个文件id
     *  此文件id是fs.ﬁles集合中的主键。
     *  可以通过文件id查询fs.chunks表中的记录，得到文件的内容
     */
    @Test
    public void testGridFs() throws FileNotFoundException {
        //要存储的文件
        File file = new File("f:/test/index_banner.ftl");
        //定义输入流
        FileInputStream fileInputStream = new FileInputStream(file);
        //向GridFs存储文件 store()  objectId=5e6dcc3d083c8969b48e4784
        ObjectId objectId = gridFsTemplate.store(fileInputStream, "轮播图测试文件01", "");
        //得到文件ID
        String content = objectId.toString();
        System.out.println(content);
    }

    /**
     * 文件存储
     * @throws FileNotFoundException
     */
    @Test
    public void testStore2() throws FileNotFoundException {
        File file = new File("F:\\test\\course.ftl");
        FileInputStream fileInputStream = new FileInputStream(file);
        //保存模板文件的内容
        ObjectId objectId = gridFsTemplate.store(fileInputStream, "课程详情模板页面发布版", "");
        String fileId = objectId.toString();
        System.out.println(fileId);//课程详情模板页面test002: 5e86ee97083c8920c02e10ce  //课程详情模板页面发布版： 5e885b43083c893b5885516d
    }

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 测试读取文件
     *  gridFSBucket用于打开下载流
     * @throws IOException
     */
    @Test
    public void testQueryFile() throws IOException {
        String fileId = "5e6dcc3d083c8969b48e4784";
        //根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        //获取流中的数据
        String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(content);
    }

    /**
     * 测试删除
     * @throws IOException
     */
    @Test
    public void testDelFile() throws IOException {
        //根据文件id删除fs.files和fs.chunks中的记录     
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is("5e6da5a2083c8962ac0b1ab2")));
    }
}
