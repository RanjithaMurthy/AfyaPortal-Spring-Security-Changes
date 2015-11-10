/**
 * Created by Mohan Sharma on 8/2/2015.
 */
//var memberSettingModule = angular.module('MemberSettingModule', []);
afyaApp.controller('MemberSettingController', ['$scope', '$http', '$window','$state', function($scope, $http, $window,$state){
    $scope.passwordPolicyMismatchMessage = "The password policy needs to be adhered. Do mouse over the information icon to read the policy and reenter it.";
    $scope.list=[];
    $scope.memberSettingObj = {};
    $scope.insuranceDetailsList = [];
    $scope.IsView = true;
    $scope.IsEdit = false;
    angular.element(document).ready(function(){
        assignWelcomeUserName();
        var username = readCookie("username");
        var data = getPatientByUsername();
        if(data != null)
        {
        //$http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
            $scope.afyaId = data.afyaId;
            $scope.memberSettingObj = data;
            console.log(data);
            $http.get('/afya-portal/getInsuranceByAfyaId').success(function(data){
                $scope.insuranceDetailsList = data;
            }).error(function(data){
                sweetAlert("error fetching insurance details");
            });
        }
        else
            sweetAlert("error fetching data");
        //}).error(function(data){
        //    sweetAlert("error fetching data");
        //});
    });

    $scope.calculateAgeByDateOfBirth = function(age, dateOfBirth){
        if(angular.equals(age, null) ||  angular.equals(age, '')) {
            var ageDiffInMilliSec = Date.now() - new Date(dateOfBirth).getTime();
            var ageDate = new Date(ageDiffInMilliSec);
            return Math.abs(ageDate.getUTCFullYear() - 1970);
        } else {
            return age;
        }
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

    $scope.updateMemberDetails = function(){
        var obj = JSON.stringify($scope.memberSettingObj);
       // alert(obj);
        $http({
         url : '/afya-portal/anon/updateMemberDetails',
         method : "POST",
         headers: {
         'Content-Type': 'application/json'
         },
         data : obj
         }).success(function(data){
         //$window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-member-settings.html";
         $state.go("PatientDashboard/Patient-upcoming-member-settings")
         }).error(function(data){
         sweetAlert("error updating..");
         });
    };

    $scope.displayEditWindow = function(){
        $scope.IsView = false;
        $scope.IsEdit = true;
    }

    $scope.changePassword = function()
    {
        $scope.newPassword = "";
        $scope.confirmPassword = "";
        $scope.currentPassword = "";
        $scope.isSubmit=true;
        $("#changePasswordModel").modal("show");
       //$state.go('ResetPassword', {email:$scope.memberSettingObj.emailId, user:$scope.memberSettingObj.username})
    }

    $scope.isSubmit=true;
    var mediumRegex = new RegExp("^((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]))(?=.*[!@#\$%\^&\*])(?=.{8,})");
    $scope.submit = function(){
        if($scope.currentPassword == "" || $scope.currentPassword == undefined)
                {
                    $scope.message = 'Please enter current password';
                    $scope.isSubmit=false;
                    return false;
                }
                else
                {
                    $scope.message = '';
                    $scope.isSubmit=true;
                }

                if($scope.newPassword == "" || $scope.newPassword == undefined)
                {
                    $scope.message = 'Please enter new password';
                    $scope.isSubmit=false;
                    return false;
                }
                else
                {
                    $scope.message = '';
                    $scope.isSubmit=true;
                }

                if($scope.confirmPassword == "" || $scope.confirmPassword == undefined)
                {
                    $scope.message = 'Please enter confirm password';
                    $scope.isSubmit=false;
                    return false;
                }
                else
                {
                    $scope.message = '';
                    $scope.isSubmit=true;
                }

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
                                 $scope.message = 'Password Policy mismatch. Mouse over the information Icon to view the detailed Password Policy';
                                 $scope.isSubmit=false;
                                 return false;
                            }
                            else
                            {
                                $scope.message = '';
                                $scope.isSubmit=true;
                            }
                        }
                    }
                    if(str.indexOf(' ') != -1){
                        sweetAlert({
                            title: "Oops",
                            text: $scope.passwordPolicyMismatchMessage,
                            type: "error"//error
                        });
                        $scope.message = 'Password Policy mismatch. Mouse over the information Icon to view the detailed Password Policy';
                        $scope.isSubmit=false;
                        return false;
                    }
                    else
                    {
                        $scope.message = '';
                        $scope.isSubmit=true;
                    }

                    if($scope.newPassword == $scope.confirmPassword){
                        $scope.message = '';
                        $scope.isSubmit=true;
                    }else{
                        $scope.isSubmit=false;
                        $scope.message = "Password not matching with confirm password";
                        return false;
                    }
                }
                else {
                    $scope.message = "Password Policy mismatch. Mouse over the information Icon to view the detailed Password Policy";
                    $scope.isSubmit=false;
                    return false;
                }
                $http.get("/afya-portal/ValidateCurrentPassword?password=" + $scope.currentPassword).success(function(data){
                    if(data == "success") {
                        $scope.userDetails = {};
                        $scope.userDetails.userName = $scope.memberSettingObj.username;
                        $scope.userDetails.newPassword = $scope.newPassword;
                        //alert('Data..'+JSON.stringify($scope.userDetails));
                        $http.post('/afya-portal/anon/resetPassword',JSON.stringify($scope.userDetails)).then(function(response) {
                           //$scope.isSubmit=true;
                           //alert('Data..'+JSON.stringify(response));
                           //$scope.datas = response.data;
                           $("#changePasswordModel").modal("hide");
                            sweetAlert('Success','Congrats, your password has been reset Successfully. Afya is a secured system and thanks for following the policies','success');
                           //}, function(response) {
                        });
                    }
                    else
                    {
                        $scope.message = data;
                        $scope.isSubmit=false;
                    }
                });
    }
    $scope.clearErr = function(){
        $scope.isSubmit=true;
    };
}]);

