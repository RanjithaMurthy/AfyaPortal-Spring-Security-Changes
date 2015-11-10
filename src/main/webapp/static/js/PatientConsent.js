/**
 * Created by Ranjitha on 8/12/2015.
 */

//PatientConsentModule = angular.module('PatientConsentModule',[])
afyaApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
    delete $httpProvider.defaults.headers.post['Content-type'];
}
]);

afyaApp.controller('PatientConsentController', ['$scope', '$http', '$window', function($scope, $http, $window){
    $scope.portalBaseURL = null;
    $scope.clinicBaseURL = null;
    $scope.alerts = [];
    $scope.memberProfileObj={};
    $scope.afyaId = null;
    $scope.username = null;

    angular.element(document).ready(function(){
        assignWelcomeUserName();
        $http.get('/afya-portal/static/application-deployment.properties').then(function(response){
            if(!angular.equals(response.data.PORTAL_BASE_URL, null) && !angular.equals(response.data.PORTAL_BASE_URL, '')){
                $scope.portalBaseURL = response.data.PORTAL_BASE_URL;
            }else{
                $scope.portalBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            if(!angular.equals(response.data.CLINIC_BASE_URL, null) && !angular.equals(response.data.CLINIC_BASE_URL, '')){
                $scope.clinicBaseURL = response.data.CLINIC_BASE_URL;
            }else{
                $scope.clinicBaseURL = 'http://5.9.249.196:7878/afya-portal';
            }
            $scope.username = readCookie("username");

            // Getting Patient consent list
            var data = getPatientByUsername();
            //$http.get('/afya-portal/anon/getPatientByUsername?username='+$scope.username).success(function(data){
            if(data != null)
            {
                $scope.afyaId = data.afyaId;
                $scope.memberProfileObj = data;
                $http.get('/afya-portal/getPatientConsentByUsername').success(function(data){
                    $scope.patientConsentList = data;
                }).error(function(){
                    sweetAlert("error fetching data");
                });
            }
            else
                sweetAlert("error fetching data");
            //}).error(function(data){
            //    sweetAlert("error fetching data");
            //});
        });
    });

    $scope.UpdatePatientConsent = function(){
        var jsonObj = [];
        var consent = [];
        $('#patientConsentset input:checked').each(function () {
            var data = {'consentId':$(this).attr("value"), 'consent':"true"};
            consent.push(data);
        });

        $('#patientConsentset input:not(:checked)').each(function () {
            var data = {'consentId':$(this).attr("value"), 'consent':"false"};
            consent.push(data);
        });

        var jsonObj = JSON.stringify(consent);

        // updating Patient consent
        $http.post('/afya-portal/updatePatientConsentForUsername', jsonObj, {headers: {'accept': 'text/plain'}}).success(function (data) {
            sweetAlert('Congrats, your consent for sharing the information has been done Successfully. Afya is a secured system and thanks for following the policies.');
            //window.location = "Patient-consent.html";
        }).error(function (data) {
            sweetAlert("error on updating data");
        });
    }

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


}]);