package com.yy.service.impl;

import com.yy.annotion.PostConstruct;
import com.yy.annotion.Qualifier;
import com.yy.annotion.Service;
import com.yy.service.FishService;

/**
 * @author ：hzz
 * @description：TODO
 * @date ：2020/10/9 17:26
 */
@Service("BeginService")
public class BeginService {
    @Qualifier("fishServiceImpl")
    FishService fishService;

    @PostConstruct
    public void init(){
    fishService.get();
    }
}
