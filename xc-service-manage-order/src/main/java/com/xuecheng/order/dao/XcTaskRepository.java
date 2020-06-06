package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {
    /*
     * 取出指定时间之前的记录
     */
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable,Date updateTime);

    /**
     * 更新任务处理时间
     * @param id
     * @param updateTime
     * @return
     */
    @Modifying
    @Query("update XcTask t set t.updateTime = :updateTime where t.id = :id")
    public int updateTaskTime(@Param(value = "id") String id, @Param(value="updateTime") Date updateTime);

    /**
     * 考虑订单服务将来会集群部署，为了避免任务在1分钟内重复执行，这里使用乐观锁
     * 实现思路如下：
     * 1) 每次取任务时判断当前版本及任务id是否匹配，如果匹配则执行任务，如果不匹配则取消执行。
     * 2) 如果当前版本和任务Id可以匹配到任务则更新当前版本加1.
     * 使用乐观锁方式校验任务id和版本号是否匹配，匹配则版本号加1
     * @param id
     * @param version
     * @return
     */
    @Modifying
    @Query("update XcTask t set t.version = :version+1 where t.id = :id and t.version = :version")
    public int updateTaskVersion(String id,int version);
}
