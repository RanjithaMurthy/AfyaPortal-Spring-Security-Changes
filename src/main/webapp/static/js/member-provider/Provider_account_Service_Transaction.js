/**
 * Created by pradyumna on 11-06-2015.
 */

//angular.module('ProviderTransactionModule', ['ngMessages'])
afyaApp.controller('ProviderTransactionController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.paymentList = [];
        $scope.username = readCookie("username");

       angular.element(document).ready(function(){
            assignWelcomeUserName();
            $http.get('/afya-portal/getTransactionDetailByUsername').success(function(data){
                $scope.paymentList = data;
            }).error(function(){
                sweetAlert("error fetching data");
            });
        });

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


