package com.bolota.SysSentinelClient.Controller;

import com.bolota.SysSentinelClient.Entities.SystemEntity;
import com.bolota.SysSentinelClient.Entities.SystemVolatileEntity;
import com.bolota.SysSentinelClient.Entities.DTOs.SystemDTO;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.Date;

import static com.bolota.SysSentinelClient.Security.SysSentinelClientSecurity.*;
import static com.bolota.SysSentinelClient.Service.SysSentinelClientService.*;
import static java.lang.Thread.sleep;

public class SysSentinelClientController {
    static String UUID = "null";

    private static final String sysEndPoint = "/api/systems/";

    public static final MediaType JSON = MediaType.parse("application/json");

    private static void sendDtoNoAuth(String urlAndPort, SystemDTO sysDTO, OkHttpClient client) throws IOException {
        boolean condUUIDNull = true;
        if (!isUUIDNull()) {
            UUID = getUUID();
            sysDTO.setUUID(UUID);
            condUUIDNull = false;
        }

        RequestBody body = RequestBody.create(JSON,genJSON(sysDTO));
        String rsp = "Null";
        boolean notSentCond = true;
        while (notSentCond) {
            try {
                sleep(10000);
                Request request = new Request.Builder().url(urlAndPort + sysEndPoint +"sysinfo").header("JwtToken","null").header("RegisterToken",getAuthToken()).post(body).build();
                Response responseBody = client.newCall(request).execute();
                rsp = responseBody.body().string();
                generateJwtFile(getTokenfromString(rsp));
                writeUUID(getUUIDfromString(rsp));
                responseBody.body().close();
                if (responseBody.code() == 200) {
                    System.out.println("[" + new Date().toString() + "]" + " 'Greetings' request sent, successfully!\n\nInitializing volatile data transmission...");
                    if(condUUIDNull){
                        UUID = getUUIDfromString(rsp);
                        writeUUID(UUID);
                    }
                    notSentCond = false;
                }
                else {
                    System.out.println("[" + new Date() + "]" + " Failed to send the request to: " + urlAndPort + sysEndPoint + "sysinfo, " + "code: " + responseBody.code() + " | retrying in 10 seconds...");
                }
            }
            catch (Exception e) {
                System.out.println("[" + new Date() + "] " + e + " | Retrying in 10 seconds... (No Auth)");
            }
        }
    }
    private static void sendDtoAuth(String urlAndPort, SystemDTO sysDTO, OkHttpClient client){
        boolean condUUIDNull = true;
        if (!isUUIDNull()) {
            UUID = getUUID();
            sysDTO.setUUID(UUID);
            condUUIDNull = false;
        }
        RequestBody body = RequestBody.create(JSON,genJSON(sysDTO));
        String rsp = "Null";
        boolean notSentCond = true;
        while (notSentCond) {
            try {
                sleep(10000);
                Request request = new Request.Builder().url(urlAndPort + sysEndPoint + "sysinfo").header("JwtToken",getJwtToken()).header("RegisterToken","null").post(body).build();
                Response responseBody = client.newCall(request).execute();
                if (responseBody.code() == 200) {
                    System.out.println("[" + new Date().toString() + "]" + " 'Greetings' request sent, successfully!\n\nInitializing volatile data transmission...");
                    notSentCond = false;
                }
                else {
                    System.out.println("[" + new Date() + "]" + " Failed to send the request to: " + urlAndPort + sysEndPoint +"sysinfo, " + "code: " + responseBody.code() + " | retrying in 10 seconds...");
                }
            }
            catch (Exception e) {
                System.out.println("[" + new Date() + "] " + e + " | Retrying in 10 seconds... (Auth)");
            }

        }
    }
    public static void sendSystemDTO(String urlAndPort, SystemDTO sysDTO, OkHttpClient client) throws IOException {
        if (isJwtFilePresent()){
            sendDtoAuth(urlAndPort, sysDTO, client);
        }
        else {
            sendDtoNoAuth(urlAndPort, sysDTO, client);
        }
        //return rsp;
    }
    public static void sendSystemVolatileInfo(String urlAndPort, SystemVolatileEntity svie, OkHttpClient client){
        svie.setUUID(getUUID());
        RequestBody body = RequestBody.create(JSON,genJSON(svie));
        String rsp = "Null";
        try{
            Request request = new Request.Builder().url(urlAndPort + sysEndPoint +"sysinfovolatile").header("Authorization","Bearer "+getJwtToken()).post(body).build();
            Response responseBody = client.newCall(request).execute();
            rsp = responseBody.body().string();
            if (responseBody.code() == 200) {
                System.out.println("[" + new Date() +"]" + " 'Volatile System Info' sent, successfully! In 10 seconds, sending another one...");
            }
            else {
                System.out.println("[" + new Date() +"]" + " Failed to send the request to: " + urlAndPort + sysEndPoint +"sysinfovolatile, " + "code: " + responseBody.code() + " | retrying in 10 seconds...");
            }
        }
        catch(Exception e){
            System.out.println("[" + new Date() +"] " + e + " | retrying in 10 seconds...");
        }
    }
    public static void runClient(String urlAndPort) throws IOException {
        OkHttpClient client = new OkHttpClient();
        SystemEntity se = new SystemEntity();
        SystemDTO sdto = new SystemDTO(se);
        SystemVolatileEntity svie = new SystemVolatileEntity(se);
        String trueUrl = "http://" + urlAndPort;
        sendSystemDTO(trueUrl, sdto, client);
        while (true){
            try {
                sleep(3000);
                svie.updateVolatileInfo(se);
                sendSystemVolatileInfo(trueUrl,svie,client);
                sleep(7000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
