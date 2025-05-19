package com.nyc.hosp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "role")
public class Role {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Column(length = 100, unique = true, nullable = false)
    private String rolename;

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(final Integer roleId) {
        this.roleId = roleId;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(final String rolename) {
        this.rolename = rolename;
    }

}
