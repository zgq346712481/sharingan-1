package cn.moyada.sharingan.instrument.boost.proxy;

import cn.moyada.sharingan.instrument.boost.NameUtil;
import cn.moyada.sharingan.instrument.boost.common.FieldInfo;
import cn.moyada.sharingan.instrument.boost.common.ProxyField;
import cn.moyada.sharingan.instrument.boost.common.ProxyMethod;
import javassist.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xueyikang
 * @since 0.0.1
 **/
public class JavassistMirrorProxy<T> extends JavassistProxy<T> {

    public JavassistMirrorProxy(Class invokeClass, Method invokeMethod, Class<T> invokeInterface, Class<? extends T> invokeParam, String[] privateVariables) {
        super(invokeClass, invokeMethod, invokeInterface, invokeParam, privateVariables);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> wrapper(Class<T> target, List<ProxyMethod> methods) throws NotFoundException, CannotCompileException {
        CtClass targetClass = classPool.getCtClass(target.getName());

        // 镜像方式
        CtClass ctClass = classPool.getCtClass(target.getName());
        ctClass.setName(NameUtil.getProxyName(ctClass.getName()));

        addField(ctClass);

        CtMethod ctMethod;
        for (ProxyMethod method : methods) {
            ctMethod = findMethod(targetClass, method);
            if (null == ctMethod) {
                continue;
            }

            try {
                insertMethod(ctMethod, method, invokeObjName, proxyMethod);
            } catch (CannotCompileException e) {
                e.printStackTrace();
                return target;
            }
        }

        // writeFile(ctClass);

        return (Class<T>) ctClass.toClass();
    }

    /**
     * 插入方法
     * @param ctMethod
     * @param method
     * @param objName
     * @param methodName
     * @throws CannotCompileException
     */
    private void insertMethod(CtMethod ctMethod, ProxyMethod method, String objName, String methodName)
            throws CannotCompileException {

        invokeBody.append("{ \n");
        invokeBody.append("if (null != ").append(invokeObjName).append(") {\n");

        invokeBody.append(invokeInterfaceName)
                .append(" ")
                .append(LOCAL_VARIABLE)
                .append(" = new ")
                .append(invokeParamName)
                .append("();\n");

        // method param
        invokeBody.append(LOCAL_VARIABLE).append(".").append(NameUtil.getSetFunction("protocol")).append("(\"").append(method.getProtocol()).append("\");\n");
        invokeBody.append(LOCAL_VARIABLE).append(".").append(NameUtil.getSetFunction("domain")).append("(\"").append(method.getDomain()).append("\");\n");

        // application param
        for (FieldInfo fieldInfo : privateVariables) {
            invokeBody.append(LOCAL_VARIABLE)
                    .append(".")
                    .append(fieldInfo.getSetMethodName())
                    .append("(this.")
                    .append(fieldInfo.getPrimitiveName())
                    .append(");\n");
        }

        // invocation args
        for (ProxyField proxyField : method.getProxyParams()) {
            invokeBody.append(LOCAL_VARIABLE)
                    .append(".addArgs(\"").append(proxyField.getParamName())
                    .append("\", $").append(proxyField.getParamIndex() + 1).append(");\n");
        }

        invokeBody.append(objName)
                .append(".")
                .append(methodName)
                .append("(")
                .append(LOCAL_VARIABLE)
                .append("); \n }\n }");

        String proxyBody = invokeBody.toString();

        invokeBody.delete(0, invokeBody.length());

        if (method.isProxyBefore()) {
            ctMethod.insertBefore(proxyBody);
        }
        if (method.isProxyAfter()) {
            ctMethod.insertAfter(proxyBody);
        }
    }
}