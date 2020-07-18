package com.atguan.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguan.gmall.bean.SpuInfo;
import com.atguan.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;


    @RequestMapping("/spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo) {

        List<SpuInfo> spuList = manageService.getSpuList(spuInfo);
        return spuList;
    }

    @RequestMapping("/saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo) {

        if (spuInfo != null) {
            manageService.saveSpuInfo(spuInfo);
        }
    }
}
