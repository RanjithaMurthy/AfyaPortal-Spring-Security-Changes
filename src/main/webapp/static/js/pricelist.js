/**
 * Created by pradyumna on 11-06-2015.
 */

// angular.module('aafyaPriceListModule', ['ngMessages'])
afyaApp.controller('PriceController', ['$scope', '$http', '$window', '$stateParams','$state', '$location', function ($scope, $http, $window, $stateParams, $state, $location) {

        $scope.trail = "N";
        $scope.saveOnly = "N";
        // $scope.showPackages = true;
        $scope.selectedPackage = null;
        $scope.portalBaseUrl = "/afya-portal";
        $scope.subscribeStep = "STEP_1";
        $scope.providerData = null;
        $scope.chkTermsAndConditionsChecked = false;
        $scope.chkViewedTrainingSchedule = false;
        $scope.cityList = null;
        $scope.showFreeTrialMessage = false;

        $scope.$cookie = $.cookie;

        $scope.paymentGatewayMessage = afyaApp.paymentGatewayMessage;

        $scope.afyaTermsCondition = false;
        $scope.afyaProgram = false;

        $scope.progressMessage = null;
        $scope.paymentTrackIdMessage = null;

        /*if($stateParams != undefined){
            $scope.filterServicePackId = $stateParams.packageId;
            $scope.filterPriceOption = $stateParams.period;
        }else{
            $scope.filterServicePackId = null;
            $scope.filterPriceOption = null;
        }*/
        // $scope.filterServicePackId = getParameterValueByProperty('packageId');
        // $scope.filterPriceOption = getParameterValueByProperty('period');
        $scope.currency = 'KD'; // TO DO: take this from DB

        $scope.username = readCookie("username");
        $scope.registerData = {};
        $scope.SMSSenderId = "";
        $scope.SMSSenderIdVerified = false;

        // status
        // updatePaymentStatus("");
        $scope.hasActivePackage = false;
        $scope.hasNotificationSubscritpion = false;
        $scope.hasTrainingSubscritpion = false;

        // controlling
        $scope.hideInactivePacks = false;

        // payment Mode selection
        $scope.PayMode = {
                           knetValue: {name: 'KNET', fees: '', totalPayable: ''},
                           visaMasterValue: {name: 'VISAMASTER', fees: '', totalPayable: ''}
                         };
        $scope.PayMode.mode = $scope.PayMode.visaMasterValue;

        // Messages
        $scope.showMessageAboutToExpire = false;
        $scope.showMessageExpired = false;
        $scope.$evalAsync(function(){
            // remove bootstrap hide class
            // for expire messages containing div
            $("#expireMessage-div").removeClass('hide');
            // for breadcrumb images
            $("#price-breadcrumb img").removeClass('hide');
        });


        $scope.showTrygainButton = false;

        $scope.getProviderData = function(successCB, forceUpdate){
            if($scope.providerData == null || forceUpdate == true) {
                var username = $scope.$cookie("username");
                // var username = $cookies["username"];
                if(forceUpdate == true){
                    afyaSessionStore().remove('userObject');
                }

                var providerData = getProviderByUsername();
                if(providerData != null){
                    // store Provider Data
                    $scope.providerData = providerData;
                    // update SMS Sender ID and Sender ID Verified flag
                    $scope.SMSSenderId = providerData.smsSenderId;
                    $scope.SMSSenderIdVerified = providerData.smsSenderIdVerified;
                    // invoke success CB
                    successCB($scope.providerData);
                }else{
                    $scope.providerData = null;
                    sweetAlert("error fetching data");
                }

                    /*$http.get($scope.portalBaseUrl + '/anon/getProviderByUsername?username=' + username).success(function (providerData) {
                        $scope.providerData = providerData;
                        successCB($scope.providerData);
                    }).error(function (data) {
                        $scope.providerData = null;
                        sweetAlert("error fetching data");
                    });*/
                //}
            }else{
                successCB($scope.providerData);
            }
        };

        angular.element(document).ready(function(){
            $scope.getProviderData(function(providerData){
                // nothing to do here
            });
        });

        $scope.calculateAmount = function(pkg) {
            if (pkg.priceOption == "HALF_YEARLY") {
                pkg.amount = Number(pkg.halfYearlyPricePerUom * pkg.numberOfUsers).toFixed(3);
            } else {
                pkg.amount = Number(pkg.annuallyPricePerUom * pkg.numberOfUsers).toFixed(3);
            }
            pkg.amount = pkg.amount | 0;
            $scope.calculateTotalAmount();
        };

        $scope.updateExpiryDays = function(package){
            package.expiryDays = deriveExpiryDays(package);  // package.priceOption == 'HALF_YEARLY' ? 180 : 365;
            package.expiryDate = new Date(package.subscriptionDate);
            //package.expiryDate.setDate(package.expiryDate.getDate() + package.expiryDays);
            //package.expiryDate.setMonth(package.expiryDate.getMonth() + (package.priceOption == 'ANNUALLY' ? 12 : 6));
            package.expiryDate.setDate(package.expiryDate.getDate() + package.expiryDays);
        };

        $scope.calculateNotificationAmount = function(service) {

            service.amount = Number(service.packs*service.rate).toFixed(3);
            service.numberOfSMSToSubscribe = service.packs*service.numberOfSMS;

            $scope.calculateTotalAmount();
        };
        $scope.calculateTrainingAmount = function(service) {

            service.amount = Number(service.numberOfHours*service.rate).toFixed(3);

            $scope.calculateTotalAmount();
        };

        $scope.generateQuotation = function(){

            if(validateAndPickSelectedPackage('N','Y','N') == false)    // quotation is not for trial and not for Save-Only
                return false;


            // send email
            var packageQuotationDto = {};
            packageQuotationDto.membershipId = $scope.providerData.membershipId;
            packageQuotationDto.memberName =  $scope.providerData.firstName + ' ' + ($scope.providerData.middleName != null ? $scope.providerData.middleName + ' ' : '') + $scope.providerData.lastName;
            packageQuotationDto.memberType = $scope.providerData.memberType; // Premium/Community
            packageQuotationDto.referredBy = '';
            packageQuotationDto.date = new Date();
            packageQuotationDto.packName = $scope.selectedPackage.packageName;
            packageQuotationDto.packageId = $scope.selectedPackage.packageId;
            packageQuotationDto.subscriptionPeriod = $scope.selectedPackage.priceOption== "ANNUALLY" ? "Annual" : "Half Year";
            packageQuotationDto.paymentAmount = $scope.totalAmount, // $scope.selectedPackage.priceOption == "ANNUALLY" ? $scope.selectedPackage.annuallyPricePerUom : $scope.selectedPackage.halfYearlyPricePerUom;
            packageQuotationDto.paymentCurrency = 'KD'; // TO DO: should come from Data-Base (but how? from which tables?)
            packageQuotationDto.subscriptionStartDate = new Date();
            packageQuotationDto.subscriptionEndDate = new Date();
            packageQuotationDto.subscriptionEndDate.setMonth(packageQuotationDto.subscriptionEndDate.getMonth()
                + (packageQuotationDto.subscriptionPeriod == 'ANNUALLY' ? 12 : 6)); // ?? TO DO: SHOULD THIS BE FROM existing Package in case of Top-up?
            packageQuotationDto.subscriptionDays = $scope.selectedPackage.expiryDays, // packageQuotationDto.subscriptionPeriod == 'ANNUALLY' ? 365 : 182;
            packageQuotationDto.smartSolutions = $.map($scope.selectedPackage.services, function(n, i){ return n.serviceName; }).join(', ');
            packageQuotationDto.smartServicesPatient = $.map($scope.selectedPackage.patientServices, function(n, i){ return n.serviceName; }).join(', ');
            packageQuotationDto.smartServicesCareProviders = $.map($scope.selectedPackage.providerServices, function(n, i){ return n.serviceName; }).join(', ');
            packageQuotationDto.notificationServicePacks = $scope.selectedPackage.notificationServices.length > 0 ? $scope.selectedPackage.notificationServices[0].packs : 0;
            packageQuotationDto.subscriptionStartDateFormated = getFormatedDate(packageQuotationDto.subscriptionStartDate);
            packageQuotationDto.subscriptionEndDateFormated = getFormatedDate(packageQuotationDto.subscriptionEndDate);
            packageQuotationDto.dateFormated = getFormatedDate(packageQuotationDto.date);

            // EMail the quotation
            $http.post('/afya-portal/anon/send_mail_package_quotation', packageQuotationDto).success(function (data,status,headers,config) {
                sweetAlert("The Quotation has been sent to your registered Email Id");
            }).error(function (data) {
                //sweetAlert("error sending Package Quotation");
                sweetAlert({ title: "", text: 'We have encountered issues while sending across the Quotation', type: "error" });
            });

            return;


            // get logged in provider details
            $scope.getProviderData(generateQuotation);

            // helper function to trigger opening of quotation in new window
            function generateQuotation() {
                // on success of provider details, construct quote-params
                var quoteParams = {};
                quoteParams.membershipId = $scope.providerData.membershipId;
                quoteParams.memberName =  $scope.providerData.firstName + ' ' + ($scope.providerData.middleName != null ? $scope.providerData.middleName + ' ' : '') + $scope.providerData.lastName;
                quoteParams.memberType = $scope.providerData.memberType; // Premium/Community
                quoteParams.referredBy = '';
                quoteParams.date = new Date();
                quoteParams.packName = $scope.selectedPackage.packageName;
                quoteParams.subscriptionPeriod = $scope.selectedPackage.priceOption== "ANNUALLY" ? "Annual" : "Half Year";
                quoteParams.paymentAmount = $scope.totalAmount, // $scope.selectedPackage.priceOption == "ANNUALLY" ? $scope.selectedPackage.annuallyPricePerUom : $scope.selectedPackage.halfYearlyPricePerUom;
                quoteParams.paymentCurrency = 'KD'; // TO DO: should come from Data-Base (but how? from which tables?)
                quoteParams.subscriptionStartDate = new Date();
                quoteParams.subscriptionEndDate = new Date();
                quoteParams.subscriptionEndDate.setMonth(quoteParams.subscriptionEndDate.getMonth()
                + (quoteParams.subscriptionPeriod == 'ANNUALLY' ? 12 : 6)); // ?? TO DO: SHOULD THIS BE FROM existing Package in case of Top-up?
                quoteParams.subscriptionDays = $scope.selectedPackage.expiryDays, // quoteParams.subscriptionPeriod == 'ANNUALLY' ? 365 : 182;
                quoteParams.smartSolutions = $.map($scope.selectedPackage.services, function(n, i){ return n.serviceName; }).join(', ');
                quoteParams.smartServicesPatient = $.map($scope.selectedPackage.patientServices, function(n, i){ return n.serviceName; }).join(', ');
                quoteParams.smartServicesCareProviders = $.map($scope.selectedPackage.providerServices, function(n, i){ return n.serviceName; }).join(', ');
                quoteParams.notificationServicePacks = $scope.selectedPackage.notificationServices.length > 0 ? $scope.selectedPackage.notificationServices[0].packs : 0;

                // store the params in cookie
                $.removeCookie("packSubscribeQuoteParams", { path: '/afya-portal'});
                $scope.$cookie("packSubscribeQuoteParams",JSON.stringify(quoteParams));

                // open the quotation page in new window
                $window.open('subscription_quotation.html', 'quotation');
            };
        };


        $scope.getUserSubscriptionHistory = function(options){
            var username = $scope.$cookie("username");
            $http.get('/afya-portal/getUserSubscriptionHistory?serviceType=' + options.serviceType
                    + '&packageId=' + options.package.packageId + '&packageCategory=' + options.package.packageCategory
                    + '&isSubscriptionActive=' + options.isSubscriptionActive).success(function (subscriptionHistoryData) {
                options.successCB(subscriptionHistoryData);
            }).error(function (data) {
                $scope.providerData = null;
                //sweetAlert("error fetching Subscription History Data");
                sweetAlert({ title: "Sorry", text: 'We have encountered some issues please retry', type: "error" });
            });
        };

        $scope.displayAfyaPolicyAndCalculateTotalAmount = function(service){
            $scope.afyaProviderPolicyForServiceHandler.showPopup(service, null);
            /*service.activated = 0;
            $scope.calculateTotalAmount();
            alert("service selected");*/
        };

        /*$scope.submitSubscription = function() {
            // $scope.trail = trail;
            *//*
            //check for mutiple package activated
            var activated_packages ="";
            var activated_count = 0;
            var _selectedPackage = null;
            for (i = 0; i < $scope.packages.length; i++) {
                var package = $scope.packages[i];

                if (package.activated == 1) {
                    // capture the first selected package (as we need to save this)
                    if(_selectedPackage == null)
                        _selectedPackage = package;
                    // construct a sting of comma separated list of activated packages
                    if (activated_packages != "") {
                        activated_packages = activated_packages + ",";
                    }
                    activated_count++;
                    activated_packages =  activated_packages + package.packageName;
                    if ((package.numberOfUsers == 0) || (package.numberOfUsers == null) || (package.numberOfUsers == "")) {
                        alert("Number of users for active package should be greater than zero. ["+package.packageName+"]");
                        return false;
                    }
                }
            }
            if (activated_count == 0) {
                alert("Please select a Package to Subscribe.");
                return false;
            } else if (activated_count > 1) {
                alert("Only one package can be active. ["+activated_packages+"]");
                return false;
            }

            $scope.selectedPackage = _selectedPackage;*//*


            if(validateAndPickSelectedPackage($scope.trail,'N',$scope.saveOnly) == false)
                return false;

            $scope.getProviderData(function(providerData){

                function processNextOne(){
                    submitSubscriptionInternal();
                }

                if(providerData != null && providerData.memberType == 'VISITOR') {
                $scope.memberRegistrationHander.showPopup(function(){
                        processNextOne();
                    });
                }else{
                    processNextOne();
                }
                *//*if(providerData != null && providerData.memberType == 'VISITOR') {
                    getAllPremiumMember();
                    $("#ProviderMemberRegistration").modal("show");
                }else{
                    submitSubscriptionInternal();
                }*//*
            });

            *//*
            $http.post('subscribe_package', _packages).success(function (data,status,headers,config) {

                if (data.message == "success") {
                    alert("Success");
                }
            });*//*
        };*/

        function submitSubscriptionInternal(){
            if(validateAndPickSelectedPackage($scope.trail,'N',$scope.saveOnly) == false)
                return false;

            if($scope.trail == "N") {
                // --> to handle New Paid Subscription, Add-on Subscription, Save-Changes on Paid Package, Upgrade from Trial to Paid Subscription
                //     Note: There is a check in the Subscribe service in Portal, to determine if this is a new Subscription or an existing Subscription based on 'Activated' flag.
                //          To determine if this is an Upgrade from Trial, the existing Subscriptions 'Trial flag' will be compared to the 'Subscribe' request 'Trial Flag'
                //              if they are different its an Upgrade

                // initiate Payment (after payment the subscription would be saved)

                // check if this is a simple-save or an Subscription
                if((/*$scope.totalAmount == 0 || */$scope.saveOnly == 'Y')
                    && $scope.hasActivePackage == true
                    && ($scope.providerData != undefined && $scope.providerData.subscriptionTrail == false)){
                    // update status
                    updatePaymentStatus("SAVING_CHANGES");
                    // simple save
                    subscribePackage(function(){
                        //showSuccessMessage("Saved Successfully!");
                        updatePaymentStatus("Saved Successfully");
                    });
                }else{
                    // update status
                    updatePaymentStatus("PAYMENT");
                    // subscription
                    makePaymentAndSubscribe();
                }
            }else{
                // --> to handle New Trial Subscription, Save-Changes on Trial Subscription
                // update status
                if($scope.saveOnly == 'Y')
                    updatePaymentStatus("SAVING_CHANGES");
                else
                    updatePaymentStatus("Subscribing Package...");
                // do save/subscription
                subscribePackage(function(){
                    //showSuccessMessage("Thank you for Choosing Afya Services : Enjoy your FREE Trail for 30 Days");

                    if($scope.saveOnly == 'Y')
                        updatePaymentStatus("Saved Successfully");
                    else
                        updatePaymentStatus("Subscribed Trial");
                });
            }
        };

        function validateAndPickSelectedPackage(trial, quote, saveOnly){
            // $scope.trail = trial;
            // $scope.saveOnly = saveOnly;

            if(quote === "N"){  // if this is not for quotation, validate if checkboxes are checked
                // ensure the Terms and Conditions check box is checked
                if($scope.chkTermsAndConditionsChecked == false){
                    sweetAlert("Please Read & Accept the Terms & Conditions");
                    return false;
                }

                // ensure the Training Sechedule check box is checked (if required)
                if(!$scope.chkViewedTrainingSchedule        // if Accept checkbox not checked
                    && ( /* if there are any Training Packages selected */ $.grep($scope.trainingPackages,function(pack){ return $.grep(pack.services, function(svc){ return svc.activated == 1;}).length; }).length
                        || !$scope.hasActivePackage // if no Package is Active (this is the first time Subscription/Trial)
                        || ($scope.providerData.subscriptionTrail && trial === "N" && quote === "N" && saveOnly === "N"))){ // if Trial is active and the Provider is paying for it
                    sweetAlert("Please Read & Accept the Self Learning schedule");
                    return false;
                }
            }

            //check for mutiple package activated
            var activated_packages_service ="";
            var activated_count = 0;
            var service_activated_count = 0;
            var training_activated_count = 0;
            var _selectedPackage = null;
            for (i = 0; i < $scope.packages.length; i++) {
                var package = $scope.packages[i];

                if (package.activated == 1) {
                    // capture the first selected package (as we need to save this)
                    if(_selectedPackage == null)
                        _selectedPackage = package;
                    // construct a sting of comma separated list of activated packages
                    if (activated_packages_service != "") {
                        activated_packages_service = activated_packages_service + ",";
                    }
                    activated_count++;
                    if(package.packageCategory == "SERVICE")
                        service_activated_count ++;
                    else
                        training_activated_count ++;
                    if(package.packageCategory == "SERVICE")
                        activated_packages_service =  activated_packages_service + package.packageName;
                    if (package.packageCategory == "SERVICE" && ((package.numberOfUsers == 0) || (package.numberOfUsers == null) || (package.numberOfUsers == ""))) {
                        sweetAlert("Number of users for active package should be greater than zero. ["+package.packageName+"]");
                        return false;
                    }else if (package.packageCategory == "TRAINING" && ((package.numberOfHours == 0) || (package.numberOfHours == null) || (package.numberOfHours == ""))){
                        sweetAlert("Number of Hours for active Training package should be greater than zero. ["+package.packageName+"]");
                        return false;
                    }
                }
            }
            if (service_activated_count == 0) {
                if(trial == "Y")
                    sweetAlert("Important, please select the service package for the Trial.");
                else if(quote == "Y")
                    sweetAlert("Important, please select the service package for auto generation of the Quote.");
                else
                    sweetAlert("Important, please select the service package for the Subscription.");
                return false;
            } else if (service_activated_count > 1) {
                sweetAlert("Only one Service Package can be active. ["+activated_packages_service+"]");
                return false;
            }

            // validate activated Notification Services
            /*if(trial == "Y"){
                var selectedNotificationService = $.grep(_selectedPackage.notificationServices, function(svc, idx){
                    return svc.activated == 1;
                });
                if(selectedNotificationService.length > 0){
                    sweetAlert("Notification Subscription is not included in Trial.");
                    return 0;
                }
            }*/
            var invalidNotificationServices = $.grep(_selectedPackage.notificationServices, function(svc, idx){
                return svc.activated == 1 && svc.packs == 0;
            });
            if(invalidNotificationServices.length > 0){
                sweetAlert("Number of Packs for Notification service should be greater than zero.");
                return false;
            }

            // validate activated Training Package Services
            var activatedTrainingServices = $.map($scope.trainingPackages, function(pack, idx){
                var serviceList = $.grep(pack.services, function(svc,idx1){
                    return svc.activated == 1;
                });
                // mark a hasActiveService flag in the pack
                pack.hasActiveService = serviceList.length > 0;
                return serviceList;
            });
            /*if(trial == "Y" && activatedTrainingServices.length > 0){
                sweetAlert("Training Subscription is not included in Trial.");
                return false;
            }*/
            // invalid training service
            var invalidService = $.grep(activatedTrainingServices, function(svc, idx){
                return svc.numberOfHours == 0 || svc.numberOfHours == null || svc.numberOfHours == '';
            });
            // show message if invalid parameters in selected services
            if(invalidService.length > 0){
                sweetAlert("Number of Hours for Training package should be greater than zero.");
                return false;
            }

            // zero Amount validation
            //if($scope.hasActivePackage == false || ($scope.providerData != undefined && $scope.providerData.subscriptionTrail == true)){
            if($scope.saveOnly != 'Y'){
                if($scope.totalAmount == 0){
                    sweetAlert("Please select a Package/Service to Subscribe.");
                    return false;
                }
            }

            /*// check Terms and Conditions
            if(($scope.chkTermsAndConditionsChecked == false || $scope.chkViewedTrainingSchedule == false) && quote == 'N'){
                sweetAlert("Please accept the Terms & Conditions to continue");
                return false;
            }*/

            $scope.selectedPackage = _selectedPackage;
            return true;
        };

        $scope.goToStep1 = function(){
            // update current step
            $scope.subscribeStep = 'STEP_1';
            // disable active packages
            // $scope.disablePackageContainerIfActivePackage();    //TO DO: MAY NOT BE REQUIRED
            // $('.pricePackage :input').attr('disabled', false);
            // update bread-crumb
            $('.breadcrumb a').removeClass('active');
            $('.breadcrumb a:nth-child(1)').addClass('active');
            // show inactive packs
            $scope.hideInactivePacks = false;
            // enable/disable
            enableDisableInputFieldsAllPacks($scope, $scope.subscribeStep);
            // scroll to top
            $location.hash("pricingPlanTop1");
            /*if(providerData.memberType == 'PREMIUM') {
                _package.disableAllServicesButNotification = true;
                _package.disableTrainingPackage = false;
                _package.disableNotificationService = false;
            }else{ // if VISITOR
                _package.disableAllServicesButNotification = false;
                _package.disableTrainingPackage = false;
                _package.disableNotificationService = false;
            }*/
        };

        /*$scope.saveChanges = function(){
            $scope.getProviderData(function(provider){
                // goto step2
                $scope.goToStep2(provider.trail, 'Y');
            });
        };*/

        $scope.goToStep2 = function(trail, saveOnly){
            $scope.trail = trail;
            $scope.saveOnly = saveOnly;
            if(validateAndPickSelectedPackage($scope.trail,'N',$scope.saveOnly) == false)       // This Step is not applicable for Quotation
                return false;

            $scope.getProviderData(function(providerData){
                //
                function processNextSubStepOne(){
                    // final Sub-Step after finishing with all the prompts (Update Provider Registration, SMS Sender-ID registration)
                    function processNextSubStepTwo(){
                        goToStep2Internal();
                    }
                    // check if 'smsSenderId' is registered, if not show popup to register 'smsSenderId'
                    if((providerData.smsSenderId == null || providerData.smsSenderId === "null") && $scope.saveOnly == 'N'){   // no SMS Sender ID
                        // check if any notification service is selected
                        var notificationSelected = ($.grep($scope.selectedPackage.notificationServices, function(svc, idx){
                            return svc.activated == 1;
                        }).length) > 0;
                        // if notification is selected, then prompt for SMS Sender ID
                        if(notificationSelected == true){   // notification service is selected
                            $scope.smsSenderIdHandler.showPopup("ACCEPT", function(){ // prompt for SMS Sender ID
                                processNextSubStepTwo();    // proceed further after SMS Sender ID is registered
                            });
                        } else {   // notification service NOT select, NO NEED to prompt for SMS Sender ID
                            processNextSubStepTwo();
                        }
                    } else {    // SMS Sender-ID already registered, NO NEED to prompt for SMS Sender ID again
                        processNextSubStepTwo();
                    }
                }

                // if the user is visitor, Show Registration Complete popup
                if(providerData != null && providerData.memberType == 'VISITOR' && $scope.saveOnly == 'N') {
                    $scope.memberRegistrationHander.showPopup(function(){
                        processNextSubStepOne();
                    });
                }else{
                    processNextSubStepOne();
                }
            });

            /*$scope.getProviderData(function(providerData){
                if(providerData != null && providerData.memberType == 'VISITOR') {
                    getAllPremiumMember();
                    $("#ProviderMemberRegistration").modal("show");
                }else{
                    goToStep2Internal();
                }
            });*/
        };

        function goToStep2Internal(){
            if(validateAndPickSelectedPackage($scope.trail,'N',$scope.saveOnly) == false)    // This Step is not applicable for Quotation
                return false;
            $scope.subscribeStep = 'STEP_2';
            // expand all panels
            expandAllPanels();
            // disable all input fields
            // $('.pricePackage :input').attr('disabled', true);
            // update bread-crumb
            $('.breadcrumb a').removeClass('active');
            $('.breadcrumb a:nth-child(2)').addClass('active');
            // hide inactive packs
            $scope.hideInactivePacks = true;
            // enable/disable
            enableDisableInputFieldsAllPacks($scope, $scope.subscribeStep);
            // scroll to top
            // -- window.location.href = "#";
            // -- window.location.hash = "#regi-afya-slider";
            $location.hash("pricingPlanTop2");
            /*_package.disableAllServicesButNotification = true;
            _package.disableTrainingPackage = true;
            _package.disableNotificationService = true;*/
        };

        $scope.goToStep3 = function(){
            $scope.subscribeStep = 'STEP_3';
            $('.breadcrumb a').removeClass('active');
            $('.breadcrumb a:nth-child(3)').addClass('active');
            $scope.showTrygainButton = false;
            // updatePaymentStatus("");
            // scroll to top
            //-- window.location.href = "#";
            //-- window.location.hash = "#regi-afya-slider";
            //$location.hash("");
            $location.hash("pricingPlanTop3");
            // $scope.submitSubscription('N');
            submitSubscriptionInternal();
        };

        $scope.goToPage = function(url){
            $state.go(url);
        };

        /*$scope.disablePackageContainerIfActivePackage = function() {
            if($scope.hasActivePackage == false)
                $('.pricePackage :input').attr('disabled', false);
            else
                $('.pricePackage :input').attr('disabled', true);
        };*/

        /* Post Package to subscribe by updating DB */
        function subscribePackage(successCB, paymentId){
            var _packages = [];
            for(i=0; i <$scope.packages.length; i++) {
                var _package = $scope.packages[i];
                _package.trail = $scope.trail;
                // include notification only when saveOnly is false
                if($scope.saveOnly == 'N'){
                    // check if we have notification services
                    var hasNotificationServices = ($.grep($scope.selectedPackage.notificationServices, function(svc, idx){
                                    return svc.activated == 1;
                    }).length) > 0;
                    // if we notification services are selected then included them
                    if(hasNotificationServices == true){
                        for(i=0; i < _package.notificationServices.length;i++) {
                            _package.services.push( _package.notificationServices[i]);
                        }
                    }
                }
                for(i=0; i < _package.patientServices.length;i++) {
                    _package.services.push( _package.patientServices[i]);
                }
                for(i=0; i < _package.providerServices.length;i++) {
                    _package.services.push( _package.providerServices[i]);
                }
                // if save (only) button is clicked, then deactivate notification services
                /*if($scope.saveOnly == 'Y'){
                    $.each(_package.notificationServices, function(idx, val){
                        val.activated = 0;
                    });
                }*/
                // add package to list
                _packages.push(_package);
            }

            // include training services only when saveOnly is false
            if($scope.saveOnly == 'N'){

                for(i=0; i <$scope.trainingPackages.length; i++) {

                    // check if this pack has activated services
                    var hasActivatedService = ($.grep($scope.trainingPackages[i].services, function(svc,idx1){
                        return svc.activated == 1;
                    }).length) > 0 ;

                    // skip package if no activated services
                    if(hasActivatedService == false)
                        continue;

                    // set trial flag
                    $scope.trainingPackages[i].trail = $scope.trail;
                    // push the training package
                    var _trainingPackage = $scope.trainingPackages[i];
                    _packages.push(_trainingPackage);
                    // if save (only) button is clicked, then deactivate training services
                    /*if($scope.saveOnly == 'Y'){
                        $.each(_trainingPackage.services, function(idx,val){
                            val.activated = 0;
                        });
                    }*/
                }
            }
            paymentId = paymentId | '';
            $http.post('/afya-portal/anon/subscribe_package?smsSenderId=' + $scope.SMSSenderId + "&paymentId=" + paymentId, _packages).success(function (data,status,headers,config) {
                if (data.message == "success") {
                    // force update to read the provider data
                    $scope.getProviderData(function(){}, true);
                    // alert("Success");
                    // GO TO Step-2
                    // $('.pricePackage :input').attr('disabled', true);
                    //
                    // TO DO: check how to generalize
                    $('.breadcrumb a:nth-child(3)').addClass('active');
                    $('.breadcrumb a:nth-child(2)').removeClass('active');
                    $('.breadcrumb a:nth-child(1)').removeClass('active');

                    successCB();
                }
            });
        };

        /*$scope.onRegistrationClick = function() {
            $scope.registerData.username = $scope.username;
            // updating Premium Registration
            $http({
                url: '/afya-portal/anon/modifyUserLoginDetails',
                method:'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'accept':'text/plain'
                },
                data: JSON.stringify($scope.registerData)
            }).success(function(data){
                *//*if($scope.trail == 'Y'){
                    submitSubscriptionInternal();
                }
                else {*//*
                    goToStep2Internal();
                //}
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        };*/


        /* original - commented by kannan
        $scope.submitSubscription = function(trail) {
            $scope.trail = trail;
            //check for mutiple package activated
            var activated_packages ="";
            var activated_count = 0;
            for (i = 0; i < $scope.packages.length; i++) {
                var package = $scope.packages[i];

                if (package.activated == 1) {
                    if (activated_packages != "") {
                        activated_packages = activated_packages + ",";
                    }
                    activated_count++;
                    activated_packages =  activated_packages + package.packageName;
                    if ((package.numberOfUsers == 0) || (package.numberOfUsers == null) || (package.numberOfUsers == "")) {
                        alert("Number of users for active package should be greater than zero. ["+package.packageName+"]");
                        return false;
                    }
                }
            }
            if (activated_count > 1) {
                alert("Only one package can be active. ["+activated_packages+"]");
                return false;
            }
            var _packages = [];
            for(i=0; i <$scope.packages.length; i++) {
                 var _package = $scope.packages[i];
                 _package.trail = trail;
                 for(i=0; i < _package.notificationServices.length;i++) {
                     _package.services.push( _package.notificationServices[i]);
                 }
                for(i=0; i < _package.patientServices.length;i++) {
                    _package.services.push( _package.patientServices[i]);
                }
                for(i=0; i < _package.providerServices.length;i++) {
                    _package.services.push( _package.providerServices[i]);
                }
                _packages.push(_package);
            }
            for(i=0; i <$scope.trainingPackages.length; i++) {
                $scope.trainingPackages[i].trail = trail;
                _packages.push($scope.trainingPackages[i]);
            }
            $http.post('subscribe_package', _packages).success(function (data,status,headers,config) {

                if (data.message == "success") {
                    alert("Success");
                }
            });
        };*/

        $scope.calculateTotalAmount = function() {
            $scope.totalAmount = 0.0;
            // service package price
            if($scope.hasActivePackage == false || ($scope.providerData != undefined && $scope.providerData.subscriptionTrail == true)) {  // if package is already active (add-on subscription) then skip accumulating package prices
                for (i = 0; i < $scope.packages.length; i++) {
                    var package = $scope.packages[i];
                    if ((package.amount == null) || (parseFloat(package.amount) == 0.0))
                        continue;
                    if (package.activated == 1) {
                        $scope.totalAmount = $scope.totalAmount + parseFloat(package.amount);
                        // add unpaid amount if any (to account for notifications and trainings chosen during Trial
                        $scope.totalAmount = $scope.totalAmount + (package.pendingPayment | 0);
                    }
                }
            }

            // notification service price
            //if($scope.hasNotificationSubscritpion == false) {
                for (i = 0; i < $scope.packages.length; i++) {
                    var package = $scope.packages[i];
                    for (j = 0; j < package.notificationServices.length; j++) {
                        var service = package.notificationServices[j];
                        if ((service.amount == null) || (parseFloat(service.amount) == 0.0))
                            continue;
                        if (service.activated == 1) {
                            $scope.totalAmount = $scope.totalAmount + parseFloat(service.amount);
                        }
                    }
                }
            //}

            // training price
            //if($scope.hasTrainingSubscritpion == false) {
                for (i = 0; i < $scope.trainingPackages.length; i++) {
                    var package = $scope.trainingPackages[i];
                    for (j = 0; j < package.services.length; j++) {
                        var service = package.services[j];
                        if ((service.amount == null) || (parseFloat(service.amount) == 0.0))
                            continue;
                        if (service.activated == 1) {
                            $scope.totalAmount = $scope.totalAmount + parseFloat(service.amount);
                        }
                    }
                }
            //}
            $scope.totalAmount = Number($scope.totalAmount).toFixed(3);
        };

        $scope.isloggedIn = false;
        $scope.packages = [];
        $scope.trainingPackages = [];
        $scope.totalAmount = 0.000;

        $scope.getProviderData(function(){
            $http.get("/afya-portal/anon/get_packages").success(function(data){
                $http.get("/afya-portal/anon/getActivePackageServiceUsageForTenant?tenantId=" + $scope.providerData.tenantId).success(function(svcUsageData){populatePackages(data, svcUsageData);});
            });
        });

        function populatePackages(data, svcUsageData){
              // if a package is activated then show only that package, else if query string contains package ID show only that package for subscription else show all packages for subscription
              var packageList = null;
              var _activePackageList = $.grep(data.packages, function(dto,idx){return dto.activated == true;});
              if(_activePackageList.length > 0){
                  // has active package
                  $scope.hasActivePackage = true;
                  packageList = $.grep(data.packages, function(dto,idx){return (dto.activated == true || dto.packageCategory != "SERVICE");});
              }else{
                  $scope.hasActivePackage = false;
                  // apply filters if any
                  if($scope.filterServicePackId != null && $scope.filterServicePackId != ''){
                      var _requiredPackageList = $.grep(data.packages, function(dto,idx){return (dto.packageCategory != "SERVICE" || dto.packageId == $scope.filterServicePackId);});
                      if($scope.filterPriceOption != null && $scope.filterPriceOption != '')
                        _requiredPackageList = $.map(_requiredPackageList, function(dto, idx){ if(dto.packageCategory == "SERVICE" && dto.packageId == $scope.filterServicePackId){dto.priceOption = $scope.filterPriceOption;} return dto; });
                      packageList = _requiredPackageList;
                  }else {
                      packageList = data.packages;
                  }
              }

              // by default show the trial message (assuming no active subscriptions)
              $scope.showFreeTrialMessage = true;

              for (i = 0; i < packageList.length; i++) {
                   var packageDto = packageList[i];

                  // skip packages that do not belong to the Logged-in user's 'Provider Type' (Clinic or Pharmacy)
                  if(packageDto.packageType.toUpperCase() != $scope.providerData.providerType.toUpperCase())
                        continue;

                   if (packageDto.packageCategory == "SERVICE") {
                       var  package = {};
                        package.packageId = packageDto.packageId;
                        package.packageName = packageDto.packageName;
                        package.solutionName = packageDto.solutionName;
                        package.halfYearlyPricePerUom = Number(packageDto.halfYearlyPricePerUom).toFixed(3);
                        package.annuallyPricePerUom = Number(packageDto.annuallyPricePerUom).toFixed(3);
                        package.selectedPricePerUom =  package.halfYearlyPricePerUom;
                        package.trialPeriodDays = packageDto.trialPeriodDays;
                        package.pendingPayment = packageDto.pendingPayment;
                        package.isTrialExpired = packageDto.isTrialExpired;

                       package.priceOption = packageDto.priceOption;
                       if (packageDto.numberOfUsers != null && packageDto.numberOfUsers > 0)
                           package.numberOfUsers = packageDto.numberOfUsers;
                       else
                           package.numberOfUsers = 1;   // default number of users
                       if(package.priceOption == null)
                           package.priceOption = 'ANNUALLY';    // default price option
                        package.amount = Number(packageDto.amount).toFixed(3);
                        package.activated = packageDto.activated;
                        package.packageCategory = packageDto.packageCategory;
                        package.services = [];
                        package.patientServices = [];
                        package.providerServices = [];
                        package.notificationServices = [];
                        package.totalServices = packageDto.totalServices;
                        package.disablePackageSelection = false;
                       package.disableAllServicesButNotification = false;
                       package.disableNotificationService = false;
                       if(packageDto.subscriptionDate != null && packageDto.subscriptionDate != ''){
                           package.subscriptionDate =  packageDto.subscriptionDate;
                       }else{
                           package.subscriptionDate = new Date();
                       }
                       package.expiryDays = deriveExpiryDays(package);     // package.priceOption == 'HALF_YEARLY' ? 180 : 365;
                       // expiry Date
                       var expiryDate = new Date(package.subscriptionDate);
                       //expiryDate.setMonth(expiryDate.getMonth() + (package.priceOption == 'ANNUALLY' ? 12 : 6));
                       expiryDate.setDate(expiryDate.getDate() + package.expiryDays);
                       package.expiryDate = expiryDate;

                        // check if package is about to expire
                        var millisBetween = package.expiryDate.getTime() - (new Date()).getTime();
                        var millisecondsPerDay = 1000 * 60 * 60 * 24;
                        var days = millisBetween/millisecondsPerDay;
                        if(days >= 0 && days < 7)
                            $scope.showMessageAboutToExpire = true;
                        else if(days < 0 && days >= -7)
                            $scope.showMessageExpired = true;

                        // if package is active and has crossed the trial period, then hide the trial message
                        //if(package.activated == 1 && ((package.expiryDays - days) > package.trialPeriodDays)){
                        if(package.isTrialExpired == true){
                            $scope.showFreeTrialMessage = false;
                        }

                       // if(package.activated == true)
                       // $scope.hasActivePackage = true;
                        if (packageDto.services != null) {
                             for (j = 0; j < packageDto.services.length; j++) {
                                  var serviceDto = packageDto.services[j];
                                  var service = {};
                                  service.serviceId = serviceDto.serviceId;
                                  service.serviceName = serviceDto.serviceName;
                                  service.serviceDisplayName = serviceDto.serviceDisplayName;
                                  service.serviceSequence = serviceDto.serviceSequence;
                                  service.activated = 0;
                                  service.disabled = 0;  // kannan - to disable service selection radio-button if there are active
                                  if (serviceDto.serviceType =="TOP_LEVEL") {
                                       package.services.push(service);
                                  } else if (serviceDto.serviceType =="PATIENT") {
                                       if (serviceDto.activated == null) {
                                           service.activated = 1;
                                       } else {
                                           service.activated = serviceDto.activated;
                                       }
                                       // enable/disable service selection
                                       if(svcUsageData != undefined){
                                            var hasActiveSchedule = ($.grep(svcUsageData, function(svc,idx){
                                                return svc.serviceName == service.serviceName && svc.hasActiveSchedule == 1;
                                            }).length) > 0;
                                            service.disabled = hasActiveSchedule;   // disable if service has active-schedules
                                       }
                                       package.patientServices.push(service);
                                  } else if (serviceDto.serviceType =="PROVIDER") {
                                      if (serviceDto.activated == null) {
                                          service.activated = 1;
                                      } else {
                                          service.activated = serviceDto.activated;
                                      }
                                       package.providerServices.push(service);
                                  } else if (serviceDto.serviceType =="NOTIFICATION") {
                                      service.packs = 0;    // serviceDto.packs;         // service.packs = 0;  RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                                      service.expireDays = serviceDto.expireDays;
                                      service.numberOfSMS = serviceDto.numberOfSMS;
                                      service.numberOfSMSToSubscribe = 0;
                                      service.rate = Number(serviceDto.rate).toFixed(3);
                                      service.amount = 0; // Number(serviceDto.amount).toFixed(3); // service.amount = 0;   // RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                                      service.activated = 0;  // serviceDto.activated;  // service.activated = 0;  // RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                                      if(serviceDto.activated == 1)
                                          $scope.hasNotificationSubscritpion = true;
                                      package.notificationServices.push(service);
                                  }
                             }
                        }
                        $scope.packages.push(package);

                   } else  if (packageDto.packageCategory == "TRAINING") {
                       var  package = {};
                       package.packageId = packageDto.packageId;
                       package.packageName = packageDto.packageName;
                       package.solutionName = packageDto.solutionName;
                       package.packageCategory = packageDto.packageCategory;
                       package.disableTrainingPackage = false;
                       package.services = [];
                       if (packageDto.services != null) {
                           for (j = 0; j < packageDto.services.length; j++) {
                               var serviceDto = packageDto.services[j];
                               var service = {};
                               service.serviceId = serviceDto.serviceId;
                               service.serviceName = serviceDto.serviceName;
                               service.numberOfHours = 0; // serviceDto.numberOfHours;    // service.numberOfHours = 0; // RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                               service.rate = Number(serviceDto.rate).toFixed(3);
                               service.activated = 0; // serviceDto.activated;    // service.activated = 0; // RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                               if(serviceDto.activated == 1)
                                   $scope.hasTrainingSubscritpion = true;
                               service.amount = 0;  // Number(serviceDto.amount).toFixed(3);   // service.amount = 0;  // RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                               package.services.push(service);
                           }
                       }
                       $scope.trainingPackages.push(package);
                   }
              }
              // enable/disable training schedule
              // write to console
              console.log($scope.packages);
              console.log($scope.trainingPackages);
              // calculate total amount
              $scope.calculateTotalAmount();


            /*setTimeout(function() {
                  $('.panel-heading span.clickable').on("click", function (e) {
                      if ($(this).hasClass('panel-collapsed')) {
                          // expand the panel
                          $(this).parents('.panel').find('.panel-body1').slideDown();
                          $(this).removeClass('panel-collapsed');
                          $(this).find('i').removeClass('fa-plus').addClass('fa-minus');
                      }
                      else {
                          // collapse the panel
                          $(this).parents('.panel').find('.panel-body1').slideUp();
                          $(this).addClass('panel-collapsed');
                          $(this).find('i').removeClass('fa-minus').addClass('fa-plus');
                      }
                  });
              },4000);*/

            // $scope.disablePackageContainerIfActivePackage();
         };

         // Note: Changes should be synced with Portal code to Subscribe Package and Services-Susbscribed page
         function deriveExpiryDays(package){
            // TO DO: Add Trial Period only if this is the First Package Subscriptoin
            return (package.priceOption == 'HALF_YEARLY' ? 180 : 365) + (package.trialPeriodDays || 0);
         }

        /*$scope.onDisplaySMSSenderClick = function(){
            $("#SMSSender").modal("show");
        };*/

        /*$scope.onUpdateSMSSenderClick = function(){
            //$http.get($scope.portalBaseUrl + '/anon/getProviderByUsername?username=' + $scope.username).success(function(data){
            var data = getProviderByUsername();
            if(data != null){
                // UPDATEING SMS SENDER ID
                var value = { "tenantId" : data.membershipId,"smsSenderName" : $scope.SMSSenderId }
                $http({
                    url: '/afya-portal/anon/updateSMSSenderInGivenTenant',
                    method:'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'accept':'application/json'
                    },
                    data: JSON.stringify(value)
                }).success(function(data){
                    $("#SMSSender").modal("hide");
                }).error(function(data){
                    sweetAlert("error fetching data");
                });
            }else{
                sweetAlert("error fetching data");
            }

            //}).error(function(data){
            //    sweetAlert("error fetching data");
            //});
        };*/

        //
        // -- Payment Gateway Integration - kannan
        //
        function makePaymentAndSubscribe(){
            //alert('Option Selected = ' + mode);
            // for Processing Charges refer tale payment_gateway_processing_fees
            var payerType = 'MERCHANT';
            $http.get($scope.portalBaseUrl + '/anon/getPaymentProcessingFees?payerType=' + payerType + '&amount=' + $scope.totalAmount).success(function (data) {
                // copy fees and total payable into PayMode binding object
                data = data[0];
                $scope.PayMode.amountPayable = $scope.totalAmount;
                $scope.PayMode.knetValue.fees = data.debitCardFees;
                $scope.PayMode.knetValue.totalPayable = data.debitCardTotalPayable;
                $scope.PayMode.visaMasterValue.fees = data.creditCardFees;
                $scope.PayMode.visaMasterValue.totalPayable = data.creditCardTotalPayable;
                $scope.PayMode.payerType = payerType;
                // show the Model dialogue for user to choose Payment Channel
                $("#modalPaymentMode").modal("show");
            }).error(function (data, status, headers, config) {
                console.log("Error while Getting Premium Member!" + status);
            });


            /*var options = {
                //afyaId: $scope.patientList.afyaId, -- this is not available
                username: $.cookie('username'),
                paymentAmount: $scope.totalAmount,
                clinicId: '',
                doctorId: '',
                transactionType: 'PACKAGE_SUBSCRIPTION', *//* APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT *//*
                packageId: $scope.selectedPackage.packageId,
                apptSlot: '',
                description: 'PACKAGESUBSCRIPTION', // selectedPackage.packageName.replace(/ /g,''),
                // description2: "description2",
                errorCB: paymentErrorCB,
                pollCallback: paymentPollCallbackCB,
                baseUrlForService : $scope.portalBaseUrl + "/anon/",
                paymentChannel : mode
            };
            console.log(options);
            // calling payment
            afyaApp.payit.initiatePayment(options);*/
        };

        // Payment Mode selection click handler
        $scope.onPaymentModeSelectionClick = function (mode) {
            // alert('Option Selected = ' + mode);
            var options = {
                afyaId: '', // this is not available
                username: $scope.$cookie('username'),
                paymentAmount: $scope.PayMode.mode.totalPayable, //$scope.totalAmount,
                processingFees: $scope.PayMode.mode.fees,
                clinicId: '',
                doctorId: '',
                transactionType: 'PACKAGE_SUBSCRIPTION', // APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT
                packageId: $scope.selectedPackage.packageId,
                apptSlot: '',
                description: 'PACKAGESUBSCRIPTION', // selectedPackage.packageName.replace(/ /g,''),
                // description2: "description2",
                errorCB: paymentErrorCB,
                pollCallback: paymentPollCallbackCB,
                baseUrlForService: $scope.portalBaseUrl + "/anon/",
                paymentChannel: mode,
                payerType: $scope.PayMode.payerType,
            };
            console.log(options);
            // calling payment
            afyaApp.payit.initiatePayment(options);
        };

        function paymentErrorCB(errMsg) {
            sweetAlert(errMsg);
            updatePaymentStatus("Error");
        };

        function paymentPollCallbackCB(result) {
            //  integrate changes with the digest cycle
            /*try{
                $scope.$apply(function(){
                    paymentPollCallbackCBInternal();
                });
            }
            catch(err){
                paymentPollCallbackCBInternal();
            }*/

            //  integrate changes with the digest cycle
            paymentPollCallbackCBInternal();
            if($scope.$$phase != '$digest' && $scope.$$phase != '$apply')
                $scope.$apply();

            // actual business logic
            function paymentPollCallbackCBInternal(){
                //alert('success payment');
                console.log("Poll Status: " + result.status);
                //BookAppointmentWithSuccessPayment()

                // Take actions based on Payment Status
                if ((result.status == "Declined") || (result.status == "Timed Out")) { // NOTE: THIS IS FOR PRODUCTION
                    // if(result.status == "Declined"){ // NOTE: THIS IS FOR TESTING
                    // $("#modalConfirmation").modal("show");
                    updatePaymentStatus("Declined");
                }else if (result.status == "Success") {  // NOTE: THIS IS FOR PRODUCTION
                    //else if(result.status == "Success" || result.status == "Timed Out"){ // NOTE: THIS IS FOR TESTING
                    // $("#modalConfirmation").modal("hide");
                    updatePaymentStatus("Subscribing Package...");
                    // perform Final steps after a Successfull Payment
                    subscribePackage(function(){
                        updatePaymentStatus("Subscribed", result.trackId);
                        // showSuccessMessage("Subscribed Successfully!");
                    }, result.paymentId);
                    //if(mainOption == "OPT_APPOINTMENT" || mainOption == "OPT_APPOINTMENT_REQUEST" || mainOption == "OPT_TELE_CONSULTATION" || mainOption == "OPT_HOME_VISIT")
                    /*if ($scope.appointmentVisitType == "Premium Visit" || $scope.appointmentVisitType == "Tele Consultation Visit" || $scope.appointmentVisitType == "Home Visit") {
                        BookAppointmentWithSuccessPayment();
                    }*/
                }else if(result.status == "ERROR_CONNECTIVITY"){
                    updatePaymentStatus("ERROR_CONNECTIVITY");
                }else if(result.status == "ERROR_UNKNOWN"){
                    updatePaymentStatus("ERROR_UNKNOWN");
                }else if(result.status == "Waiting"){
                    updatePaymentStatus("Waiting...");
                }else{
                    updatePaymentStatus(result.status);
                }
                //else if(mainOption == "OPT_HOME_PHARMACY"){}
            }
        };

        function showSuccessMessage(message){
            $scope.progressMessage = message;
            /*sweetAlert({
                    title: message, // "Subscribed Successfully!",
                    text: "",
                    type: "info",
                    showCancelButton: false,
                    confirmButtonText: "OK",
                    closeOnConfirm: false
                },
                function(){
                    // $window.location.href = "web_pages/member_area/provider/Provider_account.html";
                    $window.location.href = "ProviderDashboard";
                });*/
        };


        /////////////// Member Registration hander ///////////////////////////////
        $scope.onRegistrationClick = function(){
            $scope.memberRegistrationHander.onRegistrationClick();
        };
        $scope.memberRegistrationHander = {
            doneCallback: null,

            showPopup: function(doneCallback){
                /*if(this.doneCallback != null)
                    throw 'this.doneCallback is already registered';*/
                this.doneCallback = doneCallback;
                $scope.registerData.providerRecommendedBy = "";
                getAllPremiumMember();
                // populate provider details
                $scope.registerData.providerAddress1 = $scope.providerData.address || "";
                $scope.registerData.providerAddress2 = $scope.providerData.address2 || "";
                $scope.registerData.providerCountry = $scope.providerData.country  || "";
                $scope.registerData.providerGovernorate = $scope.providerData.state  || "";
                // $scope.cityList.city = $scope.providerData.city  || null;
                $scope.registerData.providerPostalCode = $scope.providerData.postalCode  || "";
                $scope.registerData.providerLicenseNo = $scope.providerData.licenseNo  || "";
                $scope.registerData.providerRegistrationNo = $scope.providerData.registrationNo  || "";
                $scope.registerData.providerRecommendedBy = $scope.providerData.recommendedBy  || "";
                // show registration popup
                $("#ProviderMemberRegistration").modal("show");
            },

            onRegistrationClick: function() {
                var thisObj = this;
                $scope.registerData.username = $scope.username;

                if(typeof( $scope.cityList) == "undefined" ||  $scope.cityList == "")
                {
                    $("#validateCity").text('Please select city');
                    return false;
                }
                else
                {
                    $scope.registerData.providerCity =  $scope.cityList.city;
                    $("#validateCity").text('');
                }

                // updating Premium Registration
                $http({
                    url: '/afya-portal/anon/modifyUserLoginDetails',
                    method:'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'accept':'text/plain'
                    },
                    data: JSON.stringify($scope.registerData)
                }).success(function(data){
                    // retrieve Provider Data with Force Update
                    if(data == 'successfully update')
                    {
                        $("#ProviderMemberRegistration").modal("hide");
                        $scope.getProviderData(function(providerData){
                            thisObj.doneCallback();
                            thisObj.doneCallback = null;
                        }, true);
                    }
                }).error(function(data){
                    sweetAlert("error fetching data");
                    thisObj.doneCallback = null;
                });
            },
        };

        /////////////// SMS Sender ID hander ///////////////////////////////
        $scope.smsSenderIdHandler = {
            doneCallback: null,

            mode: null,

            showPopup: function(mode, doneCallback){
                /*if(this.doneCallback != null)
                    throw 'this.doneCallback is already registered';*/
                $scope.smsSenderIdHandler.mode = mode;

                if(mode == "VERIFY"){
                    $scope.SMSSenderId = $scope.providerData.smsSenderId;
                }else{
                    $scope.SMSSenderId = "";
                }

                this.doneCallback = doneCallback;
                    $("#SMSSender").modal("show");
            },

            onUpdateSMSSenderClick: function(mode){
                //$http.get($scope.portalBaseUrl + '/anon/getProviderByUsername?username=' + $scope.username).success(function(data){
                if(!$scope.hasActivePackage){
                    // if no active package, then tenant DB and Tenant record is not created, hence SMS Sender ID update cannot be done
                    if(mode == "ACCEPT"){
                        // invoke callback to proceed with further processing
                        var thisObj = $scope.smsSenderIdHandler;
                        thisObj.doneCallback();
                        thisObj.doneCallback = null;
                    }else if(mode == "VERIFY"){
                        // nothing to do here
                    }
                    return;
                }
                var data = getProviderByUsername();
                if(data != null){
                    // UPDATEING SMS SENDER ID
                    var value = { "tenantId" : data.tenantId,"smsSenderName" : $scope.SMSSenderId }
                    var thisObj = this;
                    $http({
                        url: '/afya-portal/anon/updateSMSSenderInGivenTenant',
                        method:'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'accept':'application/json'
                        },
                        data: JSON.stringify(value)
                    }).success(function(data){
                        $("#SMSSender").modal("hide");
                        // retrieve Provider Data with Force Update
                        $scope.getProviderData(function(providerData){
                            if(mode == "ACCEPT"){
                                // invoke callback to proceed with further processing
                                thisObj.doneCallback();
                                thisObj.doneCallback = null;
                            }else if(mode == "VERIFY"){
                                // update SMS Sender Verification
                                $scope.smsSenderIdHandler.onVerifiedSmsSenderId();
                            }
                        }, true);
                    }).error(function(data){
                        sweetAlert("error updating SMS Sender ID");
                        thisObj.doneCallback = null;
                    });
                }else{
                    sweetAlert("error fetching data");
                }

                //}).error(function(data){
                //    sweetAlert("error fetching data");
                //});
            },

            onVerifiedSmsSenderId: function(){
                //$http.get($scope.portalBaseUrl + '/anon/getProviderByUsername?username=' + $scope.username).success(function(data){
                var data = getProviderByUsername();
                if(data != null){
                    // UPDATEING SMS SENDER ID verified
                    var value = { "tenantId" : data.tenantId}
                    var thisObj = this;
                    $http({
                        url: '/afya-portal/anon/updateSMSSenderVerifiedInGivenTenant',
                        method:'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'accept':'application/json'
                        },
                        data: JSON.stringify(value)
                    }).success(function(data){
                        // retrieve Provider Data with Force Update
                        $scope.getProviderData(function(providerData){
                            sweetAlert("Updated SMS Sender ID to Verified");
                        }, true);
                    }).error(function(data){
                        sweetAlert("Error updating SMS Sender ID to Verified!");
                        thisObj.doneCallback = null;
                    });
                }else{
                    sweetAlert("Error fetching data");
                }
            },
        };

        /////////////// Afya Provider Policy for Service Popup hander ///////////////////////////////
        $scope.afyaProviderPolicyForServiceHandler = {
            doneCallback: null,
            currService: null,

            showPopup: function(service, doneCallback){
                /*if(this.doneCallback != null)
                    throw 'this.doneCallback is already registered';*/
                $scope.providerServicePolicyContent = "";
                // reset selection till the user aggrees the policy
                service.activated = 0;
                // done callback
                this.doneCallback = doneCallback;
                // reference to service object
                this.currService = service;
                // show the modal popup
                $("#afyaProviderPolicyForService").modal("show");
                // retrieve pocilty by service
                $http.get($scope.portalBaseUrl + '/anon/getProviderPoilicyForService?serviceName=' + service.serviceName
                    , {headers : {'accept': 'text/html'}}).success(function (data) {
                    $scope.providerServicePolicyContent = data;
                }).error(function (data, status, headers, config) {
                    console.log("Error while Getting Provider Policies for Services!" + status);
                    sweetAlert("error fetching data");
                });
            },

            onAgreeClicked: function(){
                // set the status to selected
                this.currService.activated = 1;
                // calculate total anount
                $scope.calculateTotalAmount();
                // reset the current service
                this.currService = null;
                // invoke donecallback if none is defined
                if(this.doneCallback != null)
                    this.doneCallback();
            },
        };



        ////////////// PAYMENT GATEWAY INTEGRATION RELATED SERVICE CALLS /////////////////
        if (window.afyaApp.svc == undefined)
            window.afyaApp.svc = {};
        window.afyaApp.svc.addPaymentTransactionForAppointment = function (settings) {
            // make service call
            $http.post($scope.portalBaseUrl + '/anon/addPaymentTransactionForAppointment?transactionType=' + settings.transactionType + "&transactionAmount=" + settings.transactionAmount + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId + "&apptSlot=" + settings.apptSlot + "&packageId=" + settings.packageId + "&username=" + settings.username + "&processingFees=" + settings.processingFees + "&payerType=" + settings.payerType + "&paymentChannel=" + settings.paymentChannel).success(function (data) {
                settings.success(data);
            }).error(function (data, status, headers, config) {
                // sweetAlert('Error : addPaymentTransactionForAppointment');
                settings.error(status);
            });

            //        afyaApp.config.baseUrlForSearch + "addPaymentTransactionForAppointment?transactionType=" + settings.transactionType + "&transactionAmount=" + settings.transactionAmount
            //    + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId
            //    + "&apptSlot=" + settings.apptSlot

        };

        //updatePaymentTransactionWithIsysStatus
        window.afyaApp.svc.updatePaymentTransactionWithIsysStatus = function (settings) {
            // make service call
            $http.post($scope.portalBaseUrl + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus + "&isysMerchantRef=" + settings.isysMerchantRef).success(function (data) {
                settings.success(data);
            }).error(function (data, status, headers, config) {
                // sweetAlert('Error : updatePaymentTransactionWithIsysStatus');
                settings.error(data, status, headers, config);
            });

            //        afyaApp.config.baseUrlForSearch + "updatePaymentTransactionWithIsysStatus?paymentId=" + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef
            //      + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp
            //      + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus

        };

        // get query string value by properties
        function getParameterValueByProperty(type) {
            type = type.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + type + "=([^&#]*)"),
                results = regex.exec(location.search);
            return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        };

        function getAllPremiumMember(){
            $http.get('/afya-portal/getAllInvitedPremiumMember').success(function (data) {
                $('#RegRecommendedBy').empty();
                $('<option>').val("").text("").appendTo('#RegRecommendedBy');
                $.each(data, function(index, value) {
                    $('<option>').val(value.username).text(value.firstName + ' ' + value.lastName).appendTo('#RegRecommendedBy');
                });
            }).error(function (data, status, headers, config) {
                console.log("Error while Getting Premium Member!" + status);
            });
        };

        function updatePaymentStatus(paymentStatus, paymentTrackId){
            $scope.paymentStatus = paymentStatus;
            $scope.paymentTrackIdMessage = null;
            $scope.processMessageIcon = "/afya-portal/static/images/right-icon.png";
            $scope.showTrygainButton = false;
            switch($scope.paymentStatus){
                /*case "":
                    $scope.progressMessage = "Error";   // SHOULD NOT BE HERE
                    $scope.processMessageIcon = "/afya-portal/static/images/wrong-icon.png";
                break;*/
                case "PAYMENT":
                    $scope.progressMessage = "Payment for Subscription";
                break;
                case "Error":
                    $scope.progressMessage = "Sorry, the payment transaction has failed. Please retry"; // "Error when Processing Payment";
                    $scope.showTrygainButton = true;
                    $scope.processMessageIcon = "/afya-portal/static/images/wrong-icon.png";
                break;
                /*case "Declined":
                    $scope.progressMessage = "Your transaction has failed!"; // "Payment Declined";
                    $scope.showTrygainButton = true;
                break;*/
                case "Subscribing Package...":
                    $scope.progressMessage = "Subscribing Package...";
                break
                case "SAVING_CHANGES":
                    $scope.progressMessage = "Saving...";
                break;
                case "Subscribed":
                    if($scope.hasActivePackage)
                        $scope.progressMessage = "You have successfully subscribed for Afya Services. Thanks for believing in Afya and being part of the community to change the way care experience can be provided.";
                    else
                        $scope.progressMessage = "You have successfully subscribed for Afya Services. Thanks for believing in Afya and being part of the community to change the way care experience can be provided. Welcome to Afya Community Care. Our Community Care team will connect with you in 24 Hrs.for activating the SMART Solutions for you. In case of any queries please feel free to contact us on '+965-94407654'";
                    $scope.paymentTrackIdMessage = "<span>Please refer the Payment Track ID <strong>" + paymentTrackId + "</strong> in future communication.</span>";
                break;
                case "Waiting...":
                    $scope.progressMessage = "Processing Payment...";
                break;
                case "Saved Successfully":
                    $scope.progressMessage = "Saved Successfully!";
                break;
                case "Subscribed Trial":
                    $scope.progressMessage = "Thank you for Choosing Afya Services : Enjoy your FREE Trial for " + $scope.selectedPackage.trialPeriodDays + " Days. Welcome to Afya Community Care. Our Community Care team will connect with you in 24 Hrs.for activating the SMART Solutions for you. In case of any queries please feel free to contact us on '+965-94407654'";
                break;
                case "ERROR_CONNECTIVITY":
                    $scope.progressMessage = "Check your internet connection and try again.....";
                    $scope.showTrygainButton = true;
                    $scope.processMessageIcon = "/afya-portal/static/images/wrong-icon.png";
                break;
                case "ERROR_UNKNOWN":
                    $scope.progressMessage = "This service is temporarily unavailable. Please try after some time";
                    $scope.showTrygainButton = true;
                    $scope.processMessageIcon = "/afya-portal/static/images/wrong-icon.png";
                break;
                default:
                    // alert("Invalid paymentStatus : " + paymentStatus);
                    // $scope.progressMessage = $scope.paymentStatus;
                    $scope.progressMessage = "Sorry, the payment transaction has failed. Please retry";
                    $scope.showTrygainButton = true;
                    $scope.processMessageIcon = "/afya-portal/static/images/wrong-icon.png";
                break;
            }
            //  integrate changes with the digest cycle
            //$scope.safeApply();
        };

        /////////////////////// Utilities ///////////////////////////////////////////////////
        // round number to decimal places [considering from 5]
        $scope.setDecimal = function (input) {
            if (isNaN(input)) return input;
            // If we want 1 decimal place, we want to mult/div by 10
            // If we want 2 decimal places, we want to mult/div by 100, etc
            // So use the following to create that factor
            var places = 3;
            var factor = "1" + Array(+(places > 0 && places + 1)).join("0");
            return (Math.round(input * factor) / factor).toFixed(places);
        };

        // wrapper around angularjs apply  (source: https://coderwall.com/p/ngisma/safe-apply-in-angular-js)
        /*$scope.safeApply = function(fn) {
          var phase = this.$root.$$phase;
          if(phase == '$apply' || phase == '$digest') {
            if(fn && (typeof(fn) === 'function')) {
              fn();
            }
          } else {
            this.$apply(fn);
          }
        };*/

        $scope.allCities = [];
        $scope.getAllCities = function(){
            $http.get('/afya-portal/anon/getAllCities').success(function(data){
                $scope.allCities = data;
                // set selected city (if any)
                $scope.cityList = ($scope.providerData.city) ? $.grep($scope.allCities, function(obj){return obj.city === $scope.providerData.city;})[0] : null;
            }).error(function(data){

            });
        };

        $scope.populateStateAndCountryByCity = function(cityObj){
            if(typeof(cityObj) != "undefined")
            {
                $http.get("/afya-portal/anon/getStateCountryBasedOnCity?city="+cityObj.city).success(function(data){
                    $scope.registerData.providerGovernorate =  data.state;
                    $scope.registerData.providerCountry =  data.country;
                    console.log(data);
                }).error(function(){
                    alert("error");
                });
            }
        };

        $scope.afyaTermsAndConditionClick = function()
        {
            $scope.afyaTermsCondition = true;
            //$state.go('#AboutAfya/membership-agreement');
        }

        $scope.afyaTrainingProgramClick = function()
        {
            $scope.afyaProgram = true;
            //$state.go('#AfyaServices/ecosystem-adaptation');
        }

    }]).directive('packagePostRepeatDirective', function() {
        return function(scope, element, attrs) {
            console.log('directive');
            // scope.disablePackageContainerIfActivePackage();  //TO DO: MAY NOT BE REQUIRED

            // calculate the Total amount to update the same in display
            scope.calculateAmount(scope.package);

            // get provider data
            scope.getProviderData(function(providerData){
                scope.$evalAsync(function(){
                    enableDisableInputFields(scope, scope.subscribeStep, scope.package);
                });
            });

            // retrieve Active Service Subscription data and bind the same
            scope.$evalAsync(function(){
                var package = scope.package;
                var serviceType = null;

                // workout the required Service Type
                if(package.packageCategory == 'SERVICE'){
                    serviceType = 'NOTIFICATION';   // every 'Service Package', will have a Notification Service
                }else if(package.packageCategory == 'TRAINING'){
                    serviceType = 'TRAINING';       // a 'Training Package' will only have a Training Service
                }else{
                    serviceType = null;             // should never be here, unless a new Package-Category is introduced
                }
                // sanity check
                if(serviceType == null)
                    return;
                // prepare options to retrieve subscription history
                var options = {
                    package : package,
                    serviceType: serviceType,
                    isSubscriptionActive: true,
                    successCB: function(subscriptionHistoryData){
                        if(serviceType == 'NOTIFICATION')
                            package.subscribedActiveNotificationServices = subscriptionHistoryData;
                        else (serviceType == 'TRAINING')
                            package.subscribedActiveServices = subscriptionHistoryData;
                    }
                };
                // retrieve data
                scope.getUserSubscriptionHistory(options);
            });

            // accordian event handlers (to show/hide the detail Panels)
            scope.$evalAsync(function() {
                $('.panel-heading span.clickable').off("click").on("click", function (e) {
                    if ($(this).hasClass('panel-collapsed')) {
                        // expand the panel
                        $(this).parents('.panel').find('.panel-body1').slideDown();
                        $(this).removeClass('panel-collapsed');
                        $(this).find('i').removeClass('fa-plus').addClass('fa-minus');
                    }
                    else {
                        // collapse the panel
                        $(this).parents('.panel').find('.panel-body1').slideUp();
                        $(this).addClass('panel-collapsed');
                        $(this).find('i').removeClass('fa-minus').addClass('fa-plus');
                    }
                });
            });
        };
    });


function enableDisableInputFieldsAllPacks(scope, step){
    // service packages
    $.each(scope.packages, function(idx,pack){
        enableDisableInputFields(scope, scope.subscribeStep, pack);
    });
    // training packages
    $.each(scope.trainingPackages, function(idx,pack){
        enableDisableInputFields(scope, scope.subscribeStep, pack);
    });
};

function enableDisableInputFields(scope, step, pack){
    var providerData = scope.providerData;
    switch(step){
        case 'STEP_1': {
             /*if(providerData.subscriptionTrail == true) {// is VISITOR with Trial subscription
                pack.disablePackageSelection = true;    // temporary fix, to disable Package selection for trial users, until provision is made to change users count with corresponding payment
                pack.disableAllServicesButNotification = false;
                pack.disableTrainingPackage = false;
                pack.disableNotificationService = false;
            }else */ if(providerData.memberType == 'PREMIUM'){   // already subscribed to a Pack
                pack.disablePackageSelection = true;
                pack.disableAllServicesButNotification = false;
                pack.disableTrainingPackage = false;  // scope.hasTrainingSubscritpion ? true : false;
                pack.disableNotificationService = false;  // scope.hasNotificationSubscritpion ? true : false;
            }else{ // is VISITOR with-out Trial subscription
                pack.disablePackageSelection = false;
                pack.disableAllServicesButNotification = false;
                pack.disableTrainingPackage = false;
                pack.disableNotificationService = false;
            }
        }
            break;
        case 'STEP_2': {
            pack.disablePackageSelection = true;
            pack.disableAllServicesButNotification = true;
            pack.disableTrainingPackage = true;
            pack.disableNotificationService = true;
        }
            break;
        case 'STEP_3':
            break;
    }
}

function collapseAllPanels(){
    $('.panel-heading span.clickable').each(function(){
        // collapse the panel
        $(this).parents('.panel').find('.panel-body1').slideUp();
        $(this).addClass('panel-collapsed');
        $(this).find('i').removeClass('fa-minus').addClass('fa-plus');
    });
};

function expandAllPanels(){
    $('.panel-heading span.clickable').each(function(){
        // expand the panel
        $(this).parents('.panel').find('.panel-body1').slideDown();
        $(this).removeClass('panel-collapsed');
        $(this).find('i').removeClass('fa-plus').addClass('fa-minus');
    });
};

function getFormatedDate(date) { // DD-MM-YYYY
    var year = date.getFullYear();
    var month = (date.getMonth() + 1);
    if (month < 10)
        month = '0' + month;
    var date = date.getDate();
    if (date < 10)
        date = '0' + date;

    var formatedDate = date + '-' + month + '-' + year;
    return formatedDate;
};
