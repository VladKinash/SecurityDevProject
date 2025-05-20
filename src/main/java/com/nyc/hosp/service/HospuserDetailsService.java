package com.nyc.hosp.service;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.repos.HospuserRepository;
import com.nyc.hosp.repos.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospuserDetailsService implements UserDetailsService {

    @Autowired
    private HospuserRepository hospuserRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Hospuser user = hospuserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (loginAttemptService.isLocked(user)) {
            user.setLocked(true);
            hospuserRepository.save(user);
            throw new UsernameNotFoundException("Too many failed attempts. Account locked temporarily.");
        }

        if (user.isLocked()) {
            throw new UsernameNotFoundException("Account is locked.");
        }

        String rolename = user.getRole().getRolename().toUpperCase();
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getUserpassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + rolename))
        );
    }

}
