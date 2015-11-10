/**
 * Created by pradyumna on 11-06-2015.
 */

//angular.module('ProviderSubscribeModule', ['ngMessages'])
afyaApp.controller('ProviderSubscribeController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.trail = "N";
        $scope.isloggedIn = false;
        $scope.packages = [];
        $scope.trainingPackages = [];
        $scope.memberDetail = [];
        $scope.totalAmount = 0.000;
        $scope.subscribedTrainingServices = null;
        $scope.notificationUsage = {totalSmsCount: null, sentSmsCount: null};
        $scope.showFreeTrialMessage = false;

        angular.element(document).ready(function(){
            assignWelcomeUserName();
            var username = readCookie("username");
            var data = getProviderByUsername();
            if(data != null)
            {
            //$http.get('/afya-portal/anon/getProviderByUsername?username='+username).success(function(data){
                $scope.memberDetail = data;
                populateData();
            //}).error(function(data){
            //    alert("error fetching data");
            //});
            }
            else
            {
                //sweetAlert("error fetching data");
                sweetAlert({ title: "", text: 'Error fetching data', type: "error" });
            }
        });

        function populateData()
        {
            $http.get("/afya-portal/anon/get_packages").success(function(data){

                  // by default show the trial message (assuming no active subscriptions)
                  $scope.showFreeTrialMessage = true;

                  var packageList = data.packages;
                  for (i = 0; i < packageList.length; i++) {
                       var packageDto = packageList[i];
                       if(packageDto.activated && packageDto.packageType.toUpperCase() == $scope.memberDetail.providerType.toUpperCase())
                       {
                           if (packageDto.packageCategory == "SERVICE")
                           {
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
                                if (packageDto.numberOfUsers != null)
                                   package.numberOfUsers = packageDto.numberOfUsers;
                                else
                                   package.numberOfUsers = 0;
                                package.amount = Number(packageDto.amount).toFixed(3);
                                package.activated = packageDto.activated;
                                package.packageCategory = packageDto.packageCategory;
                                package.services = [];
                                package.patientServices = [];
                                package.providerServices = [];
                                package.notificationServices = [];
                                package.totalServices = packageDto.totalServices;
                                if(packageDto.subscriptionDate != null && packageDto.subscriptionDate != ''){
                                   package.subscriptionDate =  packageDto.subscriptionDate;
                                }else{
                                   package.subscriptionDate = new Date();
                                }
                               package.expiryDays = deriveExpiryDays(package);// package.priceOption == 'HALF_YEARLY' ? 180 : 365;
                               // expiry Date
                               var expiryDate = new Date(package.subscriptionDate);
                               // expiryDate.setDate(expiryDate.getDate() + package.expiryDays);
                               //expiryDate.setMonth(expiryDate.getMonth() + (package.priceOption == 'ANNUALLY' ? 12 : 6));
                               expiryDate.setDate(expiryDate.getDate() + package.expiryDays);
                               package.expiryDate = expiryDate;

                                // compute the number of days left for expiry
                                var millisBetween = package.expiryDate.getTime() - (new Date()).getTime();
                                var millisecondsPerDay = 1000 * 60 * 60 * 24;
                                var days = millisBetween/millisecondsPerDay;

                                // if package is active and has crossed the trial period, then hide the trial message
                                //if(package.activated == 1 && ((package.expiryDays - days) > package.trialPeriodDays)){
                                if(package.isTrialExpired == true){
                                    $scope.showFreeTrialMessage = false;
                                }

                               if (packageDto.services != null) {
                                     for (j = 0; j < packageDto.services.length; j++) {
                                          var serviceDto = packageDto.services[j];
                                         var service = {};
                                          service.serviceId = serviceDto.serviceId;
                                          service.serviceName = serviceDto.serviceName;
                                          service.serviceDisplayName = serviceDto.serviceDisplayName;
                                          service.serviceSequence = serviceDto.serviceSequence;
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
                                              service.packs = serviceDto.packs;
                                              service.expireDays = serviceDto.expireDays;
                                              service.numberOfSMS = serviceDto.numberOfSMS;
                                              service.rate = Number(serviceDto.rate).toFixed(3);
                                              service.amount = Number(serviceDto.amount).toFixed(3);
                                              service.activated = serviceDto.activated;
                                              package.notificationServices.push(service);
                                          }
                                     }
                               }
                                $scope.packages.push(package);

                           }
                           else  if (packageDto.packageCategory == "TRAINING")
                           {
                                var  package = {};
                                package.packageId = packageDto.packageId;
                                package.packageName = packageDto.packageName;
                                package.solutionName = packageDto.solutionName;
                                package.packageCategory = packageDto.packageCategory;
                                package.services = [];
                                if (packageDto.services != null) {
                                    for (j = 0; j < packageDto.services.length; j++) {
                                        var serviceDto = packageDto.services[j];
                                        var service = {};
                                        service.serviceId = serviceDto.serviceId;
                                        service.serviceName = serviceDto.serviceName;
                                        service.numberOfHours = serviceDto.numberOfHours;
                                        service.rate = Number(serviceDto.rate).toFixed(3);
                                        service.activated = serviceDto.activated;
                                        service.amount = Number(serviceDto.amount).toFixed(3);
                                        package.services.push(service);
                                    }
                                }
                                $scope.trainingPackages.push(package);

                           }
                       }
                  }
                  console.log($scope.packages);
                  console.log($scope.trainingPackages);

                    // Retrieve all subscriptions for Training Services
                    var trainingOptions = {
                        serviceType: 'TRAINING',
                        isSubscriptionActive: null,
                        successCB: function(subscriptionHistoryData){
                            $scope.subscribedTrainingServices = subscriptionHistoryData;
                        }
                    };
                    // retrieve data
                    $scope.getUserSubscriptionHistory(trainingOptions);
                    $scope.getPackageSubscriptionUsage();

                  setTimeout(function() {
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
                  },4000);
             });
         };

        // Note: Changes should be synced with Portal code to Subscribe Package and Services-Susbscribed page
        function deriveExpiryDays(package){
             // TO DO: Add Trial Period only if this is the First Package Subscriptoin
             return (package.priceOption == 'HALF_YEARLY' ? 180 : 365) + (package.trialPeriodDays || 0);
        }

        $scope.getUserSubscriptionHistory = function(options){
            var username = readCookie("username");
            options.package = options.package || {};
            $http.get('/afya-portal/getUserSubscriptionHistory?serviceType=' + options.serviceType
                    + '&packageId=' + (options.package.packageId || '') + '&packageCategory=' + (options.package.packageCategory || '')
                    + '&isSubscriptionActive=' + (options.isSubscriptionActive || '')).success(function (subscriptionHistoryData) {
                options.successCB(subscriptionHistoryData);
            }).error(function (data) {
                $scope.providerData = null;
                //sweetAlert("error fetching Subscription History Data");
                sweetAlert({ title: "Sorry", text: 'We have encountered some issues please retry', type: "error" });
            });
        };

        $scope.getPackageSubscriptionUsage = function(){
            var username = readCookie("username");
            $http.get('/afya-portal/getPackageNotificationUsageFromProviderUsername').success(function (notificationUsageData) {
                $scope.notificationUsage = notificationUsageData[0];
            }).error(function (data) {
                $scope.notificationUsage = {totalSmsCount: null, sentSmsCount: null};
                sweetAlert({ title: "Sorry", text: 'We have encountered issue while fetching data. Please retry after some time.', type: "error" });
            });
        };

    }]).directive('packagePostRepeatDirective', function() {
               return function(scope, element, attrs) {
                   console.log('directive');

                   // retrieve Active Service Subscription data and bind the same
                   scope.$evalAsync(function(){
                       var package = scope.package;
                       var serviceType = null;

                       // workout the required Service Type
                       if(package.packageCategory == 'SERVICE'){
                           serviceType = 'NOTIFICATION';   // every 'Service Package', will have a Notification Service
                       //}else if(package.packageCategory == 'TRAINING'){
                       //    serviceType = 'TRAINING';       // a 'Training Package' will only have a Training Service
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
               };
           });


