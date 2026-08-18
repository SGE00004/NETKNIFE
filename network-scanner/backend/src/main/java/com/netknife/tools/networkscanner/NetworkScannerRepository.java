package com.netknife.tools.networkscanner;

import com.netknife.tools.networkscanner.model.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NetworkScannerRepository extends JpaRepository<NetworkDevice, Long> {

    Optional<NetworkDevice> findByMacAddress(String macAddress);
}
