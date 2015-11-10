/**
 * Created by Mohan Sharma on 8/2/2015.
 */

var newMessagesModule = angular.module('NewMessagesModule', []);

newMessagesModule.controller('NewMessageController', ['$scope','$http','$window', function($scope, $http, $window){

    $scope.upcomingNeMessageList = [];
    $scope.newMessage = {};
    $scope.latestMessage = {};

    angular.element(document).ready(function() {
        $http.get('/afya-portal/anon/getNews').success(function (data) {
            data.sort(function (a, b) {
                return new Date(a.newsDate) - new Date(b.newsDate);
            });
            $scope.latestMessage = data[0];
            data.shift();
            $scope.upcomingNeMessageList = data;
            console.log(data);
        });
    });
}]);