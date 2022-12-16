package com.nike.ncp.core.route.strategy;

import com.nike.ncp.common.biz.model.ReturnT;
import com.nike.ncp.common.biz.model.TriggerParam;
import com.nike.ncp.core.route.ExecutorRouter;

import java.util.List;


public class ExecutorRouteFirst extends ExecutorRouter {

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
