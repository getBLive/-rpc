package org.dy.service.client;

public interface IDiscoveryService {
    void discoveryService(String connectStr)throws Exception;

    String getService(Class destClass) throws Exception;
}
