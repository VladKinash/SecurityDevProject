package com.nyc.hosp.service;

import com.nyc.hosp.domain.Hospuser;
import com.nyc.hosp.repos.HospuserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsService {

    @Autowired
    private HospuserRepository hospuserRepository;

}
