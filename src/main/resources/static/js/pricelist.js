/**
 * Created by pradyumna on 11-06-2015.
 */

// angular.module('aafyaPriceListModule', ['ngMessages'])
afyaApp.controller('PriceController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.trail = "N";
        // $scope.showPackages = true;
        $scope.selectedPackage = null;
        $scope.portalBaseUrl = "/afya-portal";
        $scope.subscribeStep = "STEP_1";
        $scope.providerData = null;
        $scope.chkTermsAndConditionsChecked = false;
        $scope.chkViewedTrainingSchedule = false;
        $scope.filterServicePackId = getParameterValueByProperty('packageId');
        $scope.filterPriceOption = getParameterValueByProperty('period');
        $scope.currency = 'KD'; // TO DO: take this from DB

        $scope.username = readCookie("username");
        $scope.registerData = {};
        $scope.SMSSenderId = "";

        // status
        $scope.paymentStatus = "";
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

        angular.element(document).ready(function(){
            $scope.getProviderData(function(providerData){
                if(providerData != null) {
                    $scope.SMSSenderId = providerData.smsSenderId;
                }
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
            package.expiryDays = package.priceOption == 'HALF_YEARLY' ? 180 : 365;
            package.expiryDate = new Date(package.subscriptionDate);
            //package.expiryDate.setDate(package.expiryDate.getDate() + package.expiryDays);
            //package.expiryDate.setMonth(package.expiryDate.getMonth() + (package.priceOption == 'ANNUALLY' ? 12 : 6));
            package.expiryDate.setDate(package.expiryDate.getDate() + package.expiryDays);
        }

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

            if(validateAndPickSelectedPackage('N','Y') == false)    // quotation is not for trial
                return false;

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
                $.cookie("packSubscribeQuoteParams",JSON.stringify(quoteParams));

                // open the quotation page in new window
                $window.open('subscription_quotation.html', 'quotation');
            };
        };

        $scope.getProviderData = function(successCB, forceUpdate){
            if($scope.providerData == null || forceUpdate == true) {
                var username = $.cookie("username");
                $http.get($scope.portalBaseUrl + '/anon/getProviderByUsername?username=' + username).success(function (providerData) {
                    $scope.providerData = providerData;
                    successCB($scope.providerData);
                }).error(function (data) {
                    $scope.providerData = null;
                    sweetAlert("error fetching data");
                });
            }else{
                successCB($scope.providerData);
            }
        }

        $scope.getUserSubscriptionHistory = function(options){
            var username = $.cookie("username");
            $http.get($scope.portalBaseUrl + '/anon/getUserSubscriptionHistory?username=' + username + '&serviceType=' + options.serviceType
                    + '&packageId=' + options.package.packageId + '&packageCategory=' + options.package.packageCategory
                    + '&isSubscriptionActive=' + options.isSubscriptionActive).success(function (subscriptionHistoryData) {
                options.successCB(subscriptionHistoryData);
            }).error(function (data) {
                $scope.providerData = null;
                //sweetAlert("error fetching Subscription History Data");
                sweetAlert({ title: "Sorry", text: 'We have encountered some issues please retry', type: "error" });
            });
        }

        $scope.submitSubscription = function(trail) {
            $scope.trail = trail;
            /*
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

            $scope.selectedPackage = _selectedPackage;*/


            if(validateAndPickSelectedPackage($scope.trail,'N') == false)
                return false;

            $scope.getProviderData(function(providerData){
                if(providerData != null && providerData.memberType == 'VISITOR') {
                    getAllPremiumMember();
                    $("#ProviderMemberRegistration").modal("show");
                }else{
                    submitSubscriptionInternal();
                }
            });

            /*
            $http.post('subscribe_package', _packages).success(function (data,status,headers,config) {

                if (data.message == "success") {
                    alert("Success");
                }
            });*/
        };

        function submitSubscriptionInternal(){
            if(validateAndPickSelectedPackage($scope.trail,'N') == false)
                return false;

            if($scope.trail == "N") {
                // initiate Payment (after payment the subscription would be saved)

                // check if this is a simple-save or an Subscription
                if($scope.totalAmount == 0
                    && $scope.hasActivePackage == true
                    && ($scope.providerData != undefined && $scope.providerData.subscriptionTrail == false)){
                    // simple save
                    subscribePackage(function(){
                        showSuccessMessage("Saved Successfully!");
                    });
                }else{
                    // subscriotion
                    makePaymentAndSubscribe();
                }
            }else{
                subscribePackage(function(){
                    showSuccessMessage("Thank you for Choosing Afya Services : Enjoy your FREE Trail for 30 Days");
                });
            }
        }

        function validateAndPickSelectedPackage(trial, quote){
            $scope.trail = trial;
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
                sweetAlert("Important, please select the service package for auto generation of the Quote.");
                return false;
            } else if (service_activated_count > 1) {
                sweetAlert("Only one Service Package can be active. ["+activated_packages_service+"]");
                return false;
            }

            // validate activated Notification Services
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
            if($scope.hasActivePackage == false || ($scope.providerData != undefined && $scope.providerData.subscriptionTrail == true)){
                if($scope.totalAmount == 0){
                    sweetAlert("Please select a Package/Service to Subscribe.");
                    return false;
                }
            }

            // check Terms and Conditions
            if(($scope.chkTermsAndConditionsChecked == false || $scope.chkViewedTrainingSchedule == false) && quote == 'N'){
                sweetAlert("Please accept the Terms & Conditions to continue");
                return false;
            }

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

        $scope.goToStep2 = function(){
            if(validateAndPickSelectedPackage('N','N') == false)    // quotation is not for trial
                return false;

            $scope.getProviderData(function(providerData){
                if(providerData != null && providerData.memberType == 'VISITOR') {
                    getAllPremiumMember();
                    $("#ProviderMemberRegistration").modal("show");
                }else{
                    goToStep2Internal();
                }
            });
        };

        function goToStep2Internal(){
            if(validateAndPickSelectedPackage('N','N') == false)    // quotation is not for trial
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
            window.location.href = "#";
            window.location.hash = "#regi-afya-slider";
            /*_package.disableAllServicesButNotification = true;
            _package.disableTrainingPackage = true;
            _package.disableNotificationService = true;*/
        };

        $scope.goToStep3 = function(){
            $scope.subscribeStep = 'STEP_3';
            $('.breadcrumb a').removeClass('active');
            $('.breadcrumb a:nth-child(3)').addClass('active');
            $scope.paymentStatus = "";
            // scroll to top
            window.location.href = "#";
            window.location.hash = "#regi-afya-slider";
            // $scope.submitSubscription('N');
            submitSubscriptionInternal();
        };

        /*$scope.disablePackageContainerIfActivePackage = function() {
            if($scope.hasActivePackage == false)
                $('.pricePackage :input').attr('disabled', false);
            else
                $('.pricePackage :input').attr('disabled', true);
        };*/

        /* Post Package to subscribe by updating DB */
        function subscribePackage(successCB){
            var _packages = [];
            for(i=0; i <$scope.packages.length; i++) {
                var _package = $scope.packages[i];
                _package.trail = $scope.trail;
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
                $scope.trainingPackages[i].trail = $scope.trail;
                _packages.push($scope.trainingPackages[i]);
            }

            $http.post('/afya-portal/anon/subscribe_package', _packages).success(function (data,status,headers,config) {
                if (data.message == "success") {
                    // force update to read the provider data
                    $scope.getProviderData(function(){}, true);
                    // alert("Success");
                    // GO TO Step-2
                    // $('.pricePackage :input').attr('disabled', true);
                    //
                    $('.breadcrumb a:nth-child(2)').addClass('active');
                    $('.breadcrumb a:nth-child(1)').removeClass('active');

                    successCB();
                }
            });
        };

        $scope.onRegistrationClick = function() {
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
                if($scope.trail == 'Y'){
                    submitSubscriptionInternal();
                }
                else {
                    goToStep2Internal();
                }
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        }


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
            if($scope.hasActivePackage == false || ($scope.providerData != undefined && $scope.providerData.subscriptionTrail == true)) {  // if package is already active (add-on subscription) then skip accumulating package prices
                for (i = 0; i < $scope.packages.length; i++) {
                    var package = $scope.packages[i];
                    if ((package.amount == null) || (parseFloat(package.amount) == 0.0))
                        continue;
                    if (package.activated == 1) {
                        $scope.totalAmount = $scope.totalAmount + parseFloat(package.amount);
                    }
                }
            }

            if($scope.hasNotificationSubscritpion == false) {
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
            }

            if($scope.hasTrainingSubscritpion == false) {
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
            }
            $scope.totalAmount = Number($scope.totalAmount).toFixed(3);
        };

        $scope.isloggedIn = false;
        $scope.packages = [];
        $scope.trainingPackages = [];
        $scope.totalAmount = 0.000;

        $scope.getProviderData(function(){
            $http.get("/afya-portal/anon/get_packages").success(function(data){ populatePackages(data);});
        });

        function populatePackages(data){
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
                       package.expiryDays = package.priceOption == 'HALF_YEARLY' ? 180 : 365;
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

                       // if(package.activated == true)
                       // $scope.hasActivePackage = true;
                        if (packageDto.services != null) {
                             for (j = 0; j < packageDto.services.length; j++) {
                                  var serviceDto = packageDto.services[j];
                                 var service = {};
                                  service.serviceId = serviceDto.serviceId;
                                  service.serviceName = serviceDto.serviceName;
                                  service.activated = 0;
                                  if (serviceDto.serviceType =="TOP_LEVEL") {
                                       package.services.push(service);
                                  } else if (serviceDto.serviceType =="PATIENT") {
                                       if (serviceDto.activated == null) {
                                           service.activated = 1;
                                       } else {
                                           service.activated = serviceDto.activated;
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
                                      if(service.activated == 1)
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
                               if(service.activated == 1)
                                   $scope.hasTrainingSubscritpion = true;
                               service.amount = 0;  // Number(serviceDto.amount).toFixed(3);   // service.amount = 0;  // RESETTING AS USER CAN BUY THIS AS AN ADD-ON
                               package.services.push(service);
                           }
                       }
                       $scope.trainingPackages.push(package);
                   }
              }
              console.log($scope.packages);
              console.log($scope.trainingPackages);
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

        $scope.onDisplaySMSSenderClick = function(){
            $("#SMSSender").modal("show");
        };

        $scope.onUpdateSMSSenderClick = function(){
            $http.get($scope.portalBaseUrl + '/anon/getProviderByUsername?username=' + $scope.username).success(function(data){
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
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        };

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
        }

        // Payment Mode selection click handler
        $scope.onPaymentModeSelectionClick = function (mode) {
            // alert('Option Selected = ' + mode);
            var options = {
                afyaId: '', // this is not available
                username: $.cookie('username'),
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
        }

        function paymentErrorCB(errMsg) {
            $scope.paymentStatus = "Error";
            sweetAlert(errMsg);
        }

        function paymentPollCallbackCB(result) {
            //alert('success payment');
            console.log("Poll Status: " + result.status);
            //BookAppointmentWithSuccessPayment()

            // Take actions based on Payment Status
            if ((result.status == "Declined") || (result.status == "Timed Out")) { // NOTE: THIS IS FOR PRODUCTION
                // if(result.status == "Declined"){ // NOTE: THIS IS FOR TESTING
                // $("#modalConfirmation").modal("show");
                $scope.paymentStatus = "Declined";

            }else if (result.status == "Success") {  // NOTE: THIS IS FOR PRODUCTION
                //else if(result.status == "Success" || result.status == "Timed Out"){ // NOTE: THIS IS FOR TESTING
                // $("#modalConfirmation").modal("hide");
                $scope.paymentStatus = "Subscribing Package...";
                // perform Final steps after a Successfull Payment
                subscribePackage(function(){
                    showSuccessMessage("Subscribed Successfully!");
                });
                //if(mainOption == "OPT_APPOINTMENT" || mainOption == "OPT_APPOINTMENT_REQUEST" || mainOption == "OPT_TELE_CONSULTATION" || mainOption == "OPT_HOME_VISIT")
                /*if ($scope.appointmentVisitType == "Premium Visit" || $scope.appointmentVisitType == "Tele Consultation Visit" || $scope.appointmentVisitType == "Home Visit") {
                    BookAppointmentWithSuccessPayment();
                }*/
            }else{
                $scope.paymentStatus = "Waiting...";
            }
            //else if(mainOption == "OPT_HOME_PHARMACY"){}
        }

        function showSuccessMessage(message){
            sweetAlert({
                    title: message, // "Subscribed Successfully!",
                    text: "",
                    type: "info",
                    showCancelButton: false,
                    confirmButtonText: "OK",
                    closeOnConfirm: false
                },
                function(){
                    $window.location.href = "web_pages/member_area/provider/Provider_account.html";
                });
        }

        ////////////// PAYMENT GATEWAY INTEGRATION RELATED SERVICE CALLS /////////////////
        if (window.afyaApp.svc == undefined)
            window.afyaApp.svc = {};
        window.afyaApp.svc.addPaymentTransactionForAppointment = function (settings) {
            // make service call
            $http.post($scope.portalBaseUrl + '/anon/addPaymentTransactionForAppointment?transactionType=' + settings.transactionType + "&transactionAmount=" + settings.transactionAmount + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId + "&apptSlot=" + settings.apptSlot + "&packageId=" + settings.packageId + "&username=" + settings.username + "&processingFees=" + settings.processingFees + "&payerType=" + settings.payerType + "&paymentChannel=" + settings.paymentChannel).success(function (data) {
                settings.success(data);
            }).error(function (data, status, headers, config) {
                sweetAlert('Error : addPaymentTransactionForAppointment');
            });

            //        afyaApp.config.baseUrlForSearch + "addPaymentTransactionForAppointment?transactionType=" + settings.transactionType + "&transactionAmount=" + settings.transactionAmount
            //    + "&transactionTimestamp=" + settings.transactionTimestamp + "&isysTrackingRef=" + settings.isysTrackingRef + "&afyaId=" + settings.afyaId + "&apptClinicId=" + settings.apptClinicId + "&apptDoctorId=" + settings.apptDoctorId
            //    + "&apptSlot=" + settings.apptSlot

        };

        //updatePaymentTransactionWithIsysStatus
        window.afyaApp.svc.updatePaymentTransactionWithIsysStatus = function (settings) {
            // make service call
            $http.post($scope.portalBaseUrl + '/anon/updatePaymentTransactionWithIsysStatus?paymentId=' + settings.paymentId + "&isysTrackingRef=" + settings.isysTrackingRef + "&isysPaymentStatus=" + settings.isysPaymentStatus + "&isysPaymentStatusTimestamp=" + settings.isysPaymentStatusTimestamp + "&isysHttpResponseStatus=" + settings.isysHttpResponseStatus).success(function (data) {
                settings.success(data);
            }).error(function (data, status, headers, config) {
                sweetAlert('Error : updatePaymentTransactionWithIsysStatus');
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
        }

        function getAllPremiumMember()
        {
            $http.get($scope.portalBaseUrl + '/anon/getAllPremiumMember').success(function (data) {
                $.each(data, function(index, value) {
                    $('<option>').val(value.username).text(value.firstName + ' ' + value.lastName).appendTo('#RegRecommendedBy');
                });
            }).error(function (data, status, headers, config) {
                console.log("Error while Getting Premium Member!" + status);
            });
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
            if(providerData.memberType == 'PREMIUM'){   // already subscribed to a Pack
                pack.disablePackageSelection = true;
                pack.disableAllServicesButNotification = false;
                pack.disableTrainingPackage = false;  // scope.hasTrainingSubscritpion ? true : false;
                pack.disableNotificationService = false;  // scope.hasNotificationSubscritpion ? true : false;
            }else if(providerData.subscriptionTrail == true) {// is VISITOR with Trial subscription
                pack.disablePackageSelection = false;
                pack.disableAllServicesButNotification = false;
                pack.disableTrainingPackage = false;
                pack.disableNotificationService = false;
            }
            else{ // is VISITOR with-out Trial subscription
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