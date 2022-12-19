package com.nike.ncp.scheduler.core.route.strategy;

import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.biz.model.TriggerParam;
import com.nike.ncp.scheduler.core.route.ExecutorRouter;

import java.util.List;
import java.util.Random;


public class ExecutorRouteRandom extends ExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return new ReturnT<String>(address);
    }

}
