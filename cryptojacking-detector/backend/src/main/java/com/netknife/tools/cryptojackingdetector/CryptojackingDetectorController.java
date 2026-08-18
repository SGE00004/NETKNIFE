package com.netknife.tools.cryptojackingdetector;

import com.netknife.tools.cryptojackingdetector.dto.CryptojackingAlertDto;
import com.netknife.tools.cryptojackingdetector.dto.CryptojackingStatusDto;
import com.netknife.tools.cryptojackingdetector.dto.KillProcessResultDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cryptojacking-detector")
public class CryptojackingDetectorController {

    private final CryptojackingDetectorService service;

    public CryptojackingDetectorController(CryptojackingDetectorService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public CryptojackingStatusDto status() {
        return service.status();
    }

    @GetMapping("/history")
    public List<CryptojackingAlertDto> history(@RequestParam(defaultValue = "50") int limit) {
        return service.history(limit);
    }

    @PostMapping("/processes/{pid}/kill")
    public KillProcessResultDto killProcess(@PathVariable long pid) {
        return service.killProcess(pid);
    }
}
