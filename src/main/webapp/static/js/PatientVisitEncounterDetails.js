/**
 * Created by Mohan Sharma on 8/14/2015.
 */
var patientVisitEncounterModule = angular.module('PatientVisitEncounterModule', []);
patientVisitEncounterModule.controller('PatientVisitEncounterController', ['$scope', '$http', function($scope, $http){

    $scope.visit = {};
    $scope.chiefComplaints = [];
    $scope.allegies = [];
    $scope.labOrders = [];
    $scope.diagnosis = [];
    $scope.procedures = [];
    $scope.prescriptions = [];
    $scope.patientDetailItem = {};
    $scope.currentDate = new Date();
    $scope.logoURL = null;
    $scope.logoWithAddress = null;
    $scope.getEncounterDetailsForVisitForGivenPatientVisitAndTenant = function() {
        var data = $.cookie("visitData");
        $scope.logoURL = $.cookie("logoUrl");
        $scope.logoWithAddress = $.parseJSON($.cookie("logoWithAddress"));
        $scope.visit = JSON.parse(data);
        var formattedDate = getFormatedDate(new Date($scope.visit.visitDate));
        $http.get('/afya-portal/getEncounterDetailsForGivenPatientVisit?tenantId=' + $scope.visit.clinicTenantId + '&startDate=' + formattedDate+ '&doctorId=' + $scope.visit.doctorId + '&visitStartDateTime=' + $scope.visit.visitStartDateTime + '&visitEndDateTime=' + $scope.visit.visitEndDateTime).success(function (data) {
            console.log(JSON.stringify(data));
            $scope.chiefComplaints = data.chiefComplaints;
            $scope.allegies = data.allegies;
            $scope.labOrders = data.labOrders;
            $scope.diagnosis = data.diagnosis;
            $scope.procedures = data.procedures;
            $scope.prescriptions = data.prescriptions;
            if($scope.chiefComplaints.length > 0){
                $scope.patientDetailItem = $scope.chiefComplaints[0];
            }
        }).error(function (data) {
            sweetAlert("error redirecting");
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