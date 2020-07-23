package com.atguan.gmall.service;

import com.atguan.gmall.bean.SkuLsInfo;
import com.atguan.gmall.bean.SkuLsParams;
import com.atguan.gmall.bean.SkuLsResult;

public interface ListService {


    /**
     * 保存数据到es中
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);


    /**
     * 检索
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

}
