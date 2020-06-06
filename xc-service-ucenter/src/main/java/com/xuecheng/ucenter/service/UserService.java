package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private XcUserRepository xcUserRepository;

    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    /**
     * 根据用户账号查询用户信息
     * @param username
     * @return
     */
    public XcUser findXcUserByUsername(String username){
        return xcUserRepository.findXcUserByUsername(username);
    }

    /**
     * 根据账号查询用户的信息,返回用户拓展信息(比XcUser增加了permissions权限信息,companyId企业信息)
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username){
        //根据用户名查询用户信息
        XcUser xcUser = this.findXcUserByUsername(username);
        //没有查询到
        if(xcUser == null){
            return null;
        }

        //根据用户id查询用户权限
        String userId = xcUser.getId();
        //查询用户所有权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUseId(userId);

        //查询用户id所属公司id
        XcCompanyUser xcCompanyUserId = xcCompanyUserRepository.findXcCompanyUserByUserId(userId);

        //获取用户所属公司id
        String companyId = null;
        if(xcCompanyUserId!=null){
            companyId = xcCompanyUserId.getCompanyId();
        }
        //XcUserExt比XcUser增加了permissions权限信息,companyId企业信息
        XcUserExt xcUserExt = new XcUserExt();
        //进行对象之间属性的赋值，避免通过get、set方法一个一个属性的赋值
        BeanUtils.copyProperties(xcUser,xcUserExt);
        //设置公司id
        xcUserExt.setCompanyId(companyId);
        //设置用户拥有的权限
        xcUserExt.setPermissions(xcMenus);
        return xcUserExt;
    }
}
