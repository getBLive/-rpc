package org.dy.service;

import org.dy.service.register.impl.netty.NettyRegister;
import org.dy.service.register.impl.netty.ServicesScanConf;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ServerMain {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext application = new AnnotationConfigApplicationContext(ServicesScanConf.class);
        NettyRegister bean = application.getBean(NettyRegister.class);
        bean.onstart();
    }
}

