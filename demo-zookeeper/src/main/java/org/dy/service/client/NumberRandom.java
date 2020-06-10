package org.dy.service.client;

import java.util.List;
import java.util.Random;

public class NumberRandom extends AbstractLoadBalanceStrategy {

    @Override
    protected String doGetHostAddr(List<String> addrs) {
        return addrs.get(new Random().nextInt(addrs.size()));
    }
}
