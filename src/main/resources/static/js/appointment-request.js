/**
* Created on 08/02/2015 19:30:00.
*/

AppointmentModule = angular.module('AppointmentRequestModule', ['mgcrea.ngStrap', 'angularUtils.directives.dirPagination'])
AppointmentModule.config(function ($datepickerProvider) {
    angular.extend($datepickerProvider.defaults, {
        dateFormat: 'dd-MM-yyyy',
        autoclose : true,
        startWeek: 1
    });
})

AppointmentModule.controller('AppointmentRequestController', ['$scope', '$http', function ($scope, $http) {
    $scope.clinicList = [];
    $scope.doctorList = [];
    $scope.patientList = [];
    $scope.portalBaseURL7879 = '/afya-portal'; //http://5.9.249.196:7878/afya-portal
    $scope.clinicBaseURL7577 = 'http://5.9.249.196:7577/ospedale';

    $scope.context = { clinicId: null, providerId: null};
    $scope.clinicLocation = { clinicLocation: null };
    $scope.appointmentSearchType = null;
    $scope.providerId = null;
    $scope.clinicId = null;
    $scope.appointmentConfData = { firstName: null, middleName: null, lastName: null, email: null, mobile: null, date:null, purpose:null };
    $scope.packageServiceFilter = "?packageServiceName=Appointment  Request - Doctors";

    /*scope to hold the selected variables*/
    $scope.clinicName = null;
    $scope.doctorName = null;

    angular.element(document).ready(function () {
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
        populateData();
    });

    $scope.onSearchClick = function () {
        populateData();
    }

    $scope.onDisplayDoctorsClick = function (clinicId, location) {
        displayDoctorsByClinic(clinicId, location);
    }

    // clearing arraylist
    function clearList() {
        $scope.clinicList = [];
        $scope.doctorList = [];
    }

    // get list of clinic/doctor according to Search Selection
    function populateData() {
        var option = $("#selSearch :selected").text();
        clearList();
        $scope.appointmentSearchType = option;
        if (option == "Clinic") {
            $http.get($scope.portalBaseURL7879 + '/anon/getAllClinics' + $scope.packageServiceFilter).success(function (data) {
                $scope.clinicList = data;
            });
        }
        else {
            $http.get($scope.portalBaseURL7879 + '/anon/getAllDoctors' + $scope.packageServiceFilter).success(function (data) {
                $scope.doctorList = data;
            });
        }
    }

    // get list of doctors by clinicId
    function displayDoctorsByClinic(clinicId, location) {
        clearList();
        // Set clinic location
        $scope.clinicLocation = location;
        //alert('location : ' + selectedLocation);
        $http.get($scope.portalBaseURL7879 + '/anon/fetchProvidersByClinicId?clinicId=' + clinicId).success(function (data) {
            $scope.doctorList = data;
        });
    }

    // on Request Appointment Click
    $scope.onRequestAppointmentClick = function (doctor) {
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
        if($scope.username != undefined || $scope.username != null){
            $http.get('/afya-portal/anon/getPatientByUsername?username=' + $scope.username).success(function (data) {
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
            });
        }
        else{
            $scope.context = { clinicId: clinicId, providerId: providerId};
             // Showing Login Modal
             $("#myModal").modal("show");
        }

    }

    function ShowLoginErrorMessage() {
        sweetAlert('Please Enter the Correct Username and Password !');
    }

    // On Login Submit
    $scope.onLoginClick = function (userName, password) {
        //alert('userName=' + userName + ' password=' + password);
        $http.post('/afya-portal/api_login?username=' + userName + '&password=' + password).success(function (data) {
            //alert('Logged in successfully.');
            if (data.message == "success") {
                // Calling Same book appointment click
                ValidatePatientLogedInStatus($scope.context.clinicId, $scope.context.providerId);
                // close modal
                $("#myModal").modal("hide");
            }
            else if (data.message == "error") {
                // Calling Same book appointment click
                ShowLoginErrorMessage();
            }
            //console.log(data);
        }).error(function (data, status, headers, config) {
            // user does not exists or password is incorrect
            ShowLoginErrorMessage();
            console.log(status);
        });
    }

    function GetTimeMessage(preferedTime1, preferedTime2, preferedTime3){
        preferedTime1 = preferedTime1==undefined ? '': preferedTime1;
        preferedTime2 = preferedTime2==undefined ? '': preferedTime2;
        preferedTime3 = preferedTime3==undefined ? '': preferedTime3;
        var timeMessage = " Preferred time(s) : " + preferedTime1 + (preferedTime2 == "" ? "": ", "+ preferedTime2) + (preferedTime3 == "" ? "": ", "+ preferedTime3);
        return timeMessage;
    }
    //on Confirmation Continue Click
    $scope.onSendRequestClick = function (date, preferedTime1, preferedTime2, preferedTime3) {
        if (date == null || date == undefined) {
            sweetAlert({ title: "",text: 'Please provide a Date',type: "info" });
            return false;
        }
        // Append message[as reffereed times] to purpose if any
        if(preferedTime1 == undefined && preferedTime2 == undefined && preferedTime3 == undefined){
            //$scope.appointmentConfData.purpose += " Preferred time(s) : " + preferedTime1 + preferedTime2 + preferedTime3;
        }
        else {
            var message = GetTimeMessage(preferedTime1, preferedTime2, preferedTime3);
            $scope.appointmentConfData.purpose = ($scope.appointmentConfData.purpose == null ? "" : $scope.appointmentConfData.purpose) + message;
        }


        var toDate = date;
        var pFullName = $scope.appointmentConfData.firstName + ' ' + ($scope.appointmentConfData.middleName != null ? $scope.appointmentConfData.middleName + ' ' : '') + $scope.appointmentConfData.lastName;
        var obj = JSON.stringify(
        {
            "messageText": $scope.appointmentConfData.purpose,
            "urgent":true,
            "sentOn": (toDate.getFullYear() + '-' +  ((toDate.getMonth() + 1) >= 10 ? '' : '0') + (toDate.getMonth() + 1) + '-' +  (toDate.getDate() >= 10 ? '' : '0') + toDate.getDate() + ' ' + toDate.getHours() + ':' + toDate.getMinutes() + ':' + toDate.getSeconds()),		//toDate.toISOString(),
            "patientId":"",
            "patientFirstName":$scope.appointmentConfData.firstName,
            "patientMiddleName":$scope.appointmentConfData.middleName,
            "patientLastName":$scope.appointmentConfData.lastName,
            "patientEmail":$scope.appointmentConfData.email,
            "patientContactNo":$scope.appointmentConfData.mobile,
            "doctorId":$scope.appointmentConfData.doctorId,
            "visitType": 'AppointmentRequest'
            //"visitType":"Telephony"scheduleId: $scope.scheduleToUpdate.scheduleId, appointmentStartDate : startDateTime, appointmentEndDate : endDateTime, notes : schedule.notes, visitType : schedule.visitType
        });

        $http({
            url: $scope.clinicBaseURL7577 + '/clinicMaster/requestAppointment?clinicId=' + $scope.appointmentConfData.clinicId,
            method:"POST",
            headers: {
                'Content-Type': 'application/json'
            },
            data: obj
        }).success(function(data){
            $("#modalConfirmation").modal('hide');
            var bookedOn = getAppointmentFormatedDate(toDate) + " 12:00:00";//31-08-2015 16:00:00
            window.location = "confirmation.html?clinicId=" + $scope.clinicId + "&providerId=" + $scope.providerId + '&bookedOn=' + bookedOn + '&pName=' + pFullName + '&notes=' + $scope.appointmentConfData.purpose + '&type=AppointmentRequest';
        }).error(function(data){
        });
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

} ]);