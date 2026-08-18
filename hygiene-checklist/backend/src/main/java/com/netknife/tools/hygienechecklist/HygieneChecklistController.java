package com.netknife.tools.hygienechecklist;

import com.netknife.tools.hygienechecklist.dto.HygieneChecklistDto;
import com.netknife.tools.hygienechecklist.dto.HygieneItemDto;
import com.netknife.tools.hygienechecklist.dto.UpdateHygieneItemRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hygiene")
public class HygieneChecklistController {

    private final HygieneChecklistService service;

    public HygieneChecklistController(HygieneChecklistService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public HygieneChecklistDto check() {
        return service.runChecks();
    }

    @GetMapping("/checklist")
    public HygieneChecklistDto checklist() {
        return service.getChecklist();
    }

    @PatchMapping("/checklist/{itemId}")
    public HygieneItemDto updateItem(@PathVariable String itemId, @RequestBody UpdateHygieneItemRequest request) {
        return service.updateManualItem(itemId, request);
    }
}
