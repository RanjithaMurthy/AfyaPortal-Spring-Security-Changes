/**
 * Created by Mohan Sharma on 8/10/2015.
 */
var InvoiceDetailModule = angular.module("PharmacyInvoiceDetailModule", []);
InvoiceDetailModule.controller("PharmacyInvoiceDetailsController", ['$scope', '$http', '$sce', function($scope, $http, $sce){

    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.visit = {};
    $scope.invoiceDetails = [];
    // $scope.InvoiceItem = {};
    $scope.invoice = {};
    $scope.invoiceDiscount = null;
    $scope.invoiceAmount = null;
    $scope.netAmount = null;
    $scope.invoiceInsuranceDetails = {};
    $scope.logoURL = null;
    $scope.logoWithAddress = null;
    $scope.patientServicePolicyContent = "";
    /* NOT USED
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
            $scope.logoWithAddress = $.parseJSON($.cookie("logoWithAddress"));
            $scope.visit = JSON.parse(data);
            var formattedDate = getFormatedDate(new Date($scope.visit.visitDate));
            $http.get('/afya-portal/anon/getInvoiceDetailsForGivenVisitFromGivenTenant?tenantId='+$scope.visit.clinicTenantId+'&afyaId='+$scope.visit.AFYA_ID+'&startDate='+formattedDate+'&doctorId='+$scope.visit.doctorId+'&visitStartDateTime='+$scope.visit.visitStartDateTime+'&visitEndDateTime='+$scope.visit.visitEndDateTime).success(function(data){
                $scope.invoiceDetails = data;
                if(data.length > 0) {
                    $scope.InvoiceItem = data[0];
                    $scope.getInsuranceDetailsFromClinic();
                    $scope.getAfyaPolicyForPatient();
                }
                console.log(data);
            }).error(function (data) {
                sweetAlert("error redirecting");
            });
        });
    };*/

    $scope.getInvoiceDetailsForLastInvoiceForGivenTenant = function(){
        // TO DO: need to have a intermediate to show the list of invoices for the schedule, to allow user to select, then show the invoice with details for the selected invoice
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
            if($.cookie("logoWithAddress") != undefined)
                $scope.logoWithAddress = $.parseJSON($.cookie("logoWithAddress"));
            else
                $scope.logoWithAddress = false;

            $scope.visit = JSON.parse(data);
            // to invoices for the visit (Schedule)
            // $http.get('/afya-portal/anon/getPharmacyInvoiceRecordsConsolidatedForOrder?pharmacyId='+$scope.visit.clinicTenantId+'&orderId='+$scope.visit.pharmacyOrderId).success(function(data){
                /*var lastInvoice = data[data.length - 1];
                if(lastInvoice == undefined){
                    // sweetAlert("There is no Invoice for this Visit");
                    return;
                }*/
                var lastInvoice = JSON.parse($.cookie("invoiceData"));
                // select invoice
                $scope.invoice = lastInvoice;
                $http.get('/afya-portal/anon/getPrescriptionInvoiceLineItemDetailsForGivenPatient?tenantId='+$scope.visit.clinicTenantId+'&invoiceId='+lastInvoice.invoiceId).success(function(data){
                    $scope.invoiceDetails = $.grep(data, function(val){return val.invoiceItemTypeId != 'PITM_DISCOUNT_ADJ';});
                    // computations for the selected invoice
                    if(data.length > 0) {
                        // find discount item
                        var discountItem = $.grep(data, function(val){return val.invoiceItemTypeId == 'PITM_DISCOUNT_ADJ';})[0];
                        $scope.invoiceDiscount = discountItem != undefined ? discountItem.amount : 0;
                        // compute total
                        $scope.invoiceAmount = 0;
                        $.each($scope.invoiceDetails, function(idx,val){
                            $scope.invoiceAmount += val.amount;
                        });
                        // compute net amount
                        $scope.netAmount = $scope.invoiceAmount + $scope.invoiceDiscount;
                        // $scope.InvoiceItem = data[0];
                        // $scope.getInsuranceDetailsFromClinic();
                    }else{
                        $scope.invoiceDiscount = null;
                        $scope.invoiceAmount = null;
                        $scope.netAmount = null;
                    }
                    $scope.getAfyaPolicyForPatient();
                    console.log(data);
                }).error(function (data) {
                    sweetAlert("error redirecting");
                });
            /*}).error(function (data) {
                sweetAlert("error redirecting");
            });;*/
        });
    };

    /*$scope.getInsuranceDetailsFromClinic = function(){
        $http.get($scope.clinicBaseURL+'/clinicMaster/getInsuranceDetailsBySchedule?clinicId='+$scope.visit.clinicTenantId+'&invoiceId='+$scope.InvoiceItem.invoiceId).success(function(data){
            $scope.invoiceInsuranceDetails = data;
            console.log("invoiceInsuranceDetails : "+data);
        }).error(function(){

        });
    }*/

    $scope.getAfyaPolicyForPatient = function(){
        var serviceName = '';
        switch($scope.visit.visitType){
            case "Premium Visit":
                serviceName = "Appointment  Premium - Doctors";
            break;
            case "Consult Visit":
                serviceName = "Appointment  Request - Doctors";
            break;
            case "Tele Consultation Visit":
                serviceName = "Tele Consultation";
            break;
            case "Home Visit":
                serviceName = "Appointment - Home Visit Doctor";
            break;
            case "Home Pharmacy":
                serviceName = "Home Pharmacy - Refill of Prescription";
            break;
            default:
            serviceName = '';       // should never be here
        }
        // retrieve pocilty by service
        $http.get($scope.portalBaseURL + '/anon/getPatientPoilicyForService?serviceName=' + serviceName
            , {headers : {'accept': 'text/html'}}).success(function (data) {
            $scope.patientServicePolicyContent = $sce.trustAsHtml(data);
        }).error(function (data, status, headers, config) {
            console.log("Error while Getting Patient Policies for Services!" + status);
            sweetAlert("error fetching data");
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