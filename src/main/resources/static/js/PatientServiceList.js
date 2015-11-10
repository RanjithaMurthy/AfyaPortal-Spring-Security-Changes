/**
 * Created by Mohan Sharma on 8/5/2015.
 */
var serviceListModule = angular.module('ServiceListModule', [])
serviceListModule.controller('ServiceListController',['$scope', '$http', '$window', function($scope, $http, $window){
    $scope.serviceList = [];
    $scope.service = {};
    $scope.portalBaseURL = null;
    $scope.afyaId = null;
    $scope.listOfAvailedService = [];
    $scope.serviceForModal = {};
    angular.element(document).ready(function(){
        var username = readCookie("username");
        $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            $scope.afyaId = data.afyaId;
            $http.get('/afya-portal/anon/getPatientVisitCountByServicesConsolidated?afyaId='+data.afyaId+'&fromDate=&toDate=').success(function(data){
                $scope.serviceList = data;
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        }).error(function(data){
            sweetAlert("error fetching data");
        });
    });

    $scope.getAvailedServiceDetails = function(service){
        sweetAlert(service.visitType);
        $http.get('/afya-portal/anon/getVisitHistoryConsolidated?afyaId='+$scope.afyaId+'&fromDate=&toDate=&visitType='+service.visitType+'&clinicName=&doctorName=').success(function(data){
            $scope.listOfAvailedService = data;
            //$scope.<count> = service.visitType;
            //alert("data");
            //return data;
        }).error(function(data){
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
    };

    $scope.openModal = function(data){
        $scope.serviceForModal = data;
        $http.get('/afya-portal/anon/getVisitHistoryConsolidated?afyaId='+$scope.afyaId+'&fromDate=&toDate=&visitType='+data.visitType+'&clinicName=&doctorName=').success(function(data){
            $scope.listOfAvailedService = data;
        }).error(function(data){
            sweetAlert("error fetching data");
        });
    };

    $scope.availService = function(service){
        $window.location.href = '/afya-portal/web_pages/afya_member_request/appointments.html?visitType='+service.visitType;
    };

}]);