
//angular.module('ForgotPasswordModule',['ngRoute'])
 afyaApp.controller('ResetPasswordRequestController',['$scope','$http',  '$location', function($scope, $http,  $location){
        $scope.passwordPolicyMismatchMessage = "The password policy needs to be adhered. Do mouse over the information icon to read the policy and reenter it.";
               $scope.datas={};
               //alert(" .."+JSON.stringify($location.search()));
               //alert($location.search().user);
               var user = $location.search().user;
               $scope.isSubmit=true;
               $scope.isSuccess = false;
               var mediumRegex = new RegExp("^((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]))(?=.*[!@#\$%\^&\*])(?=.{8,})");
               $scope.submit = function(){
               if(mediumRegex.test($scope.newPassword)) {
               var str = $scope.newPassword;
                           var iChars = "!@#$%^&*";
                                       for (var i = 0; i < str.length-1; i++) {
                                          if (iChars.indexOf(str.charAt(i)) != -1) {
                                              if(iChars.indexOf(str.charAt(i+1)) != -1){
                                                   sweetAlert({
                                                           title: "Oops",
                                                           text: $scope.passwordPolicyMismatchMessage,
                                                           type: "error"//error
                                                       });
                                                   return false;
                                              }
                                          }
                                       }
                if(str.indexOf(' ') != -1){
                    sweetAlert({
                        title: "Oops",
                        text: $scope.passwordPolicyMismatchMessage,
                        type: "error"//error
                    });
                    return false;
                }
                   if($scope.newPassword == $scope.confirmPassword){

                       $scope.userDetails = {};
                       $scope.userDetails.userName = user;
                       $scope.userDetails.newPassword = $scope.newPassword;
                       //alert('Data..'+JSON.stringify($scope.userDetails));
                       $http.post('/afya-portal/anon/resetPassword',JSON.stringify($scope.userDetails)).then(function(response) {
                                   $scope.isSubmit=true;
                                   $scope.isSuccess = true;
                                   //alert('Data..'+JSON.stringify(response));
                                   //$scope.datas = response.data;
                                   }, function(response) {
                                   });
                    }else{
                        $scope.isSubmit=false;
                        $scope.isSuccess = false;
                    }
                }else{
                    sweetAlert({
                        title: "Oops",
                        text: $scope.passwordPolicyMismatchMessage,
                        type: "error"//error
                    });
                    return false;
                }
               }
               $scope.clearErr = function(){
                    $scope.isSubmit=true;
               };
       	}]);