package com.mj.shirotest.repository;


import com.mj.shirotest.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepsitory extends JpaRepository<PermissionEntity,Long> {
    List<PermissionEntity> findByUserId(Long userId);
}
