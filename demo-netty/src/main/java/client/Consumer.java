package client;

import client.clientProxy.ServiceProxy;
import service.api.IHelloService;
import service.register.RquestParam;

public class Consumer {
    public static void main(String[] args) throws Exception {
        RquestParam requestParam = new RquestParam("localhost",8848,IHelloService.class,"1.0");
        IHelloService helloService = ServiceProxy.getServices(requestParam);
        if(helloService==null) {
            return;
        }
        String ddy = helloService.sayHello("dy");
        System.out.println(ddy);
    }
}
