// JavaScript Document
$(document).ready(function () {
    // load popup
    $("#pop_up_window").load("/afya-portal/login_window.html");

    initializePage();

    if(afyaSessionStore().get("isLock"))
    {
        $("#LockModal").modal("show");
        getUserName();
    }

});

function readCookieFromUserName(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

        //Calls after login is successful
        function loginSuccessHandler(url){
            debugger;
            // read cookie to determine Patient / Provider
            var userRole = readCookieFromUserName("role");
            var nameWelcome='';
            var nameHeader='';
            var userObject;
            // if patient redirect to Patient-Dashboard
            var usernameCookie = readCookie("username");
            if(userRole == null){
                afyaApp.vars.$state.go("index");
            } if(userRole == 'ROLE_PATIENT'){
                userObject=getPatientByUsername();
                //afyaApp.vars.$state.go("PatientDashboard", {}, { reload: true });
               // if (userObject.MemberType.toLowerCase() != 'visitor') {
                     afyaApp.vars.$state.go("PatientDashboard", {}, { reload: true });

//                }
//                else{
//                     afyaApp.vars.$state.go("index",{cache:false}, { reload: true });
//
//
//                }
            }
            else{
                userObject=getProviderByUsername();

               // if (userObject.memberType == 'PREMIUM') {
                     afyaApp.vars.$state.go("ProviderDashboard", {}, { reload: true });

//                }
//                else{
//                     afyaApp.vars.$state.go("index",{}, { reload: true });
//
//                }
            }
            initializePage();

        }


        var initializePage = function(){
            assignHeaderURLs();
            assignFooterURLs();
        }


//declare global variable for the member login
var memberLoginPopup='data-toggle="modal" data-target="#member_login_spring_ajax"';

        //assign header urls based on login
        var assignHeaderURLs= function (){
            if(readCookieFromUserName("username")==null || readCookieFromUserName("role") == null)  {

                $("#headerTopLogin").html('<a href="#" ' + memberLoginPopup + '><i class="fa fa-user"></i>&nbsp; &nbsp;<strong> Member Log in</strong></a>');
                $("#headerPricingPlan").html('<p><a href="#" ' + memberLoginPopup + '>Pricing Plan</a></p>');
                $("#headerJoinIn").html('<a href="#JoinIn">Join In &nbsp; <i class="fa fa-chevron-right"></i></a> ');
                $("#headerMemberDashboard").html('<p><a href="#Member-area/member-account-dashboard">Member Dashboard</a></p> ');
                $("#headerMemberSupport").html('<p><a href="#" ' + memberLoginPopup + '>Member Support</a></p>');
                $("#headerMemberSupportDashboard").html('<p><a href="#" ' + memberLoginPopup + '>Member Support</a></p>');
                $("#headerCommunitySupport").html('<a href="#" ' + memberLoginPopup + '><i class="fa fa-phone"></i> &nbsp; &nbsp;  Community Support</a>');
                $("#headerLock").html('&nbsp;');
            }
            else {
                if(afyaSessionStore().get('userObject')==null){
                     getUserObjectFromRole();
                }

                var userObject = afyaSessionStore().get('userObject');

                var headerUserName=  (userObject.salutation || '')+" "+(userObject.firstName || '') +" "+(userObject.lastName || '')
                $("#headerTopLogin").html('<i class="fa fa-user"></i> <strong> <span id="userNameText">'+headerUserName+'</span>&nbsp;<a href="#" onclick="removeAllCookies()">Logout</a></strong>');
                $("#headerMemberSupport").html('<p><a href="#AfyaServices/member-support">Member Support</a></p>');
                $("#headerMemberSupportDashboard").html('<p><a href="#AfyaServices/member-support">Member Support</a></p>');
                $("#headerJoinIn").empty();
                $("#headerCommunitySupport").html('<a href="#AfyaServices/member-support "><i class="fa fa-phone"></i> &nbsp; &nbsp;  Community Support</a>');
                $("#headerLock").html('<a href="#" data-toggle="modal" data-target="#LockModal" onclick="getUserName()">Lock</a>');

                if(readCookieFromUserName("username")!=null && isProviderRole()) {
                    if( userObject.providerType == 'Pharmacy')
                        $("#headerPricingPlan").html('<p><a class="clickable" data-target="#mySmallModalComingSoonPharmacy" data-toggle="modal">Pricing Plan</a></p>');
                    else
                        $("#headerPricingPlan").html('<p><a href="#AfyaServices/pricing-plan">Pricing Plan</a></p>');
                   // if( userObject.memberType == 'PREMIUM'){
                        $("#headerMemberDashboard").html('<p><a href="#ProviderDashboard">Member Dashboard</a></p> ');
//                    }
//                    else {
//                        $("#headerMemberDashboard").html('<p><a href="#">Member Dashboard</a></p> ');
//                    }
                }
                if(readCookieFromUserName("username")!=null && (readCookieFromUserName("role") == "ROLE_PATIENT")) {
                    $("#headerPricingPlan").html('<p>Pricing Plan</p>');
                    //if (userObject.MemberType.toLowerCase() != 'visitor') {
                        $("#headerMemberDashboard").html('<p><a href="#PatientDashboard">Member Dashboard</a></p> ');
//                    }
//                    else {
//                        $("#headerMemberDashboard").html('<p><a href="#">Member Dashboard</a></p> ');
//                    }
                }
            }
        }
        //assign header urls based on login
        var assignFooterURLs= function (){
            if(readCookieFromUserName("username")==null || readCookieFromUserName("role") == null)  {
                 $("#footerAfyaPatientServices").html('<a href="#AfyaServices/afya-patient-service">Patient</a>');
                 $("#footerAfyaProviderServices").html('<a href="#AfyaServices/care-provider-services">Care Provider</a>');
                 $("#footerPricingPlan").html('<a href="#" ' + memberLoginPopup + '>Pricing Plan</a>');
                 $("#footerPatientAccount").html('<a href="#" ' + memberLoginPopup + '>Patient</a>');
                 $("#footerProviderAccount").html('<a href="#" ' + memberLoginPopup + '>Care Provider</a>');

                 $("#footerMemberJoinIn").html('<a href="#JoinIn">Not a Member? Join In</a>');
                 $("#footerMemberDashboard").html('<a href="#Member-area/member-account-dashboard">Member Dashboard</a>');
                 $("#footerMemberSupport").html('<a href="#" ' + memberLoginPopup + '>Member Support</a>');
                 $("#footerMemberSupportDashboard").html('<a href="#" ' + memberLoginPopup + '>Member Support</a>');

            }
            else {
                if(afyaSessionStore().get('userObject')==null){
                     getUserObjectFromRole();
                }

                var userObject = afyaSessionStore().get('userObject');

                var headerUserName=  (userObject.salutation || '')+" "+(userObject.firstName || '') +" "+(userObject.lastName || '')
                $("#footerMemberJoinIn").html('Not a Member? Join In');
                $("#footerMemberSupport").html('<a href="#AfyaServices/member-support">Member Support</a>');
                $("#footerMemberSupportDashboard").html('<a href="#AfyaServices/member-support">Member Support</a>');

                if(readCookieFromUserName("username")!=null && isProviderRole()) {
                    $("#footerAfyaPatientServices").html('<a href="#AfyaServices/afya-patient-service">Patient</a>');
                    $("#footerAfyaProviderServices").html('<a href="#AfyaServices/care-provider-services">Care Provider</a>');
                    $("#footerPricingPlan").html('<a href="#AfyaServices/pricing-plan">Pricing Plan</a>');
                    $("#footerPatientAccount").html('Patient');

                   // if( userObject.memberType == 'PREMIUM'){
                        $("#footerProviderAccount").html('<a href="#ProviderDashboard">Care Provider</a>');
                        $("#footerMemberDashboard").html('<a href="#ProviderDashboard">Member Dashboard</a>');
//                    }
//                    else {
//                        $("#footerProviderAccount").html('<a href="#">Care Provider</a>');
//                        $("#footerMemberDashboard").html('<a href="#">Member Dashboard</a>');
//                    }
                }
                if(readCookieFromUserName("username")!=null && (readCookieFromUserName("role") == "ROLE_PATIENT")) {

                    $("#footerAfyaPatientServices").html('<a href="#AfyaServices/afya-patient-service">Patient</a>');
                    $("#footerAfyaProviderServices").html('<a href="#AfyaServices/care-provider-services">Care Provider</a>');
                    $("#footerPricingPlan").html('Pricing Plan');
                    $("#footerProviderAccount").html('Care Provider');
                    //if (userObject.MemberType.toLowerCase() != 'visitor') {
                        $("#footerPatientAccount").html('<a href="#PatientDashboard">Patient</a>');

                        $("#footerMemberDashboard").html('<a href="#PatientDashboard">Member Dashboard</a>');
//                    }
//                    else {
//                        $("#footerPatientAccount").html('Patient');
//                        $("#footerMemberDashboard").html('<p><a href="#">Member Dashboard</a></p> ');
//                    }


                }
            }
        }

        function homePharmacyClick(){
            //get patient object by username
            var patientData = getPatientByUsername();
            if(patientData != null && (patientData.username != null || patientData.username != undefined)){
                if(readCookieFromUserName("role") != "ROLE_PATIENT"){
                    initializePage();
                    sweetAlert({ title: "", text: 'Hi, This service is available only if you are logged in as a Patient.', type: "warning" });
                    return false;
                }

                sweetAlert({
                  title: "",
                  text: "The service is available for Afya Members with valid prescriptions",
                  type: "info",
                  showCancelButton: true,
                  confirmButtonColor: "#DD6B55",
                  confirmButtonText: "OK",
                  cancelButtonText: "Cancel",
                  closeOnConfirm: true
                },
                function(isConfirm){
                    if (isConfirm)
                        window.location.href = '#homePharmacy';// redirect homePharmacy
                });
                // redirect homePharmacy
                //window.location.href = '#homePharmacy';
                //$state.go("homePharmacy");
            }
            else {
                sweetAlert({
                  title: "",
                  text: "The service is available for Afya Members with valid prescriptions, if you are a Afya Member please login to your account",
                  type: "info",
                  showCancelButton: true,
                  confirmButtonColor: "#DD6B55",
                  confirmButtonText: "OK",
                  cancelButtonText: "Cancel",
                  closeOnConfirm: true
                },
                function(isConfirm){
                    if (isConfirm)
                        $("#myLogin").modal("show");
                });
            }
        }

        //Validate for provider roles
        var isProviderRole = function(){
            isProvider=false;

            if((readCookieFromUserName("role") == "ROLE_FACILITY_ADMIN"  || readCookieFromUserName("role") == "PHARMACY_ROLE" || readCookieFromUserName("role") == "ROLE_ADMIN")){
                isProvider= true;
            }
            return isProvider;
        }

        //Get the userObject when its null from role
        var getUserObjectFromRole = function(){
            if((readCookieFromUserName("role") == "ROLE_PATIENT")){
                getPatientByUsername();
            }
            if((readCookieFromUserName("role") == "ROLE_FACILITY_ADMIN"  || readCookieFromUserName("role") == "PHARMACY_ROLE")){
                getProviderByUsername();
            }

        }
        // Gets the Patient object from username
        var getPatientByUsername= function(){
            if(afyaSessionStore().get('userObject')==null || !afyaSessionStore().get('userObject').username)
            {
                $.ajax({
                         url: "/afya-portal/getPatientByUsername",
                         type: 'get',
                         dataType: 'json',
                         async: false,
                         success: function (data) {
                            afyaSessionStore().remove('userObject');
                            afyaSessionStore().add('userObject',data);
                         }
                });
            }
            return afyaSessionStore().get('userObject');
        }
        // Gets the Provider object from username
        var getProviderByUsername= function(){

            if(afyaSessionStore().get('userObject')==null || !afyaSessionStore().get('userObject').username)
            {

                $.ajax({
                         url: "/afya-portal/getProviderByUsername",
                         type: 'get',
                         dataType: 'json',
                         async: false,
                         success: function (data) {
                            afyaSessionStore().remove('userObject');
                            afyaSessionStore().add('userObject',data);
                         }
                 });
            }

            return afyaSessionStore().get('userObject');
        }


        var assignWelcomeUserName= function(){
            var userObject =afyaSessionStore().get('userObject');
            var nameWelcome = "";
            if(readCookieFromUserName("role") == "ROLE_PATIENT"){
                nameWelcome = "<h2>Welcome, "+(userObject.salutation || '')+" "+(userObject.firstName || '') +" "+(userObject.lastName || '')
                + (userObject.memberTypeDisplayName ? "&nbsp;|&nbsp;" + userObject.memberTypeDisplayName + " Member": '')
                + (userObject.afyaId ? "&nbsp;-&nbsp;" + userObject.afyaId : '') + "<h2>";
            }
            else
            {
                nameWelcome = "<h2>Welcome, "+(userObject.salutation || '')+" "+(userObject.firstName || '') +" "+(userObject.lastName || '')
                +(userObject.memberTypeDisplayName ? "&nbsp;|&nbsp;" + userObject.memberTypeDisplayName + " Member": '')+"<h2>";
            }
            $("#nameDiv").html(nameWelcome);
        }
        var assignHeaderUserName= function(){

        }




      // ekolightbox js script

      $(document).ready(function ($) {

          // delegate calls to data-toggle="lightbox"
          $(document).delegate('*[data-toggle="lightbox"]:not([data-gallery="navigateTo"])', 'click', function (event) {
              event.preventDefault();
              return $(this).ekkoLightbox({
                  onShown: function () {
                      if (window.console) {
                          return console.log('Checking our the events huh?');
                      }
                  },
                  onNavigate: function (direction, itemIndex) {
                      if (window.console) {
                          return console.log('Navigating ' + direction + '. Current item: ' + itemIndex);
                      }
                  }
              });
          });

//                                        //Programatically call
//                                        $('#open-image').click(function (e) {
//                                            e.preventDefault();
//                                            $(this).ekkoLightbox();
//                                        });
//                                        $('#open-youtube').click(function (e) {
//                                            e.preventDefault();
//                                            $(this).ekkoLightbox();
//                                        });

          $(document).delegate('*[data-gallery="navigateTo"]', 'click', function (event) {
              event.preventDefault();
              return $(this).ekkoLightbox({
                  onShown: function () {
                      var a = this.modal_content.find('.modal-footer a');
                      if (a.length > 0) {
                          a.click(function (e) {
                              e.preventDefault();
                              this.navigateTo(2);
                          }.bind(this));
                      }
                  }
              });
          });


          // fade in #back-top
          $(function () {

              // scroll body to 0px on click
              $('#back-top').click(function () {
                  $('body,html').animate({
                      scrollTop: 0
                  }, 800);
                  return false;
              });
          });

      });

       function getUserName(){
            var username = readCookieFromUserName('username');
            $("#lblUserName").html(username);
            $("#txtUnlockPassword").val('');
            $("#lblLockError").html('');
            //$.removeCookie("isLock", { path: '/afya-portal'});
            //$.removeCookie("currentURL", { path: '/afya-portal'});
            //$.cookie("isLock", true);
            //$.cookie("currentURL", document.URL.split( document.origin)[1]);
            afyaSessionStore().remove('isLock');
            afyaSessionStore().remove('currentURL');
            afyaSessionStore().add("isLock", true);
            afyaSessionStore().add("currentURL", document.URL.split( document.origin)[1]);
       }

       function btnUnlockClicked()
       {
            $.ajax({
                 url: "/afya-portal/loginViaPopUp?username=" + readCookieFromUserName("username") + "&password=" + $('#txtUnlockPassword').val(),
                 type: 'post',
                 async: false,
                 success: function (data) {
                    //if(data.message == "success") {
                        //$.removeCookie("isLock", { path: '/afya-portal'});
                        //$.cookie("isLock", false);
                        afyaSessionStore().remove('isLock');
                        afyaSessionStore().add("isLock", false);
                        $('#LockModal').modal('hide');
                        //window.location.href = $.cookie("currentURL");
                        window.location.href = afyaSessionStore().get("currentURL");

                 },
                 error : function(data){
                    console.log(data);
                    $('#lblLockError').html('<label style="color:red">Invalid Password</label>');
                   // sweetAlert({ title: "Sorry", text: data.responseJSON.message, type: "error" });
                 }
            });
       }
