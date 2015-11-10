/**
 * Created by pradyumna on 11-06-2015.
 */

//angular.module('aafyaRegistrationModule', ['ngMessages'])
afyaApp.controller('ProviderController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.passwordPolicyMismatchMessage = "The password policy needs to be adhered. Do mouse over the information icon to read the policy and reenter it.";
        $scope.passwordFailMismatchMessage = "Password does not match, please reenter the password.";
        $scope.onlyNumbers = /^[0-9]+$/;
        $scope.provider = {};
        $scope.termCondition = false;
        $scope.provider.loginPreference = "EMAIL";
        $scope.provider.oldRegUsername = null;
        $scope.readTermsOfServiceClicked = false;
        $scope.readPrivacyPolicyClicked = false;
        var mediumRegex = new RegExp("^((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]))(?=.*[!@#\$%\^&\*])(?=.{8,})");
        /*$scope.allCities = [];
        $scope.cityStateCountry = {};*/
        //var mediumRegex = new RegExp("^(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))(?=.{6,})");

       /* $scope.getAllCities = function(){
            $http.get('/afya-portal/anon/getAllCities').success(function(data){
                $scope.allCities = data;
            }).error(function(data){

            });
        };*/

        /*$scope.populateStateAndCountryByCity = function(cityObj){
            $http.get("/afya-portal/anon/getStateCountryBasedOnCity?city="+cityObj.city).success(function(data){
                $scope.provider.selectedState =  data.state;
                $scope.provider.selectedCountry =  data.country;
                console.log(data);
            }).error(function(){
                alert("error");
            });
        };*/

        $scope.submitOTP = function () {
            $http.get('otp_confirmation?emailId='+$scope.provider.mobile+"&otp_token="+$scope.provider.otpToken).then(function (response) {
                if (response.data == "success") {
                    document.getElementById("lblProviderErrorMessage").innerHTML = '';
                    $('#provider_registration_step_2').css("display", "none");
                    $('#provider_registration_step_3').css("display", "block");
                    $("#provider_registration_tab").toggleClass('step2 step3');
                    $("#provider_registration_step_2_number").attr('class', 'no-tab');
                    $("#provider_registration_step_3_text").attr('class', 'selected-stage-text');
                    $("#provider_registration_step_2_number").attr('class', 'no-tab-unselected');
                    $("#provider_registration_step_2_text").attr('class', 'unselected-stage-text');
                } else {
                   document.getElementById("lblProviderErrorMessage").innerHTML = response.data;
                    //alert("Invalid OTP");
                }
            });
        };

        $scope.regenerateOTP = function () {
            $http.get('regenerate_otp?username='+$scope.provider.mobile).then(function (response) {
                if (response.data == "success") {
                    sweetAlert("Afya has successfully resend the OTP ( One Time Password) to your mobile " + $scope.provider.mobile);
                }else if(response.data === "verified") {
                    sweetAlert("You had already verified yourself. Please Login to the Application from the portal home page.");
                } else {
                    sweetAlert({ title: "Sorry",text: 'We have enountered issues in generating OTP. Please try after some time.',type: "error" });
                }
            });
        };

        $scope.resendEmail = function () {
            $http.get('/afya-portal/anon/resend_email?username='+$scope.provider.email).then(function (response) {
                if (response.data === "success") {
                    sweetAlert("Afya has successfully resend the verification Email to your account " + $scope.provider.email);
                }else if(response.data === "verified") {
                    sweetAlert("You had already verified yourself. Please Login to the Application from the portal home page.");
                } else {
                    sweetAlert({ title: "Sorry",text: 'We have encountered issues in trying to send mail. Please retry after some time.',type: "error" });
                }
            });
        };

        // Care Provider Join In Click
        $scope.submitProviderData = function () {
        if(mediumRegex.test($scope.provider.password)) {
            // validate city
            if(!$scope.cityList)
            {
                $("#validateCity").text('Please select city');
                return false;
            }
            else
            {
                $scope.provider.selectedCity =  $scope.cityList.city;
                $("#validateCity").text('');
            }
            // validate password and save
            var str = $scope.provider.password;
            var iChars = "!@#$%^&*";
            for (var i = 0; i < str.length-1; i++) {
               if (iChars.indexOf(str.charAt(i)) != -1) {
                   if(iChars.indexOf(str.charAt(i+1)) != -1){
                        sweetAlert({ title: "Oops", text: $scope.passwordPolicyMismatchMessage, type: "error" });
                        return false;
                   }
               }
            }
            if(str.indexOf(' ') != -1){
                sweetAlert({ title: "Oops", text: $scope.passwordPolicyMismatchMessage, type: "error"});
                return false;
            }
            if ($scope.provider.password != $scope.provider.confirmPassword ) {
                sweetAlert({ title: "Oops", text: $scope.passwordFailMismatchMessage, type: "error"});
                return false;
            }
            //$scope.provider.selectedCity = $scope.provider.selectedCity.city;
            console.log($scope.provider)
            $http.post('provider_sign_up', $scope.provider).success(function (data,status,headers,config) {
                console.log($scope.provider)
                if (data.message == "success") {
                     document.getElementById("providerAlreadyVerificationDiv").innerHTML = '';
                    $('#provider_registration_step_1').css("display", "none");
                    $('#provider_registration_step_2').css("display", "block");
                    $("#provider_registration_tab").toggleClass('step1 step2');
                    $("#provider_registration_step_2_number").attr('class', 'no-tab');
                    $("#provider_registration_step_2_text").attr('class', 'selected-stage-text');
                    $("#provider_registration_step_1_number").attr('class', 'no-tab-unselected');
                    $("#provider_registration_step_1_text").attr('class', 'unselected-stage-text');
                    if ($scope.provider.loginPreference == "EMAIL") {
                        $('#providerOTPFormDiv').css("display", "none");
                        $('#providerEmailDiv').css("display", "block");
                    } else {
                        $('#providerEmailDiv').css("display", "none");
                        $('#providerOTPFormDiv').css("display", "block");
                    }
                }
                else if(data.message == "NotVerified")
                {
                    if ($scope.provider.loginPreference == "MOBILE") {
                        document.getElementById("providerAlreadyVerificationDiv").innerHTML = '<p>Sorry, this Mobile was registered but verification was not completed. Please verify the request or use alternate Mobile Number to continue the registration.</p>';
                    }
                    else
                    {
                        document.getElementById("providerAlreadyVerificationDiv").innerHTML = '<p>Sorry, this email was registered but verification was not completed. Please verify the request or use alternate mail id to continue the registration.</p>';
                    }
                    $('#provider_registration_step_1').css("display", "none");
                    $('#provider_registration_step_2').css("display", "block");
                    $("#provider_registration_tab").toggleClass('step1 step2');
                    $("#provider_registration_step_2_number").attr('class', 'no-tab');
                    $("#provider_registration_step_2_text").attr('class', 'selected-stage-text');
                    $("#provider_registration_step_1_number").attr('class', 'no-tab-unselected');
                    $("#provider_registration_step_1_text").attr('class', 'unselected-stage-text');
                    if ($scope.provider.loginPreference == "EMAIL") {
                        $('#providerOTPFormDiv').css("display", "none");
                        $('#providerEmailDiv').css("display", "block");
                    } else {
                        $('#providerEmailDiv').css("display", "none");
                        $('#providerOTPFormDiv').css("display", "block");
                    }
                }
                else {
                    sweetAlert(data.message);
                }
            });
            }else{
                sweetAlert({ title: "Oops", text: $scope.passwordPolicyMismatchMessage, type: "error" });
                return false;
            }
        };

        $scope.backToStep1 = function(){
            // hide step-2 and display Step-1
            $('#provider_registration_step_1').css("display", "block");
            $('#provider_registration_step_2').css("display", "none");
            $("#provider_registration_tab").toggleClass('step1 step2');
            $("#provider_registration_step_2_number").attr('class', 'selected-stage-text');
            $("#provider_registration_step_2_text").attr('class', 'no-tab');
            $("#provider_registration_step_1_number").attr('class', 'unselected-stage-text');
            $("#provider_registration_step_1_text").attr('class', 'no-tab-unselected');
            // capture preious username
            $scope.provider.oldRegUsername = $scope.provider.loginPreference === 'EMAIL' ? $scope.provider.email : $scope.provider.mobile;
        };

        $scope.Navigation = function(link) {
            //$window.open(link, 'terms-of-use');
            $window.open(link, '_blank');
        }

        // fetch all cities - to populate cities dropdown
        $scope.allCities = [];
        $scope.getAllCities = function(){
            $http.get('/afya-portal/anon/getAllCities').success(function(data){
                $scope.allCities = data;
            }).error(function(data){
                // sweetAlert({ title: "",text: 'Error fetching data ',type: "error" });
            });
        };

    }])
afyaApp.controller('PatientController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.passwordPolicyMismatchMessage = "The password policy needs to be adhered. Do mouse over the information icon to read the policy and reenter it.";
        $scope.passwordFailMismatchMessage = "Password does not match, please reenter the password.";
        $scope.patient = {};
        $scope.termCondition = false;
        $scope.patient.loginPreference = "EMAIL";
        $scope.patient.oldRegUsername = null;
        $scope.readTermsOfServiceClicked = false;
        $scope.readPrivacyPolicyClicked = false;
        var mediumRegex = new RegExp("^((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]))(?=.*[!@#\$%\^&\*])(?=.{8,})");
        $scope.submitOTP = function () {
            $http.get('otp_confirmation?emailId='+$scope.patient.mobileNumber+"&otp_token="+$scope.patient.otpToken).then(function (response) {
                if (response.data == "success") {
                    document.getElementById("lblPatientErrorMessage").innerHTML = '';
                    $('#patient_registration_step_2').css("display", "none");
                    $('#patient_registration_step_3').css("display", "block");
                    $("#patient_registration_tab").toggleClass('step2 step3');
                    $("#patient_registration_step_3_number").attr('class', 'no-tab');
                    $("#patient_registration_step_3_text").attr('class', 'selected-stage-text');
                    $("#patient_registration_step_2_number").attr('class', 'no-tab-unselected');
                    $("#patient_registration_step_2_text").attr('class', 'unselected-stage-text');
                } else {
                     document.getElementById("lblPatientErrorMessage").innerHTML = response.data;
                    //alert("Invalid OTP");
                }
            });
        };
        $scope.regenerateOTP = function () {
            $http.get('regenerate_otp?username='+$scope.patient.mobileNumber).then(function (response) {
                if (response.data == "success") {
                    sweetAlert("Afya has successfully resend the OTP ( One Time Password) to your mobile " + $scope.patient.mobileNumber);
                }else if(response.data === "verified") {
                    sweetAlert("You had already verified yourself. Please Login to the Application from the portal home page.");
                } else {
                    sweetAlert({ title: "Sorry",text: 'We have enountered issues in generating OTP. Please try after some time.',type: "error" });
                }
            });
        };

        $scope.resendEmail = function () {
            $http.get('/afya-portal/anon/resend_email?username='+$scope.patient.emailId).then(function (response) {
                if (response.data === "success") {
                    sweetAlert("Afya has successfully resend the verification Email to your account " + $scope.patient.emailId);
                }else if(response.data === "verified") {
                    sweetAlert("You had already verified yourself. Please Login to the Application from the portal home page.");
                } else {
                    sweetAlert({ title: "Sorry",text: 'We have encountered issues in trying to send mail. Please retry after some time.',type: "error" });
                }
            });
        };

        $scope.submitPatientData = function () {
        if(mediumRegex.test($scope.patient.password)) {
            var str = $scope.patient.password;
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

            if ($scope.patient.password != $scope.patient.confirmPassword ) {
                sweetAlert({
                    title: "Oops",
                    text: $scope.passwordFailMismatchMessage,
                    type: "error"//error
                });
                return false;
            }
            $http.post('patient_sign_up', $scope.patient).success(function (data,status,headers,config) {
                if (data.message == "success") {
                    document.getElementById("patientAlreadyVerificationDiv").innerHTML = '';
                    $('#patient_registration_step_1').css("display", "none");
                    $('#patient_registration_step_2').css("display", "block");
                    $("#patient_registration_tab").toggleClass('step1 step2');
                    $("#patient_registration_step_2_number").attr('class', 'no-tab');
                    $("#patient_registration_step_2_text").attr('class', 'selected-stage-text');
                    $("#patient_registration_step_1_number").attr('class', 'no-tab-unselected');
                    $("#patient_registration_step_1_text").attr('class', 'unselected-stage-text');
                    if ($scope.patient.loginPreference == "EMAIL") {
                        $('#patientOTPFormDiv').css("display", "none");
                        $('#patientEmailDiv').css("display", "block");
                    } else {
                        $('#patientEmailDiv').css("display", "none");
                        $('#patientOTPFormDiv').css("display", "block");
                    }
                }
                else if(data.message == "NotVerified")
                {
                    if ($scope.patient.loginPreference == "MOBILE") {
                        document.getElementById("patientAlreadyVerificationDiv").innerHTML = '<p>Sorry, this Mobile was registered but verification was not completed. Please verify the request or use alternate Mobile Number to continue the registration.</p>';
                    }
                    else
                    {
                        document.getElementById("patientAlreadyVerificationDiv").innerHTML = '<p>Sorry, this email was registered but verification was not completed. Please verify the request or use alternate mail id to continue the registration.</p>';
                    }
                    $('#patient_registration_step_1').css("display", "none");
                    $('#patient_registration_step_2').css("display", "block");
                    $("#patient_registration_tab").toggleClass('step1 step2');
                    $("#patient_registration_step_2_number").attr('class', 'no-tab');
                    $("#patient_registration_step_2_text").attr('class', 'selected-stage-text');
                    $("#patient_registration_step_1_number").attr('class', 'no-tab-unselected');
                    $("#patient_registration_step_1_text").attr('class', 'unselected-stage-text');
                    if ($scope.patient.loginPreference == "EMAIL") {
                        $('#patientOTPFormDiv').css("display", "none");
                        $('#patientEmailDiv').css("display", "block");
                    } else {
                        $('#patientEmailDiv').css("display", "none");
                        $('#patientOTPFormDiv').css("display", "block");
                    }
                }
                else {
                    //sweetAlert(data.message);
                    sweetAlert({ title: "Sorry", text: data.message, type: "warning"});
                }

          });
          }else{
            sweetAlert({
                title: "Oops",
                text: $scope.passwordPolicyMismatchMessage,
                type: "error"
            });
            return false;
          }
        };
        $scope.backToStep1 = function(){
                // hide step-2 and display Step-1
                $('#patient_registration_step_1').css("display", "block");
                $('#patient_registration_step_2').css("display", "none");
                $("#patient_registration_tab").toggleClass('step1 step2');
                $("#patient_registration_step_2_number").attr('class', 'no-tab-unselected');
                $("#patient_registration_step_2_text").attr('class', 'unselected-stage-text');
                $("#patient_registration_step_1_number").attr('class', 'no-tab');
                $("#patient_registration_step_1_text").attr('class', 'selected-stage-text');
                // capture preious username
                $scope.patient.oldRegUsername = $scope.patient.loginPreference === 'EMAIL' ? $scope.patient.emailId : $scope.patient.mobileNumber;
        };

        $scope.testFn = function () {
            sweetAlert("Called Test");
        };


        $scope.Navigation = function(link) {
            //$window.open(link, 'terms-of-use');
            $window.open(link, '_blank');
        }

    }])
afyaApp.controller('PayerController', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.passwordPolicyMismatchMessage = "The password policy needs to be adhered. Do mouse over the information icon to read the policy and reenter it.";
        $scope.passwordFailMismatchMessage = "Password does not match, please reenter the password.";
        $scope.carePayer = {};
        $scope.termCondition = false;
        $scope.carePayer.loginPreference = "EMAIL";
        $scope.carePayer.oldRegUsername = null;
        $scope.readTermsOfServiceClicked = false;
        $scope.readPrivacyPolicyClicked = false;
        var mediumRegex = new RegExp("^((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]))(?=.*[!@#\$%\^&\*])(?=.{8,})");
        $scope.allCities = [];
        $scope.cityStateCountry = {};
        var mediumRegex = new RegExp("^(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))(?=.{6,})");

        $scope.getAllCities = function(){
            $http.get('/afya-portal/anon/getAllCities').success(function(data){
                $scope.allCities = data;
            }).error(function(data){

            });
        };

        $scope.populateStateAndCountryByCity = function(cityObj){
            $http.get("/afya-portal/anon/getStateCountryBasedOnCity?city="+cityObj.city).success(function(data){
                $scope.carePayer.selectedState =  data.state;
                $scope.carePayer.selectedCountry =  data.country;
                console.log(data);
            }).error(function(){
                sweetAlert("error");
            });
        };

        $scope.submitOTP = function () {
            $http.get('otp_confirmation?emailId='+$scope.carePayer.mobileNumber+"&otp_token="+$scope.carePayer.otpToken).then(function (response) {
                if (response.data == "success") {
                    document.getElementById("lblPayerErrorMessage").innerHTML = '';
                    $('#payer_registration_step_2').css("display", "none");
                    $('#payer_registration_step_3').css("display", "block");
                    $("#payer_registration_tab").toggleClass('step2 step3');
                    $("#care_payer_registration_step_3_number").attr('class', 'no-tab');
                    $("#care_payer_registration_step_3_text").attr('class', 'selected-stage-text');
                    $("#care_payer_registration_step_2_number").attr('class', 'no-tab-unselected');
                    $("#care_payer_registration_step_2_text").attr('class', 'unselected-stage-text');
                } else {
                     document.getElementById("lblPayerErrorMessage").innerHTML = response.data;
                    //alert("Invalid OTP");
                }
            });
        };
        $scope.regenerateOTP = function () {
            $http.get('regenerate_otp?username='+$scope.carePayer.mobileNumber).then(function (response) {
                if (response.data == "success") {
                    sweetAlert("Afya has successfully resend the OTP ( One Time Password) to your mobile " +$scope.carePayer.mobileNumber);
                }else if(response.data === "verified") {
                    sweetAlert("You had already verified yourself. Please Login to the Application from the portal home page.");
                } else {
                    sweetAlert({ title: "Sorry",text: 'We have enountered issues in generating OTP. Please try after some time.',type: "error" });
                }
            });
        };

        $scope.resendEmail = function () {
            $http.get('/afya-portal/anon/resend_email?username='+$scope.carePayer.emailId).then(function (response) {
                if (response.data === "success") {
                    sweetAlert("Afya has successfully resend the verification Email to your account " + $scope.carePayer.emailId);
                }else if(response.data === "verified") {
                    sweetAlert("You had already verified yourself. Please Login to the Application from the portal home page.");
                } else {
                    sweetAlert({ title: "Sorry",text: 'We have encountered issues in trying to send mail. Please retry after some time.',type: "error" });
                }
            });
        };

        $scope.submitPayerData = function () {
            if(mediumRegex.test($scope.carePayer.password)) {
                var str = $scope.carePayer.password;
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

                if ($scope.carePayer.password != $scope.carePayer.confirmPassword ) {
                    sweetAlert({
                        title: "Oops",
                        text: $scope.passwordFailMismatchMessage,
                        type: "error"//error
                    });
                    return false;
                }
                $http.post('/afya-portal/anon/care_payer_sign_up', $scope.carePayer).success(function (data,status,headers,config) {
                    if (data.message == "success") {
                        document.getElementById("payerAlreadyVerificationDiv").innerHTML = '';
                        $('#payer_registration_step_1').css("display", "none");
                        $('#payer_registration_step_2').css("display", "block");
                        $("#payer_registration_tab").toggleClass('step1 step2');
                        $("#care_payer_registration_step_2_number").attr('class', 'no-tab');
                        $("#care_payer_registration_step_2_text").attr('class', 'selected-stage-text');
                        $("#care_payer_registration_step_1_number").attr('class', 'no-tab-unselected');
                        $("#care_payer_registration_step_1_text").attr('class', 'unselected-stage-text');
                        if ($scope.carePayer.loginPreference == "EMAIL") {
                            $('#payerOTPFormDiv').css("display", "none");
                            $('#payerEmailDiv').css("display", "block");
                        } else {
                            $('#payerEmailDiv').css("display", "none");
                            $('#payerOTPFormDiv').css("display", "block");
                        }
                    }
                    else if(data.message == "NotVerified") {
                        if ($scope.carePayer.loginPreference == "MOBILE") {
                            document.getElementById("payerAlreadyVerificationDiv").innerHTML = '<p>Sorry, this Mobile was registered but verification was not completed. Please verify the request or use alternate Mobile Number to continue the registration.</p>';
                        }
                        else {
                            document.getElementById("payerAlreadyVerificationDiv").innerHTML = '<p>Sorry, this email was registered but verification was not completed. Please verify the request or use alternate mail id to continue the registration.</p>';
                        }
                        $('#payer_registration_step_1').css("display", "none");
                        $('#payer_registration_step_2').css("display", "block");
                        $("#payer_registration_tab").toggleClass('step1 step2');
                        $("#care_payer_registration_step_2_number").attr('class', 'no-tab');
                        $("#care_payer_registration_step_2_text").attr('class', 'selected-stage-text');
                        $("#care_payer_registration_step_1_number").attr('class', 'no-tab-unselected');
                        $("#care_payer_registration_step_1_text").attr('class', 'unselected-stage-text');
                        if ($scope.carePayer.loginPreference == "EMAIL") {
                            $('#payerOTPFormDiv').css("display", "none");
                            $('#payerEmailDiv').css("display", "block");
                        } else {
                            $('#payerEmailDiv').css("display", "none");
                            $('#payerOTPFormDiv').css("display", "block");
                        }
                    }
                    else {
                        sweetAlert(data.message);
                    }
                });
            }
            else {
                sweetAlert({
                    title: "Oops",
                    text: $scope.passwordPolicyMismatchMessage,
                    type: "error"//error
                });
                return false;
            }
        };

        $scope.backToStep1 = function(){
                // hide step-2 and display Step-1
                $('#payer_registration_step_1').css("display", "block");
                $('#payer_registration_step_2').css("display", "none");
                $("#payer_registration_tab").toggleClass('step1 step2');
                $("#care_payer_registration_step_2_number").attr('class', 'no-tab-unselected');
                $("#care_payer_registration_step_2_text").attr('class', 'unselected-stage-text');
                $("#care_payer_registration_step_1_number").attr('class', 'no-tab');
                $("#care_payer_registration_step_1_text").attr('class', 'selected-stage-text');
                // capture preious username
                $scope.carePayer.oldRegUsername = $scope.carePayer.loginPreference === 'EMAIL' ? $scope.carePayer.emailId : $scope.carePayer.mobileNumber;
        };

        $scope.testFn = function () {
            sweetAlert("Called Test");
        };
        $scope.information = function(){
            sweetAlert("* Contains At least 8 chars\n* Contains at least one digit\n* Contains at least one lower alpha char and one upper \n  alpha char"+
                              "\n* Contains at least one char within a set of special chars \n  (!@#%$^&*)\n * Does not contain space, tab, etc.");
        };

        $scope.Navigation = function(link) {
            //$window.open(link, 'terms-of-use');
            $window.open(link, '_blank');
        }

    }])
afyaApp.controller('PasswordReset', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.submitPatientData = function () {
            $http.post('send-password-req', {emailAddress:$scope.emailAddress}).success(function () {
                $('#reset_btn').attr('disabled',true);
                $('#reset_success').modal('show');
                $('#reset_success').on('hidden.bs.modal', function () {
                    $window.location.href='/afya-portal';
                });
            });
        };

    }]).directive('passwordStrength', [
        function() {
            return {
                require: 'ngModel',
                restrict: 'E',
                scope: {
                    password: '=ngModel'
                },

                link: function(scope, elem, attrs, ctrl) {
                    scope.$watch('password', function(newVal) {

                        scope.strength = isSatisfied(newVal && newVal.length >= 8) +
                            isSatisfied(newVal && /[A-z]/.test(newVal)) +
                            isSatisfied(newVal && /(?=.*\W)/.test(newVal)) +
                            isSatisfied(newVal && /\d/.test(newVal));

                        function isSatisfied(criteria) {
                            return criteria ? 1 : 0;
                        }
                    }, true);
                },
                template: '<div class="progress">' +
                    '<div class="progress-bar progress-bar-danger" style="width: {{strength >= 1 ? 25 : 0}}%"></div>' +
                    '<div class="progress-bar progress-bar-warning" style="width: {{strength >= 2 ? 25 : 0}}%"></div>' +
                    '<div class="progress-bar progress-bar-warning" style="width: {{strength >= 3 ? 25 : 0}}%"></div>' +
                    '<div class="progress-bar progress-bar-success" style="width: {{strength >= 4 ? 25 : 0}}%"></div>' +
                    '</div>'
            }
        }
    ])
    .directive('patternValidator', [
        function() {
            return {
                require: 'ngModel',
                restrict: 'A',
                link: function(scope, elem, attrs, ctrl) {
                    ctrl.$parsers.unshift(function(viewValue) {

                        var patt = new RegExp(attrs.patternValidator);

                        var isValid = patt.test(viewValue);

                        ctrl.$setValidity('passwordPattern', isValid);

                        // angular does this with all validators -> return isValid ? viewValue : undefined;
                        // But it means that the ng-model will have a value of undefined
                        // So just return viewValue!
                        return viewValue;

                    });
                }
            };
        }
    ]);

/*
https://dejanvasic.wordpress.com/2015/02/07/angularjs-password-strength-indicator/
*/
