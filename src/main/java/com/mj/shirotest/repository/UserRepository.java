package com.mj.shirotest.repository;

import com.mj.shirotest.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findFirstByUserName(String userName);
}
