package com.atguan.gmall.manage.mapper;

import com.atguan.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {


    /**
     * 根据
     * @return
     * @param spuId
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);


    /**
     * 根据spuId和skuId查询属性列表
     * @param id
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String id, String spuId);
}
