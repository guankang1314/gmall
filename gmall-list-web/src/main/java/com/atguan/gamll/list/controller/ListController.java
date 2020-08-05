package com.atguan.gamll.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguan.gmall.bean.*;
import com.atguan.gmall.service.ListService;
import com.atguan.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {


    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String listData(SkuLsParams skuLsParams, HttpServletRequest request) {

        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.search(skuLsParams);

//        String jsonString = JSON.toJSONString(skuLsResult);

        //获取平台属性集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);


        //定义面包屑集合
        List<BaseAttrValue> baseAttrValueList = new ArrayList<>();

        //判断参数条makeUrlParam件
        String urlParam = makeUrlParam(skuLsParams);
        //迭代器
        Iterator<BaseAttrInfo> attrInfoIterator = baseAttrInfoList.iterator();
        synchronized (attrInfoIterator) {

            while (attrInfoIterator.hasNext()) {

                BaseAttrInfo baseAttrInfo = attrInfoIterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

                for (BaseAttrValue baseAttrValue : attrValueList) {

                    if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                        for (String valueId : skuLsParams.getValueId()) {
                            if (valueId.equals(baseAttrValue.getId())) {

                                attrInfoIterator.remove();

                                BaseAttrValue baseAttrValue1 = new BaseAttrValue();
                                baseAttrValue1.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                                //重新制作url
                                String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                                baseAttrValue1.setUrlParam(makeUrlParam);
                                baseAttrValueList.add(baseAttrValue1);


                            }
                        }
                    }
                }
            }
        }
//        for (Iterator<BaseAttrInfo> attrInfoIterator = baseAttrInfoList.iterator(); attrInfoIterator.hasNext(); ) {
//            BaseAttrInfo baseAttrInfo = attrInfoIterator.next();
//            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//
//            for (BaseAttrValue baseAttrValue : attrValueList) {
//
//                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
//                    for (String valueId : skuLsParams.getValueId()) {
//                        if (valueId.equals(baseAttrValue.getId())) {
//
//                            attrInfoIterator.remove();
//
//                            BaseAttrValue baseAttrValue1 = new BaseAttrValue();
//                            baseAttrValue1.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
//
//                            //重新制作url
//                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
//                            baseAttrValue1.setUrlParam(makeUrlParam);
//                            baseAttrValueList.add(baseAttrValue1);
//
//
//                        }
//                    }
//                }
//            }
//        }

        //保存分页数据
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("totalPages",skuLsResult.getTotalPages());

        //保存检索关键字
        request.setAttribute("keyword",skuLsParams.getKeyword());

        request.setAttribute("urlParam",urlParam);

        //保存面包屑
        request.setAttribute("baseAttrValueList",baseAttrValueList);

        //保存平台属性集合
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);

        //保存商品信息
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        return "list";
    }


    //判断具体有哪些参数
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {

        String urlParam = "";

        //判断是否根据平台属性查询
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0) {

            urlParam+="keyword="+skuLsParams.getKeyword();
        }

        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0) {

            if (urlParam.length()>0) {
                urlParam+="&";
            }

            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        //平台属性值id
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {

            for (String valueId : skuLsParams.getValueId()) {

                if (excludeValueIds != null && excludeValueIds.length > 0) {
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)) {
                        //本次循环结束
                        continue;
                    }
                }

                if (urlParam.length()>0) {
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;

            }
        }

        return urlParam;

    }
}
