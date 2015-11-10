﻿/**
 * Created on 08/02/2015 19:30:00.
 */

//AppointmentModule = angular.module('AppointmentModule', ['mgcrea.ngStrap', 'angularUtils.directives.dirPagination'])
afyaApp.config(['$httpProvider', '$datepickerProvider',function ($httpProvider, $datepickerProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
    delete $httpProvider.defaults.headers.post['Content-type'];
    angular.extend($datepickerProvider.defaults, {
        dateFormat: 'dd-MM-yyyy',
        autoclose : true,
        startWeek: 1
    });
}]);

afyaApp.controller('AppointmentController', ['$scope', '$http', '$stateParams','$state', '$location', '$window',  function ($scope, $http, $stateParams,$state, $location, $window) {

    $scope.clinicList = [];
    $scope.doctorList = [];
    $scope.innerDoctorList = [];
    $scope.patientList = [];
    $scope.portalBaseURL7879 = '/afya-portal';  //'/afya-portal';// 'http://5.9.249.196:7879/afya-portal'; //http://5.9.249.196:7878/afya-portal
    $scope.clinicBaseURL7577 = 'http://5.9.249.196:7577/ospedale';
    $scope.doctorFromAvailServiceEvent = 0;
    //    $scope.portalBaseURL7879 = 'http://5.9.249.196:7879/afya-portal';

    $scope.context = { clinicId: null, providerId: null, appointmentDate: null, timeSlot: null };
    $scope.clinicLocation = { clinicLocation: null };
    $scope.appointmentVisitType = { visitType: null };
    $scope.appointmentSearchType = "Clinic";
    $scope.providerSearchType = "Premium";
    $scope.appointmentDateSelected = null;
    $scope.providerId = null;
    $scope.clinicId = null;
    $scope.appointmentConfData = { firstName: null, middleName: null, lastName: null, email: null, mobile: null, clinic: null, doctor: null, slot: null, tariff: null, tariffTotal: null };
    $scope.patientRegisterData = { cvilId: null, firstName: null, middleName: null, lastName: null, nationality: null, gender: null, dateOfBirth: null, email: null, mobile: null, loginPreference : null };
    $scope.packageServiceFilter = null;
    // payment mode properties
    $scope.PayMode = {
                       knetValue: {name: 'KNET', fees: '', totalPayable: ''},
                       visaMasterValue: {name: 'VISAMASTER', fees: '', totalPayable: ''}
                     };
    $scope.currency = 'KD'; // TO DO: take this from DB
    $scope.PayMode.mode = $scope.PayMode.visaMasterValue;

    //$scope.dataLoaded = true;
    /*scope to hold the selected variables*/
    $scope.clinicName = null;
    $scope.doctorName = null;
    $scope.appointmentSlotSelected = null;
    $scope.providerTariff = null;
    $scope.providerTariffTotal = null;
    //$scope.appointmentVisitType=$stateParams.visitType;
    $scope.appointmentVisitType =  $location.search().visitType;
    $scope.appointmentBreadcrumbText =  null;
    $scope.patientServicePolicyContent = "";

    $scope.paymentGatewayMessage = afyaApp.paymentGatewayMessage;
    $scope.showSearchFilter = true;
    /* Added After UI Changes(27 Oct) */
    $scope.totalMembers = 0;
    $scope.selectionCount = 0;
    $scope.isVisitor = true;
    $scope.isSearchCareProvider = false;

    angular.element(document).ready(function () {
        //get visitType from query string
        //$scope.appointmentVisitType = getParameterValueByProperty('visitType');

        $scope.doctorFromAvailServiceEvent = $location.search().doctor;//getParameterValueByProperty('doctor');
        $scope.providerFromAvailServiceEvent = $location.search().clinic;//getParameterValueByProperty('clinic');
        SetPackageServiceFilter($scope.appointmentVisitType);//$scope.packageServiceFilter
        // get portal URL from properties
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL7879 = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL7879 = 'http://5.9.249.196:7878/afya-portal';
            }
            if(!angular.equals(response.data.CLINIC_BASE_URL, null) && !angular.equals(response.data.CLINIC_BASE_URL, '' && !angular.equals(response.data.CLINIC_BASE_URL, undefined))){
                $scope.clinicBaseURL7577 = response.data.CLINIC_BASE_URL;
            }else{
                $scope.clinicBaseURL7577 = 'http://5.9.249.196:7878/afya-portal';
            }
            // get username if exists
            var username = $.cookie("username");
            $scope.username = username;
        });

        // populate default load data
        if($scope.doctorFromAvailServiceEvent > 0){
            populateBasedOnProviderDoctorAndVisitType($scope.doctorFromAvailServiceEvent, $scope.providerFromAvailServiceEvent);
        } else{
            populateData();
        }

        $("#CivilId").keypress(function (e) {
            //if the letter is not digit then display error and don't type anything
            if (e.which != 8 && e.which != 0 && (e.which < 48 || e.which > 57)) {
                //display error message
                $("#errmsg").html("Enter Numbers Only").show().fadeOut("slow");
                return false;
            }
        });
    });

    $scope.onSearchClick = function () {
        //$scope.dataLoaded = false;
        populateData();
        clearSearchText();
        //$scope.dataLoaded = true;
    }

    $scope.onMemberTypeChange = function () {
        populateData();
    }

    // Book Appointment with Doctor Click (Show Doctors By Clinic)
    $scope.onShowDoctorsClick = function (clinicId, location, index) {
        for(var i=0;i<$scope.clinicList.length;i++){
            $('#docList' + $scope.clinicList[i].clinicId).css("display", "none");
        }
        //$("#docContent0").addClass("doc-active");
        $('#docList' + clinicId).css("display", "block");
        displayDoctorsByClinic(clinicId, location);
        //clearSearchText(); // commented due to no need of clearing[Going to 1st page issue resolved]
    }

    $scope.addClass = function (clinicId, providerId, index) {
        for(var i=0;i<$scope.innerDoctorList.length;i++){
            //$('#docContent' + i).css("display", "none");
             $("#docContent" + clinicId + $scope.innerDoctorList[i].providerId).removeClass("doc-active");
        }
        //var d = document.getElementById("docContent" + index);
        //d.className = d.className + " doc-active";
        $("#docContent" + clinicId + providerId).addClass("doc-active");
    }

    // clearing array list
    function clearList() {
        $scope.clinicList = [];
        $scope.doctorList = [];
        $scope.innerDoctorList = [];
        //$scope[tempSlotList] = null;
        $scope.mySlot = null;
    }
    function clearSearchText() {
        $scope.searchText = null;
    }

    function getProviderSummary () {
        //http://localhost:7879/afya-portal/anon/getProviderSummary?summaryType=|ALL_PROVIDERS|
        $http.get($scope.portalBaseURL7879 + '/anon/getProviderSummary?summaryType=|ALL_PROVIDERS|').success(function (data) {
            var newData = $.map($.grep(data, function (obj, idx) {
                return (obj.SummaryType == "ALL_PROVIDERS");
            }), function (obj, idx) { return obj; });
            $scope.totalMembers = newData[0].SummaryCount;
            //console.log(data);
        });
    }

    // get list of clinic/doctor according to Search Selection
    function populateData() {
        //var option = $("#selSearch :selected").text();
        //var option = 'Clinic';
        clearList();
        var option = $scope.appointmentSearchType;
        if (option == "Clinic") {
            $http.get($scope.portalBaseURL7879 + '/anon/getAllClinics' + $scope.packageServiceFilter).success(function (data) {
                if($scope.isSearchCareProvider){
                    var providerMemberType = ($scope.providerSearchType == "Premium") ? "PREMIUM" : "COMMUNITY";
                    var clinicList = $.map($.grep(data, function (obj, idx) {
                        return (obj.providerMemberType == providerMemberType);
                    }), function (obj, idx) { return obj; });
                    $scope.clinicList = clinicList;
                }
                else{
                    $scope.clinicList = data;
                }
                $scope.selectionCount = $scope.clinicList.length;
                //console.log(data);
            });
        }
        else {
            $http.get($scope.portalBaseURL7879 + '/anon/getAllDoctors' + $scope.packageServiceFilter).success(function (data) {
                $scope.doctorList = data;
                $scope.selectionCount = data.length;
                //console.log(data);
            });
        }
        getProviderSummary();
    }

    function populateBasedOnProviderDoctorAndVisitType(doctorFromAvailServiceEvent, providerFromAvailServiceEvent){
        $http.get('/afya-portal/anon/getAllDoctors')
            .success(function (data) {
                clearList();
                angular.forEach(data, function(eachData){
                    if(angular.equals(doctorFromAvailServiceEvent, eachData.providerId) && angular.equals(providerFromAvailServiceEvent, eachData.clinicId))
                        $scope.doctorList.push(eachData);
                });})
            .error(function(data){
            });
    };
    // get list of doctors by clinicId
    function displayDoctorsByClinic(clinicId, location) {
        //clearList();
        $scope.doctorList = [];
        $scope.innerDoctorList = [];
        // Set clinic location
        $scope.clinicLocation = location;
        //alert('location : ' + selectedLocation);
        $http.get($scope.portalBaseURL7879 + '/anon/fetchProvidersByClinicId?clinicId=' + clinicId).success(function (data) {
            //$scope.appointmentSearchType = "Doctor";
            if(data.length > 0) {
                //$scope.doctorList = data;// ??????????????????? check this
                $scope.innerDoctorList = data;
            }
            else
                sweetAlert({ title: "",text: 'No Doctors Available !',type: "info" });
        });
    }

    // On AppointmentDate change
    $scope.onAppointmentCalendarDateChange = function (clinicId, providerId, appointmentDate, index) {
        //$(this).datepicker('hide');
        appointmentDate = new Date(appointmentDate);
        appointmentDate = getAppointmentFormatedDate(appointmentDate);
        //alert(clinicId + ',' + providerId + ',' + appointmentDate);
        getAvailableTimeSlotsByDate(clinicId, providerId, appointmentDate, index);
    }

    // get Available Time slots By Provider and Date
    function getAvailableTimeSlotsByDate(clinicId, providerId, appointmentDate, index) {
        //console.log(index);
        //alert('Inside getAvailableTimeSlotsByDate');
        //providerId = 10041; Rawan Qais
        $http.get($scope.clinicBaseURL7577 + '/clinicMaster/getAvailableTimeslotsForAGivenTenantDateAndDoctor?tenantId=' + clinicId + '&doctorId=' + providerId + '&appointmentDate=' + appointmentDate + '&visitType=' + $scope.appointmentVisitType).success(function (data) {
        //$http.get('/afya-portal/anon/getAvailableTimeslotsForAGivenTenantDateAndDoctor?tenantId=' + clinicId + '&doctorId=' + providerId + '&appointmentDate=' + appointmentDate + '&visitType=' + $scope.appointmentVisitType).success(function (data) {
            var tempSlotList = "appointmentTimeSlots" + index;
            // get required slots according to visitType
            var appointmentLists = $.map($.grep(data, function (obj, idx) {
                return (obj.visitType == $scope.appointmentVisitType);
            }), function (obj, idx) { return obj; });

            $scope[tempSlotList] = appointmentLists;
            if (data.length > 0)
                $scope.mySlot = $scope[tempSlotList][0]; // need to bind the default
            if( Object.prototype.toString.call( data ) === '[object Array]' ) {
                if (appointmentLists.length <= 0) {
                    sweetAlert({ title: "Sorry",text: 'Slots for the selected dates are not available, please select alternate dates & slots.',type: "info" });
                    return false;
                }
            }
            else if( typeof data === 'string' ) {
                if (appointmentLists.length <= 0) {
                    sweetAlert({ title: "Sorry", text: data, type: "info"});
                    return false;
                }
            }

            if (appointmentLists.length <= 0) {
                //sweetAlert('Sorry! There are no slots available for this date. Please Select another date.');
                sweetAlert({ title: "", text: data, type: "info"});
            }
            //console.log(appointmentLists);
        }).error(function (data, status, headers, config) {
            //var s = data;
            if (data.indexOf("appointment from past") > -1) {
                //
                var tempSlotList = "appointmentTimeSlots" + index;
                // get required slots according to visitType
                //var appointmentLists = null;

                $scope[tempSlotList] = null;// appointmentLists;
                sweetAlert({ title: "Sorry",text: 'Date has passed we all have moved ahead, select future dates',type: "warning" });
            }
            else
                sweetAlert({ title: "",text: "Hi, This doctor has not activated the Calendar for this service, please try later.",type: "error" });
        });
    }

    // on Book Appointment Click
    $scope.onBookAppointmentClick = function (doctor, appointmentDate, timeSlot) {
        // add location if its not there[if it coming from doctor filteration]
        if(doctor.location != undefined){
            // Set clinic location
            $scope.clinicLocation = doctor.location;
        }
        if (appointmentDate == null || appointmentDate == undefined || timeSlot == null || timeSlot == undefined) {
            sweetAlert({ title: "Oops",text: 'For booking need to select to Appointment Date & Slot.',type: "info" });
            return false;
        }
        //getDoctorTariff(doctor.clinicId, doctor.providerId, $scope.appointmentVisitType)

        // assign timeSlot, clinicName, doctorName to Scope
        $scope.appointmentSlotSelected = timeSlot;
        $scope.clinicName = doctor.clinicName;
        $scope.doctorName = doctor.firstName + ' ' + doctor.lastName;
        //$scope.appointmentConfData = { clinic: doctor.clinicName, doctor: doctor.firstName + doctor.lastName, slot: $scope.appointmentSlotSelected, tariff: $scope.providerTariff };

        appointmentDate = getAppointmentFormatedDate(appointmentDate);
        $scope.appointmentDateSelected = appointmentDate;
        $scope.providerId = doctor.providerId;
        $scope.clinicId = doctor.clinicId;

        /*if($scope.providerTariff == null)
            getDoctorTariff(doctor.clinicId, doctor.providerId, $scope.appointmentVisitType)*/

        $http.get($scope.portalBaseURL7879 + '/anon/getDoctorTariff?clinicId=' + $scope.clinicId + '&doctorId=' + $scope.providerId + '&visitTypeName=' + $scope.appointmentVisitType).success(function (data) {
            $scope.providerTariff = setDecimal(data.BILLABLE_AMOUNT_RCM);
            $scope.providerTariffTotal = setDecimal(data.BILLABLE_AMOUNT_TOTAL);

            ValidatePatientLogedInStatus(doctor.clinicId, doctor.providerId, appointmentDate, timeSlot);
        }).error(function (data, status, headers, config) {
            console.log(status);
        });
        //ValidatePatientLogedInStatus(doctor.clinicId, doctor.providerId, appointmentDate, timeSlot);
    }

    // Validate User before Book appointment
    function ValidatePatientLogedInStatus(clinicId, providerId, appointmentDate, timeSlot) {
        //if($scope.username != undefined || $scope.username != null){
            //get patient object by username
            var data = getPatientByUsername();
            if(data != null && (data.username != null || data.username != undefined)){
                if(readCookieFromUserName("role") != "ROLE_PATIENT"){
                    initializePage();
                    sweetAlert({ title: "", text: 'Hi, This service is available only if you are logged in as a Patient.', type: "warning" });
                    return false;
                }
                $scope.patientList = data; // assigning to scope so that can use it after confirm page
                //set appointment confirmation data
                $scope.appointmentConfData = { firstName: data.firstName, middleName: data.middleName, lastName: data.lastName, email: data.emailId, mobile: data.mobileNumber, clinic: $scope.clinicName, doctor: $scope.doctorName, slot: $scope.appointmentDateSelected + ' (' + $scope.appointmentSlotSelected + ')', tariff: $scope.providerTariff, tariffTotal: $scope.providerTariffTotal };
                // show confirmation/Complete Registration window by Status
                if (data.afyaId != null || data.afyaId != undefined) {
                    //$("#modalConfirmation").modal("show");
                    $scope.afyaPatientPolicyForServiceHandler.showPopup(function(){
                        $("#modalConfirmation").modal("show");
                    });
                }
                else {
                    //set patient register data
                    $scope.patientRegisterData = { cvilId: null, firstName: data.firstName, middleName: data.middleName, lastName: data.lastName, nationality: null, gender: null, dateOfBirth: null, email: data.emailId, mobile: data.mobileNumber, loginPreference: data.loginPreference };
                    GetAllNationality();
                    // show full registration modal [if user has done minimal registration]
                    $("#modalFullRegistration").modal("show");
                }
            }
            else {
                $scope.context = { clinicId: clinicId, providerId: providerId, appointmentDate: appointmentDate, timeSlot: timeSlot };
                // Showing Login Modal
                //$("#myModal").modal("show");
                $("#myModal").modal("show");
            }
        //}
        //else{
            //sweetAlert({ title: "",text: 'User Not logged in.',type: "warning" });
        //    $scope.context = { clinicId: clinicId, providerId: providerId, appointmentDate: appointmentDate, timeSlot: timeSlot };
        //    // Showing Login Modal
        //    //$("#myModal").modal("show");
        //    $("#myModal").modal("show");
        //}
    }

    function ShowLoginErrorMessage() {
        //sweetAlert('Please Enter the Correct Username and Password !');
        sweetAlert({ title: "Error", text: "Please Enter the Correct Username and Password !", type: "error" });
    }

    // On Login Submit
    $('#loginform_appointment').submit(function (event) {
        event.preventDefault();
        var url= location.href;
        //alert(url);
        var data = 'username=' + $('#appointmentUsername').val() + '&password=' + $('#appointmentPassword').val();
        //alert(data);
        $.ajax({
            data: data,
            timeout: 1000,
            type: 'POST',
            url: '/afya-portal/login'

        }).done(function(data, textStatus, jqXHR) {
        // $("#member_login_spring_ajax").modal("hide");
             $("#myModal").modal("hide");
            // Calling Same book appointment click
            ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId, $scope.context.appointmentDate, $scope.context.timeSlot);
            // Set header and footer after post login
            initializePage();

        }).fail(function(jqXHR, textStatus, errorThrown) {
            //alert('Booh! Wrong credentials, try again!');
            var errormessage= JSON.parse(jqXHR.responseText)
            console.log(errormessage);
            sweetAlert({ title: "Sorry", text: errormessage.message, type: "error" });
        });
    });
//    $scope.onLoginClick = function (userName, password) {
//        /*$.ajax({
//            url : '/afya-portal/loginViaPopUp?username='+ userName +'&password='+ password,
//            method : 'POST',
//            contentType:"application/json",
//            Accept : "application/json",
//            //data : JSON.stringify({username : userName, password : password}),
//            success : function(data){
//            // Calling Same book appointment click
//                ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId, $scope.context.appointmentDate, $scope.context.timeSlot);
//                // close modal
//                $("#myModal").modal("hide");
//                // Set header and footer after post login
//                initializePage();
//            },
//            error : function(data){
//                // user does not exists or password is incorrect
//                ShowLoginErrorMessage();
//                console.log(status);
//            }
//        });*/
//        $http.post('/afya-portal/loginViaPopUp?username=' + userName + '&password=' + password).success(function (data) {
//            // close modal
//            $("#myModal").modal("hide");
//            // Calling Same book appointment click
//            ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId, $scope.context.appointmentDate, $scope.context.timeSlot);
//            // Set header and footer after post login
//            initializePage();
//        }).error(function (data, status, headers, config) {
//            // user does not exists or password is incorrect
//            ShowLoginErrorMessage();
//            console.log(status);
//        });
//    }

    // get Doctor tariff Amount
    function getDoctorTariff(clinicId, providerId, visitType){
        $http.get('/afya-portal/anon/getDoctorTariff?clinicId=' + clinicId + '&doctorId=' + providerId + '&visitTypeName=' + visitType).success(function (data) {
            $scope.providerTariff = data.BILLABLE_AMOUNT_RCM;
            $scope.providerTariffTotal = data.BILLABLE_AMOUNT_TOTAL;
        }).error(function (data, status, headers, config) {
            console.log(status);
        });
    }

    $scope.onRegisterClick = function (patientRegisterData){
        //alert('Registration clicked' + patientRegisterData);
        if(patientRegisterData.dateOfBirth != null && $('#RegDateOfBirth').val() != "")
            patientRegisterData.dateOfBirth = dateFormatting($('#RegDateOfBirth').val());
        if(patientRegisterData.nationality == null)
            patientRegisterData.nationality = $('#nationalityList').val();
        if($('#Gender').val() == ""){
            sweetAlert({ title: "",text: 'Gender is Mandatory',type: "warning" });
            return false;
        }
        else{
            patientRegisterData.gender = $('#Gender').val();
        }

        if(patientRegisterData.dateOfBirth == null || patientRegisterData.dateOfBirth == undefined){
            //sweetAlert('Date of Birth cannot be empty.');
            sweetAlert({ title: "",text: 'Date of Birth is Mandatory',type: "warning" });
            return false;
        }
        //patientRegisterData.dateOfBirth='1985-05-30'; new Date("02-02-2015").toISOString().substring(0, 10)
        //afyaApp.config.baseUrlForSearch + 'patient/retrieveAfyaId?tenantId='+ settings.tenantId + '&facilityType=' + settings.facilityType,
        var userDetail = {
            "civilId": patientRegisterData.cvilId,
            "firstName": patientRegisterData.firstName,
            "middleName": patientRegisterData.middleName,
            "lastName": patientRegisterData.lastName,
            "gender": patientRegisterData.gender,
            "religion": '',
            "dateOfBirth": patientRegisterData.dateOfBirth,
            "age": "",
            "maritalStatus": "",
            "occupation": "",
            "employmentStatus": "",
            "preferredLanguage": "",
            "emailId": patientRegisterData.email,
            "mobileNumber": patientRegisterData.mobile,
            "faxNumber": "",
            "officePhone": "",
            "patientType": "",
            "address": "",
            "additionalAddress": "",
            "city": "",
            "postalCode": "",
            "state": "",
            //"country": $('#nationalityList').prev().text()
            "nationality": patientRegisterData.nationality
        };
        // construct settings object
        var settings = {
            tenantId: $scope.clinicId,
            facilityType: "CLINIC",
            userDetail: userDetail
        };
        console.log(settings);
        // hide full registration form
        $("#modalFullRegistration").modal("hide");
        // invoke register user
        $http({
            url : '/afya-portal/anon/patient/retrieveAfyaId?tenantId='+ settings.tenantId + '&facilityType=' + settings.facilityType,
            method : 'POST',
            data : settings.userDetail,
            headers : {
                'Content-Type': 'application/json',
                'Accept' : 'text/plain'
            }
        }).success(function (afyaId) {
            // removing userObject to get the latest object after AfyaId updates
            afyaSessionStore().remove('userObject');
            /*// hide full registration form
            $("#modalFullRegistration").modal("hide");*/
            $http({
                url : '/afya-portal/persistUserLoginFacilityAssociation?tenantId='+$scope.clinicId+'&facilityType=CLINIC',
                method : 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept' : 'text/plain'
                }
            }).success(function(tenantdata){
                console.log('Full Registration Completed successfully');
                console.log(afyaId);
                ValidatePatientLogedInStatus($scope.clinicId, $scope.providerId, $scope.appointmentDateSelected, $scope.appointmentSlotSelected);
            }).error(function(tenantdata){
                //alert("error from tenant assoc");
            });
        }).error(function (data, status, headers, config) {
            console.log(status);
            // since its not coming in success so calling in error (But Registration is getting successful)
           // ValidatePatientLogedInStatus($scope.clinicId, $scope.providerId, $scope.appointmentDateSelected, $scope.appointmentSlotSelected);
        });
    }

    //on Confirmation Continue Click
    $scope.onConfirmationContinue = function (purpose, confdata) {

        // $scope.afyaPatientPolicyForServiceHandler.showPopup(function(){onConfirmationContinueInternal();});

        onConfirmationContinueInternal();

        function onConfirmationContinueInternal(){
            var amtPayable = 0;
            if($scope.appointmentVisitType == "Premium Visit"){
                amtPayable = confdata.tariff;
            }
            else
                amtPayable = confdata.tariffTotal;
            if(amtPayable == null || amtPayable == undefined || amtPayable == 0){
                sweetAlert({ title: "", text: 'Hi, the payment amount is mandatory for moving ahead.', type: "warning" });
                return false;
            }

            $scope.purpose = purpose;// Assign Purpose to scope variable
            var timeSlots = GenerateStartTimeAndEndTime($scope.appointmentSlotSelected, $scope.appointmentDateSelected);
            // verify patient weather having AfyaId/DOB
            if ($scope.patientList.afyaId != null || $scope.patientList.afyaId != undefined) {
                var payerType = 'CONSUMER';
                // for Processing Charges refer tale payment_gateway_processing_fees
                $http.get($scope.portalBaseURL7879 + '/anon/getPaymentProcessingFees?payerType=CONSUMER&amount=' + amtPayable).success(function (data) {
                    // copy fees and total payable into PayMode binding object
                    data = data[0];
                    $scope.PayMode.knetValue.fees = setDecimal(data.debitCardFees);
                    $scope.PayMode.knetValue.totalPayable = setDecimal(data.debitCardTotalPayable);
                    $scope.PayMode.visaMasterValue.fees = setDecimal(data.creditCardFees);
                    $scope.PayMode.visaMasterValue.totalPayable = setDecimal(data.creditCardTotalPayable);
                    $scope.PayMode.payerType = payerType;
                    // show the Model dialogue for user to choose Payment Channel
                    $("#modalPaymentMode").modal("show");
                }).error(function (data, status, headers, config) {
                    console.log("Error while Getting Payment Processing Fees!" + status);
                });
            }
            else{
                // show full registration modal [if user has done minimal registration]
                $("#modalFullRegistration").modal("show");
            }
        }
    }

    $scope.onPaymentModeSelectionClick = function (mode){
        //alert('Option Selected = ' + mode);

        var timeSlots = GenerateStartTimeAndEndTime($scope.appointmentSlotSelected, $scope.appointmentDateSelected);

        var options = {
            afyaId: $scope.patientList.afyaId,
            paymentAmount: $scope.PayMode.mode.totalPayable, //$scope.totalAmount,
            processingFees: $scope.PayMode.mode.fees,
            clinicId: $scope.clinicId,
            doctorId: $scope.providerId,
            transactionType: 'APPOINTMENT_BOOKING', /* APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT */
            apptSlot: timeSlots['start'],
            description: "APPOINTMENTBOOKING",
            description2: "description2",
            errorCB: paymentErrorCB,
            pollCallback: paymentPollCallbackCB,
            baseUrlForService : $scope.portalBaseURL7879 + "/anon/",
            paymentChannel : mode,
            payerType: $scope.PayMode.payerType
        };
        console.log(options);
        // calling payment
        afyaApp.payit.initiatePayment(options);
    }

    function paymentErrorCB(errMsg) {
        sweetAlert(errMsg);
    }

    function paymentPollCallbackCB(result) {
        //alert('success payment');
        console.log("Poll Status: " + result.status);
        //BookAppointmentWithSuccessPayment()

        // Take actions based on Payment Status
        /*if ((result.status == "Declined") || (result.status == "Timed Out")) { // NOTE: THIS IS FOR PRODUCTION
            // if(result.status == "Declined"){ // NOTE: THIS IS FOR TESTING
            $("#modalConfirmation").modal("show");
        }
        else*/ if (result.status == "Success") {  // NOTE: THIS IS FOR PRODUCTION
            //else if(result.status == "Success" || result.status == "Timed Out"){ // NOTE: THIS IS FOR TESTING
            $("#modalConfirmation").modal("hide");
            // perform Final steps after a Successfull Payment
            //if(mainOption == "OPT_APPOINTMENT" || mainOption == "OPT_APPOINTMENT_REQUEST" || mainOption == "OPT_TELE_CONSULTATION" || mainOption == "OPT_HOME_VISIT")
            if ($scope.appointmentVisitType == "Premium Visit" || $scope.appointmentVisitType == "Tele Consultation Visit" || $scope.appointmentVisitType == "Home Visit") {
                BookAppointmentWithSuccessPayment(result.trackId);
            }
        }
        else if(result.status == "ERROR_CONNECTIVITY"){
            //updatePaymentStatus("ERROR_CONNECTIVITY");
            sweetAlert({ title: "Error", text: 'Check your internet connection and try again.....', type: "error" });
        }
        else if(result.status == "ERROR_UNKNOWN"){
            //updatePaymentStatus("ERROR_UNKNOWN");
            sweetAlert({ title: "Error", text: 'This service is temporarily unavailable. Please try after some time', type: "error" });
        }
        else if (result.status == "Timed Out"){
            $("#modalConfirmation").modal("show");
            sweetAlert({ title: "Error", text: 'Your transaction has timed out!', type: "error" });
        }else if (result.status != "Waiting"){
             $("#modalConfirmation").modal("show");
             sweetAlert({ title: "Sorry", text: 'The payment transaction has failed. Please retry', type: "error" });
        }
        else {
            //updatePaymentStatus("Waiting...");
        }
        //else if(mainOption == "OPT_HOME_PHARMACY"){}
    }

    function BookAppointmentWithSuccessPayment(paymentTrackId) {
        var data = $scope.patientList;
        var dob = new Date($scope.patientList.dateOfBirth); // Getting Date object from DOB(string)
        var appointmentDate = $scope.appointmentDateSelected;
        //appointmentDate = getAppointmentFormatedDate(appointmentDate);
        var timeSlots = GenerateStartTimeAndEndTime($scope.appointmentSlotSelected, appointmentDate);
        var gender = $scope.patientList.gender;
        var location = $scope.clinicLocation;
        var purpose = $scope.purpose;
        var civilId = $scope.patientList.civilId;
        //********************* Submit Book Appointment ****************//
        var bookingDetail = {
            providerId: $scope.providerId, // example: "10002"
            firstName: $scope.patientList.firstName,
            lastName: $scope.patientList.lastName,
            mobileNumber: $scope.patientList.mobileNumber,
            emailId: $scope.patientList.emailId,
            appointmentStartDate: timeSlots['start'], //"04-08-2015 16:00:00",
            appointmentEndDate: timeSlots['end'],  //"04-08-2015 16:20:00",
            location: location, // example : "001"
            notes: purpose, ///*THis will come from confirmation Page*/info.purpose,
            visitType: $scope.appointmentVisitType, // "Premium Visit",
            gender: gender, //data.gender,
            dateOfBirth: getFormatedDate_DDMMYYYY(dob) + ' ' + getFormatedTime(dob), // '22-01-1980 00:00:00'
            civilId:civilId
        };
        console.log(bookingDetail);

        // Post Appointment data to Book an appointment
        $http({
            url: $scope.clinicBaseURL7577 + '/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId=' + $scope.clinicId,
            method:"POST",
            headers: {
                'Content-Type': 'application/json',
                'accept' : 'text/plain'
            },
            data: bookingDetail
        }).success(function(data){
            // removing userObject to get the latest object after AfyaId updates
            var patData = getPatientByUsername($scope.username);
            if(patData.MemberType == "Visitor") { //"Visitor | Community"
                afyaSessionStore().remove('userObject');
                getPatientByUsername($scope.username);
            }
            var pFullName = bookingDetail.firstName + ' ' + bookingDetail.lastName;
            sweetAlert({
                    title: "Success",
                    text: 'Thanks for being part of Afya, the scheduled appointment has been booked. Please view in My Account > Upcoming services.<br> Please refer the Payment Track ID <strong>' + paymentTrackId + '</strong> in future communication',
                    type: "success",//error
                    html: true,
                },
            function () {
                // redirect to confirmation.html to show the booking details
                $state.go("confirmation", {"clinicId": $scope.clinicId , "providerId" : $scope.providerId , 'bookedOn' : timeSlots['start'] , 'pName' : pFullName , 'notes' : $scope.purpose})
            });
        }).error(function(data, status, headers, config){
            console.log(status);
        });
        /*$http.post($scope.clinicBaseURL7577 + '/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId=' + $scope.clinicId, bookingDetail).success(function (data) {
            //alert('Appointment Booked - View Details in "Upcoming Services"');


            //window.location = "confirmation.html?clinicId=" + $scope.clinicId + "&providerId=" + $scope.providerId + '&bookedOn=' + timeSlots['start'] + '&pName=' + pFullName + '&notes=' + purpose;
        }).error(function (data, status, headers, config) {
            console.log(status);
        });*/
        //********************* Submit Book Appointment END ****************//
    }

    $('#modalConfirmation').on('shown.bs.modal', function () {
        $('#purpose').focus();
    })

    function GetAllNationality(){
        $http.get($scope.portalBaseURL7879 + '/anon/getAllNationality').success(function (data) {
            $.each(data, function(index, value) {
                if(value.nationality != '')
                    $('<option>').val(value.nationality).text(value.nationality).appendTo('#nationalityList');
            });
            $('#nationalityList').find('option[value="Kuwaiti"]').attr('selected', 'selected');
            //$("#nationalityList").selectmenu("refresh");
        }).error(function (data, status, headers, config) {
            console.log("Error while Getting Nationality Data!" + status);
        });
    }

    // Set packageservice filter in get all methods[Clinic/Doctor]
    function SetPackageServiceFilter(visitType){
        switch(visitType) {
            case "Premium Visit":
                $scope.packageServiceFilter = "?packageServiceName=Appointment  Premium - Doctors";
                $scope.appointmentBreadcrumbText = 'Premium Appointment';
                break;
            case "Tele Consultation Visit":
                $scope.packageServiceFilter = "?packageServiceName=Tele Consultation";
                $scope.appointmentBreadcrumbText = 'Tele Consultation';
                break;
            case "Home Visit":
                $scope.packageServiceFilter = "?packageServiceName=Appointment - Home Visit Doctor";
                $scope.appointmentBreadcrumbText = 'Home Visit';
                break;
            default:
                $scope.packageServiceFilter = "";
                $scope.appointmentBreadcrumbText = '';
                $scope.isSearchCareProvider = true;
        }
        /*// since calling in above default so no need to use this
            if(visitType == null || visitType == undefined)
            $scope.isSearchCareProvider = true;*/
    }

    // get class dynamically according to visit type
    $scope.appliedClass = function() {
        // checking for appointmentVisitType if NULL then Hide the action Buttons(Book Appointment)
        // deciding the classes according to visit Type
        if ($scope.appointmentVisitType === "" || $scope.appointmentVisitType === undefined) {
            return "col-sm-8 appointments";// two columns for doctors
        } else {
            return "col-sm-4 appointments"; // default
        }
    }

    $scope.onPaymentModeBackClick = function (){
        // show confirmation and hide payment mode
        $("#modalPaymentMode").modal("hide");
        $("#modalConfirmation").modal("show");
    }

    $scope.onConfirmationBackClick = function (){
        // show Policy  and hide confirmation
        $("#modalConfirmation").modal("hide");
        //$("#afyaPatientPolicyForService").modal("show");
        $scope.afyaPatientPolicyForServiceHandler.showPopup(function(){
            $("#modalConfirmation").modal("show");
        });
    }

    $scope.onViewAllMemberServicesClick = function () {
        //href="#Member-area/provider-member-area" target="_blank"
        $window.open("#Member-area/provider-member-area", '_blank');
    }





    function getParameterValueByProperty(type) {
        type = type.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + type + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    // Generate Appointment StartDate and EndDate By Slot and AppointmentDate
    function GenerateStartTimeAndEndTime(timeSlot, appointmentDate) {
        var result = new Object();
        // splitting string by operator (-); will return for two arraylist only
        var timeSlots = timeSlot.split("-", 2);
        result['start'] = appointmentDate + ' ' + timeSlots[0].trim() + ":00";
        result['end'] = appointmentDate + ' ' + timeSlots[1].trim() + ":00";
        return result;
    }

    // date functions Existing
    function getFormatedDate(date) {
        var year = date.getFullYear();
        var month = (date.getMonth() + 1);
        if (month < 10)
            month = '0' + month;
        var date = date.getDate();
        if (date < 10)
            date = '0' + date;

        var formatedDate = year + '-' + month + '-' + date;
        return formatedDate;
    }

    //
    function getAppointmentFormatedDate(date) { // DD-MM-YYYY
        var year = date.getFullYear();
        var month = (date.getMonth() + 1);
        if (month < 10)
            month = '0' + month;
        var date = date.getDate();
        if (date < 10)
            date = '0' + date;

        var formatedDate = date + '-' + month + '-' + year;
        return formatedDate;
    }

    // return DD-MM-YYYY format from date object
    function getFormatedDate_DDMMYYYY(date) {
        var year = date.getFullYear();
        var month = (date.getMonth() + 1);
        if (month < 10)
            month = '0' + month;
        var date = date.getDate();
        if (date < 10)
            date = '0' + date;

        var formatedDate = date + '-' + month + '-' + year;
        return formatedDate;
    }

    function getFormatedTime(date) {
        var hours = date.getHours();
        if (hours < 10)
            hours = '0' + hours;
        var minutes = date.getMinutes();
        if (minutes < 10)
            minutes = '0' + minutes;
        var seconds = date.getSeconds();
        if (seconds < 10)
            seconds = '0' + seconds;

        var formatedTime = hours + ':' + minutes + ':' + seconds;
        return formatedTime;
    }

    function dateFormatting(date) { // date = dd/mm/YYYY
        var formatedDate = '';
        if (date != "") {
            var data = null;
            if(date.indexOf("/") > -1)
                data = date.split('/');
            else
                data = date.split('-');
            var dateObj = new Date(data[2], (data[1] - 1).toString(), data[0]);
            formatedDate = getFormatedDate(dateObj);
        }
        return formatedDate;
    }
    // round number to decimal places [considering from 5]
    setDecimal = function (input) {
        if (isNaN(input)) return input;
        // If we want 1 decimal place, we want to mult/div by 10
        // If we want 2 decimal places, we want to mult/div by 100, etc
        // So use the following to create that factor
        var places = 3;
        var factor = "1" + Array(+(places > 0 && places + 1)).join("0");
        return (Math.round(input * factor) / factor).toFixed(places);
    };


    $scope.onUserImageClick = function(image, type)
    {
        $("#modelUserProfile").modal("show");
        $scope.userImage = image;
        $scope.imageType = type;
    }
    ////////////// PAYMENT GATEWAY INTEGRATION RELATED SERVICE CALLS /////////////////
    if (window.afyaApp.svc == undefined)
        window.afyaApp.svc = {};
    window.afyaApp.svc.addPaymentTransactionForAppointment = function (settings) {
        // make service call
        $http.post($scope.portalBaseURL7879 + '/anon/addPaymentTransactionForAppointment?transactionType=' + settings.transactionType + "&transactionAmount=" + settings.transactionAmount + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId + "&apptSlot=" + settings.apptSlot + "&processingFees=" + settings.processingFees + "&payerType=" + settings.payerType + "&paymentChannel=" + settings.paymentChannel).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            sweetAlert('Error : addPaymentTransactionForAppointment');
        });
    };

    //updatePaymentTransactionWithIsysStatus
    window.afyaApp.svc.updatePaymentTransactionWithIsysStatus = function (settings) {
        // make service call
        // $http.post($scope.portalBaseURL7879 + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus).success(function (data) {
        $http.post($scope.portalBaseURL7879 + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus + "&isysMerchantRef=" + settings.isysMerchantRef).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            sweetAlert('Error : updatePaymentTransactionWithIsysStatus');
        });

        //        afyaApp.config.baseUrlForSearch + "updatePaymentTransactionWithIsysStatus?paymentId=" + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef 
        //      + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp 
        //      + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus

    };

    /////////////// Afya Patient Policy for Service Popup hander ///////////////////////////////
    $scope.afyaPatientPolicyForServiceHandler = {
        doneCallback: null,
        currService: null,
        // patientConsentReceived: false,

        /*resetConsent: function(){
            this.patientConsentReceived = false;
        },*/

        showPopup: function(doneCallback){
            // done callback
            this.doneCallback = doneCallback;
            /*if(this.patientConsentReceived == true){
                doneCallback();
            }else*/{
                var serviceName = '';
                switch($scope.appointmentVisitType){
                    case "Premium Visit":
                        serviceName = "Appointment  Premium - Doctors";
                    break;
                    case "Tele Consultation Visit":
                        serviceName = "Tele Consultation";
                    break;
                    case "Home Visit":
                        serviceName = "Appointment - Home Visit Doctor";
                    break;
                    case "homePharmacy":
                        serviceName = "Home Pharmacy - Refill of Prescription";
                    break;
                    default:
                    serviceName = '';       // should never be here
                }
                // show the modal popup
                $("#afyaPatientPolicyForService").modal("show");
                // retrieve pocilty by service
                $http.get($scope.portalBaseURL7879 + '/anon/getPatientPoilicyForService?serviceName=' + serviceName
                    , {headers : {'accept': 'text/html'}}).success(function (data) {
                    $scope.patientServicePolicyContent = data;
                }).error(function (data, status, headers, config) {
                    console.log("Error while Getting Patient Policies for Services!" + status);
                    sweetAlert("error fetching data");
                });
            }
        },

        onAgreeClicked: function(){
            // set the status to selected
            // this.patientConsentReceived = true;
            // invoke donecallback if none is defined
            if(this.doneCallback != null){
                this.doneCallback();
                this.doneCallback = null;
            }
        },
    };
} ]);
