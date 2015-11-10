/**
* Created on 08/02/2015 19:30:00.
*/

PatientAccountModule = angular.module('ConfirmationModule', [])
PatientAccountModule.controller('ConfirmationController', ['$scope', '$http', function ($scope, $http) {
    $scope.ConfirmationList = [];
    $scope.clinicId = null;
    $scope.providerId = null;
    $scope.bookedOn = null;
    $scope.reason = null;
    $scope.successMsg = null;
    $scope.patientName = null;
    $scope.isRequest = false;

    $scope.portalBaseURL7879 = '/afya-portal';//'http://5.9.249.196:7879/afya-portal';

    angular.element(document).ready(function () {
        //get property values from query string
        $scope.clinicId = getParameterValueByProperty('clinicId');
        $scope.providerId = getParameterValueByProperty('providerId');
        $scope.bookedOn = getParameterValueByProperty('bookedOn');
        $scope.reason = getParameterValueByProperty('notes');
        $scope.patientName = getParameterValueByProperty('pName');
        if(getParameterValueByProperty('type') == 'AppointmentRequest')
            $scope.isRequest = true;

        $http.get($scope.portalBaseURL7879 + '/anon/fetchProvidersByClinicId?clinicId=' + $scope.clinicId).success(function (data) {
            //$scope.doctorList = data;
            if (data.length > 0) {
                // get the doctor object from existing doctors for Clinic
                var doctorInfo = $.map($.grep(data, function (obj, idx) {
                    return (obj.providerId == $scope.providerId);
                }), function (obj, idx) { return obj; });

                $scope.doctor = doctorInfo[0];
            }
            console.log('Successfully got Doctor Details');
        });

        //if($scope.bookedOn.length > 22)
        if($scope.isRequest){
            // for request Appointment
            $scope.successMsg = "Appointment requested successfully!";
             //$("#btnUpcomingServices").hide();
             $("#btnUpcomingServices").html('Back to Home');
             $("#spnNotes").html('Note : When the Appointment is booked  an email confirmation will be sent and Appointment will be listed in Upcoming Services.');
        }
        else {
            // for Premium/Teleconsultation/Home Visit Bookings
            $scope.successMsg = "Your Appointment is booked!";
        }

        var when = dateFormatting($scope.bookedOn);
        if (when != null || when != undefined)
            $scope.bookedOn = when;
    });

    $scope.onViewUpcomingServicesClick = function () {
        //redirect according to status[i.e. for req. appont- Back to Home, rest all- Upcoming Services]
        if($scope.isRequest){
            window.location.href = "/afya-portal/index.html";
        }
        else
            window.location.href = "/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html";
    }



    // get query string value by properties
    function getParameterValueByProperty(type) {
        type = type.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + type + "=([^&#]*)"),
        results = regex.exec(location.search);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    function dateFormatting(date) { // date = dd-mm-YYYY
        var origionalDate = date;
        var formatedDate = '';
        if (date != "") {
            var time = date.substring(19, 11);
            date = date.substring(0, 10); // get date from datetime [16-08-2015 08:40:00]
            var data = date.split('-');
            var datatime = time.split(':');

            var options = null;
            //if(origionalDate.length > 22){
            if($scope.isRequest){
                formatedDate = new Date(data[2], (data[1] - 1).toString(), data[0], datatime[0], datatime[1], datatime[2]);//new Date(origionalDate);
                options = {
                    weekday: "long", year: "numeric", month: "short",
                    day: "numeric"
                };
            }
            else {
            formatedDate = new Date(data[2], (data[1] - 1).toString(), data[0], datatime[0], datatime[1], datatime[2]);
                options = {
                    weekday: "long", year: "numeric", month: "short",
                    day: "numeric", hour: "2-digit", minute: "2-digit"
                };
            }

            var date = new Date(Date.UTC(data[2], (data[1] - 1).toString(), data[0], 14, 0, 0));
            /* var options = {
                weekday: "long", year: "numeric", month: "short",
                day: "numeric", hour: "2-digit", minute: "2-digit"
            };*/
            formatedDate = formatedDate.toLocaleDateString("en-us", options)
            //formatedDate = getFormatedDate(dateObj);
        }
        return formatedDate;
    }

} ]);