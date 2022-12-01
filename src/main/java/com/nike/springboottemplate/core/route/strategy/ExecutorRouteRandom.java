package com.nike.springboottemplate.core.route.strategy;

import com.nike.springboottemplate.core.route.ExecutorRouter;
import com.nike.springboottemplate.common.biz.model.ReturnT;
import com.nike.springboottemplate.common.biz.model.TriggerParam;

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
