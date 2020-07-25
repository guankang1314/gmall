package com.atguan.gmall.service;

import com.atguan.gmall.bean.UserAddress;
import com.atguan.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    /**
     *查询所有
     * @return
     */
    List<UserInfo> findAll();


    /**
     * 根据用户id查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);

    /**
     * 登录方法
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 认证方法
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}