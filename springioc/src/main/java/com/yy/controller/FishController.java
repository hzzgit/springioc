package com.yy.controller;


import com.yy.annotion.Controller;
import com.yy.annotion.PostConstruct;
import com.yy.annotion.Qualifier;
import com.yy.annotion.RequestMapping;
import com.yy.service.FishService;


@Controller(value = "fish")
public class FishController {

    @Qualifier("fishServiceImpl")
    FishService fishService;

    //响应请求的方法
    @RequestMapping("get")
    @PostConstruct
    public String getFish(){
        return fishService.get();
    }
}
