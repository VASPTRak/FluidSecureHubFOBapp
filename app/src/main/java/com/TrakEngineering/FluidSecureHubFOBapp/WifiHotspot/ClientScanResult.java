package com.TrakEngineering.FluidSecureHubFOBapp.WifiHotspot;

/**
 * Created by VASP on 9/1/2017.
 */


public class ClientScanResult {

    private String IpAddr;
    private String HWAddr;
    private String Device;
    private boolean isReachable;

    public ClientScanResult(String ipAddr, String hWAddr, String device, boolean isReachable) {
        super();
        this.IpAddr = ipAddr;
        this.HWAddr = hWAddr;
        this.Device = device;
        this.isReachable = isReachable;
    }


}