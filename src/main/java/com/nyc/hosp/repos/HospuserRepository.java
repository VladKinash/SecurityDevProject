package com.nyc.hosp.repos;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface HospuserRepository extends JpaRepository<Hospuser, Integer> {

    Hospuser findFirstByRole(Role role);

    List<Hospuser> findByRole_RoleId(Integer roleId);

    Optional<Hospuser> findByUsername(String username);


}
