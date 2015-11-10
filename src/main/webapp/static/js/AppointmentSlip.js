/**
 * Created by Mohan Sharma on 8/8/2015.
 */
var AppointmentSlipModule = angular.module('AppointmentSlipModule', []);
AppointmentSlipModule.controller('AppointmentSlipController', ['$scope', '$http', function($scope, $http){
    $scope.visit = {};
    $scope.appointmentDetails = {};
    $scope.portalBaseURL = null;
    $scope.date = new Date();
    $scope.logoURL = null;
    $scope.logoWithAddress = null;
    $scope.getVisitData = function(){
        var data = $.cookie("visitData");
        $scope.logoURL = $.cookie("logoUrl");
        $scope.logoWithAddress = $.parseJSON($.cookie("logoWithAddress"));
        $scope.visit = JSON.parse(data);
        var formattedDate = getFormatedDate($scope.visit.visitDate);
        $http.get('/afya-portal/getVisitHistoryForView?startDate='+formattedDate+'&doctorId='+$scope.visit.doctorId+'&visitStartDateTime='+$scope.visit.visitStartDateTime+'&visitEndDateTime='+$scope.visit.visitEndDateTime).success(function(data){
            $scope.appointmentDetails = data;
            console.log(data);
        }).error(function (data) {
            sweetAlert("error redirecting");
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

        var formatedDate = year + '-' + month + '-' + date;
        return formatedDate;
    };

}]);