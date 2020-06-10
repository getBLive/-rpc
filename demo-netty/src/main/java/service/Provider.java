package service;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import service.register.RpcServicesRegister;
import service.springConfig.SpringConfig;

public class Provider {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        RpcServicesRegister rpcServicesRegister = (RpcServicesRegister) configApplicationContext.getBean("rpcServicesRegister");
        rpcServicesRegister.start();
        System.out.println("启动完成。。。。。");
    }
}
