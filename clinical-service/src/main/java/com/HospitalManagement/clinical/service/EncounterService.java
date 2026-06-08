package com.HospitalManagement.clinical.service;

import com.HospitalManagement.clinical.client.AuthClient;
import com.HospitalManagement.clinical.client.PatientClient;
import com.HospitalManagement.clinical.entity.Encounter;
import com.HospitalManagement.shared.enums.EncounterStatus;
import com.HospitalManagement.shared.enums.Roles;
import com.HospitalManagement.clinical.repository.EncounterRepository;
import com.HospitalManagement.clinical.dto.EncounterRequestDto;
import com.HospitalManagement.clinical.dto.EncounterResponseDto;
import com.HospitalManagement.shared.dto.PatientSummaryDto;
import com.HospitalManagement.shared.dto.UserSummaryDto;
import com.HospitalManagement.shared.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EncounterService {

    private static final Logger logger = LoggerFactory.getLogger(EncounterService.class);

    private final EncounterRepository encounterRepository;
    private final PatientClient patientClient;
    private final AuthClient authClient;

    public EncounterService(
            EncounterRepository encounterRepository,
            PatientClient patientClient,
            AuthClient authClient
    ) {
        this.encounterRepository = encounterRepository;
        this.patientClient = patientClient;
        this.authClient = authClient;
    }

    // READ //

    @Transactional(readOnly = true)
    public List<EncounterResponseDto> getAllEncounters() {
        logger.debug("Fetching all encounters");
        List<Encounter> encounters = encounterRepository.findAll();
        List<EncounterResponseDto> responseDtos = new ArrayList<>();

        for (Encounter encounter : encounters) {
            responseDtos.add(toResponseDto(encounter));
        }

        logger.info("Retrieved {} encounters", responseDtos.size());
        return responseDtos;
    }

    @Transactional(readOnly = true)
    public EncounterResponseDto getEncounterById(Long encounterId) {
        logger.debug("Fetching encounter with ID: {}", encounterId);
        EncounterResponseDto response = toResponseDto(findEncounter(encounterId));
        logger.info("Retrieved encounter - ID: {}", encounterId);
        return response;
    }

    // CREATE //

    public EncounterResponseDto createEncounter(EncounterRequestDto requestDto) {
        logger.info("Creating new encounter - PatientID: {}, VisitType: {}", requestDto.patientId(), requestDto.visitType());
        AuthenticatedUser clinician = getAuthenticatedClinician();
        findPatient(requestDto.patientId());

        Encounter encounter = new Encounter();
        encounter.setPatientId(requestDto.patientId());
        encounter.setClinicianId(clinician.getUserId());
        encounter.setVisitType(requestDto.visitType());
        encounter.setChiefComplaint(requestDto.chiefComplaint());
        encounter.setVitalsJson(requestDto.vitalsJson());
        encounter.setNotesJson(requestDto.notesJson());
        encounter.setDiagnosesJson(requestDto.diagnosesJson());
        encounter.setOrdersJson(requestDto.ordersJson());
        encounter.setPrescriptionsJson(requestDto.prescriptionsJson());
        encounter.setStartAt(LocalDateTime.now());
        encounter.setStatus(EncounterStatus.IN_PROGRESS);

        Encounter saved = encounterRepository.save(encounter);
        logger.info("Encounter created successfully - ID: {}", saved.getEncounterId());
        return toResponseDto(saved);
    }

    // UPDATE / COMPLETE //

    public EncounterResponseDto updateEncounter(Long encounterId, EncounterRequestDto requestDto) {
        logger.info("Updating encounter - ID: {}", encounterId);
        Encounter encounter = findEncounter(encounterId);
        AuthenticatedUser clinician = getAuthenticatedClinician();
        findPatient(requestDto.patientId());

        if (encounter.getStatus() == EncounterStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed encounters cannot be modified"
            );
        }

        encounter.setVisitType(requestDto.visitType());
        encounter.setChiefComplaint(requestDto.chiefComplaint());
        encounter.setVitalsJson(requestDto.vitalsJson());
        encounter.setNotesJson(requestDto.notesJson());
        encounter.setDiagnosesJson(requestDto.diagnosesJson());
        encounter.setOrdersJson(requestDto.ordersJson());
        encounter.setPrescriptionsJson(requestDto.prescriptionsJson());

        if (requestDto.status() == EncounterStatus.COMPLETED) {
            encounter.setStatus(EncounterStatus.COMPLETED);
            encounter.setEndAt(LocalDateTime.now());
            encounter.setSignedById(clinician.getUserId());
            encounter.setSignedAt(LocalDateTime.now());
        }

        Encounter updated = encounterRepository.save(encounter);
        logger.info("Encounter updated successfully - ID: {}", encounterId);
        return toResponseDto(updated);
    }

    public EncounterResponseDto completeEncounter(Long encounterId) {
        logger.info("Completing encounter - ID: {}", encounterId);
        Encounter encounter = findEncounter(encounterId);
        AuthenticatedUser clinician = getAuthenticatedClinician();

        if (encounter.getStatus() == EncounterStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter is already completed");
        }

        if (encounter.getStatus() != EncounterStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS encounters can be completed");
        }

        encounter.setStatus(EncounterStatus.COMPLETED);
        encounter.setEndAt(LocalDateTime.now());
        encounter.setSignedById(clinician.getUserId());
        encounter.setSignedAt(LocalDateTime.now());

        Encounter saved = encounterRepository.save(encounter);
        logger.info("Encounter completed successfully - ID: {}", encounterId);
        return toResponseDto(saved);
    }

    // DELETE //

    public void deleteEncounter(Long encounterId) {
        logger.info("Deleting encounter - ID: {}", encounterId);
        encounterRepository.delete(findEncounter(encounterId));
        logger.info("Encounter deleted successfully - ID: {}", encounterId);
    }

    // HELPERS //

    private AuthenticatedUser getAuthenticatedClinician() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            if (authUser.getRole() != Roles.CLINICIAN) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only CLINICIAN users are allowed to perform this action"
                );
            }
            return authUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
    }

    private Encounter findEncounter(Long encounterId) {
        return encounterRepository.findById(encounterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Encounter not found with id: " + encounterId));
    }

    private PatientSummaryDto findPatient(Long patientId) {
        try {
            PatientSummaryDto patient = patientClient.getPatientById(patientId);
            if (patient == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + patientId);
            }
            return patient;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with id: " + patientId);
        }
    }

    private UserSummaryDto findUser(Long userId) {
        try {
            UserSummaryDto user = authClient.getUserById(userId);
            if (user == null) {
                return null;
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private EncounterResponseDto toResponseDto(Encounter encounter) {
        PatientSummaryDto patient = findPatient(encounter.getPatientId());
        UserSummaryDto clinician = findUser(encounter.getClinicianId());
        UserSummaryDto signedBy = encounter.getSignedById() != null ? findUser(encounter.getSignedById()) : null;

        return new EncounterResponseDto(
                encounter.getEncounterId(),
                encounter.getPatientId(),
                patient != null ? patient.getName() : null,
                encounter.getClinicianId(),
                clinician != null ? clinician.getName() : null,
                encounter.getVisitType(),
                encounter.getChiefComplaint(),
                encounter.getVitalsJson(),
                encounter.getNotesJson(),
                encounter.getDiagnosesJson(),
                encounter.getOrdersJson(),
                encounter.getPrescriptionsJson(),
                encounter.getStartAt(),
                encounter.getEndAt(),
                encounter.getStatus(),
                encounter.getSignedById(),
                signedBy != null ? signedBy.getName() : null,
                encounter.getSignedAt()
        );
    }
}
