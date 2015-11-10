/**
 * Created by Mohan Sharma on 8/8/2015.
 */
var PrintInvoiceModule = angular.module('PrintInvoiceModule', []);
PrintInvoiceModule.controller('PrintInvoiceController', ['$scope', '$http', function($scope, $http){
    $scope.invoice = {};
    $scope.portalBaseURL = null;
    $scope.date = new Date();
    $scope.logoURL = null;
    $scope.invoiceLineItems = {};
    $scope.invoicePayments = {};
    $scope.GrossAmount = 0.00;
    $scope.NetAmount = 0.00;
    $scope.logoURL = null;
    $scope.logoWithAddress = null;

    $scope.getInvoiceData = function(){
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            $scope.invoice = JSON.parse($.cookie("invoice"));
            $scope.invoiceLineItems = JSON.parse($.cookie("invoiceLineItems"));
            $scope.getLogoFromTenantAndStoreInCookie($scope.invoice.CLINIC_ID);
            $scope.getDetail($scope.invoice.CLINIC_ID);
            $scope.calculateTotalGrossAmount();
            getInvoicePayments();
        });
    };

    $scope.getLogoFromTenantAndStoreInCookie = function(tenantId){
        $http.get('/afya-portal/anon/getLogoURLFromTenant?tenantId='+tenantId).success(function (data) {
            console.log(data);
            if(!angular.isUndefined(data.logoUrl) || data.logoUrl != null){
                $scope.logoURL = data.logoUrl;
                $scope.logoWithAddress =  data.logoWithAddress;
            }
        }).error(function(){
        });
    };

    $scope.getDetail = function(id){
        $http.get('/afya-portal/anon/getClinicDetailsByClinicId?clinicId='+id).success(function (data) {
            console.log(data);
            $scope.detail = data;
        }).error(function(){
        });
    };

    $scope.calculateTotalGrossAmount = function()
    {
        var total = 0.00;
        for(var i=0;i<$scope.invoiceLineItems.length;i++ )
        {
            total = total + $scope.invoiceLineItems[i].invoiceItemAmount;
        }
        $scope.GrossAmount = total;
    }

    function getInvoicePayments(){
        $http.get('/afya-portal/anon/getInvoicePaymentForInvoiceId?tenantId='+$scope.invoice.CLINIC_ID+'&invoiceId='+$scope.invoice.ID).success(function(data){
            $scope.invoicePayments = data;
        }).error(function (data) {
            sweetAlert("error fetching data");
        });
    };
}]);

var PrintPrescriptionModule = angular.module('PrintPrescriptionModule', []);
PrintPrescriptionModule.controller('PrintPrescriptionController', ['$scope', '$http', function($scope, $http){
    $scope.prescription = {};
    $scope.portalBaseURL = null;
    $scope.date = new Date();
    $scope.logoURL = null;
    $scope.prescriptionInvoiceItem = {};
    $scope.GrossAmount = 0.00;
    $scope.NetAmount = 0.00;

    $scope.getPrescriptionData = function(){
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            $scope.prescription = JSON.parse($.cookie("prescription"));
            $scope.prescriptionInvoiceLineItems = JSON.parse($.cookie("prescriptionInvoiceLineItems"));
            $scope.getLogoFromTenantAndStoreInCookie($scope.prescription.pharmacyId);
            $scope.getDetail($scope.prescription.pharmacyId);
            $scope.calculateTotalGrossAmount();
        });
    };

    $scope.getLogoFromTenantAndStoreInCookie = function(tenantId){
        $http.get('/afya-portal/anon/getPharmacyLogoURL?tenantId=' + tenantId).success(function (data) {
            if(!angular.isUndefined(data.logoUrl) || data.logoUrl != null){
                $scope.logoURL = data.logoUrl;
            }
        }).error(function(){
        });
    };

    $scope.calculateTotalGrossAmount = function()
    {
        var total = 0.00;
        for(var i=0;i<$scope.prescriptionInvoiceLineItems.length;i++ )
        {
            total = total + $scope.prescriptionInvoiceLineItems[i].amount;
        }
        $scope.GrossAmount = total;
    }

    $scope.getDetail = function(id){
        $http.get($scope.portalBaseURL + '/anon/getPharmacyDetailsByPharmacyId?pharmacyId='+id).success(function (data) {
            console.log(data);
            $scope.detail = data;
        }).error(function(){
        });
    };

}]);