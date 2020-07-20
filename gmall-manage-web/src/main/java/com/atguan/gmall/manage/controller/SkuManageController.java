package com.atguan.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.SkuInfo;
import com.atguan.gmall.bean.SpuImage;
import com.atguan.gmall.bean.SpuSaleAttr;
import com.atguan.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {


    @Reference
    private ManageService manageService;

    @RequestMapping("/spuImageList")
    public List<SpuImage> spuSaleAttrList(SpuImage spuImage) {

        //调用service
        return manageService.spuSaleAttrList(spuImage);
    }

    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(@RequestParam String spuId) {

        return manageService.getSpuSaleAttrList(spuId);

    }

    @RequestMapping("/saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo) {

        if (skuInfo != null) {
            manageService.saveSkuInfo(skuInfo);

        }
    }
}
