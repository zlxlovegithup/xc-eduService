package com.xuecheng.order;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Component
public class TestChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestChooseCourseTask.class);

//    @Scheduled(fixedRate = 5000) //上次执行开始时间后5秒执行
//    @Scheduled(fixedDelay = 5000) //上次执行结束时间后5秒执行
//    @Scheduled(initialDelay = 3000, fixedRate = 5000) //第一次延迟3秒，以后每隔5秒执行一次
    //cron表达式语法: 除了年是非必填选项,其他都是必填选项
    //        [秒] [分] [小时] [日] [月] [周] [年](非必填)
    @Scheduled(cron="0/3 * * * * *")//每隔3秒执行一次
    public void task1(){
        LOGGER.info("==============测试定时任务1开始===================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("==============测试定时任务1结束===================");
    }
}
