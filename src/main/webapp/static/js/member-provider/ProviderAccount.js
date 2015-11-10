angular.element(document).ready(function(){
    google.load('visualization', '1', { 'callback': function () {
    }, 'packages': ['corechart']
    });
});
//angular.module('ProviderAccountModule',[])
afyaApp.controller('YesterdayRevenue',['$scope','$http', function($scope, $http){
        $scope.data={};
        var myDate = new Date();
        var previousDay = new Date(myDate);
        //previousDay.setDate(myDate.getDate()-1);
        $scope.yesterDayDate = previousDay;

        $http.get('/afya-portal/anon/membercare/yesterdayClinicRevenue').then(function (response) {
            $scope.data = response.data;
            $scope.data.referralAmount = '0.000';
            $scope.data.providerType = capitalizeFirstLetter($scope.data.providerType);
            $scope.dataPercent={};
            $http.get('/afya-portal/anon/membercare/getYesterDayReferralAmount').then(function (response) {
                $scope.data.referralAmount = response.data;
            });
            var rev = $scope.data.totalRevenue;
            //alert($scope.data.clinicRevenue);
            if(rev > 0){
            $scope.dataPercent.totalRevenue = Math.round($scope.data.totalRevenue*1000)/1000;
            $scope.dataPercent.clinicRevenue = Math.round((($scope.data.clinicRevenue/rev)*100)*1000)/1000;
            $scope.dataPercent.smartServicesRevenue = Math.round((($scope.data.smartServicesRevenue/rev)*100)*1000)/1000;
            $scope.dataPercent.clinicReferralAmount =  Math.round((($scope.data.referralAmount/rev)*100)*1000)/1000;
            $scope.data.clinicRevenue = $scope.data.clinicRevenue > 0 ? $scope.data.clinicRevenue : "0.000" ;
            $scope.data.smartServicesRevenue = $scope.data.smartServicesRevenue > 0 ? $scope.data.smartServicesRevenue : "0.000" ;
            }else{
            $scope.data.totalRevenue = '0.000';
            $scope.data.clinicRevenue = '0.000';
            $scope.data.smartServicesRevenue = '0.000';
            $scope.data.referralAmount = '0.000';
            $scope.dataPercent.clinicRevenue = '0';
            $scope.dataPercent.smartServicesRevenue = '0';
            $scope.dataPercent.clinicReferralAmount =  '0';
            }
            //alert($scope.data.smartServicesRevenue);
        });

    }]).controller('MonthRevenue',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
        $scope.data={};

        var myDate = new Date();
        var previousDay = new Date(myDate);
        //previousDay.setDate(myDate.getDate()-1);

        $scope.yesterDayDate = previousDay;

        $http.get('/afya-portal/anon/membercare/monthClinicRevenue').then(function (response) {
            $scope.data = response.data;
            //alert(JSON.stringify($scope.data));
            $scope.data.referralAmount = '0.000';
            $scope.data.providerType = capitalizeFirstLetter($scope.data.providerType);
            $http.get('/afya-portal/anon/membercare/getMonthReferralAmount').then(function (response) {
                $scope.data.referralAmount = response.data;
            });
            $scope.dataPercent={};
            var rev = $scope.data.totalRevenue;
            if(rev > 0){
                $scope.dataPercent.totalRevenue = Math.round($scope.data.totalRevenue*1000)/1000;
                $scope.dataPercent.clinicRevenue = Math.round((($scope.data.clinicRevenue/rev)*100)*1000)/1000;
                $scope.dataPercent.smartServicesRevenue = Math.round((($scope.data.smartServicesRevenue/rev)*100)*1000)/1000;
                $scope.dataPercent.clinicReferralAmount =  Math.round((($scope.data.referralAmount/rev)*100)*1000)/1000;
                $scope.data.clinicRevenue = $scope.data.clinicRevenue > 0 ? $scope.data.clinicRevenue : "0.000";
                $scope.data.smartServicesRevenue = $scope.data.smartServicesRevenue > 0 ? $scope.data.smartServicesRevenue : "0.000";
            }
            else{
                $scope.data.totalRevenue = '0.000';
                $scope.data.clinicRevenue = '0.000';
                $scope.data.smartServicesRevenue = '0.000';
                $scope.data.referralAmount = '0.000';
                $scope.dataPercent.clinicRevenue = '0';
                $scope.dataPercent.smartServicesRevenue = '0';
                $scope.dataPercent.clinicReferralAmount =  '0';
            }
        });

    }]).controller('ServiceIncomeAnalysis',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
        $scope.datas = {};
        $scope.disp = true;
        $scope.show = function() {
            $scope.disp = false;
            //alert('Data..'+JSON.stringify($scope.datas));
            $scope.dateRange={};
            var startDate = new Date($scope.startDate);
            $scope.dateRange.startDate = startDate.getFullYear() + '-' + ("0" + (startDate.getMonth() + 1)).slice(-2) + '-' + ("0" + startDate.getDate()).slice(-2);
            var endDate =new Date($scope.endDate);
            $scope.dateRange.endDate = endDate.getFullYear() + '-' + ("0" + (endDate.getMonth() + 1)).slice(-2) + '-' + ("0" + endDate.getDate()).slice(-2);
            /*$scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
            $scope.dateRange.endDate = new Date($scope.endDate).toISOString().slice(0,10);*/
            //alert('Date Range..'+JSON.stringify($scope.dateRange));
            $http.post('/afya-portal/anon/membercare/incomeAnalysisByServiceType',JSON.stringify($scope.dateRange)).then(function (response) {
                $scope.datas = response.data;
                //alert('Data..'+JSON.stringify(response));
                if($scope.datas.length > 0){
                    var title = 'Income Analysis- By Service Type';//graph title
                    var id = 'chartServiceTypeIncomeAnalysis';//graph title
                    var arrIncome = [];
                    var length = $scope.datas.length;
                    arrIncome.push(['description', 'Revenue']);
                    for (var i = 0; i < length; i++) {
                        arrIncome.push([$scope.datas[i].description, $scope.datas[i].totalAmount]);
                    }
                    drawIncomeAnalysisChart(arrIncome,title,id);
                }
                else{
                    $scope.disp = true;
                }
                },function (response){
                //alert("error");
            });
        }

        $scope.hide = function(){
            $scope.datas = null;
            $scope.disp = true;
        }

    }]).controller('SpecialtyIncomeAnalysis',['$scope','$http', function($scope, $http){
                assignWelcomeUserName();
                $scope.datas={};
                $scope.disp=true;
                $scope.show = function() {
                $scope.disp = false;
                $scope.dateRange={};
                var startDate = new Date($scope.startDate);
                $scope.dateRange.startDate = startDate.getFullYear() + '-' + ("0" + (startDate.getMonth() + 1)).slice(-2) + '-' + ("0" + startDate.getDate()).slice(-2);
                var endDate =new Date($scope.endDate);
                $scope.dateRange.endDate = endDate.getFullYear() + '-' + ("0" + (endDate.getMonth() + 1)).slice(-2) + '-' + ("0" + endDate.getDate()).slice(-2);
                //$scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
                //$scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);
                //alert('Date Range..'+JSON.stringify($scope.dateRange));

                $http.post('/afya-portal/anon/membercare/incomeAnalysisBySpecialty   ',JSON.stringify($scope.dateRange)).then(function (response) {
                    $scope.datas = response.data;
                    //alert('Data..'+JSON.stringify(response));
                    if($scope.datas.length > 0){
                    var title = 'Income Analysis- By Specialty';//graph title
                    var id = 'chartSpecialtyIncomeAnalysis';//graph title
                    var arrIncome = [];
                    var length = $scope.datas.length;
                    arrIncome.push(['speciality', 'Revenue']);
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
        assignWelcomeUserName();
        $scope.datas={};
        //alert("controller called");
        // get provider member type
        var data = getProviderByUsername();
        $scope.MemberType = data.memberType;// assign MemberType Scope to Display Visitor Message
        $http.get('/afya-portal/anon/getAllClinicsAndPharmacies').then(function (response) {
            $scope.datas = response.data;
            //alert('Data..'+JSON.stringify(response));
        });

      $scope.sendResponse = function(clinic,index){
      //alert("data "+JSON.stringify(clinic));
                  /*$http.post('/afya-portal/membercare/inviteNetwork',clinic).success(successcb).error(errorcb);*/
                  $http.post('/afya-portal/anon/membercare/inviteNetwork',clinic).success(function(data){}).error(function(data){});
                  sweetAlert({
                      title: "Success",
                      text: "Thanks for building your Network, you have just invited "+ clinic.clinicName + "  to join Afya Network.",
                      type: "success"//error
                  });
                  $scope.datas.splice( index, 1 );
                  //alert("Network invitation posted successfully. Once the Provider accepts the invitation,it will be listed in Existing network");
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
        assignWelcomeUserName();
        $scope.datas={};
        // get provider member type
        var data = getProviderByUsername();
        $scope.MemberType = data.memberType;// assign MemberType Scope to Display Visitor Message

        $http.get('/afya-portal/anon/membercare/getAllNetworkRequested').then(function (response) {
            $scope.datas = response.data;
        });
        $scope.accept = function(networkDto,index){
        //$scope.datas.splice( index, 1 );
            $http.post('/afya-portal/anon/membercare/acceptNetwork',networkDto).then(function(response) {
                $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
            }, function(response) {
            });
            $scope.datas.splice( index, 1 );
            //alert("Provider invitation accepted and listed in Existing network");
        }
        $scope.reject = function(networkDto,index){
            $http.post('/afya-portal/anon/membercare/rejectNetwork',networkDto).then(function(response) {
                $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
            }, function(response) {
            });
            $location.path('/Provider_account_MyNetwork_NewNetworkRequest1.html');
            $scope.datas.splice( index, 1 );
        }
        $scope.block = function(networkDto,index){
                    $http.post('/afya-portal/anon/membercare/blockNetwork',networkDto).then(function(response) {
                        $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
                    }, function(response) {
                    });
                    $location.path('/Provider_account_MyNetwork_NewNetworkRequest1.html');
                    $scope.datas.splice( index, 1 );
                }
    }]).controller('NetworkExistingNetworkController',['$scope','$http','$location', function($scope, $http,$location){
        assignWelcomeUserName();
        $scope.datas={};
        // get provider member type
        var data = getProviderByUsername();
        $scope.MemberType = data.memberType;// assign MemberType Scope to Display Visitor Message

        $http.get('/afya-portal/anon/membercare/getAllAcceptedNetworkRequested').then(function (response) {
            $scope.datas = response.data;
        });
    }]).controller('DoctorIncomeAnalysis',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
        $scope.datas={};
        $scope.disp=true;
        $scope.show = function() {
        $scope.disp = false;
        $scope.dateRange={};
        var startDate = new Date($scope.startDate);
        $scope.dateRange.startDate = startDate.getFullYear() + '-' + ("0" + (startDate.getMonth() + 1)).slice(-2) + '-' + ("0" + startDate.getDate()).slice(-2);
        var endDate =new Date($scope.endDate);
        $scope.dateRange.endDate = endDate.getFullYear() + '-' + ("0" + (endDate.getMonth() + 1)).slice(-2) + '-' + ("0" + endDate.getDate()).slice(-2);
        //$scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
        //$scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);
        //alert('Date Range..'+JSON.stringify($scope.dateRange));

        $http.post('/afya-portal/anon/membercare/incomeAnalysisByDoctor',JSON.stringify($scope.dateRange)).then(function (response) {
            $scope.datas = response.data;
            //alert('Data..'+JSON.stringify(response));
            if($scope.datas.length > 0){
            var title = 'Income Analysis- By Doctor';//graph title
            var id = 'chartDoctorIncomeAnalysis';//graph title
            var arrIncome = [];
            var length = $scope.datas.length;
            arrIncome.push(['doctor', 'Revenue']);
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
               assignWelcomeUserName();
               $scope.datas={};
               $scope.disp=true;
               $scope.show = function() {
                   $scope.disp = false;
                   $scope.dateRange={};
                   var startDate = new Date($scope.startDate);
                   $scope.dateRange.startDate = startDate.getFullYear() + '-' + ("0" + (startDate.getMonth() + 1)).slice(-2) + '-' + ("0" + startDate.getDate()).slice(-2);
                   var endDate =new Date($scope.endDate);
                   $scope.dateRange.endDate = endDate.getFullYear() + '-' + ("0" + (endDate.getMonth() + 1)).slice(-2) + '-' + ("0" + endDate.getDate()).slice(-2);
                   //$scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
                   //$scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);

                   $http.post('/afya-portal/anon/membercare/incomeAnalysisByPatientCategory',JSON.stringify($scope.dateRange)).then(function (response) {
                       $scope.datas = response.data;
                       var title = 'Income Analysis- By Patient Category';//graph title
                       var id = 'chartPatientByCategoryIncomeAnalysis';//graph div id
                       //alert('Data..'+JSON.stringify(response));
                       if($scope.datas.length > 0){
                       var arrIncome = [];
                       var length = $scope.datas.length;
                       arrIncome.push(['category', 'Revenue']);
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
                      assignWelcomeUserName();
                      $scope.datas={};
                      // get provider member type
                      var data = getProviderByUsername();
                      $scope.MemberType = data.memberType;// assign MemberType Scope to Display Visitor Message

                      $http.get('/afya-portal/anon/membercare/getAllInitiatedContract').then(function (response) {
                      //alert('Data..'+JSON.stringify(response));
                          $scope.datas = response.data;
                      });
    }]).controller('BlockNetworkRequestController',['$scope','$http', function($scope, $http){
                      assignWelcomeUserName();
                      $scope.datas={};
                        // get provider member type
                        var data = getProviderByUsername();
                        $scope.MemberType = data.memberType;// assign MemberType Scope to Display Visitor Message

                      $http.get('/afya-portal/anon/membercare/getAllBlockedNetworkRequest').then(function (response) {
                      //alert('Data..'+JSON.stringify(response));
                      $scope.datas = response.data;
                      });
                      $scope.unBlock = function(networkDto,index){
                            $http.post('/afya-portal/anon/membercare/unBlockNetwork',networkDto).then(function(response) {
                            $location.path('/Provider_account_MyNetwork_NewNetworkRequest.html');
                            }, function(response) {
                            });
                        $scope.datas.splice( index, 1 );
                      }
    }]).controller('ReferralDataController',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
        $scope.data = {};
        $scope.datas = {};
        $scope.disp = true;

        // show Referral Income Panel
        $scope.show = function() {
            $scope.disp = false;
            /*var myDate = new Date();
             var previousDay = new Date(myDate);
             previousDay.setDate(myDate.getDate()-60);
             $scope.toDate = new Date();
             $scope.startDate = previousDay;*/
             $http.get('/afya-portal/anon/membercare/getReferralData').then(function (response) {
                 $scope.datas = response.data;
                 //return false;
             });
             $scope.submit = function(){
                 $scope.dateRange={};
                 var startDate = new Date($scope.startDate);
                 $scope.dateRange.startDate = startDate.getFullYear() + '-' + ("0" + (startDate.getMonth() + 1)).slice(-2) + '-' + ("0" + startDate.getDate()).slice(-2);
                 var endDate =new Date($scope.endDate);
                 $scope.dateRange.endDate = endDate.getFullYear() + '-' + ("0" + (endDate.getMonth() + 1)).slice(-2) + '-' + ("0" + endDate.getDate()).slice(-2);
                 //$scope.dateRange.startDate= new Date($scope.startDate).toISOString().slice(0,10);
                 //$scope.dateRange.endDate=new Date($scope.endDate).toISOString().slice(0,10);
                 $http.post('/afya-portal/anon/membercare/getReferralDataByDate',JSON.stringify($scope.dateRange)).then(function (response) {
                       $scope.datas = response.data;
                 },function (response){
                       //alert("error");
                 });
             }
        }

        // hide Referral Income Panel
        $scope.hide = function() {
            $scope.datas = null;
            $scope.disp = true;
        }

    }]).controller('LoyaltyAccountController',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
    }]).controller('LoyaltyInformationController',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
    }]).controller('AfyaPoliciesController',['$scope','$http', function($scope, $http){
        assignWelcomeUserName();
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

function capitalizeFirstLetter(string) {
    return (string.charAt(0).toUpperCase()) + (string.slice(1).toLowerCase());
}