<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>
    <table>
        <tr>
            <td>序号</td>
            <td>姓名</td>
            <td>年龄</td>
            <td>钱包</td>
        </tr>
        <!--判断某变量是否存在使用 “??” 用法为:variable??,如果该变量存在,返回true,否则返回false-->
        <#if stus??>
            <#list stus as stu>
                <tr>
                    <!--_index：得到循环的下标，使用方法是在stu后边加"_index"，它的值是从0开始 -->
                    <td>${stu_index + 1} </td>
                    <!-- if 指令即判断指令,是常用的FTL指令，freemarker在解析时遇到if会进行判断，条件为真则输出if中间的内容，否则跳过内容不再输出。-->
                    <td <#if stu.name="小明">style="background-color: blue" </#if>>${stu.name}</td>
                    <td <#if (stu.age == 18)>style="color: blueviolet" </#if>>${stu.age}</td>
                    <td <#if stu.money gt 300>style="color: red;" </#if>>${stu.money}</td>
                </tr>
            </#list>
        </#if>
    </table>

    输出stu1的学生信息: <br/>
    <!-- 缺失变量默认值使用 “!” 使用!要以指定一个默认值，当变量为空时显示默认值。  -->
    姓名: ${stuMap['stu1'].name!''} <br/>
    年龄: ${stuMap['stu1'].age} <br/>
    输出stu2的学生信息: <br/>
    姓名: ${stuMap['stu2'].name} <br/>
    年龄: ${stuMap['stu2'].age} <br/>
    输出stu1的学生信息: <br/>
    <!--如果是嵌套对象则建议使用（）括起来。-->
    <!-- (stuMap.stu1.name)!'' :表示，如果stuMap或者stu1或者name的值为空,则显示空字符串 -->
    姓名: ${(stuMap.stu1.name)!''} <br/>
    年龄: ${stuMap.stu1.age} <br/>
    生日: ${(stuMap.stu1.birthday)?string("yyyy年MM月dd日 HH时mm分ss秒")} <br/>
    输出stu2的学生信息: <br/>
    姓名: ${stuMap.stu2.name} <br/>
    年龄: ${stuMap.stu2.age} <br/>
    生日: ${(stuMap.stu2.birthday)?string("yyyy年MM月dd日 HH时mm分ss秒")} <br/>

    遍历输出两个学生的信息: <br/>
    <table>
        <tr>
            <td>序号</td>
            <td>姓名</td>
            <td>年龄</td>
            <td>钱包</td>
        </tr>
        <#list stuMap?keys as k>
            <tr>
                <td>${k_index + 1}</td>
                <td>${stuMap[k].name!''}</td>
                <td>${stuMap[k].age}</td>
                <td>${stuMap[k].money}</td>
            </tr>
        </#list>
    </table>

    <!--point是数字型，使用${point}会显示这个数字的值，不并每三位使用逗号分隔。
        如果不想显示为每三位分隔的数字，可以使用c函数将数字型转成字符串输出 -->
    ${point?c} <br/>

    <!--其中用到了 assign标签，assign的作用是定义一个变量。-->
    <#assign text="{'bank':'工商银行','account':'10101920201920212'}" />
    <#assign data=text?eval />
    开户行：${data.bank} <br>
    账号：${data.account}

</body>
</html>