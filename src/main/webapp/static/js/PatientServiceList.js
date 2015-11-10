/**
 * Created by Mohan Sharma on 8/5/2015.
 */
//var serviceListModule = angular.module('ServiceListModule', [])
afyaApp.controller('ServiceListController',['$scope', '$http', '$window','$state', function($scope, $http, $window, $state){
    $scope.serviceList = [];
    $scope.service = {};
    $scope.portalBaseURL = null;
    $scope.afyaId = null;
    $scope.listOfAvailedService = [];
    $scope.serviceForModal = {};
    angular.element(document).ready(function(){
        assignWelcomeUserName();
        var username = readCookie("username");
        var data = getPatientByUsername();
        if(data != null)
        {
        //$http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            $scope.afyaId = data.afyaId;
            $http.get('/afya-portal/getPatientVisitCountByServicesConsolidated?&fromDate=&toDate=').success(function(data){
                console.log(data);
                $scope.serviceList = $scope.constructData(data);
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        }
        else
            sweetAlert("error fetching data");
        //}).error(function(data){
        //    sweetAlert("error fetching data");
        //});
    });

    $scope.getAvailedServiceDetails = function(service){
        sweetAlert(service.visitType);
        $http.get('/afya-portal/getVisitHistoryConsolidated?fromDate=&toDate=&visitType='+service.visitType+'&clinicName=&doctorName=').success(function(data){
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
        if(angular.equals(data.visitType, 'Premium Appointment')){
            data.visitType = "Premium Visit";
        }
        if(angular.equals(data.visitType, 'Tele Consultation')){
            data.visitType = "Tele Consultation Visit";
        }
        $http.get('/afya-portal/getVisitHistoryConsolidated?&fromDate=&toDate=&visitType='+data.visitType+'&clinicName=&doctorName=').success(function(list){
            $scope.listOfAvailedService = list;
        }).error(function(error){
            sweetAlert("error fetching data");
        });
    };

    $scope.availService = function(service){
       // $window.location.href = '/afya-portal/web_pages/afya_member_request/appointments.html?visitType='+service.visitType;
       //$state.go("#appointment?visitType="+service.visitType);
        if(angular.equals(service.visitType, 'Premium Appointment')){
            service.visitType = "Premium Visit";
        }
        if(angular.equals(service.visitType, 'Tele Consultation')){
            service.visitType = "Tele Consultation Visit";
        }
       if(service.visitType == "Appointment Request")
            $state.go('appointmentRequest', {cache: false});
       else if(service.visitType == "Home Pharmacy")
            $state.go('homePharmacy', {cache: false});
       else
            $state.go('appointment',{visitType : service.visitType});
    };

    $scope.constructData = function(data){
        angular.forEach(data, function(eachData){
            if(angular.equals(eachData.visitType, 'Premium Visit')){
                eachData.visitType = "Premium Appointment";
            }
            if(angular.equals(eachData.visitType, 'Tele Consultation Visit')){
                eachData.visitType = "Tele Consultation";
            }
        });
        return data;
    };

    $scope.availServiceFromServiceListItem = function(serviceItem){
        // $('#myModalWin').modal('hide');
        //$("#myModalWin").modal("hide");
       // $window.location.href = '/afya-portal/web_pages/afya_member_request/appointments.html?visitType='+serviceItem.visitType+'&doctor='+serviceItem.doctorId+'&clinic='+serviceItem.clinicTenantId;
        //$state.go('#appointment?visitType='+serviceItem.visitType+'&doctor='+serviceItem.doctorId+'&clinic='+serviceItem.clinicTenantId)
        /*setTimeout(function(){*/
           /*if(serviceItem.visitType == "Consult Visit")   // item - visit Type is Consult Visit for Appointment Request
                $state.go('appointmentRequest', {doctor : serviceItem.doctorId, clinic : serviceItem.clinicTenantId});
           else if(serviceItem.visitType == "Home Pharmacy")
                $state.go('homePharmacy', {clinicId: serviceItem.pharmacyClinicId});
           else
                $state.go('appointment',{visitType : serviceItem.visitType, doctor : serviceItem.doctorId, clinic : serviceItem.clinicTenantId});*/
        /*}, 10000);*/

        $('#myModalWin').on('hidden.bs.modal', availServiceFromServiceListItemInternal);
        $("#myModalWin").modal("hide");
        function  availServiceFromServiceListItemInternal(){
            $('#myModalWin').off('hidden.bs.modal', availServiceFromServiceListItemInternal);

           if(serviceItem.visitType == "Consult Visit")   // item - visit Type is Consult Visit for Appointment Request
                $state.go('appointmentRequest', {doctor : serviceItem.doctorId, clinic : serviceItem.clinicTenantId});
           else if(serviceItem.visitType == "Home Pharmacy")
                $state.go('homePharmacy', {clinicId: serviceItem.pharmacyClinicId});
           else
                $state.go('appointment',{visitType : serviceItem.visitType, doctor : serviceItem.doctorId, clinic : serviceItem.clinicTenantId});

        };
    };

}]);