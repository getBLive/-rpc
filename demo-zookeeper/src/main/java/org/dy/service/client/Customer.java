package org.dy.service.client;


import org.dy.service.client.proxy.ClientProxy;
import org.dy.service.provider.IHello;
import org.dy.service.register.impl.netty.MyRequest;

public class Customer {

    public static void main(String[] args) throws Exception {
        IDiscoveryService iDiscoveryService = new ZKDiscoveryService("192.168.252.101:2181");
        for(int i=0;i<100;i++){
            try {
                MyRequest request = new MyRequest();
                request.setServiceClass(IHello.class);
                String s = iDiscoveryService.getService(IHello.class);
                if (s != null) {
                    String[] split = s.split(":");
                    request.setHost(split[0]);
                    request.setProt(new Integer(split[1]));
                    IHello hello = (IHello) ClientProxy.getService(request);
                    System.out.println(hello.sayHello("ldy"));
                    Thread.sleep(1000);
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}
