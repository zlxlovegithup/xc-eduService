package com.xuecheng.test.freemaker;

import com.xuecheng.test.freemaker.model.Student;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FreemakerTest {

    /**
     * 基于模板生成静态文件
     */
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //创建配置类
        //Configuration.getVersion() = 2.3.28
        Configuration configuration = new Configuration(Configuration.getVersion());
        //设置模板路径
        //this.getClass() = class com.xuecheng.test.freemaker.FreemakerTest
        //this.getClass().getResource("/") = file:/E:/workspace_xcEdu/xcEduService01/test-freemarker/target/test-classes/
        //classpath = E:/workspace_xcEdu/xcEduService01/test-freemarker/target/test-classes/
        //获取resources的路径
        String classpath = this.getClass().getResource("/").getPath();
        //设置模板加载的位置
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //设置字符集
        configuration.setDefaultEncoding("utf-8");
        //加载模板test1.ftl
        Template template = configuration.getTemplate("test1.ftl");
        //数据模型
        Map map = getMap();
        //静态化  模板+数据---->页面
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        //<!DOCTYPE html>
        //<html>
        //<head>
        //    <meta charset="utf-8">
        //    <title>Hello World!</title>
        //</head>
        //<body>
        //    <table>
        //        <tr>
        //            <td>序号</td>
        //            <td>姓名</td>
        //            <td>年龄</td>
        //            <td>钱包</td>
        //        </tr>
        //        <!--判断某变量是否存在使用 “??” 用法为:variable??,如果该变量存在,返回true,否则返回false-->
        //                        <tr>
        //                    <!--_index：得到循环的下标，使用方法是在stu后边加"_index"，它的值是从0开始 -->
        //                    <td>1 </td>
        //                    <!-- if 指令即判断指令,是常用的FTL指令，freemarker在解析时遇到if会进行判断，条件为真则输出if中间的内容，否则跳过内容不再输出。-->
        //                    <td style="background-color: blue" >小明</td>
        //                    <td style="color: blueviolet" >18</td>
        //                    <td style="color: red;" >1,000.86</td>
        //                </tr>
        //                <tr>
        //                    <!--_index：得到循环的下标，使用方法是在stu后边加"_index"，它的值是从0开始 -->
        //                    <td>2 </td>
        //                    <!-- if 指令即判断指令,是常用的FTL指令，freemarker在解析时遇到if会进行判断，条件为真则输出if中间的内容，否则跳过内容不再输出。-->
        //                    <td >小红</td>
        //                    <td >19</td>
        //                    <td >200.1</td>
        //                </tr>
        //    </table>
        //
        //    输出stu1的学生信息: <br/>
        //    <!-- 缺失变量默认值使用 “!” 使用!要以指定一个默认值，当变量为空时显示默认值。  -->
        //    姓名: 小明 <br/>
        //    年龄: 18 <br/>
        //    输出stu2的学生信息: <br/>
        //    姓名: 小红 <br/>
        //    年龄: 19 <br/>
        //    输出stu1的学生信息: <br/>
        //    <!--如果是嵌套对象则建议使用（）括起来。-->
        //    <!-- (stuMap.stu1.name)!'' :表示，如果stuMap或者stu1或者name的值为空,则显示空字符串 -->
        //    姓名: 小明 <br/>
        //    年龄: 18 <br/>
        //    生日: 2020年03月14日 17时57分40秒 <br/>
        //    输出stu2的学生信息: <br/>
        //    姓名: 小红 <br/>
        //    年龄: 19 <br/>
        //    生日: 2020年03月14日 17时57分40秒 <br/>
        //
        //    遍历输出两个学生的信息: <br/>
        //    <table>
        //        <tr>
        //            <td>序号</td>
        //            <td>姓名</td>
        //            <td>年龄</td>
        //            <td>钱包</td>
        //        </tr>
        //            <tr>
        //                <td>1</td>
        //                <td>小红</td>
        //                <td>19</td>
        //                <td>200.1</td>
        //            </tr>
        //            <tr>
        //                <td>2</td>
        //                <td>小明</td>
        //                <td>18</td>
        //                <td>1,000.86</td>
        //            </tr>
        //    </table>
        //
        //    <!--point是数字型，使用102,920,122会显示这个数字的值，不并每三位使用逗号分隔。
        //        如果不想显示为每三位分隔的数字，可以使用c函数将数字型转成字符串输出 -->
        //    102920122 <br/>
        //
        //    <!--其中用到了 assign标签，assign的作用是定义一个变量。-->
        //    开户行：工商银行 <br>
        //    账号：10101920201920212
        //
        //</body>
        //</html>
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("f:/test/test1.html"));
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
    }

    public Map getMap(){
        Map map = new HashMap();
        //向模型中放数据
        map.put("name","ZLX1234");

        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        stu2.setBirthday(new Date());

        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向模型中放数据
        map.put("stus",stus);

        //准备map数据
        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);

        //向数据模型中放数据
        map.put("stu1",stu1);

        //向数据模型放数据
        map.put("stuMap",stuMap);

        map.put("point", 102920122);

        return map;
    }

    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException {
        //创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板内容,这里测试时使用简单的字符串作为模板
        String templateString = "" +
                "<html>\n" +
                "   <head></head>\n" +
                "   <body>\n" +
                "   名称:${name}\n"+
                "   </body>\n"+
                "</html>";
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        //得到模板
        Template template = configuration.getTemplate("template", "utf-8");

        //数据模型
        Map map = getMap();

        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("f:/test/test2.html"));
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
    }


}
