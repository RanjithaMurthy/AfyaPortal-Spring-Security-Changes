/**
 * Created by Mohan Sharma on 8/2/2015.
 */
var patientVisitHistoryModule = angular.module('PatientVisitHistoryModule', []);

patientVisitHistoryModule.controller('PatientVisitHistoryController', ['$scope', '$http', '$window', function($scope, $http, $window){
    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.patientData = {};
    $scope.afyaId = {};
    $scope.visitHistoryList = [];
    $scope.visit = {};
    $scope.appointmentDetails = {};

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
            var username = readCookie("username");
            $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
                $scope.patientData = data;
                $scope.afyaId = data.afyaId;
                $http.get('/afya-portal/anon/getVisitHistoryConsolidated?afyaId='+data.afyaId+'&fromDate=&toDate=&clinicName=&doctorName=&visitType=').success(function(data){
                    console.log(data);
                    data.sort(function(a, b){
                        return new Date(b.visitDate) - new Date(a.visitDate);
                    });
                    $scope.visitHistoryList = [];
                    $scope.visitHistoryList = data;
                }).error(function(data){
                    sweetAlert("error fetching visit history");
                });
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        });
    });

    $scope.openModal = function(data){
        $scope.visit = data;
        $.removeCookie("visitData", { path: '/afya-portal'});
        $.cookie("visitData",JSON.stringify(data));
        $scope.getLogoFromTenantAndStoreInCookie(data.clinicTenantId);
    };

    $scope.getLogoFromTenantAndStoreInCookie = function(tenantId){
        $.removeCookie("logoUrl", { path: '/afya-portal'});
        $http.get('/afya-portal/anon/getLogoURLFromTenant?tenantId='+tenantId).success(function (data) {
            console.log(data);
            if(!angular.isUndefined(data.logoUrl) || data.logoUrl != null){
                $.cookie("logoUrl", data.logoUrl);
                $.cookie("logoWithAddress", data.logoWithAddress);
            }
        }).error(function(){
        });
    };
    $scope.openPage = function(pagename){
        if(angular.equals(pagename, "AppointmentSlip")){
            $window.open('AppointmentSlip.html', '_abc');
        }
        if(angular.equals(pagename, "Encounter")){
            $window.open('Encounter.html', '_abc');
        }
        if( angular.equals(pagename, "Invoice")){
            $window.open('Invoice.html', '_abc');
        }
        if(angular.equals(pagename, "Prescription")){
            $window.open('Prescription.html', '_abc');
        }
    };
}]);


function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
};


