/**
 * Created by Mohan Sharma on 8/2/2015.
 */
//var patientVisitHistoryModule = angular.module('PatientVisitHistoryModule', []);

afyaApp.controller('PatientVisitHistoryController', ['$scope', '$http', '$window', function($scope, $http, $window){
    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.patientData = {};
    $scope.afyaId = {};
    $scope.visitHistoryList = [];
    $scope.visit = {};
    $scope.appointmentDetails = {};
    $scope.visitModal = {showInvToSelect : false, invoiceList: null};

    angular.element(document).ready(function(){
        assignWelcomeUserName();
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
            var data = getPatientByUsername();
            //$http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            if(data != null)
            {
                $scope.patientData = data;
                $scope.afyaId = data.afyaId;
                $http.get('/afya-portal/getVisitHistoryConsolidated?fromDate=&toDate=&clinicName=&doctorName=&visitType=').success(function(data){
                    console.log(data);
                    // filter Visits of Clinic Provider Type
                    // data = $.grep(data, function(val){return val.providerType == 'Clinic';});
                    // sort
                    data.sort(function(a, b){
                        return new Date(b.visitDate) - new Date(a.visitDate);
                    });
                    $scope.visitHistoryList = [];
                    $scope.visitHistoryList = data;
                }).error(function(data){
                    sweetAlert("error fetching visit history");
                });
            }
            else
                sweetAlert("error fetching data");
            //}).error(function(data){
            //    sweetAlert("error fetching data");
            //});
        });
    });

    $scope.openModal = function(data){
        $scope.visitModal.showInvToSelect = false;
        $scope.visit = data;
        if($scope.visit.providerType == 'Clinic'){
            // clinic invoice
            $.removeCookie("visitData", { path: '/afya-portal'});
            $.cookie("visitData",JSON.stringify(data));
            $scope.getLogoFromTenantAndStoreInCookie(data.clinicTenantId);
        }else if($scope.visit.providerType == 'Pharmacy'){
            // pharmacy invoice
            // prepareCookieForPrescriptionInvoicePrint();
            $.removeCookie("visitData", { path: '/afya-portal'});
            $.cookie("visitData",JSON.stringify(data));
            $scope.getLogoFromTenantAndStoreInCookie(data.clinicTenantId);
        }
    };

    $scope.prepareCookieForPrescriptionInvoicePrint = function()
    {
        // get
        $http.get('/afya-portal/anon/getPrescriptionInvoiceLineItemDetailsForGivenPatient?tenantId='+$scope.prescription.pharmacyId+'&invoiceId='+$scope.prescription.invoiceId).success(function(data){
            $scope.prescriptionInvoiceLineItems = data;
            console.log(data);
        }).error(function(){

        });

        $scope.prescription = prescription;
        if($scope.prescription.invoiceId != null) {
            $http.get('/afya-portal/anon/getPrescriptionInvoiceLineItemDetailsForGivenPatient?tenantId='+$scope.prescription.pharmacyId+'&invoiceId='+$scope.prescription.invoiceId).success(function(data){
                $scope.prescriptionInvoiceLineItems = data;

                $.removeCookie("prescription", { path: '/afya-portal'});
                $.cookie("prescription",JSON.stringify($scope.prescription));
                $.removeCookie("prescriptionInvoiceLineItems", { path: '/afya-portal'});
                $.cookie("prescriptionInvoiceLineItems",JSON.stringify($scope.prescriptionInvoiceLineItems));
                window.open('/afya-portal/web_pages/member_area/patient/PrintInvoicePrescriptionHistory.html', '_abc');

            }).error(function(){

            });
        }
    }

    $scope.getLogoFromTenantAndStoreInCookie = function(tenantId){
        $.removeCookie("logoUrl", { path: '/afya-portal'});
        $.removeCookie("logoWithAddress", { path: '/afya-portal'});
        if($scope.visit.providerType == 'Clinic'){
            $http.get('/afya-portal/anon/getLogoURLFromTenant?tenantId='+tenantId).success(function (data) {
                console.log(data);
                if(!angular.isUndefined(data.logoUrl) || data.logoUrl != null){
                    $.cookie("logoUrl", data.logoUrl);
                    $.cookie("logoWithAddress", data.logoWithAddress);
                }
            }).error(function(){
            });
        } else if($scope.visit.providerType == 'Pharmacy'){
            $http.get('/afya-portal/anon/getPharmacyLogoURL?tenantId='+tenantId).success(function (data) {
                console.log(data);
                if(!angular.isUndefined(data.logoUrl) || data.logoUrl != null){
                    $.cookie("logoUrl", data.logoUrl);
                    $.cookie("logoWithAddress", data.logoWithAddress);
                }
            }).error(function(){
            });
        }
    };
    $scope.openPage = function(pagename){
        if(angular.equals(pagename, "AppointmentSlip")){
            $window.open('/afya-portal/web_pages/member_area/patient/AppointmentSlip.html', '_abc');
        }
        if(angular.equals(pagename, "Encounter")){
            $window.open('/afya-portal/web_pages/member_area/patient/Encounter.html', '_abc');
        }
        if( angular.equals(pagename, "Invoice")){
            if($scope.visit.providerType == 'Clinic')
                displayClinicInvoiceList();
                // $window.open('/afya-portal/web_pages/member_area/patient/Invoice.html', '_abc');
            else if($scope.visit.providerType == 'Pharmacy')
                displayPharmacyInvoiceList();
                //$window.open('/afya-portal/web_pages/member_area/patient/InvoiceForPharmacy.html', '_abc');
        }
        if(angular.equals(pagename, "Prescription")){
            $window.open('/afya-portal/web_pages/member_area/patient/Prescription.html', '_abc');
        }
    };

    function displayClinicInvoiceList(){
        // retrieve Clinic invoices from ClinicId and ScheduleId
        $http.get('/afya-portal/anon/getInvoiceRecordsConsolidatedForSchedule?clinicId='+$scope.visit.clinicTenantId+'&scheduleId='+$scope.visit.clinicScheduleId).success(function(data){
            // check if no invoices
            if(data.length == 0){
                sweetAlert("There are no Invoices for this Visit");
                return;
            }
            // if a single invoice is found, then display the invoice
            if(data.length == 1){
                $scope.visitModal.showInvToSelect = false;
                $scope.displayClinicInvoice(data[0]);
            }else{  // if multiple invoices, display list of invoices, for the user to select one invoice from the list
                $scope.visitModal.invoiceList = data;
                $scope.visitModal.showInvToSelect = true;
            }
        });
    }

    function displayPharmacyInvoiceList(){
        // retrieve Pharmacy invoices from PharmacyId and OrderId
        $http.get('/afya-portal/anon/getPharmacyInvoiceRecordsConsolidatedForOrder?pharmacyId='+$scope.visit.clinicTenantId+'&orderId='+$scope.visit.pharmacyOrderId).success(function(data){
            // check if no invoices
            if(data.length == 0){
                sweetAlert("There are no Invoices for this Order");
                return;
            }
            // if a single invoice is found, then display the invoice
            if(data.length == 1){
                $scope.visitModal.showInvToSelect = false;
                $scope.displayPharmacyInvoice(data[0]);
            }else{  // if multiple invoices, display list of invoices, for the user to select one invoice from the list
                $scope.visitModal.invoiceList = data;
                $scope.visitModal.showInvToSelect = true;
            }
        });
    }

    $scope.displayClinicInvoice = function(invoiceData){
        $.removeCookie("invoiceData", { path: '/afya-portal'});
        $.cookie("invoiceData", JSON.stringify(invoiceData));
         $window.open('/afya-portal/web_pages/member_area/patient/Invoice.html', '_abc');
    }

    $scope.displayPharmacyInvoice = function(invoiceData){
        $.removeCookie("invoiceData", { path: '/afya-portal'});
        $.cookie("invoiceData", JSON.stringify(invoiceData));
        $window.open('/afya-portal/web_pages/member_area/patient/InvoiceForPharmacy.html', '_abc');
    }
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


