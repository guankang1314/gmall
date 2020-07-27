package com.atguan.gmall.cart.service.impl;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguan.gmall.bean.CartInfo;
import com.atguan.gmall.bean.SkuInfo;
import com.atguan.gmall.cart.constant.CartConst;
import com.atguan.gmall.cart.mapper.CartInfoMapper;
import com.atguan.gmall.config.RedisUtil;
import com.atguan.gmall.service.CartInfoService;
import com.atguan.gmall.service.ManageService;
import com.google.gson.annotations.JsonAdapter;
import net.bytebuddy.description.type.TypeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.SimpleTriggerContext;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 登录时添加
     *
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        //查询购物车中是否有相同商品
        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setSkuId(skuId);
        cartInfo1.setUserId(userId);
        CartInfo cartInfo = cartInfoMapper.selectOne(cartInfo1);

        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //当前对象不为空
        if (cartInfo != null) {

            //数量相加
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //修改金额
            cartInfo.setSkuPrice(cartInfo.getCartPrice());
            //修改数据
            cartInfoMapper.updateByPrimaryKey(cartInfo);

        } else {

            //没有相同商品
            CartInfo cartInfo2 = new CartInfo();
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            cartInfo2.setSkuId(skuInfo.getId());
            cartInfo2.setCartPrice(skuInfo.getPrice());
            cartInfo2.setSkuPrice(skuInfo.getPrice());
            cartInfo2.setSkuName(skuInfo.getSkuName());
            cartInfo2.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo2.setUserId(userId);
            cartInfo2.setSkuNum(skuNum);

            //添加到数据库
            cartInfoMapper.insertSelective(cartInfo2);
            cartInfo = cartInfo2;
        }

        //同步缓存
        Jedis jedis = redisUtil.getJedis();
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfo));
        //获取过期时间
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userKey);

        //给购物车设置过期时间
        jedis.expire(cartKey,ttl.intValue());
        //关闭jedis
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        List<CartInfo> cartInfoList = new ArrayList<>();
        
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        //从缓存中获取
        List<String> stringList = jedis.hvals(cartKey);

        if (stringList != null && stringList.size() > 0) {
            for (String cartInfoStr : stringList) {
                cartInfoList.add(JSON.parseObject(cartInfoStr,CartInfo.class));
            }
            //排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });

            return cartInfoList;
        }else {
            //从数据库获取数据
            cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }

    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfos, String userId) {

        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        //开始合并
        for (CartInfo cartInfo : cartInfos) {
            boolean isMatch =false;
            for (CartInfo info : cartInfoListDB) {
                if (cartInfo.getSkuId().equals(info.getSkuId())) {
                    //赋值相加
                    info.setSkuNum(cartInfo.getSkuNum()+info.getSkuNum());
                    //修改数据库
                    cartInfoMapper.updateByPrimaryKeySelective(info);
                    isMatch = true;
                }
            }
            if (!isMatch) {
                //添加到数据库
                //赋值userId
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }
        }

        //最终返回
        List<CartInfo> cartInfoList = loadCartCache(userId);

        //增加已选中状态
        //以cookie为准
        for (CartInfo cartInfoDB : cartInfoListDB) {
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfoDB.getSkuId().equals(cartInfo.getSkuId())) {
                    cartInfoDB.setIsChecked(cartInfo.getIsChecked());
                    checkCart(cartInfoDB.getSkuId(),"1",userId);
                }
            }
        }

        return cartInfoList;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        String cartInfoJson = jedis.hget(cartKey, skuId);
        //转化为对象
        CartInfo cartInfo = JSON.parseObject(cartInfoJson,CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        //写回购物车
        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));

        //新建一个购物车来储存选中的商品,为了点击去结算时不需要再次遍历
        String cartKeyChecked = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)) {
            jedis.hset(cartKeyChecked,skuId,JSON.toJSONString(cartInfo));

        }else {
            //删除取消勾选的商品
            jedis.hdel(cartKeyChecked,skuId);
        }
        jedis.close();

    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        String userCheckedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;

        List<CartInfo> cartInfoList = new ArrayList<>();

        //从redis中获取数据
        List<String> hvals = jedis.hvals(userCheckedKey);
        if (hvals != null && hvals.size() > 0) {
            for (String cartJson : hvals) {
                cartInfoList.add(JSON.parseObject(cartJson,CartInfo.class));
            }
        }
        jedis.close();

        return cartInfoList;
    }

    /**
     * 数据库查询数据
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {

        //从cartInfo，skuInfo两张表中查
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        if (cartInfoList == null || cartInfoList.size() == 0) {
            return null;
        }

        //数据库有数据,放入redis
        Jedis jedis = redisUtil.getJedis();

        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

//        for (CartInfo cartInfo : cartInfoList) {
//            jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
//        }

        Map<String,String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        //一次放入多条数据
        jedis.hmset(cartKey,map);

        jedis.close();

        return cartInfoList;


    }
}