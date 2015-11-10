/**
 * Created by Mohan Sharma on 7/30/2015.
 */

//PatientUpcomingSmartModule = angular.module('PatientUpcomingSmartModule', []);

afyaApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
    delete $httpProvider.defaults.headers.post['Content-type'];
}
]);

afyaApp.controller('UpcomingSmartServices', ['$scope', '$http', '$window', '$state', function($scope, $http, $window, $state){
    $scope.upcomingServiceList = [];
    $scope.scheduleToPay = {};
    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.alerts = [];
    $scope.memberProfileObj={};
    $scope.afyaId = null;
    $scope.scheduleToUpdate = {};
    $scope.index = null;
    $scope.lengthOfArray = null;
    $scope.timeSlotList = [];
    $scope.username = null;
    // home pharmacy scope properties
    $scope.pharmacyOrderList = [];
    $scope.order = {};
    // payment properties
    $scope.PayMode = {
                       knetValue: {name: 'KNET', fees: '', totalPayable: ''},
                       visaMasterValue: {name: 'VISAMASTER', fees: '', totalPayable: ''}
                     };
    $scope.currency = 'KD'; // TO DO: take this from DB
    $scope.PayMode.mode = $scope.PayMode.visaMasterValue;
    $scope.PayModeFor = null;
    $scope.appointmentConfData = { firstName: null, middleName: null, lastName: null, email: null, mobile: null, clinic: null, doctor: null, slot: null, tariff: null, tariffTotal: null, visitType: null };

    $scope.paymentGatewayMessage = afyaApp.paymentGatewayMessage;

    angular.element(document).ready(function(){
        assignWelcomeUserName();
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            if(!angular.equals(response.data.CLINIC_BASE_URL, null) && !angular.equals(response.data.CLINIC_BASE_URL, '')){
                $scope.clinicBaseURL = response.data.CLINIC_BASE_URL;
            }else{
                $scope.clinicBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            var username = $.cookie("username");
            $scope.username = username;
            var data = getPatientByUsername();
            if(data != null)
            {
            //$http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
                $scope.afyaId = data.afyaId;
                $scope.memberProfileObj = data;
                GetUpcomingAppointmentsByAfyaId($scope.afyaId);
                /*$http.get('/afya-portal/anon/getUpcomingAppointments?afyaId=' + data.afyaId).success(function(data){
                    $scope.upcomingServiceList = data;
                }).error(function(){
                    sweetAlert("error fetching data");
                });*/

                // get Active Pharmacy Orders by Patient
                GetPharmacyOrdersFromAfyaIdOpenOrdersOnly($scope.afyaId);
                /*$http.get('/afya-portal/anon/getPharmacyOrdersFromAfyaId?afyaId='+ $scope.afyaId +'&openOrdersOnly=TRUE').success(function(data){
                    $scope.pharmacyOrderList = data;
                }).error(function(){
                    sweetAlert("error fetching data");
                });*/
            }
            else{
                //sweetAlert("error fetching data");
                $state.go("index");
                return false;
            }
        });
    });

    // get UpcomingAppointments By AfyaId
    function GetUpcomingAppointmentsByAfyaId(afyaId){
        $http.get('/afya-portal/anon/getUpcomingAppointments?afyaId=' + afyaId).success(function(data){
            $scope.upcomingServiceList = data;
        }).error(function(){
            sweetAlert("error fetching data");
        });
    }

    // get PharmacyOrdersFromAfyaId Open Orders Only[TRUE]
    function GetPharmacyOrdersFromAfyaIdOpenOrdersOnly(afyaId){
        $http.get('/afya-portal/anon/getPharmacyOrdersFromAfyaId?afyaId='+ afyaId +'&openOrdersOnly=TRUE').success(function(data){
            $scope.pharmacyOrderList = data;
        }).error(function(){
            sweetAlert("error fetching Pharmacy Order data");
        });
    }

    $scope.rescheduleAppointment = function(schedule){
        console.log(schedule);
        if (schedule.appointmentRescheduleDate == null || schedule.appointmentRescheduleDate == undefined || $('#timeSlot').val() == '' || $('#timeSlot').val() == undefined) {
            //alert('Please provide Appointment date and Timeslot !');
            sweetAlert({ title: "Oops",text: 'For rescheduling need to select to Appointment Date & Slot.',type: "info" });
            return false;
        }
        //$("#modalRescheduleAppointment").modal('hide');
        /*for(var i = 0 ;i < $scope.lengthOfArray; i++){
            if(angular.equals($scope.index, i))
                $('#div-'+$scope.index).show();
            else
                $('#div-'+i).hide();
        }*/
        if($scope.validationResult(schedule)){
            //var timeSlots = schedule.timeSlot.time.split("-", 2); //var timeSlots = schedule.appointmentSlot.split("-", 2);
            var timeSlots = GenerateStartTimeAndEndTime(schedule.timeSlot.time, schedule.appointmentRescheduleDate);
            var startDateTime = timeSlots['start'];//schedule.appointmentDate + ' ' + timeSlots[0].trim() + ":00";
            var endDateTime = timeSlots['end'];//schedule.appointmentDate + ' ' + timeSlots[1].trim() + ":00";
            var obj = JSON.stringify({scheduleId: $scope.scheduleToUpdate.scheduleId, appointmentStartDate : startDateTime, appointmentEndDate : endDateTime, notes : schedule.notes, visitType : schedule.visitType});
            // Closing before calling the post since it not closing inside post method
            $("#modalRescheduleAppointment").modal('hide');
            $http({
                //url: '/afya-portal/anon/rescheduleAppointmentForAGivenTenantDateAndDoctor?tenantId='+$scope.scheduleToUpdate.clinicId,
                url: $scope.clinicBaseURL + '/clinicMaster/updatePatientAppointmentWhenTriggeredFromMobileApp?clinicId='+$scope.scheduleToUpdate.clinicId,
                method:"POST",
                headers: {
                    'Content-Type': 'application/json',
                    'accept' : 'application/json'
                },
                data: obj
            }).success(function(data){
                if (data.indexOf("cannot be rescheduled") > -1) {
                    sweetAlert({ title: "Sorry", text: data, type: "warning" });
                }
                else{
                    sweetAlert({
                        title: "Information",
                        text: "Successfully Rescheduled",
                        type: "success"//error
                    },
                    function () {
                        //$("#modalRescheduleAppointment").modal('hide'); // its closing b4 post call
                        //$state.go($state.current, { cache : false }, { reload: true });
                        GetUpcomingAppointmentsByAfyaId($scope.afyaId);
                       // window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
                    });
                }
            }).error(function(data){
                /*if( Object.prototype.toString.call( data ) === '[object Array]' ) {
                    sweetAlert({ title: "", text: 'Sorry! There are no slots available for this date. Please Select another date.', type: "info" });
                }*/
                $scope.alerts.push({msg : "error occurred", type : "error"});
            });
        } else {
            return;
        }
    }

    function GetTomorrowDate(){
        var currentDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
        var day = currentDate.getDate();
        var month = currentDate.getMonth();// use +1 for accurate month
        var year = currentDate.getFullYear();
        //var tomorrow = day + "-" + month + "-" + year
        var tomorrow =  new Date(year, month, day);
        return tomorrow;
    }

    function AllowtoCancelAppointment(appointmentDate){
        var result = false;
        var data = appointmentDate.split('-');
        var newDate = new Date(new Date(data[2], (data[1] - 1).toString(), data[0]));
        var tomorrow = GetTomorrowDate();
        if(tomorrow >= newDate)
            result = false;
        else
            result = true;
        return result;
    }

    $scope.cancelAppointment = function(schedule, index, lengthOfArray){
        for(var i = 0 ;i<lengthOfArray; i++){
            if(angular.equals(index, i))
                $('#div-'+index).show();
            else
                $('#div-'+i).hide();
        }
        // check validation to Cancel appointment of tomorrow
        /*if(!AllowtoCancelAppointment(schedule.appointmentDate)){
            sweetAlert({ title: "",text: 'Cannot cancel the appointment!',type: "warning" });
            return false;
        }*/

        if($scope.validationResult(schedule)){
            sweetAlert({
              title: "Please confirm",
              //text: "There is no refund available for the service.\n Would you like to cancel the appointment?",
              text: "Do you want to cancel the Appointment?",
              type: "warning",
              showCancelButton: true,
              confirmButtonColor: "#DD6B55",
              confirmButtonText: "Yes",
              cancelButtonText: "No",
              closeOnConfirm: false
            },
            function(){
                $http({
                    url: $scope.clinicBaseURL + '/clinicMaster/cancelPatientAppointmentWhenTriggeredFromMobileApp?clinicId=' + schedule.clinicId+'&scheduleId='+schedule.scheduleId,
                    method:"POST",
                    headers: {
                        'Content-Type': 'application/json',
                        'accept' : 'application/json'
                    }
                }).success(function(data){
                    // Appointment can not be cancel before {168.00} hrs
                      if (data.indexOf("cannot be cancelled") > -1) {
                          sweetAlert({ title: "Sorry", text: data, type: "warning" });
                      }
                      else{
                          sweetAlert({
                              title: "Success",
                              text: "Schedule Cancelled Successfully",
                              type: "success"//error
                          },
                          function () {
                                //$state.transitionTo($state.current, { reload: true, inherit: false, notify: true });
                                //refreshing the same page from the same page
                                //$state.go($state.current, { cache : false }, { reload: true });
                                GetUpcomingAppointmentsByAfyaId($scope.afyaId);
                                //window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
                          });
                      }
                }).error(function(data){
                    /*if( typeof data === 'string' ) {
                        sweetAlert({ title: "", text: data, type: "warning" });
                    }
                    else*/
                    $scope.alerts.push({msg : "error occurred", type : "error"});
                });
                  /*$http.post('/afya-portal/anon/cancelAppointmentForAGivenTenantDateAndDoctor?tenantId='+schedule.clinicId+'&scheduleId='+schedule.scheduleId).success(function(data){
                  //$http.post( $scope.clinicBaseURL + '/clinicMaster/cancelPatientAppointmentWhenTriggeredFromMobileApp?clinicId=' + schedule.clinicId + '&scheduleId=' + schedule.scheduleId).success(function(data){

                  }).error(function(data){

                  });*/
            });

        } else {
            return;
        }
    };

    $scope.validationResult = function(schedule){
        $scope.alerts = [];
        if(angular.equals(schedule.scheduleId, null) || angular.equals(schedule.scheduleId ,"")){
            $scope.alerts.push({msg : "Schedule Id cannot be null", type : "error"});
            return false;
        }
        else if(angular.equals(schedule.clinicId, null) || angular.equals(schedule.clinicId ,"")){
            $scope.alerts.push({msg : "Clinic Id cannot be null", type : "error"});
            return false;
        } else{
            return true;
        }

    };

    $scope.clearAlerts = function(){
        $scope.alerts = [];
    };

    $scope.readCookie = function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    };

    $scope.showModalForReschedule = function(schedule, index, lengthOfArray){
        $("#modalRescheduleAppointment").modal();
        $scope.timeSlotList = [];
        $scope.scheduleToUpdate = schedule;
        // add a new property for the Date Selection Issue
        $scope.scheduleToUpdate.appointmentRescheduleDate = schedule.appointmentDate;
        $scope.index = index;
        $scope.lengthOfArray = lengthOfArray;
        getAvailableTimeSlotsByDate(schedule.clinicId, schedule.doctorId, schedule.appointmentDate, schedule.visitType);
    };

    $scope.findTimeSlots = function(schedule){
        //console.log(schedule);
        $scope.timeSlotList = [];
        //$http.get($scope.clinicBaseURL+'/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId='+schedule.clinicId+'&providerId='+schedule.doctorId+'&appointmentDate='+schedule.appointmentDate).success(function(data) {
        $http.get('/afya-portal/anon/getAvailableTimeslotsForAGivenTenantDateAndDoctor?tenantId='+schedule.clinicId+'&doctorId='+schedule.doctorId+'&appointmentDate='+schedule.appointmentDate+"&visitType="+schedule.visitType).success(function(data) {
            angular.forEach(data, function(eachData){
                if(angular.equals(eachData.visitType, schedule.visitType)){
                    $scope.timeSlotList.push(eachData);
                }
            });
        }).error(function(data){
            sweetAlert(data.error);
        });
    }

    $scope.onPaymentAppointmentClick = function(schedule){

        // assign to global var for accessing after payment mode confirm
        $scope.scheduleToPay = schedule;
        //get patient object by username
        var data = getPatientByUsername($scope.username);
        //set appointment confirmation data
        var docotName =  (schedule.doctorFirstName || '')+" "+(schedule.doctorMiddleName || '') +" "+(schedule.doctorLastName || '')
        $scope.appointmentConfData = { firstName: data.firstName, middleName: data.middleName, lastName: data.lastName, email: data.emailId, mobile: data.mobileNumber, clinic: schedule.clinicName, doctor: docotName, slot: schedule.appointmentDate + ' (' + schedule.appointmentSlot + ')', tariff: setDecimal(schedule.billableAmountRcm), tariffTotal: setDecimal(schedule.billableAmountTotal), visitType: schedule.visitType };
        // open confirmation Modal
        $("#modalConfirmation").modal("show");
    }

    //on Confirmation Continue Click
    $scope.onConfirmationContinue = function (purpose, confdata) {

        var amtPayable = 0;
        // assign schedule to pay to local variable
        var schedule = $scope.scheduleToPay;
        if(schedule.visitType == "Consult Visit"){
            amtPayable = schedule.billableAmountRcm;
        }
        else{
            // considering payment only for Consult Visit[Request Appointment]
            sweetAlert({ title: "", text: 'Invalid Appointment Type', type: "error" });//amtPayable = confdata.billableAmountTotal;
            return false;
        }
        // Checking for amount if null or zero
        if(amtPayable == null || amtPayable == undefined || amtPayable == 0){
            sweetAlert({ title: "", text: 'Hi, the payment amount is mandatory for moving ahead.', type: "warning" });
            return false;
        }

        $scope.afyaPatientPolicyForServiceHandler.showPopup('Appointment  Request - Doctors',function(){onConfirmationContinueInternal();});

        function onConfirmationContinueInternal(){
            $scope.purpose = purpose;// Assign Purpose to scope variable
            // verify patient weather having AfyaId/DOB
            if (schedule.afyaId != null || schedule.afyaId != undefined) {
                $scope.PayModeFor = "RequestAppointment";
                var payerType = 'CONSUMER';
                // for Processing Charges refer tale payment_gateway_processing_fees
                $http.get($scope.portalBaseURL + '/anon/getPaymentProcessingFees?payerType=CONSUMER&amount=' + amtPayable).success(function (data) {
                    // copy fees and total payable into PayMode binding object
                    data = data[0];
                    $scope.PayMode.knetValue.fees = setDecimal(data.debitCardFees);
                    $scope.PayMode.knetValue.totalPayable = setDecimal(data.debitCardTotalPayable);
                    $scope.PayMode.visaMasterValue.fees = setDecimal(data.creditCardFees);
                    $scope.PayMode.visaMasterValue.totalPayable = setDecimal(data.creditCardTotalPayable);
                    $scope.PayMode.payerType = payerType;
                    $scope.PayMode.AmountPayable = setDecimal(amtPayable);
                    // show the Model dialogue for user to choose Payment Channel
                    $("#modalPaymentMode").modal("show");
                }).error(function (data, status, headers, config) {
                    console.log("Error while Getting Payment Processing Fees!" + status);
                });

                /*// Proceed for Payment
                var options = {
                    afyaId: schedule.afyaId,
                    paymentAmount: schedule.billableAmount,
                    clinicId: schedule.clinicId,
                    doctorId: schedule.doctorId,
                    transactionType:schedule.visitType,
                    apptSlot: schedule.appointmentSlot,
                    description: "description",
                    description2: "description2",
                    errorCB: paymentErrorCB,
                    pollCallback: paymentPollCallbackCB,
                    baseUrlForService : $scope.portalBaseURL + "/anon/"
                };
                // calling payment
                afyaApp.payit.initiatePayment(options);*/
            }
        }
    }

    function paymentErrorCB(errMsg) {
        sweetAlert(errMsg);
    }

    function paymentPollCallbackCB(result) {
        //alert('success payment');
        console.log("Poll Status: " + result.status);
        //BookAppointmentWithSuccessPayment()

        // Take actions based on Payment Status
        if ((result.status == "Declined") || (result.status == "Timed Out")) { // NOTE: THIS IS FOR PRODUCTION
            // if(result.status == "Declined"){ // NOTE: THIS IS FOR TESTING
             $scope.alerts.push({msg : "error occurred", type : "error"});
        }
        else if (result.status == "Success") {  // NOTE: THIS IS FOR PRODUCTION
            PaymentConfirmation();
        }
        else if(result.status == "ERROR_CONNECTIVITY"){
            //updatePaymentStatus("ERROR_CONNECTIVITY");
            sweetAlert({ title: "Error", text: 'Check your internet connection and try again.....', type: "error" });
        }
        else if(result.status == "ERROR_UNKNOWN"){
            //updatePaymentStatus("ERROR_UNKNOWN");
            sweetAlert({ title: "Error", text: 'This service is temporarily unavailable. Please try after some time', type: "error" });
        }
        else{
            //updatePaymentStatus("Waiting...");
        }
        //else if(mainOption == "OPT_HOME_PHARMACY"){}
    }

    function PaymentConfirmation() {
        // Post Appointment data to Book an appointment
        $http({
            // url : $scope.portalBaseURL + '/anon/updateScheduleStatusToConfirmed?scheduleId='+ $scope.scheduleToPay.scheduleId + '&clinicId=' + $scope.scheduleToPay.clinicId + '&username=' + $scope.username,
            url : $scope.clinicBaseURL + '/clinicMaster/updateScheduleTentativeGenerateInvoice?clinicId=' + $scope.scheduleToPay.clinicId + '&scheduleId=' + $scope.scheduleToPay.scheduleId,
            method : "POST",
            headers : {
                'Content-Type': 'application/json',
                'accept' : 'text/plain'
            }
        }).success(function(data){
            /*// refresh the window
            $state.go($state.current, { cache : false }, { reload: true });*/
            //var pFullName = $scope.memberProfileObj.firstName + ' ' + $scope.memberProfileObj.lastName;
            sweetAlert({
                title: "Success",
                text: 'Thanks, your appointment has been booked successfully',
                type: "success"//error
            },
            function () {
                GetUpcomingAppointmentsByAfyaId($scope.afyaId);
                /*// redirect to confirmation.html to show the booking details
                $state.go("confirmation", {"clinicId": $scope.scheduleToPay.clinicId , "providerId" : $scope.scheduleToPay.doctorId , 'bookedOn' : timeSlots['start'] , 'pName' : pFullName , 'notes' : $scope.purpose})*/
            });
        }).error(function(data){
            $scope.alerts.push({msg : "error occurred", type : "error"});
        });
        /*$http.post('/afya-portal/anon/updateScheduleStatusToConfirmed?scheduleId=' + $scope.scheduleToPay.scheduleId + '&clinicId=' + $scope.scheduleToPay.clinicId + '&username=' + $scope.username, {headers: {'accept': 'text/plain'}}).success(function (data) {
            //window.location = "Patient-upcoming-smart-services.html";
            $state.go($state.current, { cache : false }, { reload: true });
            //$state.go("PatientDashboard/Patient-upcoming-smart-services");
        }).error(function (data, status, headers, config) {
             $scope.alerts.push({msg : "error occurred", type : "error"});
        });*/

    }

    $('#modalConfirmation').on('shown.bs.modal', function () {
        $('#purpose').focus();
    })

    //********************* Submit Book Appointment END ****************//

   ////////////// PAYMENT GATEWAY INTEGRATION RELATED SERVICE CALLS /////////////////
    if (window.afyaApp.svc == undefined)
        window.afyaApp.svc = {};
        window.afyaApp.svc.addPaymentTransactionForAppointment = function (settings) {
        // make service call
        $http.post('/afya-portal/anon/addPaymentTransactionForAppointment?transactionType=' + settings.transactionType + "&transactionAmount=" + settings.transactionAmount + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId + "&apptSlot=" + settings.apptSlot + "&processingFees=" + settings.processingFees + "&payerType=" + settings.payerType + "&paymentChannel=" + settings.paymentChannel).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            sweetAlert('Error : addPaymentTransactionForAppointment');
        });
    };

    //updatePaymentTransactionWithIsysStatus
    window.afyaApp.svc.updatePaymentTransactionWithIsysStatus = function (settings) {
        // make service call
        // $http.post('/afya-portal/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus).success(function (data) {
        $http.post('/afya-portal/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus + "&isysMerchantRef=" + settings.isysMerchantRef).success(function (data) {
            settings.success(data);
        }).error(function (data, status, headers, config) {
            sweetAlert('Error : updatePaymentTransactionWithIsysStatus');
        });
    };



    /*********************** Home Pharmacy Changes *****************************/

    $scope.onPaymentPharmacyOrderClick = function(order)
    {
        $scope.afyaPatientPolicyForServiceHandler.showPopup('Home Pharmacy', function(){onPaymentPharmacyOrderClickInternal();});

        function onPaymentPharmacyOrderClickInternal(){

            $scope.order = order;
            if(order.invoiceAmount > 0){
                // check for valid request for payment [isPaid=false and orderStatus=Approved]
                if (order.isPaid == false && order.orderStatus == 'ORDER_APPROVED') {
                    //sweetAlert('Opening Payment Poopup');
                    $scope.PayModeFor = "HomePharmacy";
                    var payerType = 'CONSUMER';
                    // for Processing Charges refer tale payment_gateway_processing_fees
                    $http.get($scope.portalBaseURL + '/anon/getPaymentProcessingFees?payerType=CONSUMER&amount=' + order.invoiceAmount).success(function (data) {
                        // copy fees and total payable into PayMode binding object
                        data = data[0];
                        $scope.PayMode.knetValue.fees = setDecimal(data.debitCardFees);
                        $scope.PayMode.knetValue.totalPayable = setDecimal(data.debitCardTotalPayable);
                        $scope.PayMode.visaMasterValue.fees = setDecimal(data.creditCardFees);
                        $scope.PayMode.visaMasterValue.totalPayable = setDecimal(data.creditCardTotalPayable);
                        $scope.PayMode.payerType = payerType;
                        $scope.PayMode.AmountPayable = setDecimal(order.invoiceAmount);
                        // show the Model dialogue for user to choose Payment Channel
                        $("#modalPaymentMode").modal("show");
                    }).error(function (data, status, headers, config) {
                        console.log("Error while Getting Payment Processing Fees!" + status);
                    });

                    /*// Proceed for Payment(Pharmacy Order)
                    var options = {
                        orderId : order.orderId,
                        pharmacyOrderId : order.orderId,
                        doctorName : order.doctorName,
                        pharmacyName : order.pharmacyName,
                        clinicName : order.clinicName,
                        currencyUom : order.currencyUom,
                        paymentAmount : order.invoiceAmount,
                        visitDate : order.visitDate,
                        orderStatus : order.orderStatus,
                        clinicId : order.clinicId,
                        description: "description",
                        description2: "description2",
                        errorCB : pharmacyOrderPaymentErrorCB,
                        pollCallback : pharmacyOrderPaymentPollCallbackCB,
                        baseUrlForService : $scope.portalBaseURL + "/anon/"
                    };
                    console.log(options);
                    // calling payment for pharmacy order
                    afyaApp.payit.initiatePayment(options);*/
                }
                else{
                    //$scope.alerts.push({msg : "Payment cannot proceed !", type : "error"});
                    sweetAlert("Payment cannot proceed !");
                }
            }
            else{
                sweetAlert("Payment Amount cannot be zero !");
            }
        }
    }

    // on Continue Click after Payment Mode/Channel Selection
    $scope.onPaymentModeSelectionClick = function(mode){
        //alert('Payment Mode Selected = ' + mode);
        //alert($scope.PayModeFor); // HomePharmacy | RequestAppointment
        var options = null;
        /* Payment For Can have HomePharmacy | RequestAppointment */
        if($scope.PayModeFor == "HomePharmacy"){
            var order = $scope.order; //
            // Proceed for Payment(Pharmacy Order)
            options = {
                afyaId : $scope.afyaId,
                orderId : order.orderId,
                pharmacyOrderId : order.orderId,
                doctorName : order.doctorName,
                pharmacyName : order.pharmacyName,
                clinicName : order.clinicName,
                currencyUom : order.currencyUom,
                paymentAmount: $scope.PayMode.mode.totalPayable, //order.invoiceAmount,
                processingFees: $scope.PayMode.mode.fees,
                visitDate : order.visitDate,
                orderStatus : order.orderStatus,
                clinicId : order.clinicId,
                doctorId: "",//$scope.providerId,
                transactionType: 'PHARMACY_PAYMENT', /* APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT */
                apptSlot: "",
                description: "PHARMACYPAYMENT",
                description2: "description2",
                errorCB : pharmacyOrderPaymentErrorCB,
                pollCallback : pharmacyOrderPaymentPollCallbackCB,
                baseUrlForService : $scope.portalBaseURL + "/anon/",
                paymentChannel : mode,
                payerType: $scope.PayMode.payerType
            };
        }
        else{
             var schedule = $scope.scheduleToPay;
            // Proceed for Payment [Request Appointment]
            options = {
                afyaId: schedule.afyaId,
                paymentAmount: $scope.PayMode.mode.totalPayable, //schedule.billableAmount,
                processingFees: $scope.PayMode.mode.fees,
                clinicId: schedule.clinicId,
                doctorId: schedule.doctorId,
                transactionType:schedule.visitType,
                apptSlot: schedule.appointmentSlot,
                description: "APPOINTMENTBOOKING",
                description2: "description2",
                errorCB: paymentErrorCB,
                pollCallback: paymentPollCallbackCB,
                baseUrlForService : $scope.portalBaseURL + "/anon/",
                paymentChannel : mode,
                payerType: $scope.PayMode.payerType
            };
        }
        console.log(options);
        // calling payment for pharmacy order
        afyaApp.payit.initiatePayment(options);
    }

    function pharmacyOrderPaymentErrorCB(errMsg) {
        sweetAlert(errMsg);
    }

    function pharmacyOrderPaymentPollCallbackCB(result) {
        //alert('success payment');
        console.log("Poll Status: " + result.status);

        // Take actions based on Payment Status
        if ((result.status == "Declined") || (result.status == "Timed Out")) { // NOTE: THIS IS FOR PRODUCTION
            // if(result.status == "Declined"){ // NOTE: THIS IS FOR TESTING
             $scope.alerts.push({msg : "error occurred", type : "error"});
        }
        else if (result.status == "Success") {  // NOTE: THIS IS FOR PRODUCTION
            CompleteOrderConfirmation();
            /*alert('Success Payment.');*/
            //window.location = "Patient-upcoming-smart-services.html";
        }
        else if(result.status == "Waiting"){
            //$('#PayAgain').hide();
            //alert('Payment has not succeeded! Please Pay Again.');
        }
        else if(result.status == "ERROR_CONNECTIVITY"){
            //updatePaymentStatus("ERROR_CONNECTIVITY");
            sweetAlert({ title: "Error", text: 'Check your internet connection and try again.....', type: "error" });
        }
        else if(result.status == "ERROR_UNKNOWN"){
            //updatePaymentStatus("ERROR_UNKNOWN");
            sweetAlert({ title: "Error", text: 'This service is temporarily unavailable. Please try after some time', type: "error" });
        }
        else{
            //updatePaymentStatus("Waiting...");
        }
        //else if(mainOption == "OPT_HOME_PHARMACY"){}
    }

    function CompleteOrderConfirmation(){
        // http://5.9.249.196:7577/ospedale/clinicMaster/createActivePrescriptionPayment?clinicId=1&orderId=xzcvxzcv&afyaId=KWT721
        console.log($scope.clinicBaseURL + '/clinicMaster/createActivePrescriptionPayment?clinicId='+ $scope.order.clinicId +'&orderId='+ $scope.order.orderId + '&afyaId=' + $scope.afyaId);
        $http.get($scope.clinicBaseURL + '/clinicMaster/createActivePrescriptionPayment?clinicId='+ $scope.order.clinicId +'&orderId='+ $scope.order.orderId + '&afyaId=' + $scope.afyaId ,{headers: {'accept': 'text/plain'}}).success(function(data){
            //$scope.alerts.push({msg : "Order paid successfully", type : "success"});
            sweetAlert({
                title: "Order Status",
                text: "Order Placed Successfully",
                type: "success"//error
            },

            function () {
               //$state.go("PatientDashboard/Patient-upcoming-smart-services");
               $state.go($state.current, { cache : false }, { reload: true });
               //window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
            });
            //console.log('Order placed successfully');
            //$window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
        }).error(function(data){
            $scope.alerts.push({msg : "error occurred", type : "error"});
        });

        /*$http.get('/afya-portal/anon/getPharmacyOrdersFromAfyaId?afyaId='+ $scope.afyaId +'&openOrdersOnly=TRUE').success(function(data){
            $scope.pharmacyOrderList = data;
        }).error(function(){
            alert("error fetching data");
        });*/
    }

    // Received Pharmacy order
    $scope.onReceivedPharmacyOrderClick = function(order, index) {
        // Received Pharmacy Order
        //alert('Order Completed Successfully.');
        $http.post($scope.clinicBaseURL + '/clinicMaster/completeOrder?clinicId='+ order.clinicId +'&orderId='+ order.orderId + '&afyaId=' + $scope.afyaId ).success(function(data){
            //$scope.alerts.push({msg : "Order Completed successfully", type : "success"});
            sweetAlert({
                title: "Order Status",
                text: "Thanks , you have delivered the medicines requested.",
                type: "success"//error
            },

            function () {
                 //$state.go("PatientDashboard/Patient-upcoming-smart-services");
                 $state.go($state.current, { cache : false }, { reload: true });
                //window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
            });
            //console.log('Order Completed Successfully.');
            //$window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
        }).error(function(data){
            $scope.alerts.push({msg : "error occurred", type : "error"});
        });
    }

    $scope.onCancelPharmacyOrder = function(order, index, lengthOfArray){
        /*for(var i = 0 ;i<lengthOfArray; i++){
            if(angular.equals(index, i))
                $('#divPO-'+index).show();
            else
                $('#divPO-'+i).hide();
        }*/

        if($scope.validateOrderResult(order)){
            $http.post($scope.clinicBaseURL + '/clinicMaster/cancelOrder?clinicId='+ order.clinicId +'&orderId='+ order.orderId + '&afyaId=' + $scope.afyaId ).success(function(data){
                //$scope.alerts.push({msg : "Order cancelled successfully", type : "success"});
                //sweetAlert('Order Cancelled Successfully');
                sweetAlert({
                    title: "Order Status",
                    text: "Order Cancelled Successfully",
                    type: "success"//error
                },

                function () {
                        //refreshing the same page from the same page
                        $state.go($state.current, { cache : false }, { reload: true });
                    //window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
                });
                //$window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
            }).error(function(data){
                //$scope.alerts.push({msg : "error occurred", type : "error"});
                sweetAlert("Error while Cancelling Order !");
            });
        } else {
            return;
        }
    };


    $scope.validateOrderResult = function(order){
        $scope.alerts = [];
        if(angular.equals(order.orderId, null) || angular.equals(order.orderId ,"")){
            $scope.alerts.push({msg : "Order Id cannot be null", type : "error"});
            sweetAlert('Order Id cannot be null');
            return false;
        }
        else if(angular.equals(order.clinicId, null) || angular.equals(order.clinicId ,"")){
            $scope.alerts.push({msg : "Clinic Id cannot be null", type : "error"});
            sweetAlert("Clinic Id cannot be null");
            return false;
        } else{
            return true;
        }

    };
    /*********************** Home Pharmacy Changes End *************************/





    // On AppointmentDate change
    $scope.onAppointmentCalendarDateChange = function (scheduleToUpdate) {
        /*appointmentDate = new Date(appointmentDate);
        appointmentDate = getAppointmentFormatedDate(appointmentDate);*/
        //sweetAlert(schedule.clinicId + ',' + schedule.doctorId + ',' + schedule.appointmentDate);
        //getAvailableTimeSlotsByDate(scheduleToUpdate.clinicId, scheduleToUpdate.doctorId, scheduleToUpdate.appointmentDate, scheduleToUpdate.visitType);
        getAvailableTimeSlotsByDate(scheduleToUpdate.clinicId, scheduleToUpdate.doctorId, scheduleToUpdate.appointmentRescheduleDate, scheduleToUpdate.visitType);
    }

    // get Available Time slots By Provider and Date
    function getAvailableTimeSlotsByDate(clinicId, providerId, appointmentDate, visitType) {
        //alert('Inside getAvailableTimeSlotsByDate');
        $scope.timeSlotList = [];
        if(appointmentDate != undefined){
            $http.get($scope.clinicBaseURL + '/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId=' + clinicId + '&providerId=' + providerId + '&appointmentDate=' + appointmentDate + '&visitType=' + visitType).success(function (data) {
                angular.forEach(data, function(eachData){
                    if(angular.equals(eachData.visitType, visitType)){
                        $scope.timeSlotList.push(eachData);
                    }
                });

                if( Object.prototype.toString.call( data ) === '[object Array]' ) {
                    if ($scope.timeSlotList.length <= 0) {
                        sweetAlert({ title: "Sorry",text: 'There are no slots available for this date. Please Select another date.',type: "info" });
                    }
                }
                else if( typeof data === 'string' ) {
                     if ($scope.timeSlotList.length <= 0) {
                         //sweetAlert(data);
                         sweetAlert({ title: "", text: data, type: "warning" });
                     }
                 }

                /*if (appointmentLists.length <= 0) {
                    sweetAlert(data);
                }*/
            }).error(function (data, status, headers, config) {
                if (data.indexOf("appointment from past") > -1) {
                    $scope.timeSlotList = null;
                    sweetAlert({ title: "Sorry",text: 'Date has passed we all have moved ahead, select future dates',type: "warning" });
                }
                else
                    sweetAlert({ title: "Oops",text: 'Please select an appointment Slot for moving ahead',type: "error" });
            });
        }

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

    /////////////// Afya Patient Policy for Service Popup hander ///////////////////////////////
    $scope.afyaPatientPolicyForServiceHandler = {
        doneCallback: null,
        currService: null,
        //patientConsentReceived: false,

        /*resetConsent: function(){
            this.patientConsentReceived = false;
        },*/

        showPopup: function(serviceName, doneCallback){
            // done callback
            this.doneCallback = doneCallback;
            /*if(this.patientConsentReceived == true){
                doneCallback();
            }else*/{

                /*var serviceName = '';
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
                        serviceName = "Home Pharmacy";
                    break;
                    default:
                    serviceName = '';       // should never be here
                }*/
                // show the modal popup
                $("#afyaPatientPolicyForUpcomingService").modal("show");
                // retrieve pocilty by service
                $http.get($scope.portalBaseURL + '/anon/getPatientPoilicyForService?serviceName=' + serviceName
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
            //this.patientConsentReceived = true;
            // invoke donecallback if none is defined
            if(this.doneCallback != null){
                this.doneCallback();
                this.doneCallback = null;
            }
        },
    };

}]);