package com.atguan.gamll.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.SkuLsInfo;
import com.atguan.gmall.bean.SkuLsParams;
import com.atguan.gmall.bean.SkuLsResult;
import com.atguan.gmall.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ListController {


    @Reference
    private ListService listService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String listData(SkuLsParams skuLsParams, HttpServletRequest request) {

        SkuLsResult skuLsResult = listService.search(skuLsParams);

//        String jsonString = JSON.toJSONString(skuLsResult);

        //保存商品信息
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        return "list";
    }
}
