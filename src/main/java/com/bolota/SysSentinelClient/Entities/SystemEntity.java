package com.bolota.SysSentinelClient.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import java.util.*;

import static java.lang.Thread.sleep;

@ToString
@Getter
@Setter
public class SystemEntity {
    private String UUID;
    private String name;
    private String os;
    private String host;
    private String cpu;

    @ToString.Exclude
    private ArrayList<SystemProcessEntity> processes;

    private List<String> gpu;
    private double memRamMax;

    @ToString.Exclude
    @JsonIgnore
    double newTotalDownloadUsage = 0;
    @ToString.Exclude
    @JsonIgnore
    double newTotalUploadUsage = 0;

    @ToString.Exclude
    private double totalDownloadUsage;

    @ToString.Exclude
    private double totalUploadUsage;

    @ToString.Exclude
    private HashMap<String,Double> internetCurrentUsage;

    @ToString.Exclude
    private HashMap<String, String> internetAdapters;

    @ToString.Exclude
    private HashMap<Integer, OSProcess> oldProcesses;

    @ToString.Exclude
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private final SystemInfo si;

    @ToString.Exclude
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    boolean hasRun = false;

    public SystemEntity() {
        this.si = new SystemInfo();
        this.name = si.getOperatingSystem().getNetworkParams().getHostName();
        this.os = si.getOperatingSystem().getFamily();
        this.host = si.getHardware().getComputerSystem().getModel();
        this.cpu = si.getHardware().getProcessor().getProcessorIdentifier().getName();
        this.gpu = si.getHardware().getGraphicsCards().stream().map(Object::toString).toList();
        this.memRamMax = si.getHardware().getMemory().getTotal() / (1024.0 * 1024 * 1024);
        this.processes = new ArrayList<>();
        this.oldProcesses = new HashMap<>();
        this.internetAdapters = new HashMap<>();
        this.internetCurrentUsage = new HashMap<>();
        updateNetworkAdaptersAndIp();
        this.totalDownloadUsage = 0;
        this.totalUploadUsage = 0;
        try {
            updateNetworkInfo();
            updateProcesses();
            hasRun = true;
        } catch (Exception e) {
            System.out.print(e);
        }

    }

    public List<SystemProcessEntity> searchProcess(String name) {
        return processes.stream().filter(i -> i.getName().toLowerCase().matches(name.toLowerCase())).toList();
    }

    public SystemProcessEntity searchProcess(int PID) {
        List<SystemProcessEntity> tempLst = processes.stream().filter(i -> i.getPid() == PID).limit(1).toList();
        if (tempLst.isEmpty()) {
            return null;
        } else {
            return tempLst.get(0);
        }
    }

    public void updateNetworkInfo() throws InterruptedException {
        totalDownloadUsage = 0;
        totalUploadUsage = 0;

        si.getHardware().getNetworkIFs().stream().map(NetworkIF::getBytesRecv).forEach(i -> totalDownloadUsage += i);
        si.getHardware().getNetworkIFs().stream().map(NetworkIF::getBytesSent).forEach(i -> totalUploadUsage += i);

        sleep(1000);
        newTotalDownloadUsage = 0;
        newTotalUploadUsage = 0;

        si.getHardware().getNetworkIFs().stream().map(NetworkIF::getBytesRecv).forEach(i -> newTotalDownloadUsage += i);
        si.getHardware().getNetworkIFs().stream().map(NetworkIF::getBytesSent).forEach(i -> newTotalUploadUsage += i);

        internetCurrentUsage.clear();
        internetCurrentUsage.put("Download",newTotalDownloadUsage - totalDownloadUsage);
        internetCurrentUsage.put("Upload",newTotalUploadUsage - totalUploadUsage);
    }

    public void updateProcesses() throws InterruptedException {
        processes.clear();
        double cpuNormalization = 100.0/(si.getHardware().getProcessor().getLogicalProcessorCount());
        si.getOperatingSystem().getProcesses().forEach(i -> this.oldProcesses.put(i.getProcessID(), i));
        sleep(1000);
        si.getOperatingSystem().getProcesses().forEach(i-> this.processes.add(new SystemProcessEntity(i.getName(),i.getProcessID(),i.getResidentSetSize()/(1024*1024.0),i.getVirtualSize()/(1024*1024*1024.0),i.getProcessCpuLoadBetweenTicks(oldProcesses.get(i.getProcessID()))*cpuNormalization)));
    }
    public void updateNetworkAdaptersAndIp(){
        internetAdapters.clear();
        si.getHardware().getNetworkIFs().forEach(i-> {
                if (i.getIPv4addr().length == 0) {
                    internetAdapters.put(i.getName(),"Blank");
                }
                else {
                    internetAdapters.put(i.getName(), i.getIPv4addr()[0]);
                }
            }
        );
    }
    public double getCurrentUploadUsage(){
        try {
            updateNetworkInfo();
            return internetCurrentUsage.get("Upload");
        }
        catch (Exception e){
            System.out.println(e);
            return 0.0;
        }
    }
    public double getCurrentDownloadUsage(){
        try {
            updateNetworkInfo();
            return internetCurrentUsage.get("Download");
        }
        catch (Exception e){
            System.out.println(e);
            return 0.0;
        }
    }
    public HashMap<String,Double> getInternetCurrentUsage(){
        try {
            updateNetworkInfo();
            return this.internetCurrentUsage;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public ArrayList<SystemProcessEntity> getProcesses() throws InterruptedException {
        updateProcesses();
        return this.processes;
    }
}
