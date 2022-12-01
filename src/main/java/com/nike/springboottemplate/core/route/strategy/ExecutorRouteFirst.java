package com.nike.springboottemplate.core.route.strategy;

import com.nike.springboottemplate.core.route.ExecutorRouter;
import com.nike.springboottemplate.common.biz.model.ReturnT;
import com.nike.springboottemplate.common.biz.model.TriggerParam;

import java.util.List;


public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
