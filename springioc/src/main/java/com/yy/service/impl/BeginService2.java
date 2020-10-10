package com.yy.service.impl;

import com.yy.annotion.PostConstruct;
import com.yy.annotion.Qualifier;
import com.yy.annotion.Service;

/**
 * @author ：hzz
 * @description：TODO
 * @date ：2020/10/9 17:26
 */
@Service("BeginService2")
public class BeginService2 {
    @Qualifier("BeginService")
    BeginService beginService;

    @PostConstruct
    public void init(){
        beginService.init();
    }
}
