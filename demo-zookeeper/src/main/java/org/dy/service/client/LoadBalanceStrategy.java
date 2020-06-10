package org.dy.service.client;

import java.util.List;

public interface LoadBalanceStrategy {

    String getHostAddr(List<String> addrs) throws Exception;
}
