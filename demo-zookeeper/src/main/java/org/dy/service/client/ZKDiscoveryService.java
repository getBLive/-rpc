package org.dy.service.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.dy.service.register.impl.netty.NettyRegister.SERVICE_PATH;

public class ZKDiscoveryService implements IDiscoveryService {

    public static final ConcurrentHashMap<String,List<String>> services = new ConcurrentHashMap();

    public ZKDiscoveryService(String connectStr){
        try {
            discoveryService(connectStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void discoveryService(String connectStr) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .connectString(connectStr).build();
        curatorFramework.start();

        List<String> strings = curatorFramework.getChildren().forPath(SERVICE_PATH);
        for (String serviceName:strings){
            String s = SERVICE_PATH + "/" + serviceName;
            PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework,
                    s, true);
            pathChildrenCache.start();
            pathChildrenCache.getListenable().addListener(new MyPathChildrenCacheListener());
            List<String> strings1 = curatorFramework.getChildren().forPath(s);
            services.put(s,strings1);
        }
    }

    @Override
    public String getService(Class destClass) throws Exception {
        List<String> strings = services.get(SERVICE_PATH + "/"+destClass.getName());
        LoadBalanceStrategy random = new NumberRandom();
        return random.getHostAddr(strings);
    }

    class MyPathChildrenCacheListener implements PathChildrenCacheListener {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                ChildData data = pathChildrenCacheEvent.getData();
                String servicePath = data.getPath();
                String pPath = servicePath.substring(0,servicePath.lastIndexOf("/"));
                List<String> ips = curatorFramework.getChildren().forPath(pPath);
                services.put(pPath,ips);
        }
    }
}
