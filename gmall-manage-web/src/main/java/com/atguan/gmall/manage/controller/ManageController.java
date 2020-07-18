package com.atguan.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.*;
import com.atguan.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ManageController {


    @Reference
    private ManageService manageService;

    @RequestMapping("/getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1() {

        return manageService.getCatalog1();
    }

    @RequestMapping("/getCatalog2")
    public List<BaseCatalog2> getCatalog2(@RequestParam String catalog1Id) {
        return manageService.getCatalog2(catalog1Id);
    }


    @RequestMapping("/getCatalog3")
    public List<BaseCatalog3> getCatalog3(@RequestParam String catalog2Id) {
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("/attrInfoList")
    public List<BaseAttrInfo> getAttrList(@RequestParam String catalog3Id) {
        return manageService.getAttrList(catalog3Id);
    }

    @RequestMapping("/saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);
    }

//    @RequestMapping("/getAttrValueList")
//    public List<BaseAttrValue> getAttrValueList(@RequestParam String attrId) {
//        return manageService.getAttrValueList(attrId);
//    }

    @RequestMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(@RequestParam String attrId) {

        //通过attrId查询属性
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        //返回平台属性的平台属性值集合
        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        return attrValueList;
    }

    @RequestMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return manageService.getBaseSaleAttrList();
    }

}
