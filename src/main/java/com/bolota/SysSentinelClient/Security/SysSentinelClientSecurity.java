package com.bolota.SysSentinelClient.Security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SysSentinelClientSecurity {
    private static final String authFilePath = "src/main/java/com/bolota/SysSentinelClient/Security/RegisterToken.config";
    private static final String jwtFilePath = "src/main/java/com/bolota/SysSentinelClient/Security/jwtToken.config";

    public static boolean isAuthFilePresent(){
        File file = new File(authFilePath);
        return file.exists();
    }
    public static void generateAuthFile() {
        Scanner scanner = new Scanner(System.in);
        File file = new File(authFilePath);
        try (FileWriter fw= new FileWriter(file)){
            System.out.println("Enter the Register Key:");
            fw.write("RegisterKey="+scanner.nextLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void assertAuthToken(){
        if (!isAuthFilePresent()){
            generateAuthFile();
        }
    }
    public static boolean isJwtFilePresent(){
        return new File(jwtFilePath).exists();
    }
    public static String getJwtToken() {
        File file = new File(jwtFilePath);
        try(Scanner scanner = new Scanner(file)) {
            return scanner.nextLine().substring(6);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void generateJwtFile(String token) {
        File file = new File(jwtFilePath);
        try (FileWriter fw= new FileWriter(file)){
            fw.write("token="+token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getAuthToken(){
        File file = new File(authFilePath);
        try(Scanner scanner = new Scanner(file)) {
            return scanner.nextLine().substring(12);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
