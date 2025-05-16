package com.nyc.hosp.service;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.domain.Patientvisit;
import com.nyc.hosp.model.PatientvisitDTO;
import com.nyc.hosp.repos.HospuserRepository;
import com.nyc.hosp.repos.PatientvisitRepository;
import com.nyc.hosp.util.EncryptionUtil;
import com.nyc.hosp.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class PatientvisitService {

    private final PatientvisitRepository patientvisitRepository;
    private final HospuserRepository hospuserRepository;

    public PatientvisitService(final PatientvisitRepository patientvisitRepository,
            final HospuserRepository hospuserRepository) {
        this.patientvisitRepository = patientvisitRepository;
        this.hospuserRepository = hospuserRepository;
    }

    public List<PatientvisitDTO> findAll() {
        final List<Patientvisit> patientvisits = patientvisitRepository.findAll(Sort.by("visitid"));
        return patientvisits.stream()
                .map(patientvisit -> mapToDTO(patientvisit, new PatientvisitDTO()))
                .toList();
    }

    public PatientvisitDTO get(final Integer visitid) {
        return patientvisitRepository.findById(visitid)
                .map(patientvisit -> mapToDTO(patientvisit, new PatientvisitDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final PatientvisitDTO patientvisitDTO) {
        final Patientvisit patientvisit = new Patientvisit();
        mapToEntity(patientvisitDTO, patientvisit);
        return patientvisitRepository.save(patientvisit).getVisitid();
    }

    public void update(final Integer visitid, final PatientvisitDTO patientvisitDTO) {
        final Patientvisit patientvisit = patientvisitRepository.findById(visitid)
                .orElseThrow(NotFoundException::new);
        mapToEntity(patientvisitDTO, patientvisit);
        patientvisitRepository.save(patientvisit);
    }

    public void delete(final Integer visitid) {
        patientvisitRepository.deleteById(visitid);
    }

    private PatientvisitDTO mapToDTO(final Patientvisit patientvisit,
                                     final PatientvisitDTO patientvisitDTO) {
        patientvisitDTO.setVisitid(patientvisit.getVisitid());
        patientvisitDTO.setVistidate(patientvisit.getVisitdate());

        if (patientvisit.getDiagnosis() != null) {
            try {
                patientvisitDTO.setDiagnosis(EncryptionUtil.decrypt(patientvisit.getDiagnosis()));
            } catch (Exception e) {
                patientvisitDTO.setDiagnosis("UNREADABLE/INVALID DATA");
            }
        }

        patientvisitDTO.setPatient(patientvisit.getPatient() == null ? null : patientvisit.getPatient().getUserId());
        patientvisitDTO.setDoctor(patientvisit.getDoctor() == null ? null : patientvisit.getDoctor().getUserId());
        return patientvisitDTO;
    }


    private Patientvisit mapToEntity(final PatientvisitDTO patientvisitDTO,
                                     final Patientvisit patientvisit) {
        patientvisit.setVisitdate(patientvisitDTO.getVistidate());

        if (patientvisitDTO.getDiagnosis() != null) {
            patientvisit.setDiagnosis(EncryptionUtil.encrypt(patientvisitDTO.getDiagnosis()));
        }

        final Hospuser patient = patientvisitDTO.getPatient() == null ? null : hospuserRepository.findById(patientvisitDTO.getPatient())
                .orElseThrow(() -> new NotFoundException("patient not found"));
        patientvisit.setPatient(patient);

        final Hospuser doctor = patientvisitDTO.getDoctor() == null ? null : hospuserRepository.findById(patientvisitDTO.getDoctor())
                .orElseThrow(() -> new NotFoundException("doctor not found"));
        patientvisit.setDoctor(doctor);

        return patientvisit;
    }


}
