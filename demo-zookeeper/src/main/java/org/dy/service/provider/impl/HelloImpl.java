package org.dy.service.provider.impl;

import org.dy.service.provider.IHello;
import org.dy.service.spring.MyService;

@MyService(name="打招呼服务",id= IHello.class)
public class HelloImpl implements IHello {
    @Override
    public String sayHello(String name) throws Exception {
        return "hello ,"+name;
    }
}
