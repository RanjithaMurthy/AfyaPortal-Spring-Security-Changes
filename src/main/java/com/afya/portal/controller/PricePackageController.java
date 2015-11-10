package com.afya.portal.controller;

import com.afya.portal.application.PersistFacilityCommand;
import com.afya.portal.application.RegisterPatientCommand;
import com.afya.portal.application.SubscribePackageCommand;
import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.query.PortalUtilityFinder;
import com.afya.portal.util.DatabaseUtil;
import com.afya.portal.service.MailService;
import com.afya.portal.util.UtilValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.dto.PackageDto;
import com.nzion.dto.PatientDto;
import com.nzion.dto.PackageQuotationDto;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by pradyumna on 11-06-2015.
 * <p/>
 * This will handle the registration of Patient as well as other Facility Type.
 */
@Controller
public class PricePackageController {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private MailService mailService;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.username}")
    private String jdbcUserName;
    @Value("${jdbc.password}")
    private String jdbcPassword;
    @Autowired
    private PortalUtilityFinder portalUtilityFinder;
    @Autowired
    private PortalUtilityRestController portalUtilityRestController;



    @RequestMapping(value = "/anon/get_packages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    public   ResponseEntity<String> get_packages(@CookieValue("token") String token, HttpServletResponse response) throws IOException {

        if(UtilValidator.isEmpty(token))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");

        List<PackageDto> packageDtoList = null;
        Map result = new HashMap();
        String userName ;
        userName = SessionManager.getInstance().getSession(token);

        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();
        try {
            packageDtoList = commandGateway.sendAndWait(new GenericCommandMessage("GET_PACKAGES",userName,null));
        } catch(Exception e) {
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message","error");
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        result.put("message","success");
        result.put("packages",packageDtoList);
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/anon/subscribe_package", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> subscribePackage(@CookieValue("token") String token,
                                                   @RequestBody @Valid List<PackageDto> packageDto, @RequestParam String smsSenderId,
                                                   @RequestParam(required = false) Long paymentId,
                                                   BindingResult bindingResult, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(token))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");

        Map result = new HashMap();
        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();
        String userName ;
        userName = SessionManager.getInstance().getSession(token);
        SubscribePackageCommand cmd = new SubscribePackageCommand(userName,packageDto,smsSenderId,paymentId);
        try {
            commandGateway.sendAndWait(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message",e.getMessage());
            result.put("errCode","501");
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        result.put("message","success");
        result.put("errCode","200");
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);

    }

    @RequestMapping(value = "/test_report")
    void executeTestReport(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getServletContext().getRealPath("static/reports/");
        File file = new File(path + "/test.jasper");
        try {
            ServletOutputStream out = response.getOutputStream();
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword);
            JasperReport report = (JasperReport) JRLoader.loadObject(file);
            Map<String, Object> parameter = new HashMap<String, Object>();
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameter, connection);
            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
            exporter.exportReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/quote")
    void executeQuoteReport(@CookieValue("token") String token,HttpServletRequest request, HttpServletResponse response) {

        String path = request.getServletContext().getRealPath("static/reports/");
        File file = new File(path + "/qoute.jasper");
        try {
            String username = SessionManager.getInstance().getSession(token);
            ServletOutputStream out = response.getOutputStream();
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword);
            JasperReport report = (JasperReport) JRLoader.loadObject(file);
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("SUBREPORT_DIR",path+"/");
            parameter.put("username",username);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameter, connection);
            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
            exporter.exportReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/anon/send_mail_package_quotation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> sendMailPackageQuotation(@CookieValue("token") String token, @RequestBody PackageQuotationDto packageQuotationDto
            , HttpServletResponse response, HttpServletRequest request) throws  IOException {

        if(UtilValidator.isEmpty(token))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");

        Map result = new HashMap();
        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();
        String userName ;
        userName = SessionManager.getInstance().getSession(token);

        try {
            // JpaRepositoryFactory factory = new JpaRepositoryFactory();
            // SimpleJpaRepository<UserLogin, String> userLoginRepository = factory.getCrudRepository(UserLogin.class);
            // UserLogin user = userLoginRepository.findOne(userName);
            UserLogin user = commandGateway.sendAndWait(new GenericCommandMessage("GET_USER_TYPE_COMMAND",userName,null));
            // Map model = new HashMap<String, Object>();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> model = mapper.convertValue(packageQuotationDto, Map.class);
            // get package ID
            String packageId = packageQuotationDto.getPackageId();
            // get training schedules for the package Id
            List<Map<String, Object>> trainingSchedules = portalUtilityRestController.getTrainingSessionSchedulesByPackageId(Integer.parseInt(packageId), null);
            // extract training schedules into a list and compute summary values
            // List<Object> trainingSchedules = new ArrayList<Object>();
            int totalTrainingDurationInMins, totalPracticeDurationInMins, totalAssessmentDurationInMins;
            BigDecimal totalTrainingDurationInHours, totalPracticeDurationInHours, totalAssessmentDurationInHours;
            totalTrainingDurationInMins = totalPracticeDurationInMins = totalAssessmentDurationInMins = 0;
            for(Map<String, Object> schMap : trainingSchedules){
                /*Map.Entry<String,Object> entry = schMap.entrySet().iterator().next();
                Object schedule = entry.getValue();
                trainingSchedules.add(schedule);
                String trainingDuration = BeanUtils.getProperty(schedule, "trainingDuration");
                String practiceDuration = BeanUtils.getProperty(schedule, "practiceDuration");
                String assessmentDuration = BeanUtils.getProperty(schedule, "assessmentDuration");*/
                Object tmp;
                tmp = schMap.get("trainingDuration"); if(tmp != null) totalTrainingDurationInMins += Integer.parseInt(tmp.toString());
                tmp = schMap.get("practiceDuration"); if(tmp != null) totalPracticeDurationInMins += Integer.parseInt(tmp.toString());
                tmp = schMap.get("assessmentDuration");if(tmp != null) totalAssessmentDurationInMins += Integer.parseInt(tmp.toString());
            }
            // compute total durations in hours
            totalTrainingDurationInHours = new BigDecimal((double)totalTrainingDurationInMins / 60);
            totalPracticeDurationInHours = new BigDecimal((double)totalPracticeDurationInMins / 60);
            totalAssessmentDurationInHours = new BigDecimal((double)totalAssessmentDurationInMins / 60);
            // set scale
            totalTrainingDurationInHours.setScale(2, BigDecimal.ROUND_HALF_UP);
            totalPracticeDurationInHours.setScale(2, BigDecimal.ROUND_HALF_UP);
            totalAssessmentDurationInHours.setScale(2, BigDecimal.ROUND_HALF_UP);
            // decimal formater
            DecimalFormat df = new DecimalFormat();
            df.setMinimumFractionDigits(2);
            df.setMaximumFractionDigits(2);
            df.setGroupingUsed(false);
            // add training schedules to model
            model.put("trainingSchedules", trainingSchedules);
            model.put("totalTrainingDurationInMins", totalTrainingDurationInMins);
            model.put("totalPracticeDurationInMins", totalPracticeDurationInMins);
            model.put("totalAssessmentDurationInMins", totalAssessmentDurationInMins);
            model.put("totalTrainingDurationInHours", df.format(totalTrainingDurationInHours));
            model.put("totalPracticeDurationInHours", df.format(totalPracticeDurationInHours));
            model.put("totalAssessmentDurationInHours", df.format(totalAssessmentDurationInHours));

            // base url for image
            StringBuffer url = request.getRequestURL();
            String baseUrl = url.substring(0, url.length() - request.getRequestURI().length() + request.getContextPath().length());
            model.put("baseUrl", baseUrl);

            model.put("sentFrom", "admin@aafya.com");
            model.put("firstName", user.getFirstName());   //firstName);
            model.put("lastName", user.getLastName());    //lastName);
            model.put("sendTo", user.getEmailId());
            mailService.sendMailPackageQuotation(model);
        } catch (Exception e) {
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message", e.getMessage());
            result.put("errCode","501");
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        result.put("message","success");
        result.put("errCode", "200");
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
    }

    @RequestMapping(value="/anon/getSubscribedPackageDetailsForGivenTenant", method=RequestMethod.GET)
    public @ResponseBody Map<String, Object> getSubscribedPackageDetailsForGivenTenant(@RequestParam String tenantId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "tenantId cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalUtilityFinder.getSubscribedPackageDetailsForGivenTenant(tenantId);
    }
}
