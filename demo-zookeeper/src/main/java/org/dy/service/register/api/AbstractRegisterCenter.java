package org.dy.service.register.api;

public abstract class AbstractRegisterCenter implements IRegisterCenter {

    @Override
    public void register(String serviceName,Object service) throws Exception {
        doRegister(serviceName,service);
    }

    public abstract void doRegister(String serviceName, Object service) ;

    public abstract void onstart() ;
}
