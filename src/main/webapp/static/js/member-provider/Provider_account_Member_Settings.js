/**
 * Created by Mohan Sharma on 8/2/2015.
 */
//var memberSettingModule = angular.module('ProviderMemberSettingModule', []);
afyaApp.controller('ProviderMemberSettingController', ['$scope', '$http', '$state', function($scope, $http, $state){
    $scope.passwordPolicyMismatchMessage = "The password policy needs to be adhered. Do mouse over the information icon to read the policy and reenter it.";
    $scope.memberSettingObj = {};
    $scope.IsView = true;
    $scope.IsEdit = false;
    angular.element(document).ready(function(){
        assignWelcomeUserName();
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            var username = readCookie("username");
            //var data = getProviderByUsername();
            //if(data != null)
            //{
            $http.get('/afya-portal/getProviderByUsername').success(function(data){
                $scope.memberSettingObj = data;
            }).error(function(data){
                alert("error fetching data");
            });
            //}
            //else
            //{
            //    sweetAlert("error fetching data");
            //}
        });
    });

    $scope.readCookie = function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    }

    $scope.updateMemberDetails = function(){
        var userDetail = {
            'username':$scope.memberSettingObj.username,
            'emailId':$scope.memberSettingObj.emailId,
            'loginPreference':$scope.memberSettingObj.loginPreference,
            'mobileNumber':$scope.memberSettingObj.mobileNumber,
            'officeNumber':$scope.memberSettingObj.officeNumber,
            'providerAddress1':$scope.memberSettingObj.address,
            'providerAddress2':$scope.memberSettingObj.address2,
            'providerCity':$scope.memberSettingObj.city,
            'providerGovernorate':$scope.memberSettingObj.state,
            'providerCountry':$scope.memberSettingObj.country
        };

         // updating Provider Detail
         $http({
             url: '/afya-portal/anon/updateProviderDetails',
             method:'POST',
             headers: {
                 'Content-Type': 'application/json',
                 'accept':'text/plain'
             },
             data:JSON.stringify(userDetail)
         }).success(function(data){
            if(data == "SUCCESS")
                sweetAlert("Updated Successfully.")
         }).error(function(data){
             sweetAlert("error fetching data");
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
                   //sweetAlert("Password Changed Successfully.");
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

    $scope.allCities = [];
    $scope.getAllCities = function(){
        $http.get('/afya-portal/anon/getAllCities').success(function(data){
            $scope.allCities = data;
        }).error(function(data){

        });
    };

    $scope.populateStateAndCountryByCity = function(city){
        if(typeof(city) != "undefined")
        {
            $http.get("/afya-portal/anon/getStateCountryBasedOnCity?city="+ city).success(function(data){
                $scope.memberSettingObj.state =  data.state;
                $scope.memberSettingObj.country =  data.country;
                console.log(data);
            }).error(function(){
                alert("error");
            });
        }
    };
}]);

