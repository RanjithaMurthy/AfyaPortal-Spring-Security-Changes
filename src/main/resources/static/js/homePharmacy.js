/**
* Created on 08/02/2015 19:30:00.
*/

HomePharmacyModule = angular.module('HomePharmacyModule', ['mgcrea.ngStrap', 'angularUtils.directives.dirPagination'])
HomePharmacyModule.config(function ($datepickerProvider) {
    angular.extend($datepickerProvider.defaults, {
        dateFormat: 'dd-MM-yyyy',
        autoclose : true,
        startWeek: 1
    });
})

HomePharmacyModule.controller('HomePharmacyController', ['$scope', '$http', function ($scope, $http) {
    $scope.clinicList = [];
    $scope.prescriptionList = [];
    $scope.patientList = [];
    $scope.portalBaseURL7879 = '/afya-portal';//'http://5.9.249.196:7879/afya-portal'; //http://5.9.249.196:7878/afya-portal
    $scope.clinicBaseURL7577 = 'http://5.9.249.196:7577/ospedale';
    //    $scope.portalBaseURL7879 = 'http://5.9.249.196:7879/afya-portal';

    $scope.context = { clinicId: null, providerId: null, appointmentDate: null, timeSlot: null };
    $scope.clinicLocation = { clinicLocation: null };
    //$scope.appointmentVisitType = { visitType: null };
    //$scope.appointmentSearchType = null;
    $scope.gridListTypeType = "Clinic";
    $scope.appointmentDateSelected = null;
    $scope.providerId = null;
    $scope.clinicId = null;
    $scope.afyaId = null;
    $scope.appointmentConfData = { firstName: null, middleName: null, lastName: null, email: null, mobile: null, clinic: null, doctor: null, slot: null, traff: null };
    $scope.packageServiceFilter = "?packageServiceName=";

    /*scope to hold the Order values*/
    $scope.serviceCost = null;
    $scope.pharmacyOrderId = null;
    $scope.serviceCostUOM = null;

    angular.element(document).ready(function () {
        //get visitType from query string
        //$scope.appointmentVisitType = getParameterValueByProperty('visitType');
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

    $scope.onDisplayPrescriptionClick = function (clinicId, location) {
        //alert('Inside onDisplayPrescriptionClick');
        if (clinicId != null || clinicId != undefined)
            $scope.clinicId = clinicId;

        ValidatePatientLogedInStatus(clinicId);
    }

    // Validate User before Book appointment
    function ValidatePatientLogedInStatus(clinicId) {
        if(ValidateUserLoginStatus()){
            $http.get('/afya-portal/anon/getPatientByUsername').success(function (data) {
                $scope.patientList = data; // assigning to scope so that can use it after confirm page
                $scope.afyaId = data.afyaId;
                // show confirmation window
                displayPrescriptionByClinic(clinicId);
            }).error(function (data, status) {
                // User not logged in [status is 400 Bad Request]
                if (data.error != null) {
                    // Showing Login Modal
                    $("#myModal").modal("show");
                }
                //console.log(data);
            });
        }
        else{
            $("#myModal").modal("show");
            return false;
        }


    }

    // clearing arraylist
    function clearList() {
        $scope.clinicList = [];
    }

    // get list of clinic/doctor according to Search Selection
    function populateData() {
        $http.get($scope.portalBaseURL7879 + '/anon/getAllClinics' + $scope.packageServiceFilter).success(function (data) {
            $scope.clinicList = data;
        });
    }

    // get list of Prescription by clinicId
    function displayPrescriptionByClinic(clinicId, location) {

        // Set clinic location
        $scope.clinicLocation = location;
        var afyaId = $scope.afyaId; // 'KWT721';
        //alert('location : ' + selectedLocation);
        $http.get($scope.clinicBaseURL7577 + '/clinicMaster/getActivePrescription?afyaId=' + afyaId + '&clinicId=' + clinicId).success(function (data) {
            if(data.length>0){
                clearList();
                var newObjWithFormattedDate = $.map($.grep(data, function (obj, id) {
                    return (obj.drugName != "");
                }), function (obj, id) {
                    // taking the Universal date format [yyyy-mm-dd] (Since firefox has issues to convert date from string)
                    var obj = $.extend({}, obj); obj.startDateDisplay = getAppointmentFormatedDate(new Date(obj.startDate.substring(0, 10))); return obj;
                });
                /*angular.forEach(data, function (pkg) {
                    data.startDateDisplay = getAppointmentFormatedDate(new Date(pkg.startDate));
                });*/

                $scope.prescriptionList = newObjWithFormattedDate;
                // show Place Order Button
                showPlaceOrderButton(true);
                $scope.gridListTypeType = "Prescription";
                console.log(newObjWithFormattedDate);
            }
            else {
                //sweetAlert('No Prescription Details Found!');
                sweetAlert({ title: "",text: 'No Prescription Details Found!',type: "info" });
            }
        });


    }

    // show hide place order button
    function showPlaceOrderButton(status) {
        //btnPlaceHomePharmacyOrder
        if (status)
            $('#btnPlaceHomePharmacyOrder').show();
        else
            $('#btnPlaceHomePharmacyOrder').hide();
    }

    $scope.toggleObjSelection = function ($event, patientRxId, pharmacyId) {
        $event.stopPropagation();
        //alert(patientRxId + ' , ' + pharmacyId);
        console.log('Checkbox Checked : ' + patientRxId + ' , ' + pharmacyId);
    }

    $scope.onPrescriptionPlaceOrderClick = function () {
        //alert('PrescriptionPlaceOrderClick');

        var jsonObj = [];
        var names = [];
        $('#homePharmacyFieldset input:checked').each(function () {
            names.push($(this).attr("value"));
        });

        if (names.length <= 0) {
            //sweetAlert('Please Select at least one Prescription !');
            swal("", "Please Select at least one Prescription!", "warning")
            return false;
        }
        $.each(names, function (index, value) {
            item = {}
            item["patientRxId"] = value;
            jsonObj.push(item);
        });

        //var info = $('#homePharmacyData').data();

        var settings = {
            clinicId: $scope.clinicId,
            jsonObj: JSON.stringify(jsonObj)
        };
        // Call service 
        $http.post($scope.clinicBaseURL7577 + '/clinicMaster/createActivePrescriptionOrder?clinicId=' + $scope.clinicId, settings.jsonObj).success(function (data) {
            //alert('Active Prescription Order created successfully.');
            console.log(data);

            $scope.serviceCost = data.totalAmount;
            $scope.pharmacyOrderId = data.orderId;
            $scope.serviceCostUOM = data.currencyUom;
            var message = "";
            if (data.orderId != null || data.orderId != undefined) {
                /*message = 'Order has been placed successfully.' + ' \nOrder Id :' + data.orderId + ' and Total Amount : ' + data.currencyUom + ' ' + data.totalAmount;
                message = message + '\nPlease check the updates in Upcoming Services.';*/
                sweetAlert({
                    title: "Order Status",
                    text: 'Order placed successfully - view details in "Upcoming Services"',
                    type: "success"//error
                },

                function () {
                    window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
                });
            }
            else {
                sweetAlert('Order Id Cannot be null');
                return false;
            }

            // Payment

            // To set the Payment status of the Order (after Successful Payment) 
            //            $http.get($scope.clinicBaseURL7577 + '/clinicMaster/createActivePrescriptionPayment?clinicId=' + $scope.clinicId + '&orderId' + $scope.pharmacyOrderId + '&afyaId' + $scope.afyaId).success(function (data) {
            //                alert('Success createActivePrescriptionPayment !');
            //            });
        }).error(function (data, status, headers, config) {
            //sweetAlert('Error while creating order!');
            sweetAlert({ title: "Error",text: 'Error while creating order',type: "error" });
            console.log(status);
        });
    }


    function ShowLoginErrorMessage() {
        //sweetAlert('Please Enter the Correct Username and Password !');
        sweetAlert({ title: "Error",text: 'Please Enter the Correct Username and Password',type: "error" });
    }

    // On Login Submit 
    $scope.onLoginClick = function (userName, password) {
        $http.post('/afya-portal/api_login?username=' + userName + '&password=' + password).success(function (data) {
            //alert('Logged in successfully.');
            if (data.message == "success") {
                // close modal
                $("#myModal").modal("hide");
                // Calling Get Prescription by Clinic
                ValidatePatientLogedInStatus($scope.clinicId);
            }
            else if (data.message == "error") {
                ShowLoginErrorMessage();
            }
        }).error(function (data, status, headers, config) {
            // user does not exists or password is incorrect
            ShowLoginErrorMessage();
            console.log(status);
        });
    }

    function ValidateUserLoginStatus (){
        var status = false;
        if(readCookieFromUserName("username")==null){
            status = false;
        }
        else{
            status = true;
        }
        return status;
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
            var data = date.split('/');
            var dateObj = new Date(data[2], (data[1] - 1).toString(), data[0]);
            formatedDate = getFormatedDate(dateObj);
        }
        return formatedDate;
    }


    ////////////// PAYMENT GATEWAY INTEGRATION RELATED SERVICE CALLS /////////////////
    if (window.afyaApp.svc == undefined)
        window.afyaApp.svc = {};
    window.afyaApp.svc.addPaymentTransactionForAppointment = function (settings) {
        // make service call
        $http.post($scope.portalBaseURL7879 + '/anon/addPaymentTransactionForAppointment?transactionType=' + settings.transactionType + "&transactionAmount=" + settings.transactionAmount + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId + "&apptSlot=" + settings.apptSlot).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            console.log('Error : addPaymentTransactionForAppointment');
        });

        //        afyaApp.config.baseUrlForSearch + "addPaymentTransactionForAppointment?transactionType=" + settings.transactionType + "&transactionAmount=" + settings.transactionAmount
        //    + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId
        //    + "&apptSlot=" + settings.apptSlot

    };

    //updatePaymentTransactionWithIsysStatus
    window.afyaApp.svc.updatePaymentTransactionWithIsysStatus = function (settings) {
        // make service call
        $http.post($scope.portalBaseURL7879 + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            console.log('Error : updatePaymentTransactionWithIsysStatus');
        });

        //        afyaApp.config.baseUrlForSearch + "updatePaymentTransactionWithIsysStatus?paymentId=" + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef 
        //      + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp 
        //      + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus

    };

} ]);