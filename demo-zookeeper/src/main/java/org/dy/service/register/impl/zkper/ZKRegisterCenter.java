package org.dy.service.register.impl.zkper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.dy.service.register.api.AbstractRegisterCenter;
import org.dy.service.spring.MyService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ZKRegisterCenter extends AbstractRegisterCenter implements ApplicationContextAware {
    //注册中心
    private CuratorFramework curatorFramework ;
    private StringBuilder addrstr = new StringBuilder();
    private String servicesAddr;

    public ZKRegisterCenter(String[] addrs){
        for(String addr:addrs){
            addrstr.append(",");
            addrstr.append(addr);
        }
        String addr= addrstr.toString().replaceFirst(",","");
        curatorFramework = CuratorFrameworkFactory.builder().connectString(addr).sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3)).build();
        curatorFramework.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MyService.class);
        for (String beanName:beanNames) {
            Object bean = applicationContext.getBean(beanName);
            MyService annotation = bean.getClass().getAnnotation(MyService.class);
            String id = annotation.id().getName();
            doRegister(id,servicesAddr);
        }
    }

    @Override
    public void doRegister(String serviceName, Object service) {
        System.out.println("已发现服务："+serviceName);
        try {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/services/"+serviceName+"/"+servicesAddr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onstart() {

    }


    public String getServicesAddr() {
        return servicesAddr;
    }

    public void setServicesAddr(String servicesAddr) {
        this.servicesAddr = servicesAddr;
    }
}
