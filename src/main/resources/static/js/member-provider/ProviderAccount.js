angular.element(document).ready(function(){
    google.load('visualization', '1', { 'callback': function () {
    }, 'packages': ['corechart']
    });
});
angular.module('ProviderAccountModule',[])
    .controller('YesterdayRevenue',['$scope','$http', function($scope, $http){
        $scope.data={};

        var myDate = new Date();
        var previousDay = new Date(myDate);
        previousDay.setDate(myDate.getDate()-1);

        $scope.yesterDayDate = previousDay;

        $http.get('/afya-portal/membercare/yesterdayClinicRevenue').then(function (response) {
            $scope.data = response.data;
            $scope.dataPercent={};

            var rev = $scope.data.totalRevenue;
            $scope.dataPercent.totalRevenue = Math.round($scope.data.totalRevenue*1000)/1000;
            $scope.dataPercent.clinicRevenue = Math.round((($scope.data.clinicRevenue/rev)*100)*1000)/1000;
            $scope.dataPercent.smartServicesRevenue = Math.round((($scope.data.smartServicesRevenue/rev)*100)*1000)/1000;
            $scope.dataPercent.clinicReferralAmount =  Math.round((($scope.data.clinicReferralAmount/rev)*100)*1000)/1000;

            //alert($scope.data.smartServicesRevenue);
        });

    }]).controller('MonthRevenue',['$scope','$http', function($scope, $http){
        $scope.data={};

        var myDate = new Date();
        var previousDay = new Date(myDate);
        previousDay.setDate(myDate.getDate()-1);

        $scope.yesterDayDate = previousDay;

        $http.get('/afya-portal/membercare/monthClinicRevenue').then(function (response) {
            $scope.data = response.data;

            $scope.dataPercent={};

            var rev = $scope.data.totalRevenue;
            $scope.dataPercent.totalRevenue = Math.round($scope.data.totalRevenue*1000)/1000;
            $scope.dataPercent.clinicRevenue = Math.round((($scope.data.clinicRevenue/rev)*100)*1000)/1000;
            $scope.dataPercent.smartServicesRevenue = Math.round((($scope.data.smartServicesRevenue/rev)*100)*1000)/1000;
            $scope.dataPercent.clinicReferralAmount =  Math.round((($scope.data.clinicReferralAmount/rev)*100)*1000)/1000;
        });

    }]).controller('ServiceIncomeAnalysis',['$scope','$http', function($scope, $http){
                $scope.datas={};
                $scope.disp = true;
                $scope.show = function() {
                $scope.disp = false;
                //alert('Data..'+JSON.stringify($scope.datas));
                $scope.dateRange={};
                $scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
                $scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);
                //alert('Date Range..'+JSON.stringify($scope.dateRange));
                $http.post('/afya-portal/membercare/incomeAnalysisByServiceType',JSON.stringify($scope.dateRange)).then(function (response) {
                    $scope.datas = response.data;
                    //alert('Data..'+JSON.stringify(response));
                    if($scope.datas.length > 0){
                    var title = 'Income Analysis- By Service Type';//graph title
                    var id = 'chartServiceTypeIncomeAnalysis';//graph title
                    var arrIncome = [];
                    var length = $scope.datas.length;
                    arrIncome.push(['description', 'Amount']);
                    for (var i = 0; i < length; i++) {
                        arrIncome.push([$scope.datas[i].description, $scope.datas[i].totalAmount]);
                        }
                    drawIncomeAnalysisChart(arrIncome,title,id);

                    }else{
                        $scope.disp = true;
                    }
                    },function (response){
                    //alert("error");
                    });
                }
                $scope.hide = function(){
                    $scope.datas=null;
                    $scope.disp = true;
                }

    }]).controller('SpecialtyIncomeAnalysis',['$scope','$http', function($scope, $http){
                $scope.datas={};
                $scope.disp=true;
                $scope.show = function() {
                $scope.disp = false;
                $scope.dateRange={};
                $scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
                $scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);
                //alert('Date Range..'+JSON.stringify($scope.dateRange));

                $http.post('/afya-portal/membercare/incomeAnalysisBySpecialty   ',JSON.stringify($scope.dateRange)).then(function (response) {
                    $scope.datas = response.data;
                    //alert('Data..'+JSON.stringify(response));
                    if($scope.datas.length > 0){
                    var title = 'Income Analysis- By Specialty';//graph title
                    var id = 'chartSpecialtyIncomeAnalysis';//graph title
                    var arrIncome = [];
                    var length = $scope.datas.length;
                    arrIncome.push(['speciality', 'totalAmount']);
                    for (var i = 0; i < length; i++) {
                        arrIncome.push([$scope.datas[i].speciality, $scope.datas[i].totalAmount]);
                        }
                    drawIncomeAnalysisChart(arrIncome,title,id);}else{$scope.disp = true;}
                    },function (response){
                    //alert("error");
                    });
                }
                $scope.hide = function(){
                       $scope.datas=null;
                       $scope.disp = true;
                       }
    }]).controller('NewNetworkRequestController',['$scope','$http','$location', function($scope, $http,$location){
        $scope.datas={};
        //alert("controller called");
        $http.get('/afya-portal/anon/getAllClinicsAndPharmacies').then(function (response) {
            $scope.datas = response.data;
        });

      $scope.sendResponse = function(clinic,index){
                  /*$http.post('/afya-portal/membercare/inviteNetwork',clinic).success(successcb).error(errorcb);*/
                  $http.post('/afya-portal/membercare/inviteNetwork',clinic).success(function(data){}).error(function(data){});
                  $scope.datas.splice( index, 1 );
                  //$location.path('/Provider_account_MyNetwork_NewNetworkRequest1.html');
                  /*dialogs.create('/newNetworkReq-dialog.html','whatsYourNameCtrl',{},{key: false,back: 'static'});*/
                  /*dialog('/newNetworkReq-dialog.html',
                      { id: 'rejectionModal',
                          title: "Approve/Reject",
                          successClass: 'hide',
                          controller: 'ProjectCharterApprovalCtrl',
                          css: {top: '360px'}
                      }, { clinic: clinic });*/

                  /*createDialog('/newNetworkReq-dialog.html',
                      {
                          title: "Rejection Message",

                          controller: 'NetworkRequestReceivedController'

                      }, { clinic: clinic });*/


              };

    }]).controller('NetworkRequestReceivedController',['$scope','$http','$location', function($scope, $http,$location){
        $scope.datas={};
        $http.get('/afya-portal/membercare/getAllNetworkRequested').then(function (response) {
            $scope.datas = response.data;
        });
        $scope.accept = function(networkDto,index){
        //$scope.datas.splice( index, 1 );
            $http.post('/afya-portal/membercare/acceptNetwork',networkDto).then(function(response) {
                $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
            }, function(response) {
            });
            $scope.datas.splice( index, 1 );
        }
        $scope.reject = function(networkDto,index){
            $http.post('/afya-portal/membercare/rejectNetwork',networkDto).then(function(response) {
                $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
            }, function(response) {
            });
            $location.path('/Provider_account_MyNetwork_NewNetworkRequest1.html');
            $scope.datas.splice( index, 1 );
        }
        $scope.block = function(networkDto,index){
                    $http.post('/afya-portal/membercare/blockNetwork',networkDto).then(function(response) {
                        $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
                    }, function(response) {
                    });
                    $location.path('/Provider_account_MyNetwork_NewNetworkRequest1.html');
                    $scope.datas.splice( index, 1 );
                }
    }]).controller('NetworkExistingNetworkController',['$scope','$http','$location', function($scope, $http,$location){
        $scope.datas={};
        $http.get('/afya-portal/membercare/getAllAcceptedNetworkRequested').then(function (response) {
            $scope.datas = response.data;
        });
    }]).controller('DoctorIncomeAnalysis',['$scope','$http', function($scope, $http){
        $scope.datas={};
        $scope.disp=true;
        $scope.show = function() {
        $scope.disp = false;
        $scope.dateRange={};
        $scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
        $scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);
        //alert('Date Range..'+JSON.stringify($scope.dateRange));

        $http.post('/afya-portal/membercare/incomeAnalysisByDoctor',JSON.stringify($scope.dateRange)).then(function (response) {
            $scope.datas = response.data;
            //alert('Data..'+JSON.stringify(response));
            if($scope.datas.length > 0){
            var title = 'Income Analysis- By Doctor';//graph title
            var id = 'chartDoctorIncomeAnalysis';//graph title
            var arrIncome = [];
            var length = $scope.datas.length;
            arrIncome.push(['doctor', 'amount']);
            for (var i = 0; i < length; i++) {
                arrIncome.push([$scope.datas[i].doctor, $scope.datas[i].amount]);
                }
            drawIncomeAnalysisChart(arrIncome,title,id);}else{$scope.disp = true;}
            },function (response){
            //alert("error");
            });
        }
        $scope.hide = function(){
                $scope.datas=null;
                $scope.disp = true;
                }
    }]).controller('PatientByCategoryIncomeAnalysis',['$scope','$http', function($scope, $http){
               $scope.datas={};
               $scope.disp=true;
               $scope.show = function() {
               $scope.disp = false;
               $scope.dateRange={};
               $scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
               $scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);

               $http.post('/afya-portal/membercare/incomeAnalysisByPatientCategory',JSON.stringify($scope.dateRange)).then(function (response) {
                   $scope.datas = response.data;
                   var title = 'Income Analysis- By Patient Category';//graph title
                   var id = 'chartPatientByCategoryIncomeAnalysis';//graph div id
                   //alert('Data..'+JSON.stringify(response));
                   if($scope.datas.length > 0){
                   var arrIncome = [];
                   var length = $scope.datas.length;
                   arrIncome.push(['category', 'amount']);
                       for (var i = 0; i < length; i++) {
                           arrIncome.push([$scope.datas[i].category, $scope.datas[i].amount]);
                       }

                   drawIncomeAnalysisChart(arrIncome,title,id);}else{$scope.disp = true;}
                   },function (response){
                   //alert("error");
                   });
               }
                $scope.hide = function(){
                      $scope.datas=null;
                      $scope.disp = true;
                      }
    }]).controller('InitiatedContractController',['$scope','$http', function($scope, $http){
                      $scope.datas={};
                      $http.get('/afya-portal/membercare/getAllInitiatedContract').then(function (response) {
                      //alert('Data..'+JSON.stringify(response));
                                  $scope.datas = response.data;
                              });
    }]).controller('BlockNetworkRequestController',['$scope','$http', function($scope, $http){
                      $scope.datas={};
                      $http.get('/afya-portal/membercare/getAllBlockedNetworkRequest').then(function (response) {
                      //alert('Data..'+JSON.stringify(response));
                      $scope.datas = response.data;
                      });
                      $scope.unBlock = function(networkDto,index){
                            $http.post('/afya-portal/membercare/unBlockNetwork',networkDto).then(function(response) {
                            $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
                            }, function(response) {
                            });
                      $scope.datas.splice( index, 1 );
                      }
            }]);

function drawIncomeAnalysisChart(arrIncome,title,id) {
    var data = new google.visualization.arrayToDataTable(arrIncome);
    var options = {
        title: title,
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
    drawChart(data, options, id);
}

function drawChart(data, options, id) {
    var chart = new google.visualization.ColumnChart(document.getElementById(id));
    chart.draw(data, options);
}