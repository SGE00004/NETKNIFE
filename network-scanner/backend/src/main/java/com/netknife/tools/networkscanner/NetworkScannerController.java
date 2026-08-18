package com.netknife.tools.networkscanner;

import com.netknife.tools.networkscanner.blocking.DeviceBlockingService;
import com.netknife.tools.networkscanner.blocking.dto.BlockingCapabilityDto;
import com.netknife.tools.networkscanner.dto.DeviceSummaryDto;
import com.netknife.tools.networkscanner.dto.NetworkDeviceDto;
import com.netknife.tools.networkscanner.dto.ScanResultDto;
import com.netknife.tools.networkscanner.dto.UpdateDeviceRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/network")
public class NetworkScannerController {

    private final NetworkScannerService service;
    private final DeviceBlockingService blockingService;

    public NetworkScannerController(NetworkScannerService service, DeviceBlockingService blockingService) {
        this.service = service;
        this.blockingService = blockingService;
    }

    @PostMapping("/scan")
    public ScanResultDto scan() {
        return service.scan();
    }

    @GetMapping("/devices")
    public List<NetworkDeviceDto> listDevices() {
        return service.listDevices();
    }

    @GetMapping("/summary")
    public DeviceSummaryDto getSummary() {
        return service.getSummary();
    }

    @PatchMapping("/devices/{id}")
    public NetworkDeviceDto updateDevice(@PathVariable Long id, @RequestBody UpdateDeviceRequest request) {
        return service.updateDevice(id, request);
    }

    @GetMapping("/blocking/capability")
    public BlockingCapabilityDto blockingCapability() {
        return blockingService.getCapability();
    }

    @PostMapping("/devices/{id}/block")
    public NetworkDeviceDto blockDevice(@PathVariable Long id) {
        return blockingService.blockDevice(id);
    }

    @PostMapping("/devices/{id}/unblock")
    public NetworkDeviceDto unblockDevice(@PathVariable Long id) {
        return blockingService.unblockDevice(id);
    }
}
