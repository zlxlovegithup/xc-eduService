package com.xuecheng.order.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;
/*
//    @Scheduled(fixedRate = 5000) //上次执行开始时间后5秒执行
//    @Scheduled(fixedDelay = 5000) //上次执行结束时间后5秒执行
//    @Scheduled(initialDelay = 3000, fixedRate = 5000) //第一次延迟3秒，以后每隔5秒执行一次
//cron 表达式包括6部分：
//      秒（0~59） 分钟（0~59） 小时（0~23） 月中的天（1~31） 月（1~12） 周中的天
//      （填写MON，TUE，WED，THU，FRI，SAT,SUN，或数字1~7 1表示MON，依次类推）
//    例子：
//            0/3 * * * * * 每隔3秒执行
//            0 0/5 * * * * 每隔5分钟执行
//            0 0 0 * * * 表示每天0点执行
//            0 0 12 ? * WEN 每周三12点执行
//            0 15 10 ? * MON-FRI 每月的周一到周五10点 15分执行
//            0 15 10 ? * MON,FRI 每月的周一和周五10点 15分执行
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

    @Scheduled(fixedRate = 3000)//上次执行开始时间后3秒执行
    public void task2(){
        LOGGER.info("==============测试定时任务2开始===================");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("==============测试定时任务2结束===================");
    }
*/
    /**
     * 每隔1分钟扫描消息表，向mq发送消息
     */
    @Scheduled(fixedDelay = 60000)
    public void sendChooseCourseTask(){
        //取出当前时间1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 1000);

        for (XcTask xcTask:taskList) {
            //取出任务
            //考虑订单服务将来会集群部署，为了避免任务在1分钟内重复执行，这里使用乐观锁，实现思路如下：
            //  1) 每次取任务时判断当前版本及任务id是否匹配，如果匹配则执行任务，如果不匹配则取消执行。
            //  2) 如果当前版本和任务Id可以匹配到任务则更新当前版本加1.
            if(taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                //发送选课消息
                taskService.publish(xcTask,xcTask.getMqExchange(),xcTask.getMqRoutingkey());
                LOGGER.info("send choose task id:{}",xcTask.getId());
            }
        }
    }

    /**
     * 接收选课响应结果
     * @param xcTask
     * @param message
     * @param channel
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChooseTask(XcTask xcTask, Message message, Channel channel){
        LOGGER.info("receiveChooseCourseTask...{}",xcTask.getId());
        //接收到 的消息id
        String id = xcTask.getId();
        //删除任务，添加历史任务
        taskService.finishTask(id);
    }
}
