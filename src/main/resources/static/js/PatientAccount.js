/**
 * Created by Mohan Sharma on 7/29/2015.
 */
/*angular.element(document).ready(function(){
    google.load('visualization', '1', { 'callback': function () {
    }, 'packages': ['corechart']
    });
});*/

PatientAccountModule = angular.module('PatientAccountModule',[])
PatientAccountModule.controller('FinancialOverviewController',['$scope','$http', function($scope, $http){
    $scope.financialOverviewList = [];
    $scope.startDate = null;
    $scope.endDate = null;
    $scope.totalInc = 0;
    $scope.totalPay = 0;
    $scope.total = 0;
    $scope.portalBaseURL = null;
    $scope.afyaId = null;
    angular.element(document).ready(function(){
        var username = readCookie("username");
        $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            $scope.afyaId = data.afyaId;

            google.load('visualization', '1', { 'callback': function () {
                $scope.initializeNinetyDaysData();
            }, 'packages': ['corechart']
            });
        }).error(function(data){
            sweetAlert("error fetching data");
        });
    });

    $scope.initializeNinetyDaysData = function(){
        var edate = new Date();
        $scope.endDate = (edate.getMonth() + 1) + '/' + edate.getDate() + '/' + edate.getFullYear();
        var sDate = new Date(edate.getTime() - 1000*60*60*24*90);
        $scope.startDate = (sDate.getMonth() + 1) + '/' +  sDate.getDate() + '/' + sDate.getFullYear();
        $scope.getFinancialOverviewByDate();
    };

    $scope.getFinancialOverviewByDate = function(){
        var fromDate = new Date($scope.startDate).toISOString().slice(0,10);
        var thruDate = new Date($scope.endDate).toISOString().slice(0,10);
        //$http.get('../../../static/js/financialData.json').success(function(data){
        $http.get('/afya-portal/anon/getPatientSpendingByServicesConsolidated?afyaId='+$scope.afyaId+'&fromDate=' + fromDate + '&toDate=' + thruDate).success(function(data) {
            $scope.financialOverviewList = data;
            var totalInc = 0;
            var totalPay = 0;
            var total = 0;
            angular.forEach($scope.financialOverviewList, function (eachData) {
                totalInc += eachData.insurancePayable;
                totalPay += eachData.patientPayable;
                total += eachData.amount;
            });
            $scope.totalInc = totalInc;
            $scope.totalPay = totalPay;
            $scope.total = total;

            drawFinancialChart(data);
        }).error(function(data){
            sweetAlert(data);
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


}]);

PatientAccountModule.controller('StatisticalOverviewController', ['$scope', '$http', function($scope, $http){
    $scope.servicestartDate = null;
    $scope.serviceendDate = null;
    $scope.visitstartDate = null;
    $scope.visitendDate = null;
    $scope.clinicData = {};
    $scope.pharmacyData = {};
    $scope.labData = {};
    $scope.teleconsultantData = {};
    $scope.homeVisitData = {};
    $scope.premiumVisitData = {};
    $scope.appointmentData = {};
    $scope.portalBaseURL = null;
    $scope.afyaId = null;

    angular.element(document).ready(function(){
        var username = readCookie("username");
        $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            $scope.afyaId = data.afyaId;
            $scope.initializeNinetyDaysData();
        }).error(function(data){
            sweetAlert("error fetching data");
        });
    });

    $scope.initializeNinetyDaysData = function(){
        var edate = new Date();
        $scope.serviceendDate = (edate.getMonth() + 1) + '/' + edate.getDate() + '/' + edate.getFullYear();
        $scope.visitendDate = (edate.getMonth() + 1) + '/' + edate.getDate() + '/' + edate.getFullYear();
        var sDate = new Date(edate.getTime() - 1000*60*60*24*90);
        $scope.servicestartDate = (sDate.getMonth() + 1) + '/' +  sDate.getDate() + '/' + sDate.getFullYear();
        $scope.visitstartDate = (sDate.getMonth() + 1) + '/' +  sDate.getDate() + '/' + sDate.getFullYear();

        $scope.getStatisticalServicesChart();
        $scope.getStatisticalVisitCountChart();
    };

    $scope.getStatisticalServicesChart = function(){
        var fromDate = new Date($scope.servicestartDate).toISOString().slice(0,10);
        var thruDate = new Date($scope.serviceendDate).toISOString().slice(0,10);
        $http.get('/afya-portal/anon/getPatientVisitCountByFacilityConsolidated?afyaId='+$scope.afyaId+'&fromDate=' + fromDate + '&toDate=' + thruDate).success(function (data) {
            angular.forEach(data, function (eachData) {
                if (angular.equals(eachData.Clinics, 'Clinics'))
                    $scope.clinicData = eachData;
                if (angular.equals(eachData.Clinics, 'Pharmacies'))
                    $scope.pharmacyData = eachData;
                if (angular.equals(eachData.Clinics, 'Labs'))
                    $scope.labData = eachData;
            })
            // plot the chart
            drawStatisticalServicesChart(data);
        }).error(function(data){
            sweetAlert(data.error);
        });



    };
    $scope.getStatisticalVisitCountChart = function(){
        var fromDate = new Date($scope.visitstartDate).toISOString().slice(0,10);
        var thruDate = new Date($scope.visitendDate).toISOString().slice(0,10);


        $http.get('/afya-portal/anon/getPatientVisitCountByServicesConsolidated?afyaId='+$scope.afyaId+'&fromDate=' + fromDate + '&toDate=' + thruDate).success(function (data) {
            angular.forEach(data, function (eachData) {
                if (angular.equals(eachData.visitType, 'Tele Consultation Visit'))
                    $scope.teleconsultantData = eachData;
                if (angular.equals(eachData.visitType, 'Home Visit'))
                    $scope.homeVisitData = eachData;
                if (angular.equals(eachData.visitType, 'Premium Visit'))
                    $scope.premiumVisitData = eachData;
            });
            // plot the chart
            drawStatisticalVisitCountChart(data);
        }).error(function(data){
            sweetAlert(data.error);
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

}]);

function drawStatisticalVisitCountChart(objVisitCount) {
    //        var jsonVisitCount =  '[{"visitCount": 2,"visitType": "Premium Visit"},{"visitCount": 3,"visitType": "Tele Consultation Visit"},{"visitCount": 5,"visitType": "Home Visit"}]';
    //        var objVisitCount = JSON.parse(jsonVisitCount);
    var arrVisitCount = [];
    var length = objVisitCount.length;
    arrVisitCount.push(['Visit Type', 'Visit Count']);
    for (var i = 0; i < length; i++) {
        arrVisitCount.push([objVisitCount[i].visitType, objVisitCount[i].visitCount]);
    }
    var data = new google.visualization.arrayToDataTable(arrVisitCount);
    var options = {
        title: 'Statistical - by Services',
        width: 500,
        height: 200,
        isStacked: false,
        bar: { groupWidth: '20%' },
        vAxis: {
            minValue : 0,
            viewWindow: {
                min: 0
            }
        }
    };
    drawChart(data, options, 'chartStaVisit');
}

function drawStatisticalServicesChart(objService) {
    //        var jsonService =  '[{"Clinics": "Clinics","visitCount": "25","facilityCount": "5"},{"Clinics": "Pharmacies","visitCount": "5","facilityCount": "6"},{"Clinics": "Labs","visitCount": "30","facilityCount": "10"}]';
    //        var objService = JSON.parse(jsonService);
    var arrService = [];
    var length = objService.length;
    arrService.push(['Clinics', 'Visit Count', 'Facility Count']);
    for (var i = 0; i < length; i++) {
        arrService.push([objService[i].Clinics, parseInt(objService[i].visitCount), parseInt(objService[i].facilityCount)]);
    }
    var data = new google.visualization.arrayToDataTable(arrService);
    var options = {
        title: 'Statistical - by Visit Count',
        width: 500,
        height: 200,
        isStacked: false,
        bar: { groupWidth: '40%' },
        vAxis: {
            minValue : 0,
            viewWindow: {
                min: 0
            }
        }
    };
    drawChart(data, options, 'chartStaServices');
}

/*function drawFinancialChart(objFinancial) {
 //        var jsonFinancial =  '[{"Service": "Consultation","patientPayable": 30,"insurancePayable": 50,"amount": 0},{"Service": "Procedure","patientPayable": 10,"insurancePayable": 90,"amount": 0}]';
 //        var objFinancial = JSON.parse(jsonFinancial);
 var arrFinancial = [];
 var length = objFinancial.length;
 arrFinancial.push(['Service', 'Patient', 'Insurance']);
 for (var i = 0; i < length; i++) {
 arrFinancial.push([objFinancial[i].Service, objFinancial[i].patientPayable, objFinancial[i].insurancePayable]);
 }
 var data = new google.visualization.arrayToDataTable(arrFinancial);
 var options = {
 title: 'Financial',
 width: 500,
 height: 200,
 isStacked: true,
 bar: { groupWidth: '20%' },
 vAxis: {
 minValue : 0,
 viewWindow: {
 min: 0
 }
 }
 };
 drawChart(data, options, 'chartFinancial');
 }*/

// modified on 13/08/2015
function drawFinancialChart(objFinancial) {
    var arrFinancial = [];
    var length = objFinancial.length;
    var serviceList = [];
    var instanceList = [];
    var patientList = [];
    serviceList.push('Service')
    for (var i = 0; i < length; i++) {
        serviceList.push(objFinancial[i].Service);
    }
    arrFinancial.push(serviceList);
    instanceList.push('Insurance')
    for (var i = 0; i < length; i++) {
        instanceList.push(objFinancial[i].insurancePayable);
    }
    arrFinancial.push(instanceList);
    patientList.push('Patient')
    for (var i = 0; i < length; i++) {
        patientList.push(objFinancial[i].patientPayable);
    }
    arrFinancial.push(patientList);
    var data = new google.visualization.arrayToDataTable(arrFinancial);
    var options = {
        title: 'Financial',
        width: 500,
        height: 200,
        isStacked: true,
        bar: { groupWidth: '20%' },
        vAxis: {
            minValue : 0,
            viewWindow: {
                min: 0
            }
        }
    };
    drawChart(data, options, 'chartFinancial');
}

function drawChart(data, options, id) {
    var chart = new google.visualization.ColumnChart(document.getElementById(id));
    chart.draw(data, options);
}
