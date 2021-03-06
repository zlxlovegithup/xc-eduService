package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XcUserRoleRepository extends JpaRepository<XcUserRole, String> {
    List<XcUserRole> findXcUserRoleByUserId(String userId);
}
