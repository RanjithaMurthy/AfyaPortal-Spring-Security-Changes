/**
* Created on 08/02/2015 19:30:00.
*/

//AppointmentModule = angular.module('AppointmentRequestModule', ['mgcrea.ngStrap', 'angularUtils.directives.dirPagination'])
afyaApp.config(function ($datepickerProvider) {
    angular.extend($datepickerProvider.defaults, {
        dateFormat: 'dd-MM-yyyy',
        autoclose : true,
        startWeek: 1
    });
})
.directive('ngFocusPrev', function() {
  return {
        link: function(scope, element, attrs) {
              var previousSibling = element[0].previousSibling;
              while(previousSibling.nodeType == 3) {
                    previousSibling = previousSibling.previousSibling;
              }
              element.on('click', function() {
                    previousSibling.focus();
              });
        }
  }
})

afyaApp.controller('AppointmentRequestController', ['$scope', '$http', '$stateParams','$state','$location', function ($scope, $http, $stateParams, $state, $location) {
    $scope.clinicList = [];
    $scope.doctorList = [];
    $scope.innerDoctorList = [];
    $scope.patientList = [];
    $scope.portalBaseURL7879 = '/afya-portal'; //http://5.9.249.196:7878/afya-portal
    $scope.clinicBaseURL7577 = 'http://5.9.249.196:7577/ospedale';
    $scope.doctorFromAvailServiceEvent = 0;

    $scope.context = { clinicId: null, providerId: null};
    $scope.clinicLocation = { clinicLocation: null };
    $scope.appointmentSearchType = "Clinic";
    $scope.providerId = null;
    $scope.clinicId = null;
    $scope.appointmentConfData = { afyaId:null, civilId:null, firstName: null, middleName: null, lastName: null, email: null, mobile: null, date:null, purpose:null, gender:null,dateOfBirth:null,location:null,
      date1:null, date2:null, date3:null, slot1:null, slot2:null, slot3:null, tariff:null, totalTariff:null };
    $scope.packageServiceFilter = "?packageServiceName=Appointment  Request - Doctors";
    $scope.appointmentVisitType = 'Consult Visit';
    $scope.imageType = null;

    /*scope to hold the selected variables*/
    $scope.clinicName = null;
    $scope.doctorName = null;
    $scope.showSearchFilter = true;
    /* Added After UI Changes(27 Oct) */
    $scope.totalMembers = 0;
    $scope.selectionCount = 0;
    //$scope.filterType = null;
    $scope.serviceType = 'Appointment  Request';
    $scope.DoctorsDetails = null;
    $scope.currency = 'KD'; // TO DO: take this from DB
    $scope.tariff = null;
    $scope.totalTariff = null;

    angular.element(document).ready(function () {
        $scope.doctorFromAvailServiceEvent = $location.search().doctor;//getParameterValueByProperty('doctor');
        $scope.providerFromAvailServiceEvent = $location.search().clinic;//getParameterValueByProperty('clinic');
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

        // populate data
        if($scope.doctorFromAvailServiceEvent > 0){
            populateBasedOnProviderDoctorAndVisitType($scope.doctorFromAvailServiceEvent, $scope.providerFromAvailServiceEvent);
        } else{
            populateData();
        }
    });

    $scope.onSearchClick = function () {
        populateData();
        clearSearchText();
    }
    // Show Doctors Click event
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
             $('#lblMessage' + clinicId + $scope.innerDoctorList[i].providerId).css("display", "none");
        }
        //var d = document.getElementById("docContent" + index);
        //d.className = d.className + " doc-active";
        $("#docContent" + clinicId + providerId).addClass("doc-active");
        $('#lblMessage' + clinicId + providerId).css("display", "block");
    }

    // clearing arraylist
    function clearList() {
        $scope.clinicList = [];
        $scope.doctorList = [];
        $scope.innerDoctorList = [];
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
        var option = $scope.appointmentSearchType;
        clearList();
        //$scope.appointmentSearchType = option;
        if (option == "Clinic") {
            $http.get($scope.portalBaseURL7879 + '/anon/getAllClinics' + $scope.packageServiceFilter).success(function (data) {
                $scope.clinicList = data;
                $scope.selectionCount = data.length;
                //console.log($scope.clinicList);
            });
        }
        else {
            $http.get($scope.portalBaseURL7879 + '/anon/getAllDoctors' + $scope.packageServiceFilter).success(function (data) {
                $scope.doctorList = data;
                $scope.selectionCount = data.length;
                //console.log($scope.doctorList);
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

    // on Request Appointment Click
    $scope.onRequestAppointmentClick = function (doctor) {
        $scope.DoctorsDetails = doctor;
        if ($scope.DoctorsDetails.date1 == null || $scope.DoctorsDetails.date1 == undefined ||
            $scope.DoctorsDetails.date2 == null || $scope.DoctorsDetails.date2 == undefined ||
            $scope.DoctorsDetails.date3 == null || $scope.DoctorsDetails.date3 == undefined) {
            sweetAlert({ title: "", text: 'Date is Mandatory', type: "info" });
            return false;
        }

        if ($scope.DoctorsDetails.slot1 == null || $scope.DoctorsDetails.slot1 == undefined ||
            $scope.DoctorsDetails.slot2 == null || $scope.DoctorsDetails.slot2 == undefined ||
            $scope.DoctorsDetails.slot3 == null || $scope.DoctorsDetails.slot3 == undefined) {
            sweetAlert({ title: "", text: 'Slot is Mandatory', type: "info" });
                                     return false;
        }


        date1 = getAppointmentFormatedDate(new Date($scope.DoctorsDetails.date1));
        date2 = getAppointmentFormatedDate(new Date($scope.DoctorsDetails.date2));
        date3 = getAppointmentFormatedDate(new Date($scope.DoctorsDetails.date3));

        $scope.DoctorsDetails.date1 = date1;
        $scope.DoctorsDetails.date2 = date2;
        $scope.DoctorsDetails.date3 = date3;

        var slot1 = $scope.DoctorsDetails.slot1.time;
        var slot2 = $scope.DoctorsDetails.slot2.time;
        var slot3 = $scope.DoctorsDetails.slot3.time;

        if((date1 == date2) && (slot1 == slot2 ||slot1 == slot3))
        {
            sweetAlert({ title: "", text: 'Date and Time slot should not be same', type: "warning" });
            return false;
        }

        if((date2 == date3) && (slot2 == slot1 || slot2 == slot3))
        {
            sweetAlert({ title: "", text: 'Date and Time slot should not be same', type: "warning" });
            return false;
        }
        if((date3 == date1) && (slot3 == slot1 || slot3 == slot2))
        {
            sweetAlert({ title: "", text: 'Date and Time slot should not be same', type: "warning" });
            return false;
        }

        // assign timeSlot, clinicName, doctorName to Scope
        $scope.clinicName = doctor.clinicName;
        $scope.doctorName = doctor.firstName + ' ' + doctor.lastName;
        $scope.appointmentConfData = { clinic: doctor.clinicName, doctor: doctor.firstName + doctor.lastName };

        $scope.providerId = doctor.providerId;
        $scope.clinicId = doctor.clinicId;
        ValidatePatientLogedInStatus(doctor.clinicId, doctor.providerId);
    }

    // Validate User before Book appointment
    function ValidatePatientLogedInStatus(clinicId, providerId) {
        //if($scope.username != undefined || $scope.username != null){
            //get patient object by username
            var data = getPatientByUsername($scope.username);
            if(data != null && (data.username != null || data.username != undefined)){
                if(readCookieFromUserName("role") != "ROLE_PATIENT"){
                    sweetAlert({ title: "",text: 'Hi, This service is available only if you are logged in as a Patient.',type: "warning" });
                    return false;
                }
                $scope.patientList = data; // assigning to scope so that can use it after confirm page
                // show confirmation window
                var totalTariff = 0;
                if (data.afyaId != null || data.afyaId != undefined) {
                    getDoctorTariff($scope.clinicId, $scope.providerId, $scope.appointmentVisitType);

                    $scope.appointmentConfData = { afyaId:data.afyaId , civilId:data.civilId, firstName: data.firstName, middleName: data.middleName, lastName: data.lastName, email: data.emailId, mobile: data.mobileNumber, clinic: $scope.clinicName, doctor: $scope.doctorName, purpose:null, clinicId:clinicId, gender:data.gender,dateOfBirth:data.dateOfBirth,location:$scope.clinicLocation, date1:$scope.DoctorsDetails.date1, date2:$scope.DoctorsDetails.date2,date3:$scope.DoctorsDetails.date3,slot1:$scope.DoctorsDetails.slot1.time, slot2:$scope.DoctorsDetails.slot2.time, slot3:$scope.DoctorsDetails.slot3.time, totalTariff:$scope.totalTariff  };
                    $("#modalAppointmentRequest").modal("show");
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
                $scope.context = { clinicId: clinicId, providerId: providerId};
                // Showing Login Modal
                $("#myModal").modal("show");
            }

            /*$http.get('/afya-portal/anon/getPatientByUsername?username=' + $scope.username).success(function (data) {
                //$scope.patientList = { "username": "pttwo@email.com", "afyaId": "KWT542", "civilId": null, "salutation": null, "firstName": "pat", "middleName": null, "lastName": "two", "endMostName": null, "gender": "Male", "age": "9 Month(s)", "dateOfBirth": "2014-11-10", "emailId": "pttwo@email.com", "mobileNumber": "11000011", "patientType": "CASH PAYING", "nationality": null, "loginPreference": "EMAIL", "isCommunityMember": 1, "MemberType": "Community" };
                $scope.patientList = data; // assigning to scope so that can use it after confirm page

                $scope.appointmentConfData = { firstName: data.firstName, middleName: data.middleName, lastName: data.lastName, email: data.emailId, mobile: data.mobileNumber, clinic: $scope.clinicName, doctor: $scope.doctorName, purpose:null, clinicId:clinicId };
                //$scope.appointmentConfData = { firstName: "pat", middleName: null, lastName: 'two', email: "pttwo@email.com", mobile: '1111000011', clinic: $scope.clinicName, doctor: $scope.doctorName, slot: $scope.appointmentSlotSelected, traff: $scope.providerTraff };
                // show confirmation window
                $("#modalAppointmentRequest").modal("show");
            }).error(function (data, status) {
                 if (data.error != null) {
                     $scope.context = { clinicId: clinicId, providerId: providerId};
                     // Showing Login Modal
                     $("#myModal").modal("show");
                 }
            });*/
        //}
        //else{
        //    $scope.context = { clinicId: clinicId, providerId: providerId};
        //     // Showing Login Modal
        //     $("#myModal").modal("show");
        //}

    }

    function ShowLoginErrorMessage() {
        sweetAlert('Please Enter the Correct Username and Password !');
    }

     // On AppointmentDate change
    $scope.onAppointmentCalendarDateChange = function (clinicId, providerId, appointmentDate, index) {
        //$(this).datepicker('hide');
        appointmentDate = new Date(appointmentDate);
        appointmentDate = getAppointmentFormatedDate(appointmentDate);
        //alert(clinicId + ',' + providerId + ',' + appointmentDate);
        //getAvailableTimeSlotsByDate($scope.clinicId, $scope.providerId, appointmentDate, index);

        getAvailableTimeSlotsByDate(clinicId, providerId, appointmentDate, index);
    }

    // get Available Time slots By Provider and Date
    function getAvailableTimeSlotsByDate(clinicId, providerId, appointmentDate, index) {
        //console.log(index);
        //alert('Inside getAvailableTimeSlotsByDate');
        //providerId = 10041; Rawan Qais
        //$http.get('/afya-portal/anon/getAvailableTimeslotsForAGivenTenantDateAndDoctor?tenantId=' + clinicId + '&doctorId=' + providerId + '&appointmentDate=' + appointmentDate + '&visitType=' + $scope.appointmentVisitType, {headers : {'accept': 'text/json'}}).success(function (data) {
        $http.get($scope.clinicBaseURL7577 + '/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId=' + clinicId + '&providerId=' + providerId + '&appointmentDate=' + appointmentDate + '&visitType=' + $scope.appointmentVisitType).success(function (data) {
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

    // On Login Submit
    $('#loginform_appointment').submit(function (event) {
        event.preventDefault();
        var url= location.href;
        //alert(url);
        var data = 'username=' + $('#requestUsername').val() + '&password=' + $('#requestPassword').val();
        //alert(data);
        $.ajax({
            data: data,
            timeout: 1000,
            type: 'POST',
            url: '/afya-portal/login'

        }).done(function(data, textStatus, jqXHR) {
            ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId);
            // close modal
            $("#myModal").modal("hide");
            // Set header and footer after post login
            initializePage();
        }).fail(function(jqXHR, textStatus, errorThrown) {
            //alert('Booh! Wrong credentials, try again!');
            var errormessage= JSON.parse(jqXHR.responseText)
                    console.log(errormessage);
                    sweetAlert({ title: "Sorry", text: errormessage.message, type: "error" });
        });
    });
//    // On Login Submit
//    $scope.onLoginClick = function (userName, password) {
//        /*$.ajax({
//            url : '/afya-portal/loginViaPopUp?username='+ userName +'&password='+ password,
//            method : 'POST',
//            contentType:"application/json",
//            Accept : "application/json",
//            //data : JSON.stringify({username : userName, password : password}),
//            success : function(data){
//                // Calling Same book appointment click
//                ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId);
//                // close modal
//                $("#myModal").modal("hide");
//                // Set header and footer after post login
//                initializePage();
//                *//*$("#member_login").modal("hide");
//                loginSuccessHandler();*//*
//            },
//            error : function(data){
//                // user does not exists or password is incorrect
//                ShowLoginErrorMessage();
//                console.log(status);
//            }
//        });*/
//        $http.post('/afya-portal/loginViaPopUp?username=' + userName + '&password=' + password).success(function (data) {
//            // Calling Same book appointment click
//            // close modal
//            $("#myModal").modal("hide");
//            ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId);
//            // Set header and footer after post login
//            initializePage();
//        }).error(function (data, status, headers, config) {
//            // user does not exists or password is incorrect
//            ShowLoginErrorMessage();
//            console.log(status);
//        });
//    }

    function GetTimeMessage(preferedTime1, preferedTime2, preferedTime3){
        preferedTime1 = preferedTime1==undefined ? '': preferedTime1;
        preferedTime2 = preferedTime2==undefined ? '': preferedTime2;
        preferedTime3 = preferedTime3==undefined ? '': preferedTime3;
        var timeMessage = " Preferred time(s) : " + preferedTime1 + (preferedTime2 == "" ? "": ", "+ preferedTime2) + (preferedTime3 == "" ? "": ", "+ preferedTime3);
        return timeMessage;
    }

    $scope.onSendRequestClick = function () {
        /*if ($scope.appointmentConfData.selectionDate1 == null || $scope.appointmentConfData.selectionDate1 == undefined ||
            $scope.appointmentConfData.selectionDate2 == null || $scope.appointmentConfData.selectionDate2 == undefined ||
            $scope.appointmentConfData.selectionDate3 == null || $scope.appointmentConfData.selectionDate3 == undefined) {
            sweetAlert({ title: "", text: 'Date is Mandatory', type: "info" });
            return false;
        }

        if ($scope.appointmentConfData.mySlot1 == null || $scope.appointmentConfData.mySlot1 == undefined ||
            $scope.appointmentConfData.mySlot2 == null || $scope.appointmentConfData.mySlot2 == undefined ||
            $scope.appointmentConfData.mySlot3 == null || $scope.appointmentConfData.mySlot3 == undefined) {
            sweetAlert({ title: "", text: 'Slot is Mandatory', type: "info" });
                                     return false;
        }


        var date1 = getAppointmentFormatedDate(new Date($scope.appointmentConfData.selectionDate1));
        var date2 = getAppointmentFormatedDate(new Date($scope.appointmentConfData.selectionDate2));
        var date3 = getAppointmentFormatedDate(new Date($scope.appointmentConfData.selectionDate3));

        var solt1 = $scope.appointmentConfData.mySlot1.time;
        var solt2 = $scope.appointmentConfData.mySlot2.time;
        var solt3 = $scope.appointmentConfData.mySlot3.time;

        if((date1 == date2) && (solt1 == solt2 ||solt1 == solt3))
        {
            sweetAlert({ title: "", text: 'Date and Time slot should not be same', type: "warning" });
            return false;
        }

        if((date2 == date3) && (solt2 == solt1 || solt2 == solt3))
        {
            sweetAlert({ title: "", text: 'Date and Time slot should not be same', type: "warning" });
            return false;
        }
       if((date3 == date1) && (solt3 == solt1 || solt3 == solt2))
       {
            sweetAlert({ title: "", text: 'Date and Time slot should not be same', type: "warning" });
            return false;
       }*/

        var dateTimeSlots1 = GenerateStartTimeAndEndTime($scope.DoctorsDetails.slot1.time, $scope.DoctorsDetails.date1);
        var dateTimeSlots2 = GenerateStartTimeAndEndTime($scope.DoctorsDetails.slot2.time, $scope.DoctorsDetails.date2);
        var dateTimeSlots3 = GenerateStartTimeAndEndTime($scope.DoctorsDetails.slot3.time, $scope.DoctorsDetails.date3);
        var pFullName = $scope.appointmentConfData.firstName + ' ' + ($scope.appointmentConfData.middleName != null ? $scope.appointmentConfData.middleName + ' ' : '') + $scope.appointmentConfData.lastName;
        var dob = new Date($scope.appointmentConfData.dateOfBirth);
        var obj = JSON.stringify(
        {
            "providerId": $scope.providerId,
            "afyaId":$scope.appointmentConfData.afyaId,
            "visitType":"ConsultVisit",
            "civilId":$scope.appointmentConfData.clinicId,
            "firstName":$scope.appointmentConfData.firstName,
            "lastName":$scope.appointmentConfData.lastName,
            "mobileNumber":$scope.appointmentConfData.mobile,
            "emailId":$scope.appointmentConfData.email,
            "dateOfBirth":getFormatedDate_DDMMYYYY(dob) + ' ' + getFormatedTime(dob),
            "location":$scope.appointmentConfData.location,
            "gender": $scope.appointmentConfData.gender,
            "firstAppointmentStartDate": dateTimeSlots1['start'],
            "firstAppointmentEndDate":dateTimeSlots1['end'],
            "secondAppointmentStartDate":dateTimeSlots2['start'],
            "secondAppointmentEndDate":dateTimeSlots2['end'],
            "thirdAppointmentStartDate":dateTimeSlots3['start'],
            "thirdAppointmentEndDate":dateTimeSlots3['start'],
            "notes":$scope.appointmentConfData.purpose
        });
        console.log(obj);
        // closing modal for Request Appointment
        $("#modalAppointmentRequest").modal('hide');
        $http({
            //url: '/afya-portal/anon/requestAppointment?clinicId=' + $scope.appointmentConfData.clinicId,
            url: $scope.clinicBaseURL7577 + '/clinicMaster/requestAppointment?clinicId=' + $scope.appointmentConfData.clinicId,
            method:"POST",
            headers: {
                'Content-Type': 'application/json'
            },
            data: obj
        }).success(function(data){
            console.log(data);
            // removing userObject to get the latest object after AfyaId updates
            var patData = getPatientByUsername($scope.username);
            if(patData.MemberType == "Visitor") { //"Visitor | Community"
                afyaSessionStore().remove('userObject');
                getPatientByUsername($scope.username);
            }
            sweetAlert({
                    title: "Success",
                    text: 'Thanks for being part of Afya, the appointment has been requested. Please view in My Account > Upcoming services..<br> Once clinic acknowledges the appointment, you can make the payment to confirm the appointment.',
                    type: "success",//error
                    html: true,
                },
            function () {
                // redirect to confirmation.html to show the booking details
                /*var bookedOn = dateTimeSlots1['start'] - dateTimeSlots1['end']  + ' or ' + dateTimeSlots2['start'] - dateTimeSlots2['end'] + ' or ' + dateTimeSlots3['start'] - dateTimeSlots3['end']
                $state.go("confirmation", {"clinicId": $scope.clinicId , "providerId" : $scope.providerId , 'bookedOn' : bookedOn , 'pName' : pFullName , 'notes' : $scope.appointmentConfData.purpose, 'type' : 'ConsultVisit'});*/
                // redirect to Upcoming Services to show the booking details
                $state.go("PatientDashboard/Patient-upcoming-smart-services");
            });


       }).error(function(data){
            sweetAlert({ title: "",text: 'Error while requesting',type: "error" });
       });
        //var messageToDisplay = "";
        // Append message[as reffereed times] to purpose if any
        //if(preferedTime1 == undefined && preferedTime2 == undefined && preferedTime3 == undefined){
            //$scope.appointmentConfData.purpose += " Preferred time(s) : " + preferedTime1 + preferedTime2 + preferedTime3;
        //}
        //else {
        //    messageToDisplay = GetTimeMessage(preferedTime1, preferedTime2, preferedTime3);
        //}
        // Creating Messages according to Show and send Clinic Notes
        //var messageToSendClinic = "Preferred Date : " + $('#selectionDate').val() + "." + messageToDisplay;
        //messageToDisplay = ($scope.appointmentConfData.purpose == null ? "" : $scope.appointmentConfData.purpose + ". ") + messageToDisplay;
        //$scope.appointmentConfData.purpose = ($scope.appointmentConfData.purpose == null ? "" : $scope.appointmentConfData.purpose + ". ") + messageToSendClinic;

        //var toDate = date;
        //var pFullName = $scope.appointmentConfData.firstName + ' ' + ($scope.appointmentConfData.middleName != null ? $scope.appointmentConfData.middleName + ' ' : '') + $scope.appointmentConfData.lastName;
//        var obj = JSON.stringify(
//        {
//            "messageText": $scope.appointmentConfData.purpose,
//            "urgent":true,
//            "sentOn": (toDate.getFullYear() + '-' +  ((toDate.getMonth() + 1) >= 10 ? '' : '0') + (toDate.getMonth() + 1) + '-' +  (toDate.getDate() >= 10 ? '' : '0') + toDate.getDate() + ' ' + toDate.getHours() + ':' + toDate.getMinutes() + ':' + toDate.getSeconds()),		//toDate.toISOString(),
//            "patientId":"",
//            "patientFirstName":$scope.appointmentConfData.firstName,
//            "patientMiddleName":$scope.appointmentConfData.middleName,
//            "patientLastName":$scope.appointmentConfData.lastName,
//            "patientEmail":$scope.appointmentConfData.email,
//            "patientContactNo":$scope.appointmentConfData.mobile,
//            "doctorId":$scope.appointmentConfData.doctorId,
//            "visitType": 'AppointmentRequest'
//            //"visitType":"Telephony"scheduleId: $scope.scheduleToUpdate.scheduleId, appointmentStartDate : startDateTime, appointmentEndDate : endDateTime, notes : schedule.notes, visitType : schedule.visitType
//        });
        // closing modal for Request Appointment
//        $("#modalAppointmentRequest").modal('hide');
//        $http({
//            url: $scope.clinicBaseURL7577 + '/clinicMaster/requestAppointment?clinicId=' + $scope.appointmentConfData.clinicId,
//            method:"POST",
//            headers: {
//                'Content-Type': 'application/json'
//            },
//            data: obj
//        }).success(function(data){
//            $("#modalAppointmentRequest").modal("show");
//        }).error(function(data){
//            sweetAlert({ title: "",text: 'Error while requesting',type: "error" });
//        });
    }

    $('#myModal').on('shown.bs.modal', function () {
        $('#login_username').focus();
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
                // hide full registration form
                $("#modalFullRegistration").modal("hide");
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

    $scope.onUserImageClick = function(image, type)
    {
        $("#modelUserProfile").modal("show");
        $scope.userImage = image;
        $scope.imageType = type;

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
            var data = date.split('/');
            var dateObj = new Date(data[2], (data[1] - 1).toString(), data[0]);
            formatedDate = getFormatedDate(dateObj);
        }
        return formatedDate;
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

    setDecimal = function (input) {
        if (isNaN(input)) return input;
        // If we want 1 decimal place, we want to mult/div by 10
        // If we want 2 decimal places, we want to mult/div by 100, etc
        // So use the following to create that factor
        var places = 3;
        var factor = "1" + Array(+(places > 0 && places + 1)).join("0");
        return (Math.round(input * factor) / factor).toFixed(places);
    };

     function getDoctorTariff(clinicId, providerId, visitType){
        $http.get('/afya-portal/anon/getDoctorTariff?clinicId=' + clinicId + '&doctorId=' + providerId + '&visitTypeName=' + visitType).success(function (data) {
            $scope.tariff = data.BILLABLE_AMOUNT_RCM;
            $scope.totalTariff = data.BILLABLE_AMOUNT_TOTAL;
        }).error(function (data, status, headers, config) {
            console.log(status);
        });
    }

} ]);