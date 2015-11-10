/**
 * Created by Mohan Sharma on 8/2/2015.
 */
var memberSettingModule = angular.module('MemberSettingModule', []);
memberSettingModule.controller('MemberSettingController', ['$scope', '$http', '$window', function($scope, $http, $window){
    $scope.list=[];
    $scope.memberSettingObj = {};
    $scope.insuranceDetailsList = [];
    $scope.IsView = true;
    $scope.IsEdit = false;
    angular.element(document).ready(function(){
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            var username = readCookie("username");
            $http.get('/afya-portal/anon/getPatientByUsername?username='+username).success(function(data){
                $scope.afyaId = data.afyaId;
                $scope.memberSettingObj = data;
                console.log(data);
                $http.get('/afya-portal/anon/getInsuranceByAfyaId?afyaId='+data.afyaId).success(function(data){
                    $scope.insuranceDetailsList = data;
                }).error(function(data){
                    sweetAlert("error fetching insurance details");
                });
            }).error(function(data){
                sweetAlert("error fetching data");
            });
        });
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
        $http({
            url : '/afya-portal/anon/updateMemberDetails',
            method : "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            data : obj
        }).success(function(data){
            $window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-member-settings.html";
        }).error(function(data){
            sweetAlert("error updating..");
        });
    };

    $scope.displayEditWindow = function(){
        $scope.IsView = false;
        $scope.IsEdit = true;
    }
}]);

