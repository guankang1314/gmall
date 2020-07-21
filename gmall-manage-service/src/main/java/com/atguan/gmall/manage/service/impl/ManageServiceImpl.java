package com.atguan.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.*;
import com.atguan.gmall.config.RedisUtil;
import com.atguan.gmall.manage.constant.ManageConst;
import com.atguan.gmall.manage.mapper.*;
import com.atguan.gmall.service.ManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class ManageServiceImpl implements ManageService {


    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {

        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
        //多表关联查询
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);

    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else {
            //保存BaseAttrInfo
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }


        //保存BaseAttrValue
        //先将数据删除，再重新插入
        BaseAttrValue attrValue = new BaseAttrValue();
        attrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(attrValue);


        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> attrValues = baseAttrValueMapper.select(baseAttrValue);

        return attrValues;
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {

        //查询BaseAttrInfo
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        //查询BaseAttrValue
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.select(baseAttrValue);

        attrInfo.setAttrValueList(baseAttrValues);
        return attrInfo;
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return null;
    }

    @Override
    public List<SpuInfo> getSpuList(SpuInfo spuInfo) {

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {


        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectAll();
        return baseSaleAttrList;
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {

        spuInfoMapper.insertSelective(spuInfo);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {

            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {

            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> spuSaleAttrList(SpuImage spuImage) {

        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        //两张表的关联查询
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

        return spuSaleAttrList;
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {

        //skuinfo
        skuInfoMapper.insertSelective(skuInfo);

        //skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        //skuAttrvalue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {

                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        //skuSaleValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }


    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

//        //保存到redis中
//        Jedis jedis = redisUtil.getJedis();
//
//        jedis.set("ok","测试");
//        jedis.close();

        return getSkuInfoRedisson(skuId);


//        return getSkuInfoJedis(skuId);

    }

    private SkuInfo getSkuInfoRedisson(String skuId) {

        //业务逻辑代码

        SkuInfo skuInfo = null;

        Jedis jedis = null;
        RLock myLock = null;
        //获取jedis
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://192.168.113.132:6379");

            RedissonClient redissonClient = Redisson.create(config);

            //使用redisson调用getLock
            myLock = redissonClient.getLock("myLock");

            //加锁
            myLock.lock(10, TimeUnit.SECONDS);


            jedis = redisUtil.getJedis();

            //获取缓存中的数据
            String key = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;

            //判断redis中是否有key
            if (jedis.exists(key)) {
                //在redis中取出来
                String jedisString = jedis.get(key);
                //转化为对象
                skuInfo = JSON.parseObject(jedisString, SkuInfo.class);

                return skuInfo;
            }else {

                skuInfo = getSkuInfoDB(skuId);

                //放入redis中
                String s = JSON.toJSONString(skuInfo);

                jedis.setex(key,ManageConst.SKUKEY_TIMEOUT,s);
                return skuInfo;

            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }

            if (myLock != null) {
                //解锁
                myLock.unlock();
            }

        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoJedis(String skuId) {
        SkuInfo skuInfo = null;

        Jedis jedis = null;
        //获取jedis
        try {
            jedis = redisUtil.getJedis();

            //获取缓存中的数据
            String key = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;

            //获取数据
            String skuJson = jedis.get(key);

            if (skuJson == null || skuJson.length() == 0) {

                //尝试加锁
                System.err.println("缓存中没有数据");

                //set加锁
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey, "good", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);

                if ("OK".equals(lockKey)) {

                    //此时加锁成功
                    skuInfo = getSkuInfoDB(skuId);

                    System.err.println("**********************开始加锁");

                    //放入redis中
                    String s = JSON.toJSONString(skuInfo);

                    jedis.setex(key,ManageConst.SKUKEY_TIMEOUT,s);

                    //删除锁
                    jedis.del(lockKey);
                    return skuInfo;
                }else {
                    //线程等待
                    Thread.sleep(1000);

                    //调用getSkuInfo方法
                    return getSkuInfo(skuId);
                }
            }else {
                //在redis中取出来
                String jedisString = jedis.get(key);
                //转化为对象
                skuInfo = JSON.parseObject(jedisString, SkuInfo.class);

                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    @Override
    public List<SkuImage> getSkuImageBySkuId(String skuId) {

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        return skuImageList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());

    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {

        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);

    }
}
