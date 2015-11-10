/**
 * Created by Mohan Sharma on 8/12/2015.
 */

var PrescriptionDetailModule = angular.module('PrescriptionDetailModule',[]);
PrescriptionDetailModule.controller('PrescriptionDetailController',['$scope','$http', function($scope, $http){
    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.visit = {};
    $scope.prescriptiopnDetails = [];
    $scope.prescription = {};
    $scope.logoURL = null;
    $scope.logoWithAddress = null;
    $scope.getIPrescriptionDetailsForVisitForGivenTenant = function(){
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
            var data = $.cookie("visitData");
            $scope.logoURL = $.cookie("logoUrl");
            $scope.logoWithAddress = $.parseJSON($.cookie("logoWithAddress"));
            $scope.visit = JSON.parse(data);
            var formattedDate = getFormatedDate(new Date($scope.visit.visitDate));
            $http.get('/afya-portal/getPrescriptionDetailsForGivenVisitFromGivenTenant?tenantId='+$scope.visit.clinicTenantId+'&startDate='+formattedDate+'&doctorId='+$scope.visit.doctorId+'&visitStartDateTime='+$scope.visit.visitStartDateTime+'&visitEndDateTime='+$scope.visit.visitEndDateTime).success(function(data){
                $scope.prescriptiopnDetails = data;
                if(data.length > 0) {
                    $scope.prescription = data[0];
                }
                console.log(data);
            }).error(function (data) {
                sweetAlert("error redirecting");
            });
        });
    }

    function getFormatedDate(date) { // DD-MM-YYYY
        var year = date.getFullYear();
        var month = (date.getMonth() + 1);
        if (month < 10)
            month = '0' + month;
        var date = date.getDate();
        if (date < 10)
            date = '0' + date;

        var formatedDate = year + '-' + month + '-' + date;
        return formatedDate;
    };

}]);