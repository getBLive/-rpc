package org.dy.service.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public class DesignChildrenWatcher implements PathChildrenCacheListener {

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        System.out.println(pathChildrenCacheEvent.getType()+"->"+pathChildrenCacheEvent.getData());
    }
}
