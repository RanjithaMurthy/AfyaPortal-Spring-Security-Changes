package com.nzion.dto;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 4/14/2015.
 */
@Data
public class GroupInsuredPlanDto {
    Map<String, Object> tpaDetails = Maps.newHashMap();
    Map<String, Object> insuranceDetails = Maps.newHashMap();
    private List<Map<String, Object>> insuranceForTpa = Lists.newArrayList();
    boolean isTpa;
    private String insuredGroupId;
    private Date planStartDate;
    private Date planEndDate;
    private String policyNumber;
    private List<Map<String, Object>> benefits = Lists.newArrayList();
    Map<String, Object> healthPolicy = Maps.newHashMap();
    private List<Map<String, Object>> modules = Lists.newArrayList();

    public void setInsuredGroupDetails(Map<String, Object> plan) {
        this.setInsuredGroupId(plan.get("insuredGroupId") != null ? plan.get("insuredGroupId").toString() : null);
        this.setPlanStartDate(plan.get("planStartDate") != null ? (Date) plan.get("planStartDate") : null);
        this.setPlanEndDate(plan.get("planEndDate") != null ? (Date) plan.get("planEndDate") : null);
        this.setPolicyNumber(plan.get("policyNumber") != null ? plan.get("policyNumber").toString() : null);
    }

    public void setHealthPolicyDetails(Map<String, Object> plan) {
        Map<String, Object> healthPolicy = Maps.newHashMap();
        healthPolicy.put("healthPolicyId", plan.get("healthPolicyId"));
        healthPolicy.put("healthPolicyName", plan.get("healthPolicyName"));
        this.setHealthPolicy(healthPolicy);
    }

    public void populateTpaDetails(Map<String, Object> payer) {
        Map<String, Object> tpaDetails = Maps.newHashMap();
        tpaDetails.put("insuranceCode",payer.get("insuranceCode"));
        tpaDetails.put("insuranceName",payer.get("insuranceName"));
        tpaDetails.put("payerId",payer.get("id"));
        tpaDetails.put("payerType",payer.get("payerType"));
        tpaDetails.put("insuranceShortName",payer.get("insuranceShortName"));
        tpaDetails.put("authorizationNumber",payer.get("authorizationNumber"));
        tpaDetails.put("statutoryInfo",payer.get("statutoryInfo"));
        tpaDetails.put("modeOfClaim",payer.get("modeOfClaim"));
        tpaDetails.put("contactName",payer.get("contactName"));
        tpaDetails.put("address1",payer.get("address1"));
        tpaDetails.put("address2",payer.get("address2"));
        tpaDetails.put("stateId",payer.get("stateId"));
        tpaDetails.put("districtId",payer.get("districtId"));
        tpaDetails.put("pincode",payer.get("pincode"));
        tpaDetails.put("countryId",payer.get("countryId"));
        tpaDetails.put("faxNo",payer.get("faxNo"));
        tpaDetails.put("emailId",payer.get("emailId"));
        tpaDetails.put("website",payer.get("website"));
        tpaDetails.put("poBoxNumber",payer.get("poBoxNumber"));
        this.setTpaDetails(tpaDetails);
    }

    public void populateInsuranceDetails(Map<String, Object> payer) {
        Map<String, Object> insuranceDetails = Maps.newHashMap();
        insuranceDetails.put("insuranceCode",payer.get("insuranceCode"));
        insuranceDetails.put("insuranceName",payer.get("insuranceName"));
        insuranceDetails.put("payerId",payer.get("id"));
        insuranceDetails.put("payerType",payer.get("payerType"));
        insuranceDetails.put("insuranceShortName",payer.get("insuranceShortName"));
        insuranceDetails.put("authorizationNumber",payer.get("authorizationNumber"));
        insuranceDetails.put("statutoryInfo",payer.get("statutoryInfo"));
        insuranceDetails.put("modeOfClaim",payer.get("modeOfClaim"));
        insuranceDetails.put("contactName",payer.get("contactName"));
        insuranceDetails.put("address1",payer.get("address1"));
        insuranceDetails.put("address2",payer.get("address2"));
        insuranceDetails.put("stateId",payer.get("stateId"));
        insuranceDetails.put("districtId",payer.get("districtId"));
        insuranceDetails.put("pincode",payer.get("pincode"));
        insuranceDetails.put("countryId",payer.get("countryId"));
        insuranceDetails.put("faxNo",payer.get("faxNo"));
        insuranceDetails.put("emailId",payer.get("emailId"));
        insuranceDetails.put("website",payer.get("website"));
        insuranceDetails.put("poBoxNumber",payer.get("poBoxNumber"));
        this.setInsuranceDetails(insuranceDetails);
    }

    public void setBenefitDetails(List<Map<String, Object>> groupDetails) {
        List<Map<String, Object>> benefits = Lists.newArrayList();
        Map<String, Object> benefit = null;
        for(Map<String, Object> encapsulatedPlanDetail : groupDetails){
            benefit = Maps.newHashMap();
            benefit.put("benefitPlanId", encapsulatedPlanDetail.get("benefitPlanId"));
            benefit.put("benefitPlan", encapsulatedPlanDetail.get("benefitPlan"));
            benefits.add(benefit);
        }
        this.setBenefits(benefits);
    }
}
