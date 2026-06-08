package com.HospitalManagement.clinical.controller;

import com.HospitalManagement.clinical.dto.EncounterRequestDto;
import com.HospitalManagement.clinical.dto.EncounterResponseDto;
import com.HospitalManagement.clinical.service.EncounterService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clinician/encounters")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @GetMapping
    public List<EncounterResponseDto> getAllEncounters() {
        return encounterService.getAllEncounters();
    }

    @GetMapping("/{encounterId}")
    public EncounterResponseDto getEncounterById(@PathVariable Long encounterId) {
        return encounterService.getEncounterById(encounterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EncounterResponseDto createEncounter(@Valid @RequestBody EncounterRequestDto requestDto) {
        return encounterService.createEncounter(requestDto);
    }

    @PutMapping("/{encounterId}")
    public EncounterResponseDto updateEncounter(
            @PathVariable Long encounterId,
            @Valid @RequestBody EncounterRequestDto requestDto
    ) {
        return encounterService.updateEncounter(encounterId, requestDto);
    }

    @PatchMapping("/status/{encounterId}")
    public EncounterResponseDto completeEncounter(@PathVariable Long encounterId) {
        return encounterService.completeEncounter(encounterId);
    }

    @DeleteMapping("/{encounterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEncounter(@PathVariable Long encounterId) {
        encounterService.deleteEncounter(encounterId);
    }
}
