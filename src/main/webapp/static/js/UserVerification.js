/**
 * Created by Mohan Sharma on 8/24/2015.
 */
userVerificationModule = angular.module('UserVerificationModule', []);

userVerificationModule.controller('UserVerificationController', ['$window', '$scope', function($window, $scope){

    $scope.showUserVerifiedMessage = function(){
        sweetAlert({
                title: "Success",
                text: "Thanks, the email verification was successful. Please click on 'OK' button to continue.",
                type: "success"
            },
            function () {
                $window.location.href = "/afya-portal/login"
            });    }
}]);