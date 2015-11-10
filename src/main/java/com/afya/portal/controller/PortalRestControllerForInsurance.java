package com.afya.portal.controller;

import com.afya.portal.query.PortalFinder;
import com.afya.portal.query.PortalInsuranceFinder;
import com.afya.portal.util.UtilValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.dto.GroupInsuredPlanDto;
import com.nzion.dto.ModuleServiceDto;
import com.nzion.dto.SynchronizeDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mohan Sharma on 4/16/2015.
 */
@RestController
public class PortalRestControllerForInsurance {

    private PortalFinder portalFinder;
    private Gson gson;
    private PortalInsuranceFinder portalInsuranceFinder;

    @Autowired
    public PortalRestControllerForInsurance(PortalFinder clinicalFinder, PortalInsuranceFinder portalInsuranceFinder){
        this.portalFinder = clinicalFinder;
        this.portalInsuranceFinder = portalInsuranceFinder;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();

    }

    @RequestMapping(value = "/anon/insuranceMaster/fetchListOfGroupNamesByClinicId", method = RequestMethod.GET)
    public List<Map<String, Object>> fetchListOfGroupNamesByClinicId(@RequestParam(required = false) String clinicId){
        return portalFinder.fetchListOfGroupNamesByClinicId(clinicId);
    }

    @RequestMapping(value = "/anon/insuranceMaster/fetchListOfGroupNamesByPayer", method = RequestMethod.GET)
    public List<Map<String, Object>> fetchListOfGroupNamesByPayerId(@RequestParam String payerId){
        return portalInsuranceFinder.getGroupsByTpaId(payerId);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getPlanDetailsForGroupId", method = RequestMethod.GET)
    public @ResponseBody String  getPlanDetailsForGroupId(@RequestParam String groupId,@RequestParam String dependent,@RequestParam String gender){
        GroupInsuredPlanDto insuredPlanDto = portalFinder.getPlanDetailsForGroupId(groupId,dependent,gender);
        return gson.toJson(insuredPlanDto);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getPlanDetailsForPolicyId", method = RequestMethod.GET)
    public @ResponseBody String  getPlanDetailsForPolicyId(@RequestParam String policyId,@RequestParam String dependent,@RequestParam String gender){
        GroupInsuredPlanDto insuredPlanDto = portalFinder.getPlanDetailsForPolicyId(policyId,dependent,gender);
        return gson.toJson(insuredPlanDto);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getPlanDetailsForPolicyIdAndPolicyName", method = RequestMethod.GET)
    public @ResponseBody String  getPlanDetailsForPolicyIdAndPolicyName(@RequestParam String policyId, @RequestParam String policyName, @RequestParam String dependent,@RequestParam String gender){
        GroupInsuredPlanDto insuredPlanDto = portalFinder.getPlanDetailsForPolicyIdAndPolicyName(policyId,policyName,dependent,gender);
        return gson.toJson(insuredPlanDto);
    }


    @RequestMapping(value = "/anon/insuranceMaster/allTPAPayers", method = RequestMethod.GET)
    public List<Map<String, Object>>  getAllPayersWhoAreTPA(){
        return portalInsuranceFinder.getAllTPA();
    }

    @RequestMapping(value = "/anon/insuranceMaster/getModulesByBenefitId", method = RequestMethod.GET)
    public List<Map<String, Object>> getModulesByBenefitId(@RequestParam String benefitId){
        return portalFinder.getModulesByBenefitId(benefitId);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getHISModules", method = RequestMethod.GET)
    public List<Map<String, Object>> getHISModules(){
        return portalInsuranceFinder.getHISModules();
    }

    @RequestMapping(value = "/anon/insuranceMaster/getInsuranceDetailsOfTpa", method = RequestMethod.GET)
    public List<Map<String, Object>> getInsuranceDetailsOfTpa(@RequestParam(required = false) String payerId){
        if(payerId == null){
            return portalInsuranceFinder.getAllPayerOfTypeInsurance();
        }
        return portalInsuranceFinder.getInsuranceDetailsOfTpa(payerId);
    }

    /*@RequestMapping(value = "/anon/insuranceMaster/getAllData", method = RequestMethod.GET)
    public String getAllData(){
        List<Map<String, Object>> data = portalInsuranceFinder.getData();
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.setTableName("benefit_plan");
        simpleObject.setNewRows(data);
        simpleObject.setModifiedRows(data);
        String json = gson.toJson(simpleObject);
        System.out.println(json);
        return json;
    }*/

    @RequestMapping(value = "/anonV1/insuranceMaster/getServiceOrModuleDataByServiceId", method = RequestMethod.GET)
     public String getServiceOrModuleDataByServiceId(@RequestParam String hisModuleId, @RequestParam String benefitId, @RequestParam(required = false) String groupId, @RequestParam(required = false) Set<Integer> serviceIds){
        ModuleServiceDto moduleServiceDto;
        groupId = UtilValidator.isEmpty(groupId) ? null : groupId;
        if(serviceIds == null){
            moduleServiceDto = portalInsuranceFinder.getModuleDataByModuleId(benefitId, hisModuleId,groupId);
            return gson.toJson(moduleServiceDto);
        }
        moduleServiceDto =  portalInsuranceFinder.getServiceOrModuleDataByServiceId(benefitId, hisModuleId, serviceIds, groupId);
        return gson.toJson(moduleServiceDto);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getServiceOrModuleDataByBenefitId", method = RequestMethod.GET)
    public Map<String, Object> getServiceOrModuleDataByBenefitId(@RequestParam String benefitId){
        return portalInsuranceFinder.getServiceOrModuleDataByBenefitId(benefitId);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getServiceOrModuleDataByServiceId", method = RequestMethod.GET)
    public String getServiceOrModuleDataByServiceId(@RequestParam String moduleId, @RequestParam(required = false) Set<Integer> serviceIds){
        ModuleServiceDto moduleServiceDto;
        if(serviceIds == null){
            moduleServiceDto = portalFinder.getModuleDataByModuleId(moduleId);
            return gson.toJson(moduleServiceDto);
        }
        moduleServiceDto =  portalFinder.getServiceOrModuleDataByServiceId(moduleId, serviceIds);
        return gson.toJson(moduleServiceDto);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getHealthPoliciesByPayer", method = RequestMethod.GET)
    public List<Map<String, Object>> getHealthPoliciesByPayer(@RequestParam String payerId){
        return portalInsuranceFinder.getHealthPoliciesByPayer(payerId);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getBenefitNameById", method = RequestMethod.GET)
    public Map<String, Object> getBenefitNameById(@RequestParam String benefitId){
        return portalInsuranceFinder.getBenefitNameById(benefitId);
    }

    @RequestMapping(value = "/afya-portal/api/master/syncservice", method = RequestMethod.POST)
    public ResponseEntity<String> persistData(@RequestBody String dataToPersist){
        SynchronizeDataDto synchronizeDataDto = gson.fromJson(dataToPersist, SynchronizeDataDto.class);
        return null;
    }

    @RequestMapping(value = "/anon/insuranceMaster/getAllPayers", method = RequestMethod.GET)
    public List<Map<String, Object>> getAllPayers(){
        return portalInsuranceFinder.getAllPayers();
    }

    @RequestMapping(value = "/anon/insuranceMaster/getAllCorporates", method = RequestMethod.GET)
    public List<Map<String, Object>> getAllCorporates(){
        return portalInsuranceFinder.getAllCorporates();
    }

    @RequestMapping(value = "/anon/insuranceMaster/getAllGroups", method = RequestMethod.GET)
    public List<Map<String, Object>> getAllGroups()
    {
        return portalInsuranceFinder.getAllGroups();
    }

    @RequestMapping(value = "/anon/insuranceMaster/getPolicyForIndividual", method = RequestMethod.GET)
    public List<Map<String, Object>> getPolicyForIndividual()
    {
        return portalInsuranceFinder.getPolicyForIndividual();
    }

    @RequestMapping(value = "/anon/insuranceMaster/getPolicyByGroupId", method = RequestMethod.GET)
    public List<Map<String, Object>> getPolicyByGroupId(@RequestParam String groupId)
    {
        return portalInsuranceFinder.getPolicyByGroupId(groupId);
    }
    @RequestMapping(value = "/anon/insuranceMaster/getPayerById", method = RequestMethod.GET)
    public List<Map<String, Object>> getPayerById(@RequestParam String payerId)
    {
        return portalInsuranceFinder.getPayerById(payerId);
    }

    @RequestMapping(value = "/anon/insuranceMaster/getHealthPolicyById", method = RequestMethod.GET)
    public List<Map<String, Object>> getHealthPolicyById(@RequestParam int id)
    {
        return portalInsuranceFinder.getHealthPolicyById(id);
    }
}