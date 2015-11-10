/**
 * Created by pradyumna on 11-06-2015.
 */

angular.module('aafyaRegistrationModule', ['ngMessages'])
    .controller('TrainingScheduleController', ['$scope', '$http', '$window', function ($scope, $http, $window) {

        $scope.isloggedIn = false;
        $scope.packages = {};
        $scope.schedules = [];
        $scope.packageId = "";
        $scope.currentScheduleList = [];
        $scope.sessionDuration = 0;
        $scope.sessionDurationHours = 0;
        $scope.practiceDuration = 0;
        $scope.practiceDurationHours = 0;
        $scope.assessmentDuration = 0;
        $scope.assessmentDurationHours = 0;
        $scope.counter = 0;
        $scope.patientData = {};
        $scope.afyaId = null;

        angular.element(document).ready(function(){
            var username = $.cookie("username");
            $http.get('/afya-portal/anon/getPackageByUserOrGetAllPackagesForUserFacility?username='+username).success(function(data){
                console.log(data);
                $scope.packageList = data;
            }).error(function(){
                sweetAlert("error fetching visit history");
            });
        });

        $scope.setCurrentSchedule = function(package) {
            $scope.currentScheduleList = [];
            $http.get($scope.portalBaseURL+"/anon/getTrainingSessionSchedulesByPackageId?packageId="+package.packageId).success(function(data){
                $scope.currentScheduleList = data;
                var counterForTrainingDr =0;
                var counterForPracticeDr =0;
                var counterForassessmentDr =0;
                angular.forEach($scope.currentScheduleList, function(packageData){
                    counterForTrainingDr += packageData.trainingDuration;
                    counterForPracticeDr += packageData.practiceDuration;
                    counterForassessmentDr += packageData.assessmentDuration;
                });
                $scope.sessionDuration = counterForTrainingDr;
                $scope.sessionDurationHours = counterForTrainingDr/60;
                $scope.practiceDuration = counterForPracticeDr;
                $scope.practiceDurationHours = counterForPracticeDr/60;
                $scope.assessmentDuration = counterForassessmentDr;
                $scope.assessmentDurationHours = counterForassessmentDr/60;
            });
        }
    }]);