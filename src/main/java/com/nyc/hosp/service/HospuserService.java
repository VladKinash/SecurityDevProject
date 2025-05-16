package com.nyc.hosp.service;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.domain.Patientvisit;
import com.nyc.hosp.domain.Role;
import com.nyc.hosp.model.HospuserDTO;
import com.nyc.hosp.repos.HospuserRepository;
import com.nyc.hosp.repos.PatientvisitRepository;
import com.nyc.hosp.repos.RoleRepository;
import com.nyc.hosp.util.NotFoundException;
import com.nyc.hosp.util.ReferencedWarning;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class HospuserService {

    private final HospuserRepository hospuserRepository;
    private final RoleRepository roleRepository;
    private final PatientvisitRepository patientvisitRepository;

    public HospuserService(final HospuserRepository hospuserRepository,
            final RoleRepository roleRepository,
            final PatientvisitRepository patientvisitRepository) {
        this.hospuserRepository = hospuserRepository;
        this.roleRepository = roleRepository;
        this.patientvisitRepository = patientvisitRepository;
    }

    public List<HospuserDTO> findAll() {
        final List<Hospuser> hospusers = hospuserRepository.findAll(Sort.by("userId"));
        return hospusers.stream()
                .map(hospuser -> mapToDTO(hospuser, new HospuserDTO()))
                .toList();
    }

    public HospuserDTO get(final Integer userId) {
        return hospuserRepository.findById(userId)
                .map(hospuser -> mapToDTO(hospuser, new HospuserDTO()))
                .orElseThrow(NotFoundException::new);
    }
    public Hospuser findEntityByUsername(String username) {
        return hospuserRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public String getRoleNameById(Integer roleId) {
        return roleRepository.findById(roleId)
                .map(Role::getRolename)
                .orElse("UNKNOWN");
    }


    public Integer create(final HospuserDTO hospuserDTO) {
        final Hospuser hospuser = new Hospuser();
        mapToEntity(hospuserDTO, hospuser);
        return hospuserRepository.save(hospuser).getUserId();
    }

    public void update(final Integer userId, final HospuserDTO hospuserDTO) {
        final Hospuser hospuser = hospuserRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        mapToEntity(hospuserDTO, hospuser);
        hospuserRepository.save(hospuser);
    }

    public void delete(final Integer userId) {
        hospuserRepository.deleteById(userId);
    }

    private HospuserDTO mapToDTO(final Hospuser hospuser, final HospuserDTO hospuserDTO) {
        hospuserDTO.setUserId(hospuser.getUserId());
        hospuserDTO.setUsername(hospuser.getUsername());
        hospuserDTO.setUserpassword(hospuser.getUserpassword());
        hospuserDTO.setLastlogondatetime(hospuser.getLastlogondatetime());
hospuserDTO.setLastchangepassword(hospuser.getLastchangepassword());
        hospuserDTO.setEmail(hospuser.getEmail());
        hospuserDTO.setRole(hospuser.getRole() == null ? null : hospuser.getRole().getRoleId());
        return hospuserDTO;
    }

    private Hospuser mapToEntity(final HospuserDTO hospuserDTO, final Hospuser hospuser) {
        hospuser.setUsername(hospuserDTO.getUsername());
        hospuser.setUserpassword(hospuserDTO.getUserpassword());
        hospuser.setLastlogondatetime(hospuserDTO.getLastlogondatetime());
        hospuser.setEmail(hospuserDTO.getEmail());
        hospuser.setLocked(hospuserDTO.isLocked());
        hospuser.setLastchangepassword(hospuserDTO.getLastchangepassword());
        final Role role = hospuserDTO.getRole() == null ? null : roleRepository.findById(hospuserDTO.getRole())
                .orElseThrow(() -> new NotFoundException("role not found"));
        hospuser.setRole(role);
        return hospuser;
    }

    public ReferencedWarning getReferencedWarning(final Integer userId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Hospuser hospuser = hospuserRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        final Patientvisit patientPatientvisit = patientvisitRepository.findFirstByPatient(hospuser);
        if (patientPatientvisit != null) {
            referencedWarning.setKey("hospuser.patientvisit.patient.referenced");
            referencedWarning.addParam(patientPatientvisit.getVisitid());
            return referencedWarning;
        }
        final Patientvisit doctorPatientvisit = patientvisitRepository.findFirstByDoctor(hospuser);
        if (doctorPatientvisit != null) {
            referencedWarning.setKey("hospuser.patientvisit.doctor.referenced");
            referencedWarning.addParam(doctorPatientvisit.getVisitid());
            return referencedWarning;
        }
        return null;
    }

}
