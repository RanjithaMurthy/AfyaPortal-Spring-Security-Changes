/**
 * Created by pradyumna on 11-06-2015.
 */

//angular.module('ProviderNonSubscribeModule', ['ngMessages'])
afyaApp.controller('ProviderNonSubscribeController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.trail = "N";
        $scope.memberDetail = [];
        $scope.MemberType = null;

        angular.element(document).ready(function(){
            assignWelcomeUserName();
            var username = readCookie("username");
            var data = getProviderByUsername();
            if(data != null)
            {
            //$http.get('/afya-portal/anon/getProviderByUsername?username='+username).success(function(data){
                $scope.memberDetail = data;
                $scope.MemberType = data.memberType;// assign MemberType Scope to Display Visitor Message
                populateData();
            //}).error(function(data){
            //    alert("error fetching data");
            //});
            }
            else
            {
                sweetAlert("error fetching data");
            }
        });

        $scope.calculateAmount = function(pkg) {
            if (pkg.priceOption == "HALF_YEARLY") {
                pkg.amount = Number(pkg.halfYearlyPricePerUom * pkg.numberOfUsers).toFixed(3);
            } else {
                pkg.amount = Number(pkg.annuallyPricePerUom * pkg.numberOfUsers).toFixed(3);
            }
            $scope.calculateTotalAmount();
        };

        $scope.calculateNotificationAmount = function(service) {

            service.amount = Number(service.packs*service.rate).toFixed(3);

            $scope.calculateTotalAmount();
        };
        $scope.calculateTrainingAmount = function(service) {

            service.amount = Number(service.numberOfHours*service.rate).toFixed(3);

            $scope.calculateTotalAmount();
        };

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
                 package.trail = trail;
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
        };

        $scope.calculateTotalAmount = function() {
            $scope.totalAmount = 0.0;
            for (i = 0; i < $scope.packages.length; i++) {
                var package = $scope.packages[i];
                if ((package.amount == null) || (parseFloat(package.amount) == 0.0))
                    continue;
                if (package.activated == 1) {
                    $scope.totalAmount = $scope.totalAmount + parseFloat(package.amount);
                }
            }

            for (i = 0; i < $scope.packages.length; i++) {
                var package = $scope.packages[i];
                for (j = 0; j < package.notificationServices.length; j++) {
                    var service =  package.notificationServices[j];
                    if ((service.amount == null) || (parseFloat(service.amount) == 0.0))
                        continue;
                    if (service.activated == 1) {
                        $scope.totalAmount = $scope.totalAmount + parseFloat(service.amount);
                    }
                }

            }

            for (i = 0; i < $scope.trainingPackages.length; i++) {
                var package = $scope.trainingPackages[i];
                for (j = 0; j < package.services.length; j++) {
                    var service =  package.services[j];
                    if ((service.amount == null) || (parseFloat(service.amount) == 0.0))
                        continue;
                    if (service.activated == 1) {
                        $scope.totalAmount = $scope.totalAmount + parseFloat(service.amount);
                    }
                }
            }
            $scope.totalAmount = Number($scope.totalAmount).toFixed(3);
        };

        $scope.isloggedIn = false;
        $scope.packages = [];
        $scope.trainingPackages = [];
        $scope.totalAmount = 0.000;

        function populateData()
        {
            $http.get("/afya-portal/anon/get_packages").success(function(data){
                  var packageList = data.packages;
                  for (i = 0; i < packageList.length; i++) {
                       var packageDto = packageList[i];
                       if(!packageDto.activated && packageDto.packageType.toUpperCase() == $scope.memberDetail.providerType.toUpperCase())
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
                  $scope.calculateTotalAmount();
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
        }

    }]);


