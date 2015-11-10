//angular.module('FaqModule',[])
    afyaApp.controller('FAQController',['$scope','$http','$location','$window','$sce', function($scope, $http, $location, $window,$sce){

        $scope.searchParam = '';
        $scope.datas={};

        var param = $location.search().searchParam;

        $http.get('/afya-portal/anon/getAllFaq').then(function (response) {
            if(param){
                angular.forEach(response.data, function(value) {
                    if(param == value.question){
                        $scope.datas[0] = value;
                    }
                });
            }else{
                $scope.datas = response.data;
            }
        });

        //returns trusted HTML
        $scope.toTrustedHTML = function( html ){
            return $sce.trustAsHtml( html );
        }

        // $window.location.href = '#collapse_1' ;

        /*var comp = angular.element(document.getElementById('questionA_1'));
         comp.click();
         console.log(comp);*/

        $scope.search = function(){

            $http.get('/afya-portal/anon/searchFaq?searchParam=' +  $scope.searchParam).success(function (response) {

                $scope.datas = response;
            }, function(response) {
            });
        }



    }]).controller('FAQIndexController',['$scope','$http', function($scope, $http){

        $scope.faqs={};

        $http.get('/afya-portal/anon/getFaqCategory').success(function (response) {
            $scope.faqs = response;
        }, function(response) {
        });

    }]);