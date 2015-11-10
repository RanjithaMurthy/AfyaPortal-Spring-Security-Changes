/**
 * Created by USER on 22-Aug-15.
 */
var SubscriptionQuotationModule = angular.module("SubscriptionQuotationModule", []);
SubscriptionQuotationModule.controller("SubscriptionQuotationController", ['$scope', '$http', function($scope, $http){
    var quoteParamsStr = $.cookie("packSubscribeQuoteParams");
    $scope.quoteParams = JSON.parse(quoteParamsStr);
    // alert('quoteParams.membershipId: ' + $scope.quoteParams.membershipId);
}]);
