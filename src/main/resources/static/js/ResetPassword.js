
angular.module('ForgotPasswordModule',['ngRoute'])
 .controller('ResetPasswordRequestController',['$scope','$http', '$route', '$location', function($scope, $http, $route, $location){
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