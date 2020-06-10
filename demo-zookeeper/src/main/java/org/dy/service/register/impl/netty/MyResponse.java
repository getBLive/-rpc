package org.dy.service.register.impl.netty;

import java.io.Serializable;

public class MyResponse implements Serializable {
    private Object result;

    public MyResponse(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
