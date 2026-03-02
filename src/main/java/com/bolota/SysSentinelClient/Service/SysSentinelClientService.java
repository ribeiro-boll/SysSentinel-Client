package com.bolota.SysSentinelClient.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;


public class SysSentinelClientService {

    static String UUID;
    public static String getUUIDfromString(String body) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        HashMap<String,String> uuid = om.readValue(body, HashMap.class);
        return uuid.get("UUID");
    }
    public static String getTokenfromString(String body) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        HashMap<String,String> uuid = om.readValue(body, HashMap.class);
        return uuid.get("token");
    }

    public static String genJSON(Object o){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsString(o);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void fileExists(File cache){
        try (Scanner scanner = new Scanner(cache)) {
            String urlAndPort = getURL();
            UUID = getUUID();
            System.out.println("[" + new Date() + "] " + "Uma URL ja existe no cache, deseja re-utilizar la? [" + urlAndPort + "] Y/n (Padrão: Y)");
            String anwser;
            Scanner scanner1 = new Scanner(System.in);
            anwser = scanner1.nextLine();
            if (!anwser.isBlank()) {
                if (anwser.charAt(0) == 'n' || anwser.charAt(0) == 'N') {
                    String uuidTemp = "";
                    if(!isUUIDNull()){
                        uuidTemp = getUUID();
                    }
                    fileNotExists(cache);
                    if (!uuidTemp.isBlank()) writeUUID(uuidTemp);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void fileNotExists(File cache) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[" + new Date() + "]" + " Digite a URL junto da Porta (Ex: 198.168.0.12:8080): ");
        String urlAndPort = scanner.nextLine();
        cache.delete();
        cache.createNewFile();
        try (FileWriter fw = new FileWriter(cache)) {
            fw.write("url=" + urlAndPort);
            writeUUID("null");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeUUID(String uuid){
        File cache = new File("sysSentinel.config");
        try (Scanner scanner = new Scanner(cache)) {
            String fileString = scanner.nextLine();
            String urlAndPort = fileString.substring(fileString.indexOf("=") + 1);
            try (FileWriter fw = new FileWriter(cache)) {
                fw.write("url=" + urlAndPort);
                fw.write("\nuuid="+ uuid);
                UUID = uuid;
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public static boolean isUUIDNull() {
        File cache = new File("sysSentinel.config");
        String UUIDnow;
        try (Scanner scanner = new Scanner(cache)) {
            scanner.nextLine();
            String rawUUID = scanner.nextLine();
            UUIDnow = rawUUID.substring(6);
            return UUIDnow.equals("null");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String getUUID() {
        File cache = new File("sysSentinel.config");
        try (Scanner scanner = new Scanner(cache)) {
            scanner.nextLine();
            return scanner.nextLine().substring(5);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String getURL(){
        File cache = new File("sysSentinel.config");
        try (Scanner scanner = new Scanner(cache)) {
            return scanner.nextLine().substring(4);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


/*



*/
