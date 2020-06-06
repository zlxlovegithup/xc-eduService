package com.xuecheng.test.freemaker.controller;

import com.xuecheng.test.freemaker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/freemaker")
@Controller
public class FreemakerController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/test1")
    public String freemaker(Map<String,Object> map){
        //向模型中放数据
        map.put("name","ZLX1234");

        Student stu1 = new Student();
//        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        stu2.setBirthday(new Date());

//        List<Student> stus = new ArrayList<>();
//        stus.add(stu1);
//        stus.add(stu2);

        //向模型中放数据
//        map.put("stus",stus);

        //准备map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);

        //向数据模型中放数据
        map.put("stu1",stu1);

        //向数据模型放数据
        map.put("stuMap",stuMap);

        map.put("point", 102920122);

        //返回模板文件名称 /templates/test1.ftl
        return "test1";
    }

    @RequestMapping("/banner")
    public String index_banner(Map<String,Object> map){
        String dataUrl = "http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f";
        //使用restTemplate去请求模板数据
        //forEntity = <200,{id=5a791725dd573c3574ee333f, name=轮播图, model=[{key=banner1, name=轮播图1地址, url=null, mapValue=null, value=http://192.168.101.64/group1/M00/00/01/wKhlQFp5wnCAG-kAAATMXxpSaMg864.png}, {key=banner2, name=轮播图2地址, url=null, mapValue=null, value=http://192.168.101.64/group1/M00/00/01/wKhlQVp5wqyALcrGAAGUeHA3nvU867.jpg}, {key=banner3, name=轮播图3地址, url=null, mapValue=null, value=http://192.168.101.64/group1/M00/00/01/wKhlQFp5wtWAWNY2AAIkOHlpWcs395.jpg}]},{Content-Type=[application/json;charset=UTF-8], Date=[Sat, 14 Mar 2020 16:10:56 GMT], Transfer-Encoding=[chunked]}>
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        //设置模型数据
        map.putAll(body);
        return "index_banner";
    }

    /**
     * 课程详情页面测试
     * @param map
     * @return
     */
    @RequestMapping("/course")
    public String course(Map<String,Object> map){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        return "course";
    }
}
