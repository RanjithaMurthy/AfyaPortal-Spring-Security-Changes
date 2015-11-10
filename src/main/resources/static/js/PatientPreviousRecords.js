/**
 * Created by Mohan Sharma on 8/2/2015.
 */
var patientPrescriptionInvoiceHistoryModule = angular.module('PatientPrescriptionInvoiceHistoryModule', []);
patientPrescriptionInvoiceHistoryModule.controller('PatientPrescriptionHistoryController', ['$scope', '$http', function($scope, $http){
    $scope.afyaId = null;
    $scope.visitPrescriptionList = [];
    $scope.prescription = {};
    $scope.prescriptionInvoiceLineItems = [];

    angular.element(document).ready(function(){
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            var username = readCookie("username");
            $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
                $scope.afyaId = data.afyaId;
                $http.get('/afya-portal/anon/getPrescriptionInvoiceDetailsForGivenPatient?afyaId='+data.afyaId).success(function(data){
                    $scope.visitPrescriptionList = data;
                    console.log(data);
                });
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        });
    });

    $scope.storePrescriptionInScopeWhenModalViewClicked = function(prescription){
        $scope.prescription = prescription;
        if($scope.prescription.invoiceId != null) {
            $scope.getPrescriptionLineItemsForGivenInvoiceId();
        }
    };

    $scope.getPrescriptionLineItemsForGivenInvoiceId = function(){
        $http.get('/afya-portal/anon/getPrescriptionInvoiceLineItemDetailsForGivenPatient?tenantId='+$scope.prescription.pharmacyId+'&invoiceId='+$scope.prescription.invoiceId).success(function(data){
            $scope.prescriptionInvoiceLineItems = data;
            console.log(data);
        }).error(function(){

        });
    };

}]);

patientPrescriptionInvoiceHistoryModule.controller('PatientInvoiceHistoryController', ['$scope', '$http', function($scope, $http){
    $scope.portalBaseURL = null;
    $scope.afyaId = {};
    $scope.visitInvoiceList = [];
    $scope.invoice = {};
    $scope.invoiceLineItems = [];

    angular.element(document).ready(function(){
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            var username = readCookie("username");
            $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
                $scope.afyaId = data.afyaId;
                $http.get('/afya-portal/anon/getInvoiceRecordsConsolidated?afyaId='+data.afyaId+'&fromDate=&toDate=&clinicName=&doctorName=&providerType=&invoiceNumber=').success(function(data){
                    data.sort(function(a, b){
                        return new Date(b.CREATE_TX_TIMESTAMP) - new Date(a.CREATE_TX_TIMESTAMP);
                    });
                    $scope.visitInvoiceList = data;
                    console.log(data);
                });
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        });
    });

    $scope.storeInvoiceInScopeWhenModalViewClicked = function(invoice){
        $scope.invoice = invoice;
        if($scope.invoice.ID != null) {
            $scope.getInvoiceLineItemsForGivenInvoice();
        }
    };

    $scope.getInvoiceLineItemsForGivenInvoice = function(){
        $http.get('/afya-portal/anon/getInvoiceLineItemsForGivenInvoiceFromGivenTenant?tenantId='+$scope.invoice.CLINIC_ID+'&invoiceId='+$scope.invoice.ID).success(function(data){
            $scope.invoiceLineItems = data;
            console.log(data);
        }).error(function(){

        });
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

