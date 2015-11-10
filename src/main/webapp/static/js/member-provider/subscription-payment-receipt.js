/**
 * Created by Ranjitha
 * Modified on 24/09/2015
 */
var PaymentReceiptModule = angular.module('PaymentReceiptModule', []);
PaymentReceiptModule.controller('PaymentReceiptController', ['$scope', '$http', function($scope, $http){
    $scope.detail = {};

    $scope.getData = function(){
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            $scope.detail = JSON.parse($.cookie("paymentDetail"));
            console.log($scope.detail);
        });
    };
}]);
