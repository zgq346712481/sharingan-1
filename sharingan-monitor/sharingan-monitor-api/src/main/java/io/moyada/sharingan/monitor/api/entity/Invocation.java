package io.moyada.sharingan.monitor.api.entity;

import java.util.Map;

/**
 * 调用数据
 * @author xueyikang
 * @since 0.0.1
 **/
public interface Invocation {

    String getApplication();

    void setApplication(String application);

    String getDomain();

    void setDomain(String domain);

    SerializationType getSerializationType();

    void setSerializationType(SerializationType serializationType);

    Map<String, String> getAttach();

    void addAttach(String name, String value);

    Map<String, Object> getArgs();

    void addArgs(String name, Object args);

    void addArgs(String name, boolean args);

    void addArgs(String name, byte args);

    void addArgs(String name, short args);

    void addArgs(String name, int args);

    void addArgs(String name, long args);

    void addArgs(String name, float args);

    void addArgs(String name, double args);

    void addArgs(String name, char args);
}
