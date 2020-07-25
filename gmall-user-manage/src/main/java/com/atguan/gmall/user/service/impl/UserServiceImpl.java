package com.atguan.gmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.UserAddress;
import com.atguan.gmall.bean.UserInfo;
import com.atguan.gmall.config.RedisUtil;
import com.atguan.gmall.service.UserService;
import com.atguan.gmall.user.mapper.UserAdderssMapoper;
import com.atguan.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;


import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {


    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAdderssMapoper userAdderssMapoper;

    @Autowired
    private RedisUtil redisUtil;

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

    @Override
    public UserInfo login(UserInfo userInfo) {

        //密码
        String passwd = userInfo.getPasswd();
        String digestAsHex = DigestUtils.md5DigestAsHex(passwd.getBytes());

        userInfo.setPasswd(digestAsHex);

        //登录
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info != null) {

            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix+info.getId()+userinfoKey_suffix;
            jedis.setex(userKey,userKey_timeOut,JSON.toJSONString(info));

            jedis.close();
            return info;

        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {

        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String userKey = userKey_prefix+userId+userinfoKey_suffix;

            String userJson = jedis.get(userKey);
            if (!StringUtils.isEmpty(userJson)) {
                UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);

                return userInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return null;
    }
}
