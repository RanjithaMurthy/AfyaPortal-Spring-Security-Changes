/**
 * Created by Mohan Sharma on 8/20/2015.
 */
//contactUsModule = angular.module('ContactUsModule',[]);
afyaApp.controller('ContactUsController', ['$scope', '$http', '$window', '$state', function($scope, $http, $window, $state){
    $scope.firstNo = 0;
    $scope.secondNo = 1;
    $scope.sum = 1;
    $scope.contactUsObj = {};

    $scope.getRandomNumber = function(){
        $scope.firstNo =  Math.floor(Math.random()*(10-1+1)+1);
        $scope.secondNo =  Math.floor(Math.random()*(10-1+1)+1);
        $scope.sum  = $scope.firstNo+$scope.secondNo;
        // sweetAlert($scope.firstNo+" "+$scope.secondNo+" "+$scope.sum);
    };

    $scope.emailWhenTriggeredContactUs = function() {
        if ($scope.validateInput()) {
            var obj = JSON.stringify({
                fullName: $scope.contactUsObj.fullName,
                emailId: $scope.contactUsObj.emailId,
                contactNumber: $scope.contactUsObj.contactNumber,
                purpose: $scope.contactUsObj.purpose,
                content: $scope.contactUsObj.content
            });
            $http({
                url: '/afya-portal/anon/emailWhenTriggeredContactUs',
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                },
                data: obj
            }).success(function () {
                sweetAlert({
                        title: "Information!",
                        text: "Your query has been passed onto Community Care Team, We will connect back with you in 24 Hrs",
                        type: "success"
                    },

                    function () {
                        location.reload();
                       // $state.go("AboutAfya/contact-afya");
                        //$window.location.href = "#AboutAfya/contact-afya"
                    });

            }).error(function () {
                sweetAlert({
                    title: "Sorry",
                    text:"We have encountered an Error please resubmit your message",
                    type:"error"
                });
            });

        }
    };
    $scope.validateInput = function(){
        $scope.nameRequired = null;
        $scope.emailRequired = null;
        $scope.invalidEmail = null;
        $scope.numberRequired = null;
        $scope.purposeRequired = null;
        $scope.answerRequired = null;
        $scope.queryRequired = null;
        $scope.wrongAnswer = null;
        var firstName = $('#first-name').val();
        var emailAddress = $('#email-address').val();
        var selectPurpose = $('#purpose').find(":selected").text();
        var contactNumber = $('#contact-number').val();
        var query = $('#query').val();
        var enterAnswer = $('#enter-answer').val();
        $('.error-box').attr('display', 'block');
        setTimeout(function () {
            $('.error-box').fadeIn();
        }, 1000);
        if(firstName == ''){
            $scope.nameRequired = 'Full Name Required';
            $('#first-name').focus();
            return false;
        }
        if(emailAddress == ''){
            $scope.emailRequired = 'Email Required';
            $('#email-address').focus();
            return false;
        }
        if(!(/\S+@\S+\.\S+/).test(emailAddress)){
            $scope.invalidEmail = 'Invalid Email';
            $('#email-address').focus();
            return false;
        }if(contactNumber == ''){
            $scope.numberRequired = 'Contact Number Required';
            $('#contact-number').focus();
            return false;
        }
        if(selectPurpose == '' || selectPurpose == 'Select Purpose *'){
            $scope.purposeRequired = 'Purpose Required';
            $('#purpose').focus();
            return false;
        }
        if(enterAnswer == ''){
            $scope.answerRequired = 'Answer Required';
            $('#enter-answer').focus();
            return false;
        }
        if(enterAnswer != $scope.sum){
            $scope.wrongAnswer = 'Incorrect Answer';
            $('#enter-answer').focus();
            return false;
        }
        if (query == '') {
            $scope.queryRequired = 'Enter Your Query';
            $('#query').focus();
            return false;
        }
        if ($('#first-name').val() != "" || $('#email-address').val() != "" || $('#contact-number').val() != "" || $('#query').val() != null || $('#purpose').find(":selected").text() != null ||  $('#enter-answer').val() != null) {
            setTimeout(function () {
                $('.error-box').fadeOut();
            }, 1000);
            return true;
        }
        else {
            return false;
        }
    }

    $scope.changeListener = function(data, data1){
        if(angular.equals('Full Name Required', data))
            $scope.nameRequired = null;
        if(angular.equals('Email Required', data))
            $scope.emailRequired = null;
        if(angular.equals('Invalid Email', data1))
            $scope.invalidEmail = null;
        if(angular.equals('Contact Number Required', data))
            $scope.numberRequired = null;
        if(angular.equals('Purpose Required', data))
            $scope.purposeRequired = null;
        if(angular.equals('Answer Required', data))
            $scope.answerRequired = null;
        if(angular.equals('Enter Your Query', data))
            $scope.queryRequired = null;
        if(angular.equals('Incorrect Answer', data1))
            $scope.wrongAnswer = null;
    }

    $scope.resetForm = function(){
        $scope.contactUsObj = {};
        $scope.contactUsObj.emailId = null;
    };

}]);
