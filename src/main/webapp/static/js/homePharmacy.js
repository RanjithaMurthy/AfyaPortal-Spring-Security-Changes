/**
* Created on 08/02/2015 19:30:00.
*/

// HomePharmacyModule = angular.module('HomePharmacyModule', ['mgcrea.ngStrap', 'angularUtils.directives.dirPagination'])
afyaApp.config(function ($datepickerProvider) {
    angular.extend($datepickerProvider.defaults, {
        dateFormat: 'dd-MM-yyyy',
        autoclose : true,
        startWeek: 1
    });
})

afyaApp.controller('HomePharmacyController', ['$scope', '$http', '$stateParams', '$state', '$location', function ($scope, $http, $stateParams, $state, $location) {
    $scope.clinicList = [];
    $scope.prescriptionList = [];
    $scope.patientList = [];
    $scope.portalBaseURL7879 = '/afya-portal';//'http://5.9.249.196:7879/afya-portal'; //http://5.9.249.196:7878/afya-portal
    $scope.clinicBaseURL7577 = 'http://5.9.249.196:7577/ospedale';
    //    $scope.portalBaseURL7879 = 'http://5.9.249.196:7879/afya-portal';
    $scope.dataLoaded = true;
    $scope.clinicIdFilter = $location.search().clinicId;
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
    $scope.dataLoaded = false;

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
            /*if(readCookieFromUserName("role") != "ROLE_PATIENT"){
                sweetAlert({ title: "",text: 'Hi, This service is available only if you are logged in as a Patient.',type: "warning" });
                return false;
            }*/
            //get patient object by username
            var data = getPatientByUsername($scope.username);
            $scope.patientList = data; // assigning to scope so that can use it after confirm page
            $scope.afyaId = data.afyaId;
            // show confirmation window
            displayPrescriptionByClinic(clinicId);
            /*$http.get('/afya-portal/anon/getPatientByUsername').success(function (data) {
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
            });*/
        }
        else{
            $("#myModal").modal("show");
            return false;
        }


    }

    // clearing arraylist
    function clearList() {
        $scope.clinicList = [];
        $scope.prescriptionList = [];
    }
    // get list of clinic/doctor according to Search Selection
    function populateData() {
        /*$http.get($scope.portalBaseURL7879 + '/anon/getAllClinics' + $scope.packageServiceFilter).success(function (data) {
            if($scope.clinicIdFilter != undefined)
                $scope.clinicList = $.grep(data,function(el){return el.clinicId == $scope.clinicIdFilter;});
            else
                $scope.clinicList = data;

            $scope.dataLoaded = true;
        });*/

        $scope.gridListTypeType = "Prescription";

        //get patient object by username
        var data = getPatientByUsername($scope.username);
        $scope.patientList = data; // assigning to scope so that can use it after confirm page
        $scope.afyaId = data.afyaId;
        if(data.afyaId != null || data.afyaId != undefined)
            getPrescriptionDetailsByPatient($scope.afyaId);

        // closing loader on load
        $scope.dataLoaded = true;
        //alert($scope.prescriptionList.length);
    }

    function getPrescriptionDetailsByPatient(afyaId){
         $http.get($scope.clinicBaseURL7577 + '/clinicMaster/getActivePrescription?afyaId=' + afyaId).success(function (data) {
         //$http.get('/afya-portal/anon/getPatientActivePrescription?afyaId=' + afyaId).success(function (data) {
             if(data.length > 0){
                 clearList();
                 /*var newObjWithFormattedDate = $.map($.grep(data, function (obj, id) {
                     return (obj.drugName != "");
                 }), function (obj, id) {
                     // taking the Universal date format [yyyy-mm-dd] (Since firefox has issues to convert date from string)
                     var obj = $.extend({}, obj); obj.startDateDisplay = getAppointmentFormatedDate(new Date(obj.startDate.substring(0, 10))); return obj;
                 });*/
                 var newObjWithFormattedDate = $.map($.grep(data, function (obj, id) {
                      return (obj.patientRxId != "");
                  }), function (obj, id) {
                      //var obj = $.extend({}, obj); obj.totalCountRequested = parseInt(obj.totalCount, 10); return obj;
                      var obj = $.extend({}, obj); obj.totalCountRequested = obj.totalCount; obj.totalCountTrack = parseInt(obj.totalCount); return obj;
                  });

                 $scope.prescriptionList = newObjWithFormattedDate;
                 // show Place Order Button
                 showPlaceOrderButton(true);
                 console.log($scope.prescriptionList);
             }
             else
                clearList();
             /*else {
                 sweetAlert({ title: "Sorry",text: 'You have no valid prescription to be requested for Home Pharmacy Service.',type: "info" });
             }*/
         });
     }

    // get list of Prescription by clinicId
    function displayPrescriptionByClinic(clinicId, location) {
        // Set clinic location
        $scope.clinicLocation = location;
        var afyaId = $scope.afyaId; // 'KWT721';
        //alert('location : ' + selectedLocation);
        $http.get($scope.clinicBaseURL7577 + '/clinicMaster/getActivePrescription?afyaId=' + afyaId + '&clinicId=' + clinicId).success(function (data) {
        //$http.get('/afya-portal/anon/getActivePrescription?afyaId=' + afyaId + '&clinicId=' + clinicId).success(function (data) {
            if(data.length > 0){
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
                sweetAlert({ title: "Sorry",text: 'You have no valid prescription to be requested for Home Pharmacy Service.', type: "info" });
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
        $scope.dataLoaded = false;
        var jsonObj = [];
        var names = [];
        var selectedClinicJson = [];
        var selectedPharmacyJson = [];
        $('#homePharmacyFieldset input:checked').each(function () {
            names.push($(this).attr("value"));
        });

        if (names.length <= 0) {
            //sweetAlert('Please Select at least one Prescription !');
            swal("", "Please Select at least one Prescription!", "warning")
            $scope.dataLoaded = true;
            return false;
        }
        //$scope.prescriptionList
       // $.each(names, function (index, value) {
            $scope.SelectedPrescriptionList = $.map($.grep($scope.prescriptionList, function (obj, id) {
                return names.indexOf(obj.patientRxId.toString()) >= 0;
            }), function (obj, id) { return obj; });
        //});
        console.log($scope.SelectedPrescriptionList);

        $.each($scope.SelectedPrescriptionList, function (index, obj) {
            item = {};
            item["patientRxId"] = obj.patientRxId.toString();
            item["orderQuantity"] = obj.totalCountTrack.toString();
            jsonObj.push(item);
            // create the selected clinic array to validate unique id
            clinicItem = {};
            clinicItem = obj.clinicId.toString();
            selectedClinicJson.push(clinicItem);
            // create the selected pharmacy array to validate unique id
            pharmacyItem = {};
            pharmacyItem = obj.pharmacyId.toString();
            selectedPharmacyJson.push(pharmacyItem);
        });

        if(!ValidateUniqueValue(selectedClinicJson, true)){
            sweetAlert("Oops", "Your order consists of medicine from multiple clinics. Please select Medicines from single clinic and place the order.", "warning")
            $scope.dataLoaded = true;
            return false;
        }

        if(!ValidateUniqueValue(selectedPharmacyJson, false)){
            sweetAlert("Oops", "Your order consists of medicine from multiple pharmacies. Please select Medicines from single Pharmacy and place the order.", "warning")
            $scope.dataLoaded = true;
            return false;
        }

        var settings = {
            clinicId: $scope.clinicId,
            jsonObj: JSON.stringify(jsonObj)
        };
        console.log(settings);

        // Call service 
        $http.post($scope.clinicBaseURL7577 + '/clinicMaster/createActivePrescriptionOrder?clinicId=' + $scope.clinicId, settings.jsonObj).success(function (data) {
        //$http.post('/afya-portal/anon/createActivePrescriptionOrder?clinicId=' + $scope.clinicId, settings.jsonObj).success(function (data) {
            //alert('Active Prescription Order created successfully.');
            console.log(data);
            $scope.dataLoaded = true;
            $scope.serviceCost = data.totalAmount;
            $scope.pharmacyOrderId = data.orderId;
            $scope.serviceCostUOM = data.currencyUom;
            var message = "";
            if (data.orderId != null || data.orderId != undefined) {
                /*message = 'Order has been placed successfully.' + ' \nOrder Id :' + data.orderId + ' and Total Amount : ' + data.currencyUom + ' ' + data.totalAmount;
                message = message + '\nPlease check the updates in Upcoming Services.';*/
                sweetAlert({
                    title: "Order Status",
                    text: 'Thanks, your order has been placed successfully , please view details in Upcoming Services',
                    type: "success"
                },
                function () {
                    $state.go("PatientDashboard/Patient-upcoming-smart-services");
                    //window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
                });
            }
            else {
                //sweetAlert('Order Id Cannot be null');
                sweetAlert({ title: "Sorry", text: 'Your order has not been processed by the Pharmacy. Please resend the request', type: "warning" });
                return false;
            }

            // Payment

            // To set the Payment status of the Order (after Successful Payment) 
            //            $http.get($scope.clinicBaseURL7577 + '/clinicMaster/createActivePrescriptionPayment?clinicId=' + $scope.clinicId + '&orderId' + $scope.pharmacyOrderId + '&afyaId' + $scope.afyaId).success(function (data) {
            //                alert('Success createActivePrescriptionPayment !');
            //            });
        }).error(function (data, status, headers, config) {
            //sweetAlert('Error while creating order!');
            sweetAlert({ title: "Sorry", text: 'We have faced error while placing the Order for your prescription. Please resend the request', type: "error" });
            console.log(status);
            $scope.dataLoaded = true;
        });
        //$scope.dataLoaded = true;
    }

    function ValidateUniqueValue(json, isForClinic){
        var result = true;
        var uniqueObj = json.filter(function(elem, pos) {
            return json.indexOf(elem) == pos;
        });
        if(uniqueObj.length != 1){
            result = false;
        }
        else {
            if(isForClinic){ $scope.clinicId = uniqueObj[0]; }// set the Clinic }
        }
        return result;
    }


    function ShowLoginErrorMessage() {
        //sweetAlert('Please Enter the Correct Username and Password !');
        sweetAlert({ title: "Error",text: 'Please Enter the Correct Username and Password',type: "error" });
    }

    // On Login Submit 
    $scope.onLoginClick = function (userName, password) {
        //$http.post('/afya-portal/api_login?username=' + userName + '&password=' + password).success(function (data) {
        $http.post('/afya-portal/loginViaPopUp?username=' + userName + '&password=' + password).success(function (data) {
            // close login modal
            $("#myModal").modal("hide");
            // Calling Get Prescription by Clinic
            ValidatePatientLogedInStatus($scope.clinicId);
            // Set header and footer after post login
            initializePage();
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

    $('#myLogin').on('shown.bs.modal', function () {
        $('#login_username').focus();
    })








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
        // $http.post($scope.portalBaseURL7879 + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus).success(function (data) {
        $http.post($scope.portalBaseURL7879 + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus + "&isysMerchantRef=" + settings.isysMerchantRef).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            console.log('Error : updatePaymentTransactionWithIsysStatus');
        });

        //        afyaApp.config.baseUrlForSearch + "updatePaymentTransactionWithIsysStatus?paymentId=" + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef 
        //      + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp 
        //      + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus

    };

} ]);