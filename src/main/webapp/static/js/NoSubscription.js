/**
 * Created by Mohan Sharma on 8/21/2015.
 */
NoSubscriptionModule = angular.module('NoSubscriptionModule', []);

NoSubscriptionModule.controller('NoSubscriptionController', ['$window', '$scope', function($window, $scope){

    $scope.showNoSubscriptionMessage = function(){
        sweetAlert({
                title: "Information!",
                text: "Sorry, You have not subscribed for any package.",
                type: "error"
            },
            function () {
                $window.location.href = "/afya-portal"
            });
    }
}]);