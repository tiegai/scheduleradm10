package com.nike.springboottemplate.service.impl;

import com.nike.springboottemplate.core.thread.JobCompleteHelper;
import com.nike.springboottemplate.core.thread.JobRegistryHelper;
import com.nike.springboottemplate.common.biz.AdminBiz;
import com.nike.springboottemplate.common.biz.model.HandleCallbackParam;
import com.nike.springboottemplate.common.biz.model.RegistryParam;
import com.nike.springboottemplate.common.biz.model.ReturnT;
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
