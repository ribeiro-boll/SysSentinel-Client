package com.bolota.SysSentinelClient;
import java.io.*;
import java.util.Date;

import static com.bolota.SysSentinelClient.Controller.SysSentinelClientController.*;
import static com.bolota.SysSentinelClient.Security.SysSentinelClientSecurity.*;
import static com.bolota.SysSentinelClient.Service.SysSentinelClientService.*;

public class Client {
    public static void main(String[] args) throws IOException{
        File cache = new File("sysSentinel.config");
        String urlAndPort = "";
        if (!cache.createNewFile()) {
            assertAuthToken();
            fileExists(cache);
        } else {
            assertAuthToken();
            fileNotExists(cache);
        }
        System.out.println("[" + new Date() + "]" + " UUID deste sistema: " + getUUID());
        runClient(urlAndPort);
    }
}
