package org.dy.service.client;

import java.util.List;

public abstract class AbstractLoadBalanceStrategy implements LoadBalanceStrategy{

    @Override
    public String getHostAddr(List<String> addrs) throws Exception {
        if(addrs==null || addrs.size()==0) {
            return null;
        }else if(addrs.size()==1){
            return addrs.get(0);
        }
        return doGetHostAddr(addrs);
    }

    protected abstract String doGetHostAddr(List<String> addrs);
}
