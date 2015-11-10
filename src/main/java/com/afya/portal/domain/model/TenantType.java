package com.afya.portal.domain.model;

/**
 * Created by pradyumna on 30-03-2015.
 * Modified by Mohan Sharma
 */
public enum TenantType {

    CLINIC("CLINIC"){
        @Override
        public String toString() {
            return "CLINIC";
        }
    }, PHARMACY("PHARMACY"){
        @Override
        public String toString() {
            return "PHARMACY";
        }
    }, LABORATORY("LABORATORY"){
        @Override
        public String toString() {
            return "LABORATORY";
        }
    };

    private String value;

    private TenantType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
