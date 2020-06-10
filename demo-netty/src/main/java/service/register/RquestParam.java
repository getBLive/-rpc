package service.register;

import java.io.Serializable;

public class RquestParam implements Serializable {
    private String host;
    private int port;
    private String version = "1.0";
    private Class<?> serviceClass;
    private String method;
    private Class[] paramTypes;
    private Object[] values;

    public RquestParam(String host, int port, Class<?> serviceClass, String version) {
        this.host = host;
        this.port = port;
        this.serviceClass = serviceClass;
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceName() {
        return getServiceClass().getName()+"_"+getVersion();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Class[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }
}
