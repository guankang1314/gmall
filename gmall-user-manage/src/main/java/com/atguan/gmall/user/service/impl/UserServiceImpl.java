package com.atguan.gmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguan.gmall.bean.UserAddress;
import com.atguan.gmall.bean.UserInfo;
import com.atguan.gmall.service.UserService;
import com.atguan.gmall.user.mapper.UserAdderssMapoper;
import com.atguan.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;


@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAdderssMapoper userAdderssMapoper;

    @Override
    public List<UserInfo> findAll() {


        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {

        UserAddress userAddress = new UserAddress();

        userAddress.setId(userId);

        return userAdderssMapoper.select(userAddress);
    }
}
