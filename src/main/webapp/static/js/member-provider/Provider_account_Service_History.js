/**
 * Created by pradyumna on 11-06-2015.
 */

//angular.module('ProviderServiceHistoryModule', ['ngMessages'])
afyaApp.controller('ProviderServiceHistoryController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.PaymentHistory = [];
        $scope.PatientSmartServices = [];
        $scope.NotificationServices  = [];
        $scope.portalBaseURL = "";
        $scope.username = "";
        $scope.value = "";
        $scope.count = "";

        angular.element(document).ready(function(){
            assignWelcomeUserName();
            $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
                if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                    $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
                }else{
                    $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
                }
                $scope.username = readCookie("username");

                // get Payment History
                $http.get('/afya-portal/getSubscriptionHistory').success(function(data){
                    $scope.PaymentHistory = data;
                    console.log($scope.PaymentHistory);
                });

                // get Smart Services -To Patients
                $http.get('/afya-portal/getPatientSmartServicesPackageHistory?visitType=').success(function(data){
                    $scope.PatientSmartServices = data;
                    console.log($scope.PatientSmartServices);
                });

                // get Notification -Services
                // $http.get('/afya-portal/anon/getPackageNotificationHistory?username=' + $scope.username).success(function(data){
                $http.get('/afya-portal/getPackageNotificationSubscription?dataOption=ACTIVE').success(function(data){
                    $scope.NotificationServices = data;
                    console.log($scope.NotificationServices);
                });

                // copied from ProviderTransactionController (Provider_account_Service_Transaction.js)
                $http.get('/afya-portal/getTransactionDetailByUsername').success(function(data){
                    $scope.paymentList = data;
                }).error(function(){
                    sweetAlert("error fetching data");
                });
            });
        });

        // get Count from Type & Status
        $scope.getCount = function(type, status)
        {
            for (var i = 0; i < $scope.PatientSmartServices.length; i++) {
                if($scope.PatientSmartServices[i].visitType == type && $scope.PatientSmartServices[i].usageStatus == status)
                    return $scope.PatientSmartServices[i].usageCount;
            }
            return 0;
        }

        // get Value[3 Decimal Place] from Type & Status
        $scope.getValue = function(type, status)
        {
            for (var i = 0; i < $scope.PatientSmartServices.length; i++) {
                if($scope.PatientSmartServices[i].visitType == type && $scope.PatientSmartServices[i].usageStatus == status)
                    return setDecimal($scope.PatientSmartServices[i].usageValue);
            }
            return setDecimal(0);
        }

        $scope.readCookie = function(name) {
            var nameEQ = name + "=";
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) == ' ') c = c.substring(1, c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
            }
            return null;
        }

        // round number to 3 decimal places [considering from 5 onwards]
        setDecimal = function (input) {
            if (isNaN(input)) return input;
            var places = 3;
            var factor = "1" + Array(+(places > 0 && places + 1)).join("0");
            return (Math.round(input * factor) / factor).toFixed(places);
        };


       // copied from ProviderTransactionController (Provider_account_Service_Transaction.js)
       /*angular.element(document).ready(function(){
            assignWelcomeUserName();
            $http.get('/afya-portal/anon/getTransactionDetailByUsername?username=' + $scope.username).success(function(data){
                $scope.paymentList = data;
            }).error(function(){
                sweetAlert("error fetching data");
            });
        });*/

       // copied from ProviderTransactionController (Provider_account_Service_Transaction.js)
       $scope.onPrintReceiptClicked = function(payment)
       {
             $http.get('/afya-portal/anon/getPackageTransctionDetail?paymentId=' + payment.paymentId).success(function(data){
                 $.removeCookie("paymentDetail", { path: '/afya-portal'});
                 $.cookie("paymentDetail",JSON.stringify(data[0]));
                 window.open('/afya-portal/web_pages/member_area/provider/subscription-payment-receipt.html', '_abc');
             }).error(function(){
                sweetAlert("error fetching data");
             });
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
       }

    }]);


