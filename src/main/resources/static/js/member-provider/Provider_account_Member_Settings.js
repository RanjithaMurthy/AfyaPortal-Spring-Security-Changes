/**
 * Created by Mohan Sharma on 8/2/2015.
 */
var memberSettingModule = angular.module('ProviderMemberSettingModule', []);
memberSettingModule.controller('ProviderMemberSettingController', ['$scope', '$http', function($scope, $http){
    $scope.memberSettingObj = {};
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
            $http.get($scope.portalBaseURL + '/anon/getProviderByUsername?username='+username).success(function(data){
                $scope.memberSettingObj = data;
            }).error(function(data){
                sweetAlert("error fetching data");
            });
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
            var obj = JSON.stringify($scope.memberSettingObj);
            sweetAlert(obj);
        };

    $scope.displayEditWindow = function(){
        $scope.IsView = false;
        $scope.IsEdit = true;
    }
}]);

