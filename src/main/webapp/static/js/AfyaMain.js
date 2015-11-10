// script.js

// create the module and name it scotchApp
// also include ngRoute for all our routing needs
var afyaApp = angular.module('afyaApp', ['ui.router', 'mgcrea.ngStrap', 'angularUtils.directives.dirPagination', 'ngMessages', 'ngSanitize']);

// configure our routes
afyaApp.config(function ($stateProvider, $urlRouterProvider) {



    // For any unmatched url, send to /route1
    // $urlRouterProvider.otherwise("/")

    $stateProvider
      .state('index', {
          url: "",
          templateUrl: '/afya-portal/index_content.html',
           resolve: { authenticate: authenticateStaticPages }
          //controller: 'mainController'
      })
        .state('JoinIn', {
            url: "/JoinIn",
            templateUrl: '/afya-portal/join-in.html',
            resolve: { authenticate: authenticateJoinInPage}
            //controller: 'aboutController'
        })
        .state('ResetPassword', {
          url: "/ResetPassword?email&user",
          templateUrl: '/afya-portal/web_pages/forgot_password/resetPassword.html',
          resolve: { authenticate: authenticateStaticPages }
          //controller: 'aboutController'
      })


       //----All Experience Afya pages  Start--------
                // route for the ExperienceAfya
                .state('ExperienceAfya', {
                    url: "/ExperienceAfya",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/experience-afya.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
                // route for the ExperienceAfya
                .state('ExperienceAfya/ecosystem', {
                    url: "/ExperienceAfya/ecosystem",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/afya-ecosystem.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
                // route for the ExperienceAfya
                .state('ExperienceAfya/smart-care-community', {
                    url: "/ExperienceAfya/smart-care-community",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/smart-care-community.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
                // route for the ExperienceAfya
                .state('ExperienceAfya/social-innovation', {
                    url: "/ExperienceAfya/social-innovation",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/social-innovation.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
                // route for the ExperienceAfya
                .state('ExperienceAfya/future-care', {
                    url: "/ExperienceAfya/future-care",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/future-care.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })

                .state('ExperienceAfya/afya-care-experience', {
                    url: "/ExperienceAfya/afya-care-experience",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/afya-care-experience.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
                .state('ExperienceAfya/afya-services', {
                    url: "/ExperienceAfya/afya-services",
                    templateUrl: '/afya-portal/web_pages/ExperienceAfya/afya-services.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
         .state('ExperienceAfya/faq', {
             url: "/ExperienceAfya/faq",
             templateUrl: '/afya-portal/web_pages/faq/faq.html',
             resolve: { authenticate: authenticateStaticPages }
             //controller: 'aboutController'
         })
        //----All Experience Afya pages  End--------
        //----Appointment related-- Start-------
                //Appointment
                 .state('appointment', {
                     url: "/appointment?visitType&doctor&clinic",
                     templateUrl: '/afya-portal/web_pages/afya_member_request/appointments.html',
                     resolve: { authenticate: authenticateStaticPages }
                     //controller: 'AppointmentController'
                 })
                //Confirmation 
                .state('confirmation', {
                    url: "/confirmation/:clinicId/:providerId/:bookedOn/:pName/:notes/:type",
                    templateUrl: '/afya-portal/web_pages/afya_member_request/confirmation.html',
                    //controller: 'ConfirmationController'
                })
        .state('homePharmacy', {
            url: "/homePharmacy?clinicId",
            templateUrl: '/afya-portal/web_pages/afya_member_request/homePharmacy.html',
            resolve: { authenticate: authenticateStaticPages }
           // controller: 'ConfirmationController'
        })
        .state('appointmentRequest', {
            url: "/appointmentRequest?doctor&clinic",
            templateUrl: '/afya-portal/web_pages/afya_member_request/appointment-request.html',
            resolve: { authenticate: authenticateStaticPages }
            // controller: 'ConfirmationController'
        })
    //-----Appointment related- End---------

    //--------About Afya Start--------------
        .state('AboutAfya', {
            url: "/AboutAfya",
            templateUrl: '/afya-portal/web_pages/about_afya/about-afya.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/membership-agreement', {
            url: "/AboutAfya/membership-agreement",
            templateUrl: '/afya-portal/web_pages/about_afya/membership-agreement.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/subscription-agreement', {
            url: "/AboutAfya/subscription-agreement",
            templateUrl: '/afya-portal/web_pages/about_afya/subscription-agreement.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/privacy-confidentiality', {
            url: "/AboutAfya/privacy-confidentiality",
            templateUrl: '/afya-portal/web_pages/about_afya/privacy-confidentiality.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/terms-of-use', {
            url: "/AboutAfya/terms-of-use",
            templateUrl: '/afya-portal/web_pages/about_afya/terms-of-use.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/afya-trust', {
            url: "/AboutAfya/afya-trust",
            templateUrl: '/afya-portal/web_pages/about_afya/afya-trust.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/security', {
            url: "/AboutAfya/security",
            templateUrl: '/afya-portal/web_pages/about_afya/security.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
        .state('AboutAfya/contact-afya', {
            url: "/AboutAfya/contact-afya",
            templateUrl: '/afya-portal/web_pages/about_afya/contact-afya.html',
            resolve: { authenticate: authenticateStaticPages }
            //controller: 'aboutController'
        })
    //--------About Afya End----------------

    //--------Afya Services Start--------------
                .state('AfyaServices/pricing-plan', {
                    url: "/AfyaServices/pricing-plan?packageType&packageId",
                    templateUrl: '/afya-portal/pricing-plan.html',
                    resolve: { authenticate: authenticate }
                    //controller: 'aboutController'
                })
                .state('AfyaServices/afya-mobile-app', {
                    url: "/AfyaServices/afya-mobile-app",
                    templateUrl: '/afya-portal/web_pages/afya_services/afya-mobile-app.html',
                    resolve: { authenticate: authenticateStaticPages }
                    //controller: 'aboutController'
                })
    .state('AfyaServices/security-confidentiality', {
        url: "/AfyaServices/security-confidentiality",
        templateUrl: '/afya-portal/web_pages/afya_services/security-confidentiality.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    .state('AfyaServices/scalability-of-afya', {
        url: "/AfyaServices/scalability-of-afya",
        templateUrl: '/afya-portal/web_pages/afya_services/scalability-of-afya.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    .state('AfyaServices/member-support', {
        url: "/AfyaServices/member-support",
        templateUrl: '/afya-portal/web_pages/afya_services/member-support.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('AfyaServices/afya-patient-service', {
        url: "/AfyaServices/afya-patient-service",
        templateUrl: '/afya-portal/web_pages/afya_services/afya-patient-service.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    .state('AfyaServices/care-provider-services', {
        url: "/AfyaServices/care-provider-services",
        templateUrl: '/afya-portal/web_pages/afya_services/care-provider-services.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    .state('AfyaServices/ecosystem-adaptation', {
        url: "/AfyaServices/ecosystem-adaptation",
        templateUrl: '/afya-portal/ecosystem-adaptation.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    //--------Afya Services End----------------
    //--------Member Area start----------------
     .state('Member-area', {
         url: "/Member-area",
         templateUrl: '/afya-portal/web_pages/member_area/member-area.html',
         resolve: { authenticate: authenticateStaticPages }
         //controller: 'aboutController'
     })
     .state('Member-area/member-account-dashboard', {
         url: "/Member-area/member-account-dashboard",
         templateUrl: '/afya-portal/web_pages/member_area/member-account-dashboard.html',
         resolve: { authenticate: authenticateStaticPages }
         //controller: 'aboutController'
     })
    .state('Member-area/patient-memeber-area', {
        url: "/Member-area/patient-memeber-area",
        templateUrl: '/afya-portal/web_pages/member_area/patient-memeber-area.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    .state('Member-area/provider-member-area', {
        url: "/Member-area/provider-member-area",
        templateUrl: '/afya-portal/web_pages/member_area/provider-member-area.html',
        resolve: { authenticate: authenticateStaticPages }
        //controller: 'aboutController'
    })
    //--------Member Area end------------------

    //--------Patient Dashboard start----------------
    .state('PatientDashboard', {
         url: "/PatientDashboard",
         templateUrl: '/afya-portal/web_pages/member_area/patient/Patient_account.html',
         resolve: { authenticate: authenticate }

         //controller: 'aboutController'
    })
     .state('PatientDashboard/Patient-upcoming-smart-services', {
         url: "/PatientDashboard/Patient-upcoming-smart-services",
         templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-upcoming-smart-services.html',
         resolve: { authenticate: authenticate }
         //controller: 'aboutController'
     })
    .state('PatientDashboard/Patient-consent', {
        url: "/PatientDashboard/Patient-consent",
        templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-consent.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('PatientDashboard/Patient-service-list', {
        url: "/PatientDashboard/Patient-service-list",
        templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-service-list.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('PatientDashboard/Patient_account_policies', {
        url: "/PatientDashboard/Patient_account_policies",
        templateUrl: '/afya-portal/web_pages/member_area/patient/Patient_account_policies.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
     .state('PatientDashboard/Patient-visit-records', {
         url: "/PatientDashboard/Patient-visit-records",
         templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-visit-records.html',
         resolve: { authenticate: authenticate }
         //controller: 'aboutController'
     })
     .state('PatientDashboard/Patient-smart-service', {
         url: "/PatientDashboard/Patient-smart-service",
         templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-smart-service.html',
         resolve: { authenticate: authenticate }
         //controller: 'aboutController'
     })
     .state('PatientDashboard/Patient-previous-record', {
         url: "/PatientDashboard/Patient-previous-record",
         templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-previous-record.html',
         resolve: { authenticate: authenticate }
         //controller: 'aboutController'
     })
    .state('PatientDashboard/Patient-upcoming-new-messages', {
        url: "/PatientDashboard/Patient-upcoming-new-messages",
        templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-upcoming-new-messages.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('PatientDashboard/Patient-upcoming-member-settings', {
        url: "/PatientDashboard/Patient-upcoming-member-settings",
        templateUrl: '/afya-portal/web_pages/member_area/patient/Patient-upcoming-member-settings.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    //--------Patient Dashboard end----------------

    //--------Provider Dashboard start----------------
    .state('ProviderDashboard', {
        url: "/ProviderDashboard",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_Service_Subscribe', {
        url: "/ProviderDashboard/Provider_account_Service_Subscribe",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_Service_Subscribe.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_Service_Non_Subscribe', {
        url: "/ProviderDashboard/Provider_account_Service_Non_Subscribe",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_Service_Non_Subscribe.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_Service_News', {
        url: "/ProviderDashboard/Provider_account_Service_News",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_Service_News.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_Service_History', {
        url: "/ProviderDashboard/Provider_account_Service_History",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_Service_History.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_Member_Settings', {
        url: "/ProviderDashboard/Provider_account_Member_Settings",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_Member_Settings.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_Service_Transaction', {
        url: "/ProviderDashboard/Provider_account_Service_Transaction",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_Service_Transaction.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_LoyaltyProg_information', {
        url: "/ProviderDashboard/Provider_account_LoyaltyProg_information",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_LoyaltyProg_information.html',
        resolve: { authenticate: authenticate }
         //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_LoyaltyProg_account', {
        url: "/ProviderDashboard/Provider_account_LoyaltyProg_account",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_LoyaltyProg_account.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_MyNetwork_ExistingNetwork', {
         url: "/ProviderDashboard/Provider_account_MyNetwork_ExistingNetwork",
         templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_MyNetwork_ExistingNetwork.html',
         resolve: { authenticate: authenticate }
         //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_MyNetwork_NewNetworkRequest', {
        url: "/ProviderDashboard/Provider_account_MyNetwork_NewNetworkRequest",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_MyNetwork_NewNetworkRequest.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_MyNetwork_NetworkRequestReceived', {
        url: "/ProviderDashboard/Provider_account_MyNetwork_NetworkRequestReceived",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_MyNetwork_NetworkRequestReceived.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_MyNetwork_Contracts', {
        url: "/ProviderDashboard/Provider_account_MyNetwork_Contracts",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_MyNetwork_Contracts.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_MyNetwork_NetworkRequestBlocked', {
        url: "/ProviderDashboard/Provider_account_MyNetwork_NetworkRequestBlocked",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_MyNetwork_NetworkRequestBlocked.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    .state('ProviderDashboard/Provider_account_policies', {
        url: "/ProviderDashboard/Provider_account_policies",
        templateUrl: '/afya-portal/web_pages/member_area/provider/Provider_account_policies.html',
        resolve: { authenticate: authenticate }
        //controller: 'aboutController'
    })
    //--------Provider Dashboard end----------------

       function authenticate() {

          if (readCookieFromUserName("username")!=null) {
           //Re-paint the header and footer
              initializePage();
            // Resolve the promise successfully
           // return $q.when()
          } else {
            // The next bit of code is asynchronously tricky.
            removeAllCookies();
//            $timeout(function() {
//              // This code runs after the authentication promise has been rejected.
//              // Go to the log-in page
//              $state.go('index')
//            })

            // Reject the authentication promise to prevent the state from loading
            //return $q.reject()
          }
        }

        //Check for Logged in user from static pages, if timed out then repaint the header and footer pages
        function authenticateStaticPages() {

          if (readCookieFromUserName("username")!=null) {
             //Re-paint the header and footer
                initializePage();
            // Resolve the promise successfully
           // return $q.when()
          } else {
            // The next bit of code is asynchronously tricky.
            removeAllCookiesStaticPages();

          }
        }
        //Check for Logged in user from static pages, if timed out then repaint the header and footer pages
        function authenticateJoinInPage() {

          if (readCookieFromUserName("username")!=null) {
            window.location.href = '/afya-portal';
            // Resolve the promise successfully
           // return $q.when()
          } else {
            // The next bit of code is asynchronously tricky.
            removeAllCookiesStaticPages();

          }
        }



});

// create the controller and inject Angular's $scope
afyaApp.controller('mainController', ['$scope', '$state', function ($scope, $state) {
    // create a message to display in our view
    // $scope.message = 'Everyone come and see how good I look!';
    //$scope.userObject={};
    if (afyaApp.vars == undefined)
        afyaApp.vars = {};
    afyaApp.vars.$state = $state;
    //afyaApp.vars.$scope = $scope;
    //$scope.welcomeUserName = '';
}]);

afyaApp.run(function($anchorScroll, $window,$rootScope) {

$rootScope.$on('$stateChangeSuccess',function(){
    $("html, body").animate({ scrollTop: 0 }, 200);
})

});




    var afyaSessionStore = function(){
        /**
         * Global Vars
         */
        var storage = (typeof window.localStorage === 'undefined') ? undefined : window.localStorage,
            supported = !(typeof storage == 'undefined' || typeof window.JSON == 'undefined');

        var privateMethods = {
            parseValue: function(res) {
                var val;
                try {
                    val = JSON.parse(res);
                    if (typeof val == 'undefined'){
                        val = res;
                    }
                    if (val == 'true'){
                        val = true;
                    }
                    if (val == 'false'){
                        val = false;
                    }
                    if (parseFloat(val) == val && !angular.isObject(val) ){
                        val = parseFloat(val);
                    }
                } catch(e){
                    val = res;
                }
                return val;
            }
        };
        var publicMethods = {
            /**
             * Set - let's you set a new localStorage key pair set
             * @param key - a string that will be used as the accessor for the pair
             * @param value - the value of the localStorage item
             * @returns {*} - will return whatever it is you've stored in the local storage
             */
            set: function(key,value){
                if (!supported){
                    try {
                        $.cookie(key, value);
                        return value;
                    } catch(e){
                        console.log('Local Storage not supported, make sure you have the $.cookie supported.');
                    }
                }
                var saver = JSON.stringify(value);
                 storage.setItem(key, saver);
                return privateMethods.parseValue(saver);
            },
            /**
             * Get - let's you get the value of any pair you've stored
             * @param key - the string that you set as accessor for the pair
             * @returns {*} - Object,String,Float,Boolean depending on what you stored
             */
            get: function(key){
                if (!supported){
                    try {
                        return privateMethods.parseValue($.cookie(key));
                    } catch(e){
                        return null;
                    }
                }
                var item = storage.getItem(key);
                return privateMethods.parseValue(item);
            },
            /**
             * Remove - let's you nuke a value from localStorage
             * @param key - the accessor value
             * @returns {boolean} - if everything went as planned
             */
            remove: function(key) {
                if (!supported){
                    try {
                        $.cookie(key, null);
                        return true;
                    } catch(e){
                        return false;
                    }
                }
                storage.removeItem(key);
                return true;
            },
            removeAll: function() {
                if (!supported){
                     try {

                         //$.cookie(key, null);
                         return true;
                     } catch(e){
                         return false;
                     }
                }
                storage.clear();
                return true;
            },
            /**
                 * Bind - let's you directly bind a localStorage value to a $scope variable
                 * @param $scope - the current scope you want the variable available in
                 * @param key - the name of the variable you are binding
                 * @param def - the default value (OPTIONAL)
                 * @returns {*} - returns whatever the stored value is
                 */
                add: function (key, def) {
                    def = def || '';
                    if (!publicMethods.get(key)) {
                        publicMethods.set(key, def);
                    }


                    return publicMethods.get(key);
                }
        };
        return publicMethods;
    }

// Payment Gateway messages
if(window.afyaApp == undefined)
    window.afyaApp = {};

window.afyaApp.paymentGatewayMessage = {
    visaNotAccepted: "", // "VISA Cards are not accepted / processed for payment temporarily. Please use Master Cards instead.",
    feesWaivedOffer: "Payment Gateway Fees is waived as a Special Introductory Offer.",
};

