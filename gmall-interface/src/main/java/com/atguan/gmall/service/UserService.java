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
}