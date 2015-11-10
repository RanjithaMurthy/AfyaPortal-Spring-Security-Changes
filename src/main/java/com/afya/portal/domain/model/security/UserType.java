package com.afya.portal.domain.model.security;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by pradyumna on 11-06-2015.
 */
public enum UserType {
    ADMIN {
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_FACILITY_ADMIN");
        }
        @Override
        public String toString() {
            return "ROLE_ADMIN";
        }
    }, PORTAL_ADMIN {
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_PORTAL_ADMIN");
        }
        @Override
        public String toString() {
            return "ROLE_PORTAL_ADMIN";
        }
    }, RECEPTION{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_RECEPTION");
        }
        @Override
        public String toString() {
            return "ROLE_RECEPTION";
        }
    }, NURSE{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_NURSE");
        }
        @Override
        public String toString() {
            return "ROLE_NURSE";
        }
    }, BILLING{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_BILLING");
        }
        @Override
        public String toString() {
            return "ROLE_BILLING";
        }
    }, PATIENT{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_PATIENT");
        }
        @Override
        public String toString() {
            return "ROLE_PATIENT";
        }
    }, PROVIDER{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_DOCTOR");
        }
        @Override
        public String toString() {
            return "ROLE_DOCTOR";
        }
    }, ADJUSTER{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_ADJUSTER");
        }
        @Override
        public String toString() {
            return "ROLE_ADJUSTER";
        }
    }, HOUSE_KEEPING{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_HOUSE_KEEPING");
        }
        @Override
        public String toString() {
            return "ROLE_HOUSE_KEEPING";
        }
    }, MEDICAL_ASSISTANT{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_MEDICAL_ASSISTANT");
        }
        @Override
        public String toString() {
            return "ROLE_MEDICAL_ASSISTANT";
        }
    }, TECHNICIAN{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_TECHNICIAN");
        }
        @Override
        public String toString() {
            return "ROLE_TECHNICIAN";
        }
    }, CASE_MANAGER{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_CASE_MANAGER");
        }
        @Override
        public String toString() {
            return "ROLE_CASE_MANAGER";
        }
    }, SUPER_ADMIN{
        @Override
        Set<String> getAuthorities() {
            return Sets.newHashSet("ROLE_SUPER_ADMIN");
        }
        @Override
        public String toString() {
            return "ROLE_SUPER_ADMIN";
        }
    }, PAYER{
        @Override
        Set<String> getAuthorities() { return Sets.newHashSet("ROLE_PAYER");}
        @Override
        public String toString() {  return "ROLE_PAYER"; }
    };

    abstract Set<String> getAuthorities();
}
