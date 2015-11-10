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
    $scope.getEncounterDetailsForVisitForGivenPatientVisitAndTenant = function() {
        var data = $.cookie("visitData");
        $scope.logoURL = $.cookie("logoUrl");
        $scope.visit = JSON.parse(data);
        $http.get('/afya-portal/anon/getEncounterDetailsForGivenPatientVisit?tenantId=' + $scope.visit.clinicTenantId + '&afyaId=' + $scope.visit.AFYA_ID + '&startDate=' + $scope.visit.visitDate + '&doctorId=' + $scope.visit.doctorId + '&visitStartDateTime=' + $scope.visit.visitStartDateTime + '&visitEndDateTime=' + $scope.visit.visitEndDateTime).success(function (data) {
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

}]);