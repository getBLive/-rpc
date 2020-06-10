package service.impl;


import service.anno.RpcService;
import service.api.IHelloService;

@RpcService(IHelloService.class)
public class IHelloServiceImpl implements IHelloService {
    @Override
    public String sayHello(String msg) throws Exception {
        System.out.println("sayHello()方法被调用，参数->"+msg);
        return "hello, "+msg;
    }
}
