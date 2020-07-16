package com.atguan.gmall.service;

import com.atguan.gmall.bean.*;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;

import java.util.List;

public interface ManageService {


    /**
     * 查询所有BaseCatalog1
     * @return
     */
    List<BaseCatalog1> getCatalog1();


    /**
     * 查询所有Catalog2
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级id查询BaseCatalog3
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级id查询BaseAttrInfo
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 增加属性
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据attrId查询BaseAttrValue
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 根据attrId查询平台属性
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);
}
