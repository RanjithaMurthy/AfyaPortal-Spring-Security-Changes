/**
 * Created by Mohan Sharma on 8/10/2015.
 */
var InvoiceDetailModule = angular.module("InvoiceDetailModule", []);
InvoiceDetailModule.controller("InvoiceDetailsController", ['$scope', '$http', function($scope, $http){

    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.visit = {};
    $scope.invoiceDetails = [];
    $scope.InvoiceItem = {};
    $scope.invoiceDiscount = null;
    $scope.invoiceAmount = null;
    $scope.netAmount = null;
    $scope.invoiceInsuranceDetails = {};
    $scope.logoURL = null;
    $scope.getInvoiceDetailsForVisitForGivenTenant = function(){
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
            $scope.visit = JSON.parse(data);
            $http.get('/afya-portal/anon/getInvoiceDetailsForGivenVisitFromGivenTenant?tenantId='+$scope.visit.clinicTenantId+'&afyaId='+$scope.visit.AFYA_ID+'&startDate='+$scope.visit.visitDate+'&doctorId='+$scope.visit.doctorId+'&visitStartDateTime='+$scope.visit.visitStartDateTime+'&visitEndDateTime='+$scope.visit.visitEndDateTime).success(function(data){
                $scope.invoiceDetails = data;
                if(data.length > 0) {
                    $scope.InvoiceItem = data[0];
                    $scope.getInsuranceDetailsFromClinic();
                }
                console.log(data);
            }).error(function (data) {
                sweetAlert("error redirecting");
            });
        });
    };

    $scope.getInsuranceDetailsFromClinic = function(){
        $http.get($scope.clinicBaseURL+'/clinicMaster/getInsuranceDetailsBySchedule?clinicId='+$scope.visit.clinicTenantId+'&invoiceId='+$scope.InvoiceItem.invoiceId).success(function(data){
            $scope.invoiceInsuranceDetails = data;
            console.log("invoiceInsuranceDetails : "+data);
        }).error(function(){

        });
    }
}]);