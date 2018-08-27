package cn.moyada.faker.core;

import cn.moyada.faker.common.exception.InitializeInvokerException;
import cn.moyada.faker.common.model.MethodProxy;
import cn.moyada.faker.common.utils.JsonUtil;
import cn.moyada.faker.common.utils.RuntimeUtil;
import cn.moyada.faker.common.utils.UUIDUtil;
import cn.moyada.faker.core.convert.AppInfoConverter;
import cn.moyada.faker.core.task.InvokeTask;
import cn.moyada.faker.manager.FakerManager;
import cn.moyada.faker.manager.domain.AppInfoDO;
import cn.moyada.faker.manager.domain.MethodInvokeDO;
import cn.moyada.faker.module.Dependency;
import cn.moyada.faker.module.fetch.MetadataFetch;
import cn.moyada.faker.module.handler.InvokeInfo;
import cn.moyada.faker.module.handler.InvokeMetadata;
import cn.moyada.faker.module.handler.MetadataWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Main {

    @Autowired
    private FakerManager fakerManager;

    @Autowired
    private MetadataWrapper metadataWrapper;

    @Autowired
    private MetadataFetch metadataFetch;

    public String invoke(QuestInfo questInfo) throws InitializeInvokerException {
        questInfo.setPoolSize(RuntimeUtil.getActualSize(questInfo.getPoolSize()));

        MethodInvokeDO methodInvokeDO = fakerManager.getInvokeInfo(questInfo.getInvokeId());

        Dependency dependency = getDependency(methodInvokeDO.getAppId());

        InvokeInfo invokeInfo = getInvokeInfo(methodInvokeDO);

        InvokeMetadata invokeMetadata = metadataWrapper.getProxy(dependency, invokeInfo);

        Object[] values = JsonUtil.toArray(questInfo.getInvokeExpression(), Object[].class);
        Class<?>[] paramTypes = invokeMetadata.getParamTypes();
        if(null == values || paramTypes.length != values.length) {
            throw new IllegalStateException("param number error.");
        }

        metadataFetch.checkoutClassLoader(dependency);

        // 生成调用报告序号
        String fakerId = UUIDUtil.getUUID();
        proxy.setFakerId(fakerId);
        proxy.setValues(values);

        InvokeTask invokeTask = new InvokeTask(proxy, questInfo);

        int qps = questInfo.getQps();
        int timeout = (3600 / qps) - (20 >= qps ? 0 : 50);
        // 发起调用
        if(timeout > 50) {
            invokeTask.start(timeout);
        }
        else {
            invokeTask.start();
        }

        invokeTask.shutdown();

        return "请求结果序号：" + fakerId;
    }

    private Dependency getDependency(int appId) {
        AppInfoDO appInfoDO = fakerManager.getDependencyByAppId(appId);
        if(null == appInfoDO) {
            throw new InitializeInvokerException("获取应用信息失败: " + appId);
        }

        Dependency dependency = AppInfoConverter.toDependency(appInfoDO);
        String dependencies = appInfoDO.getDependencies();
        List<AppInfoDO> appList = fakerManager.getDependencyByAppId(dependencies);
        dependency.setDependencyList(AppInfoConverter.toDependency(appList));
        return dependency;
    }

    private InvokeInfo getInvokeInfo(MethodInvokeDO methodInvokeDO) {
        InvokeInfo invokeInfo = new InvokeInfo();
        invokeInfo.setClassType(methodInvokeDO.getClassName());
        invokeInfo.setMethodName(methodInvokeDO.getMethodName());
        invokeInfo.setParamType(methodInvokeDO.getParamType());
        invokeInfo.setReturnType(methodInvokeDO.getReturnType());
        return invokeInfo;
    }
}
