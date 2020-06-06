package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    /**
     * 自定义条件查询测试
     * 测试查询所有
     */
    @Test
    public void testFindAll(){
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAlise",ExampleMatcher.GenericPropertyMatchers.contains());

        //页面别名模糊查询，需要自定义字符串的匹配器实现模糊查询
        //ExampleMatcher.GenericPropertyMatchers.contains() 包含
        //ExampleMatcher.GenericPropertyMatchers.startsWith()//开头匹配
        //条件值
        CmsPage cmsPage = new CmsPage();
        //站点ID
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //模板ID
        cmsPage.setTemplateId("5a962c16b00ffc514038fafd");
        //cmsPage.setPageAliase("分类导航");
        //创建条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        Pageable pageable = new PageRequest(0, 10);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        System.out.println(all);
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testFindPage(){
        int page = 0; //从第0页开始
        int size = 10; //查询10条记录
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    /**
     * 测试添加
     */
    @Test
    public void testInsert(){
        //定义实体类
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("s001");
        cmsPage.setTemplateId("t001");
        cmsPage.setPageName("测试页面001");
        cmsPage.setPageAliase("课程测试页面001...");
        cmsPage.setPageCreateTime(new Date());
        List<CmsPageParam> cmsPageParams = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        cmsPageParams.add(cmsPageParam);
        cmsPage.setPageParams(cmsPageParams);
        cmsPageRepository.save(cmsPage);
        System.out.println(cmsPage);
    }

    /**
     * 测试删除
     */
    @Test
    public void testDelete(){
        cmsPageRepository.deleteById("5e6398d0083c89a4788d6346");
    }

    @Test
    public void testUpdate(){
        //根据id进行查询
        //Optional是jdk1.8引入的类型，Optional是一个容器对象，它包括了我们需要的对象，
        //使用isPresent方法判断所包 含对象是否为空，isPresent方法返回false则表示Optional包含对象为空，
        // 否则可以使用get()取出对象进行操作
        Optional<CmsPage> optional = cmsPageRepository.findById("5e6399b4083c897a0cefadfd");
        //判断查询的对象是否为空
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            cmsPage.setPageName("测试页面0001");
            cmsPageRepository.save(cmsPage);
        }
    }

    @Test
    public void testFindByExample(){
        //分页查询
        int page = 0; //从第0页开始
        int size = 10; //查询10条记录
        Pageable pageable = PageRequest.of(page,size);

        //条件匹配器
        /**
         * ExampleMatcher.GenericPropertyMatchers.contains();  //包含
         * ExampleMatcher.GenericPropertyMatchers.endsWith(); //以某某结尾
         * ExampleMatcher.GenericPropertyMatchers.caseSensitive();//严格区分大小写
         * ExampleMatcher.GenericPropertyMatchers.exact(); //精确匹配
         * ExampleMatcher.GenericPropertyMatchers.ignoreCase();//忽略大小写
         * ExampleMatcher.GenericPropertyMatchers.regex();//正则表达式匹配
         * ExampleMatcher.GenericPropertyMatchers.startsWith();//以某某开始
         * ExampleMatcher.GenericPropertyMatchers.storeDefaultMatching();//默认规则
         */
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageName",ExampleMatcher.GenericPropertyMatchers.contains());

        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName("8e");

        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }

    @Autowired
    RestTemplate restTemplate;

    /**
     * 测试远程请求接口
     */
    @Test
    public void testRestTemplate(){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        System.out.println(forEntity);
    }

}
