package com.mj.shirotest.controller;

import com.alibaba.fastjson.JSONObject;
import com.mj.shirotest.config.shiro.AuthToken;
import com.mj.shirotest.constant.ShiroConstant;
import com.mj.shirotest.entity.PermissionEntity;
import com.mj.shirotest.entity.UserEntity;
import com.mj.shirotest.exception.CustomException;
import com.mj.shirotest.repository.PermissionRepsitory;
import com.mj.shirotest.repository.UserRepository;
import com.mj.shirotest.util.RedisUtil;
import com.mj.shirotest.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionRepsitory permissionRepsitory;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(String username, String password)  {

        Map<String, Object> result = new HashMap<>();
        //用户信息
        UserEntity user = userRepository.findFirstByUserName(username);
        //账号不存在、密码错误
        if (user == null) {
            result.put("status", "400");
            result.put("msg", "无该用户");
        } else if (!user.getPassword().equals(password)) {
            result.put("status", "400");
            result.put("msg", "账号或密码有误");
        } else {
            //生成token，并保存到reids
            String token = TokenUtil.sign(username,user.getId());
            user.setToken(token);
            RedisUtil.set(ShiroConstant.LOGIN_SHIRO_CACHE + user.getId(),user);
            result.put("token",token);
            result.put("status", "200");
            result.put("msg", "登陆成功");
        }

        return result;
    }

    @PostMapping("/test")
    public Map<String, Object> test()  {
        throw new RuntimeException("RuntimeException异常！");

    }

    @PostMapping("/test2")
    public Map<String, Object> test2()  {
        throw new CustomException("CustomException异常！");
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Subject sub = SecurityUtils.getSubject();
        sub.logout();
        UserEntity user = (UserEntity)sub.getPrincipal();
        RedisUtil.del(ShiroConstant.LOGIN_SHIRO_CACHE + user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("msg", "登出成功");
        return result;
    }


    //保存用户
    @PostMapping(value = "/save")
    @RequiresPermissions({"1"})
    public Map<String,String> saveUser(UserEntity user){

       userRepository.save(user);
        Map<String,String> result = new HashMap<>();
        result.put("code","200");
        result.put("msg","用户操作成功");
        result.put("obj", JSONObject.toJSONString(user));

        return result;
    }

    //删除用户
    @PostMapping(value = "/del")
    @RequiresPermissions({"2"})
    public Map<String,String> deleteUser(Long userId){

        Map<String,String> result = new HashMap<>();
        Optional<UserEntity> o = userRepository.findById(userId);
        if(o.isPresent()){
            userRepository.deleteById(userId);
            RedisUtil.del(ShiroConstant.ROLE_SHIRO_CACHE +userId,ShiroConstant.LOGIN_SHIRO_CACHE + userId);
            result.put("code","200");
            result.put("msg","用户删除成功");
        }else{
            result.put("code","400");
            result.put("msg","没有这个用户");
        }

        return result;
    }

    //修改用户权限
    @PostMapping(value = "/per")
    @RequiresPermissions({"3"})
    public Map<String,String> permission(PermissionEntity permissionEntity){
        Map<String,String> result = new HashMap<>();

        Optional<UserEntity> o = userRepository.findById(permissionEntity.getUserId());
        if(o.isPresent()){
            RedisUtil.del(ShiroConstant.ROLE_SHIRO_CACHE + permissionEntity.getUserId());
            permissionRepsitory.save(permissionEntity);
            result.put("code","200");
            result.put("msg","权限添加成功");
        }else{
            result.put("code","400");
            result.put("msg","没有这个用户");
        }

        return result;
    }

}
