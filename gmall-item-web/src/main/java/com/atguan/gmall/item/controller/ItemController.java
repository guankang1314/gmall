package com.atguan.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.SkuImage;
import com.atguan.gmall.bean.SkuInfo;
import com.atguan.gmall.bean.SkuSaleAttrValue;
import com.atguan.gmall.bean.SpuSaleAttr;
import com.atguan.gmall.service.ManageService;
import org.assertj.core.internal.cglib.asm.$AnnotationVisitor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {


    @Reference
    private ManageService manageService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request) {

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        //保存到作用域
        request.setAttribute("skuInfo",skuInfo);

//        //查询图片
//        List<SkuImage> skuImageList = manageService.getSkuImageBySkuId(skuId);
//        //保存到作用域
//        request.setAttribute("skuImageList",skuImageList);

        //查询销售属性和销售属性值集合
        List<SpuSaleAttr> spuSaleAttrList =  manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        //获取销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        //遍历集合拼接字符串
        String key = "";
        HashMap<String, Object> map = new HashMap<>();

        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {

            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);

            //开始拼接...
            if (key.length() > 0) {

                key+="|";
            }
            key+=skuSaleAttrValue.getSaleAttrValueId();
            if ((i+1) == skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())) {

                //放入map中
                map.put(key,skuSaleAttrValue.getSkuId());

                //清空string
                key = "";
            }

        }

        //将map转化为json字符串
        String s = JSON.toJSONString(map);
        System.err.println("拼接json===="+s);

        //保存
        request.setAttribute("valuesSkuJson",s);

        request.setAttribute("spuSaleAttrList",spuSaleAttrList);
        return "item";
    }
}
