package com.nike.ncp.service.impl;

import com.nike.ncp.common.biz.AdminBiz;
import com.nike.ncp.common.biz.model.HandleCallbackParam;
import com.nike.ncp.common.biz.model.RegistryParam;
import com.nike.ncp.common.biz.model.ReturnT;
import com.nike.ncp.core.thread.JobCompleteHelper;
import com.nike.ncp.core.thread.JobRegistryHelper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminBizImpl implements AdminBiz {


    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return JobCompleteHelper.getInstance().callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registryRemove(registryParam);
    }

}
