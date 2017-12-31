package cn.xueyikang.dubbo.faker.core.utils;

import cn.xueyikang.dubbo.faker.core.model.RebuildParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xueyikang
 * @create 2017-12-30 18:48
 */
public class ParamUtil {

    private static final String regex = "\\$\\{\\d+\\.\\w+\\}";
    private static final Random random = new Random();

    public static RebuildParam getRebuildParam(Object[] array) {
        Pattern p = Pattern.compile(regex);

        int length = array.length;

        Map<Integer, List<String>> rebuildParamMap = Maps.newHashMapWithExpectedSize(length);
        Set<String> rebuildParamSet = Sets.newHashSetWithExpectedSize(length);

        Matcher m;
        String find;
        List<String> param;
        for (int index = 0; index < length; index++) {
            m = p.matcher(array[index].toString());
            param = Lists.newArrayList();
            for(; m.find(); ) {
                find = m.group();
                param.add(find);
                rebuildParamSet.add(find);
                rebuildParamMap.put(index, param);
            }
        }

        RebuildParam rebuildParam = new RebuildParam();
        rebuildParam.setRebuildParamSet(rebuildParamSet);
        rebuildParam.setRebuildParamMap(rebuildParamMap);
        return rebuildParam;
    }

    public static Object[] convertValue(Object[] values, Class<?>[] paramTypes, Map<Integer, List<String>> rebuildParamMap,
                                        Map<String, List<String>> paramMap, Map<Integer, Integer> convertMap) {
        int length = values.length;
        if(0 == length) {
            return null;
        }

        Object[] argsValue = new Object[length];
        String json;
        List<String> paramsList, valueList;
        for (int index = 0; index < length; index++) {
            json = JsonUtil.toJson(values[index]);
            if(null == json) {
                argsValue[index] = null;
                continue;
            }

            paramsList = rebuildParamMap.get(index);
            if(null != paramsList) {
                for (String param : paramsList) {
                    valueList = paramMap.get(param);
                    if(null != valueList) {
                        json = json.replace(param, valueList.get(random.nextInt(valueList.size())));
                    }
                }
            }

            if(1 == convertMap.get(index)) {
                argsValue[index] = JsonUtil.toList(json, Object.class);
            }
            else {
                argsValue[index] = JsonUtil.toObject(json, paramTypes[index]);
            }
        }
        return argsValue;
    }

    public static void main(String[] args) {
         String invokeParam = "[\"${123.model}\", [{\"action\":\"${23.haha}\",\"money\":[{\"action\":\"${23.haha}\",\"money\":1111}]}], \"wishenm\"]";
//        String invokeParam = "[\"${1.model}\"]";
        Object[] array = JsonUtil.toArray(invokeParam, Object.class);
        System.out.println(getRebuildParam(array));
    }
}
