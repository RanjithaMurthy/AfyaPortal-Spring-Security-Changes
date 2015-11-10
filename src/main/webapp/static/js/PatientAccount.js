/**
 * Created by Mohan Sharma on 7/29/2015.
 */
/*angular.element(document).ready(function(){
    google.load('visualization', '1', { 'callback': function () {
    }, 'packages': ['corechart']
    });
});*/

afyaApp.config(function ($datepickerProvider) {
    angular.extend($datepickerProvider.defaults, {
        dateFormat: 'dd/MM/yyyy',
        autoclose : true,
        startWeek: 1,
        //container: 'body',
    });
});

//PatientAccountModule = angular.module('PatientAccountModule',[])
afyaApp.controller('FinancialOverviewController',['$scope','$http', function($scope, $http){
    $scope.financialOverviewList = [];
    $scope.startDate = null;
    $scope.endDate = null;
    $scope.totalInc = 0;
    $scope.totalPay = 0;
    $scope.total = 0;
    $scope.portalBaseURL = null;
    $scope.afyaId = null;
    $scope.MemberType = null;

    angular.element(document).ready(function(){
        var username = readCookie("username");
        assignWelcomeUserName();
        var data = getPatientByUsername();
        //$http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
        if(data != null)
        {
            $scope.afyaId = data.afyaId;
            $scope.MemberType = data.MemberType;

            google.load('visualization', '1', { 'callback': function () {
                $scope.initializeNinetyDaysData();
            }, 'packages': ['corechart']
            });
        }
        else
                sweetAlert("error fetching data");
        //}).error(function(data){
        //    sweetAlert("error fetching data");
        //});
    });

    $scope.initializeNinetyDaysData = function(){
        var edate = new Date();
        //$scope.endDate =  edate.getDate() + '/' + ("0" + (edate.getMonth() + 1)).slice(-2) + '/' + edate.getFullYear();
        $scope.endDate =  edate;
        var sDate = new Date(edate.getTime() - 1000*60*60*24*90);
        //$scope.startDate = sDate.getDate() + '/' + ("0" + (sDate.getMonth() + 1)).slice(-2) + '/' + sDate.getFullYear();
        $scope.startDate = sDate;
        $scope.getFinancialOverviewByDate();
    };

    $scope.getFinancialOverviewByDate = function(){
        //alert(new Date($scope.startDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")));
        //var fromDate = new Date($scope.startDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")).toISOString().slice(0,10);
        var fromDate = $scope.startDate;    //new Date($scope.startDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3"));
        fromDate = fromDate.getFullYear() + '-' + ("0" + (fromDate.getMonth() + 1)).slice(-2) + '-' + ("0" + fromDate.getDate()).slice(-2);
        //var thruDate = new Date($scope.endDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")).toISOString().slice(0,10);
        var thruDate = $scope.endDate;  //new Date($scope.endDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3"));
        thruDate = thruDate.getFullYear() + '-' + ("0" + (thruDate.getMonth() + 1)).slice(-2) + '-' + ("0" + thruDate.getDate()).slice(-2);
        //$http.get('../../../static/js/financialData.json').success(function(data){
        $http.get('/afya-portal/getPatientSpendingByServicesConsolidated?fromDate=' + fromDate + '&toDate=' + thruDate).success(function(data) {
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


}]).directive('customBindDirective', function() {
            /*$('.input-daterange1').datepicker({
                todayBtn: "linked",
                format: 'dd/mm/yyyy',
                autoclose: true
            });
    alert('test');*/
    return {}
});

afyaApp.controller('StatisticalOverviewController', ['$scope', '$http', function($scope, $http){
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
    $scope.homePharmacyData = {};
    $scope.appointmentRequestData = {};
    //$scope.appointmentData = {};
    $scope.portalBaseURL = null;
    $scope.afyaId = null;

    angular.element(document).ready(function(){
        var username = readCookie("username");
        var data = getPatientByUsername();
        if(data != null)
        {
            //$http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            $scope.afyaId = data.afyaId;
            google.load('visualization', '1', { 'callback': function () {
                $scope.initializeNinetyDaysData();
            }, 'packages': ['corechart']
            });
        }
        else
            sweetAlert("error fetching data");
        //}).error(function(data){
        //    sweetAlert("error fetching data");
        //});
    });

    $scope.initializeNinetyDaysData = function(){
        var edate = new Date();
        $scope.serviceendDate = edate;  // edate.getDate() + '/' + ("0" + (edate.getMonth() + 1)).slice(-2) + '/' + edate.getFullYear();
        $scope.visitendDate = edate;    // edate.getDate() + '/' + ("0" + (edate.getMonth() + 1)).slice(-2) + '/' + edate.getFullYear();
        var sDate = new Date(edate.getTime() - 1000*60*60*24*90);
        $scope.servicestartDate =  sDate;  // sDate.getDate() + '/' + ("0" + (sDate.getMonth() + 1)).slice(-2) + '/' + sDate.getFullYear();
        $scope.visitstartDate =   sDate;   //sDate.getDate() + '/' + ("0" + (sDate.getMonth() + 1)).slice(-2) + '/' + sDate.getFullYear();

        $scope.getStatisticalServicesChart();
        $scope.getStatisticalVisitCountChart();
    };

    $scope.getStatisticalServicesChart = function(){
        //var fromDate = new Date($scope.servicestartDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")).toISOString().slice(0,10);
        var fromDate = $scope.servicestartDate; //new Date($scope.servicestartDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3"));
        fromDate = fromDate.getFullYear() + '-' + ("0" + (fromDate.getMonth() + 1)).slice(-2) + '-' + ("0" + fromDate.getDate()).slice(-2);
        //var thruDate = new Date($scope.serviceendDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")).toISOString().slice(0,10);
        var thruDate = $scope.serviceendDate;   //new Date($scope.serviceendDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3"));
        thruDate = thruDate.getFullYear() + '-' + ("0" + (thruDate.getMonth() + 1)).slice(-2) + '-' + ("0" + thruDate.getDate()).slice(-2);

        $http.get('/afya-portal/getPatientVisitCountByFacilityConsolidated?fromDate=' + fromDate + '&toDate=' + thruDate).success(function (data) {
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
        //var fromDate = new Date($scope.visitstartDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")).toISOString().slice(0,10);
        var fromDate = $scope.visitstartDate;   // new Date($scope.visitstartDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3"));
        fromDate = fromDate.getFullYear() + '-' + ("0" + (fromDate.getMonth() + 1)).slice(-2) + '-' + ("0" + fromDate.getDate()).slice(-2);
        //var thruDate = new Date($scope.visitendDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3")).toISOString().slice(0,10);
        var thruDate = $scope.visitendDate; // new Date($scope.visitendDate.replace( /(\d+)[-/](\d+)[-/](\d+)/, "$2/$1/$3"));
        thruDate = thruDate.getFullYear() + '-' + ("0" + (thruDate.getMonth() + 1)).slice(-2) + '-' + ("0" + thruDate.getDate()).slice(-2);

        $http.get('/afya-portal/getPatientVisitCountByServicesConsolidated?fromDate=' + fromDate + '&toDate=' + thruDate).success(function (data) {
            /*// remove appointment request from the data
            data = $.grep(data, function(val){return val.visitType != 'Appointment Request'});*/
            // sort data to match the order in table
            var sortOrder = ["Tele Consultation Visit","Home Visit","Premium Visit","Home Pharmacy","Appointment Request"];
            data.sort(function(a,b){
                return sortOrder.indexOf(a.visitType) > sortOrder.indexOf(b.visitType);
            });
            // classify data to display
            angular.forEach(data, function (eachData) {
                if (angular.equals(eachData.visitType, 'Tele Consultation Visit'))
                    $scope.teleconsultantData = eachData;
                if (angular.equals(eachData.visitType, 'Home Visit'))
                    $scope.homeVisitData = eachData;
                if (angular.equals(eachData.visitType, 'Premium Visit'))
                    $scope.premiumVisitData = eachData;
                if (angular.equals(eachData.visitType, 'Home Pharmacy'))
                    $scope.homePharmacyData = eachData;
                if (angular.equals(eachData.visitType, 'Appointment Request'))
                    $scope.appointmentRequestData = eachData;
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

}]).directive('customBindDirective', function() {
                /*$('.input-daterange2').datepicker({
                    todayBtn: "linked",
                    format: 'dd/mm/yyyy',
                    autoclose: true
                });
       alert('test');*/
       return {}
   });

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
        width: 550,
        height: 200,
        isStacked: false,
        bar: { groupWidth: '20%' },
        chartArea: {  left: 70, width: "60%" },
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
        width: 550,
        height: 200,
        isStacked: false,
        bar: { groupWidth: '40%' },
        chartArea: {  left: 70, width: "60%" },
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
        width: 550,
        height: 200,
        isStacked: true,
        bar: { groupWidth: '20%' },
        chartArea: {  left: 70, width: "60%" },
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
