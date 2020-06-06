package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CmsConfigService {

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    /**
     * 根据id查询配置管理页面
     * @param id
     * @return
     */
    public CmsConfig getConfigById(String id){
        //Optional是一个容器对象，它包括了我们需要的对象，使用isPresent方法判断所包
        //      含对象是否为空，isPresent方法返回false则表示Optional包含对象为空，否则可以使用get()取出对象进行操作。
        //Optional的优点是：
        //      1、提醒你非空判断。
        //      2、将对象非空检测标准化。
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        //如果查询到了数据(不为空)
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }
}
