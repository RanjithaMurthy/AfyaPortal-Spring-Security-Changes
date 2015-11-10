/**
 * Created  on 08/18/2015.
 */

CareProviderServiceModule = angular.module('CareProviderServiceModule', ['ngSanitize']).filter('unique', function () {

     return function (items, filterOn) {

       if (filterOn === false) {
         return items;
       }

       if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
         var hashCheck = {}, newItems = [];

         var extractValueToCompare = function (item) {
           if (angular.isObject(item) && angular.isString(filterOn)) {
             return item[filterOn];
           } else {
             return item;
           }
         };

         angular.forEach(items, function (item) {
           var valueToCheck, isDuplicate = false;

           for (var i = 0; i < newItems.length; i++) {
             if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
               isDuplicate = true;
               break;
             }
           }
           if (!isDuplicate) {
             newItems.push(item);
           }

         });
         items = newItems;
       }
       return items;
     };
   });

CareProviderServiceModule.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
    delete $httpProvider.defaults.headers.post['Content-type'];
}
]);

CareProviderServiceModule.controller('CareProviderServices', ['$scope', '$http', '$window', function($scope, $http, $window){
    $scope.providerServiceList = [];
    //$scope.pharmacyServiceList = [];
    $scope.PackageCompareList = [];
    $scope.packageType = "CLINIC";
    $scope.facilityId = null;
    $scope.memberProfileObj = [];
    // data to show in HTML in comparative View
    $scope.PackageCompareDataHTML = [];
    $scope.PackageDetailsData = [];
    // login properties
    $scope.postLoginRedirect = null; // VIEWDETAIL | SUBSCRIBE
    $scope.postLoginObject = null;

    angular.element(document).ready(function(){
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
            if(username != undefined || username != null){
                $scope.username = username;
                $http.get($scope.portalBaseURL + '/anon/getProviderByUsername?username=' + username).success(function (providerData) {
                //$http.get('/afya-portal/anon/getPatientByUsername?username=' + username).success(function(data){
                    $scope.memberProfileObj = providerData;
                    // remove this once its get by patient/provider
                    $scope.facilityId = (providerData.facilityId == undefined)? '' : providerData.facilityId;//"thekuwaitclinic";// "CD46BE59-3EFC-410D-AEAE-3ECAC941934F";
                    GetServicePackagesForClinicAndPharmacy();
                    console.log(providerData);
                }).error(function(providerData){
                    //sweetAlert("error fetching data");
                });
            }
            else{
                GetServicePackagesForClinicAndPharmacy();
            }



        });
    });

    function GetServicePackagesForClinicAndPharmacy (){
        $http.get('/afya-portal/anon/getProviderServices?packageType='+ $scope.packageType + '&facilityId=' + $scope.facilityId).success(function(data){
            clearList();
            // prepare list for Periodwise(Annually/Halfyearly)
            //ExtractServiceByPeriodly(data);
            if(data.length > 0){
                // Calculate Total PackageServiceCount by all the services
                data = $.map($.grep(data, function (obj, id) {
                    return (obj.PackageId != "");
                }), function (obj, id) {var obj = $.extend({}, obj);
                    obj.TotalPackageServiceDisplayCount = obj.TopLevelServiceCount + obj.PatientServiceCount + obj.ProviderServiceCount + obj.TrainingServiceCount + obj.NotificationServiceCount;
                    return obj;
                });
            }
            $scope.providerServiceList = data;
            //alert(data.TopLevelServiceCount + data.PatientServiceCount + data.ProviderServiceCount + data.TrainingServiceCount + data.NotificationServiceCount);
        }).error(function(){
            sweetAlert("error fetching data");
        });
    }

    // proceed to subscription package click
    $scope.onProceedSubscriptionClick = function(clinicService){
        $scope.postLoginRedirect = "SUBSCRIBE"; // VIEWDETAIL | SUBSCRIBE
        $scope.postLoginObject = clinicService;
        // redirect after login
        if(ValidateUserLoginStatus()){
            ProceedSubscriptionClick();
        }
        else{
            $('#myLogin').modal("show");
            return false;
        }
    }

    function ProceedSubscriptionClick(){
        var clinicService = $scope.postLoginObject;
        //console.log(clinicService);
        //window.location.href = "/afya-portal/pricing-plan.html?packageType=" + $scope.packageType + "&packageId=" + clinicService.PackageId + "&period=" + clinicService.Period;
        window.location.href = "/afya-portal/pricing-plan.html?packageType=" + $scope.packageType + "&packageId=" + clinicService.PackageId;
        //sweetAlert('Package Subscribed Successfully.');
    }

    // view service details for selected packages
    $scope.onServiceDetailsClick = function(clinicService, serviceType, itemCount){
        if(itemCount <= 0){
            sweetAlert({ title: "Info", text: "No Details Available.", type: "info" });
            return false;
        }
        $http.get('/afya-portal/anon/getProviderServiceCompareList?packageIds='+ clinicService.PackageId).success(function(data){
            //sweetAlert('success');
            //$scope.PackageDetailsData = data;
            $scope.PackageDetailsData = $.map($.grep(data, function (obj, id) {
                        return (obj.ServiceValue == 1 && obj.PackageName == clinicService.PackageName && obj.ServiceType == serviceType);
                    }), function (obj, id) { return obj; });
            console.log($scope.PackageDetailsData);

            if($scope.PackageDetailsData.length > 0){
                // show in modal Service Details
                $("#modalServiceDetails").modal("show");
            }
        }).error(function(){
            sweetAlert({ title: "Error", text: "error fetching data", type: "error" });
        });
    }

    //onViewDetailsClick
    $scope.onViewDetailsClick = function(objService, index){
        $scope.postLoginRedirect = "VIEWDETAIL"; // VIEWDETAIL | SUBSCRIBE
        $scope.postLoginObject = objService;

        if(ValidateUserLoginStatus()){
            ViewDetailsClick();
        }
        else{
            $('#myLogin').modal("show");
            //$('#member_login').modal("show");
            return false;
        }

    }

    function ViewDetailsClick(){
        var objService = $scope.postLoginObject;
        //console.log(objService);
        $scope.serviceFullDetails = objService;

        $http.get('/afya-portal/anon/getProviderServiceItemDetails?packageId='+ $scope.serviceFullDetails.PackageId).success(function(data){
            $scope.PackageFullDetailsData = data;
            // get unique services
            $scope.UniqueServices = [];
            $scope.UniqueServices = $scope.returnUnique($scope.PackageFullDetailsData, "ServiceName");
            console.log($scope.UniqueServices);
            // Create the main object
            $scope.PackageFullDetailsResult = [];
            /*  SMART SOLUTIONS SERVICES [TOP_LEVEL], SMART SERVICES for PATIENTS [PATIENT],
                    SMART SERVICES for OTHER CARE PROVIDERS [PROVIDER], NOTIFICATION SERVICES [NOTIFICATION], SELF LEARNING HOURS [TRAINING]*/
            $scope.PackageFullDetailsResult.SmartSolutionServiceItems = CreateIndividualKeyServiceObject("TOP_LEVEL", $scope.PackageFullDetailsData);
            $scope.PackageFullDetailsResult.SmartServicesPatientItems = CreateIndividualKeyServiceObject("PATIENT", $scope.PackageFullDetailsData);
            $scope.PackageFullDetailsResult.SmartServicesOtherCareProviderItems = CreateIndividualKeyServiceObject("PROVIDER", $scope.PackageFullDetailsData);
            $scope.PackageFullDetailsResult.NotificationServiceItems = CreateIndividualKeyServiceObject("NOTIFICATION", $scope.PackageFullDetailsData);
            $scope.PackageFullDetailsResult.SelfLearningHourItems = CreateIndividualKeyServiceObject("TRAINING", $scope.PackageFullDetailsData);
            //$scope.PackageFullDetailsResult.SubscriptionItems = CreateIndividualKeyServiceObject("SUBSCRIPTION", $scope.PackageFullDetailsData);
            // showing compare package modal
            $('#modalViewFullDetails').modal('show');

            console.log($scope.PackageFullDetailsResult);
        }).error(function(){
            sweetAlert({ title: "Error", text: "error fetching Details data", type: "error" });
        });
    }

    // create each object comparison list according to ServiceType
        function CreateIndividualKeyServiceObject(type, mainData){
            var filterDataByType = $.map($.grep(mainData, function (obj, id) {
                return (obj.ServiceType == type);
            }), function (obj, id) { return obj; });

            return filterDataByType;
        }

    $scope.onServiceTypeTabClick = function (value){
        // assigning package filteration according to selection
        //alert(value);
        $scope.packageType = value;
        GetServicePackagesForClinicAndPharmacy();
    }

    $scope.onCompareServicePackagesClick = function(){
        // Show Modal modalCompareProduct
        /*sweetAlert({ title: "Success", text: "Product Compared Successfully", type: "success" });*/
        if($scope.PackageCompareList.length <= 1){
            alert("Please add at least two Packages to Compare !");
            return false;
        }

        console.log($scope.PackageCompareList);
        var packageIds = "";
        $scope.compareItemCount = 0;
        $.each($scope.PackageCompareList, function( index, value ) {
            //alert( index + ": " + value );
            packageIds +=  value.PackageId + ",";
            $scope.compareItemCount++;
        });
        // trim packageIds to proper format([1,2,] -> [1,2])
        if(packageIds.length>0){
            packageIds = packageIds.substring(0, packageIds.length - 1);
        }

        //console.log(packageIds);
        $http.get('/afya-portal/anon/getProviderServiceCompareList?packageIds='+ packageIds).success(function(data){
            //sweetAlert('success');
            $scope.PackageCompareDataHTML = data;
            // get unique packages
            $scope.UniquePackages = [];
            $scope.UniquePackages = $scope.returnUnique($scope.PackageCompareDataHTML, "PackageName");
            // Create the main object
            $scope.PackageHTMLResult = [];
            /*  SMART SOLUTIONS SERVICES [TOP_LEVEL], SMART SERVICES for PATIENTS [PATIENT],
                    SMART SERVICES for OTHER CARE PROVIDERS [PROVIDER], NOTIFICATION SERVICES [NOTIFICATION], SELF LEARNING HOURS [TRAINING]*/
            $scope.PackageHTMLResult.SmartSolutionServices = CreateIndividualKeyPackageObject("TOP_LEVEL", $scope.PackageCompareDataHTML);
            $scope.PackageHTMLResult.SmartServicesPatients = CreateIndividualKeyPackageObject("PATIENT", $scope.PackageCompareDataHTML);
            $scope.PackageHTMLResult.SmartServicesOtherCareProviders = CreateIndividualKeyPackageObject("PROVIDER", $scope.PackageCompareDataHTML);
            $scope.PackageHTMLResult.NotificationServices = CreateIndividualKeyPackageObject("NOTIFICATION", $scope.PackageCompareDataHTML);
            $scope.PackageHTMLResult.SelfLearningHours = CreateIndividualKeyPackageObject("TRAINING", $scope.PackageCompareDataHTML);
            $scope.PackageHTMLResult.Subscription = CreateIndividualKeyPackageObject("SUBSCRIPTION", $scope.PackageCompareDataHTML);
            // showing compare package modal
            $('#modalCompareProduct').modal("show");

            console.log($scope.PackageHTMLResult);
        }).error(function(){
            sweetAlert({ title: "Error", text: "error fetching data", type: "error" });
        });
        /*$scope.PackageCompareDataHTML = [{ PackageName:"JOIN IN", ServiceType:"SMART SOLUTIONS SERVICES", ServiceName:"Front Desk - Operations", ServiceValue:"false"},
        { PackageName:"JOIN IN", ServiceType:"SMART SOLUTIONS SERVICES", ServiceName:"Patient Invoicing Services (Cash)", ServiceValue:"true"},
        { PackageName:"JOIN IN", ServiceType:"SMART SERVICES for PATIENTS", ServiceName:"Appointment Premium - Doctors", ServiceValue:"true"},
        { PackageName:"JOIN IN", ServiceType:"SMART SERVICES for PATIENTS", ServiceName:"Appointment Request  - Doctors", ServiceValue:"false"},
        { PackageName:"JOIN IN", ServiceType:"SMART SERVICES for OTHER CARE PROVIDERS", ServiceName:"Refer to Clinics / Doctors", ServiceValue:"false"},
        { PackageName:"JOIN IN", ServiceType:"SMART SERVICES for OTHER CARE PROVIDERS", ServiceName:"E Prescription", ServiceValue:"false"},
            { PackageName:"SMART", ServiceType:"SMART SOLUTIONS SERVICES", ServiceName:"Front Desk - Operations", ServiceValue:"true"},
            { PackageName:"SMART", ServiceType:"SMART SOLUTIONS SERVICES", ServiceName:"Patient Invoicing Services (Cash)", ServiceValue:"true"},
            { PackageName:"SMART", ServiceType:"SMART SERVICES for PATIENTS", ServiceName:"Appointment Premium - Doctors", ServiceValue:"true"},
            { PackageName:"SMART", ServiceType:"SMART SERVICES for PATIENTS", ServiceName:"Appointment Request  - Doctors", ServiceValue:"true"},
            { PackageName:"SMART", ServiceType:"SMART SERVICES for OTHER CARE PROVIDERS", ServiceName:"Refer to Clinics / Doctors", ServiceValue:"true"},
            { PackageName:"SMART", ServiceType:"SMART SERVICES for OTHER CARE PROVIDERS", ServiceName:"E Prescription", ServiceValue:"true"}];*/
    }

    // create each object comparison list according to ServiceType
    function CreateIndividualKeyPackageObject(serviceItemType, mainData){
        var filterDataByType = $.map($.grep(mainData, function (obj, id) {
            return (obj.ServiceType == serviceItemType);
        }), function (obj, id) { return obj; });
        var uniqueSmartSolutionServicePackages = $scope.returnUnique(filterDataByType, "ServiceName")
        var nObj = [];
        angular.forEach(uniqueSmartSolutionServicePackages, function (item) {
            var tempObj = {};
            tempObj.ServiceName = item.ServiceName;
            var i = 0;
            angular.forEach($scope.UniquePackages, function (pkg) {
                tempObj["Package" + i] = $.map($.grep(filterDataByType, function (obj, id) {
                    return (obj.PackageName == pkg.PackageName && obj.ServiceName == item.ServiceName && obj.ServiceType == serviceItemType);
                }), function (obj, id) {
                    i++;
                    return obj.ServiceValue;
                })[0];
            });
            nObj.push(tempObj);
        });
        return nObj;
    }

    // return unique values by column from array
    $scope.returnUnique = function (items, filterOn) {

        if (filterOn === false) {
          return items;
        }

        if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
          var hashCheck = {}, newItems = [];

          var extractValueToCompare = function (item) {
            if (angular.isObject(item) && angular.isString(filterOn)) {
              return item[filterOn];
            } else {
              return item;
            }
          };

          angular.forEach(items, function (item) {
            var valueToCheck, isDuplicate = false;

            for (var i = 0; i < newItems.length; i++) {
              if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
                isDuplicate = true;
                break;
              }
            }
            if (!isDuplicate) {
              newItems.push(item);
            }

          });
          items = newItems;
        }
        return items;
      };

    $scope.toggleAddtoCompareChecked = function ($event, servicePackage, index, checked) {
        $event.stopPropagation();
        //alert(checked);
        if(checked){
            // Create New property for Duplicate Package Id(Annually/Half yearly)
            servicePackage.IndividualPackageId = index;
        }
        AddRemovePackagesToScope(checked, servicePackage);
        console.log($scope.PackageCompareList);
    }

    $scope.onCloseComparePackageClick = function (objPackage){
        // remove the package(object)
        AddRemovePackagesToScope(false, objPackage);

        // uncheck checkbox item
        if(objPackage.PackageType == "CLINIC"){
            // checkboxAdd_{{$index+1}} , objPackage.IndividualPackageId
            $('#checkboxAdd_' + objPackage.IndividualPackageId).attr('checked', false);
        }
        else {
            // checkbox_{{$index+1}}, objPackage.IndividualPackageId
            $('#checkbox_' + objPackage.IndividualPackageId).attr('checked', false);
        }
    }

    function AddRemovePackagesToScope (checked, obj){
        if(checked){
            // add package to scope array
            $scope.PackageCompareList.push(obj);
        }
        else{
            // remove package from scope array
            $scope.PackageCompareList = jQuery.grep($scope.PackageCompareList, function(value) {
              return value != obj;
            });
        }
    }

    function ExtractServiceByPeriodly (serviceData)
    {
        var data = serviceData;

        // get Halfyearly Packages
        var halfyearlyPackages = $.map($.grep(data, function (hobj, idx) {
            return (hobj.HalfyearlyPrice > 0);
        }), function (hobj, idx) {var obj = $.extend({}, hobj); obj.displayPrice = hobj.HalfyearlyPrice; obj.Period = "Halfyearly"; return obj; });
        /*console.log(halfyearlyPackages);
        var store = halfyearlyPackages;*/
        // get Yearly Packages
        var yearlyPackages = $.map($.grep(data, function (yobj, idy) {
            return (yobj.AnnualPrice > 0);
        }), function (yobj, idy) {var obj = $.extend({}, yobj);  obj.displayPrice = yobj.AnnualPrice; obj.Period = "Yearly"; return obj; });
        //console.log(yearlyPackages);

        var allPackages = [];
        // merging two objects into single
        $scope.providerServiceList = $.merge( $.merge(allPackages, halfyearlyPackages), yearlyPackages );
        //$scope.providerServiceList = serviceData;
        console.log('Service Extracted by Package Period(Annually/Halfyearly)');
    }

    // On Login Submit
    $scope.onLoginClick = function (userName, password) {
        //alert('userName=' + userName + ' password=' + password);
        $http.post('/afya-portal/api_login?username=' + userName + '&password=' + password).success(function (data) {
            //alert('Logged in successfully.');
            if (data.message == "success") {
                // Calling Post login methods
                if($scope.postLoginRedirect == "VIEWDETAIL"){
                    ViewDetailsClick();
                }
                else if($scope.postLoginRedirect == "SUBSCRIBE"){
                     ProceedSubscriptionClick();
                }
                else {
                    sweetAlert({ title: "Error", text: "Invalid Login", type: "error" });
                }
                // close modal
                $("#myLogin").modal("hide");
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

    function ShowLoginErrorMessage() {
        //sweetAlert('Please Enter the Correct Username and Password !');
        sweetAlert({ title: "Error", text: "Please Enter the Correct Username and Password !", type: "error" });
    }





    // clearing array list
    function clearList() {
        $scope.providerServiceList = [];
        //$scope.pharmacyServiceList = [];
        $scope.PackageCompareList = [];
    }

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

}]);

CareProviderServiceModule.filter('myFilter', function () {
    return function (items, category) {
        console.log(items);
        var newItems = [];
        for (var i = 0; i < items.length; i++) {
            if (items[i].ServiceName == category) {
                newItems.push(items[i]);
            }
        };

        return newItems;
    }
});