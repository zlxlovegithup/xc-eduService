package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    /**
     * 取出前n条任务,取出指定时间之前处理的任务
     * @param updateTime
     * @param n
     * @return
     */
    public List<XcTask> findTaskList(Date updateTime, int n){
        //设置分页参数,取出前n条记录
        Pageable pageable = new PageRequest(0,n);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = xcTasks.getContent();
        return content;
    }

    /**
     * 发布消息
     * @param xcTask
     * @param ex
     * @param routingKey
     */
    @Transactional
    public void publish(XcTask xcTask,String ex,String routingKey){
        //根据id查询出任务
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if(optional.isPresent()){
            //String exchange, String routingKey, Object object
            //发送消息
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask);
            //更新任务时间为当前时间
            XcTask one = optional.get();
            one.setUpdateTime(new Date());
            //保存到数据库
            xcTaskRepository.save(one);
        }
    }

    /**
     * 使用乐观锁方法校验任务
     * @param taskId
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String taskId,int version){
        //使用乐观锁方式校验任务id和版本号是否匹配，匹配则版本号加1
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    /**
     * 删除任务
     * @param taskId
     */
    @Transactional
    public void finishTask(String taskId){
        //从任务表查询出任务
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if(taskOptional.isPresent()){
            //取出任务
            XcTask xcTask = taskOptional.get();
            //设置任务的删除时间
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            //将xcTask的属性值赋值给xcTaskHis
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            //将历史任务保存到历史人物数据库
            xcTaskHisRepository.save(xcTaskHis);
            //将任务从任务数据库中删除
            xcTaskRepository.delete(xcTask);
        }
    }

}
