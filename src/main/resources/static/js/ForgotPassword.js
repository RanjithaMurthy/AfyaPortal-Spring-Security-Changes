angular.module('ForgotPasswordModule',['ngRoute'])
 .controller('ForgotPasswordController',['$scope','$http', function($scope, $http){
 alert("hi");
        $scope.datas={};
        $scope.isSuccess = false;
        $scope.continue = function(){
        alert('Data..'+JSON.stringify($scope.userName));
        /*$http.post('/afya-portal/anon/getEmailId',$scope.userName).then(function(response) {
                    alert("success");
                    //alert('Data..'+JSON.stringify(response));
                    //$scope.datas = response.data;
                    }, function(response) {
                    alert("error");
                    });*/
        $http.post('/afya-portal/anon/getEmailId',$scope.userName).success(function(response) {
                            alert("success.. "+SON.stringify(response));
                            //alert('Data..'+JSON.stringify(response));
                            //$scope.datas = response.data;
                            }).error(function(response) {
                            alert("err.. "+JSON.stringify(response));
                            });
        $scope.isSuccess = true;
        }

	}]).controller('ResetPasswordRequestController',['$scope','$http', '$route', '$location', function($scope, $http, $route, $location){
               $scope.datas={};
               //alert(" .."+JSON.stringify($location.search()));
               //alert($location.search().user);
               var user = $location.search().user;
               $scope.isSubmit=true;
               $scope.isSuccess = false;
               $scope.submit = function(){
               if($scope.newPassword == $scope.confirmPassword){
                   $scope.userDetails = {};
                   $scope.userDetails.userName = user;
                   $scope.userDetails.newPassword = $scope.newPassword;
                   alert('Data..'+JSON.stringify($scope.userDetails));
                   $http.post('/afya-portal/anon/resetPassword',JSON.stringify($scope.userDetails)).then(function(response) {
                               alert('Data..'+JSON.stringify(response));
                               //$scope.datas = response.data;
                               }, function(response) {
                               });
                   $scope.isSuccess = true;
                }else{
                    $scope.isSubmit=false;
                }
               }
       	}]);