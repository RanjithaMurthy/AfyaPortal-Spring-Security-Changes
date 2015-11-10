/**
 * Created by Mohan Sharma on 8/17/2015.
 */
//var PatientSmartServiceModule = angular.module('PatientSmartServiceModule', []);
afyaApp.controller('PatientSmartServiceController', ['$scope','$window', '$http', function($scope, $window, $http){
    $scope.visits = [];
    angular.element(document).ready(function() {
        assignWelcomeUserName();
        var username = $.cookie("username");
        var data = getPatientByUsername();
        if(data != null)
        {
        //$http.get('/afya-portal/anon/getPatientByUsername?username=' + username).success(function (data) {
            $scope.afyaId = data.afyaId;
            $scope.visits = [];
            $http.get('/afya-portal/getVisitHistoryConsolidated?fromDate=&toDate=&visitType=&clinicName=&doctorName=').success(function (data) {
                console.log(data);
                angular.forEach(data, function(eachData){
                    if(angular.equals(eachData.visitType , 'Premium Visit') || angular.equals(eachData.visitType , 'Tele Consultation Visit') || angular.equals(eachData.visitType , 'Home Visit')){
                        $scope.visits.push(eachData);
                    }
                });
                if($scope.visits.length){
                    $scope.visits.sort(function(a, b){
                        return new Date(b.visitDate) - new Date(a.visitDate);
                    });
                }
            }).error(function (data) {
            });
        }
        else
            sweetAlert("error fetching data");
        //}).error();
    });
}]);