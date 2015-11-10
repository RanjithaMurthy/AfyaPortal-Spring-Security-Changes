
       function footerElement(){
           var patientAccount='';
           var afyaServicesPatientAccount='<li class="sub-li"><a href="/afya-portal/web_pages/afya_services/afya-patient-service.html">Patient</a></li>';
           var providerAccount='';
           var afyaServicesProviderAccount='<li class="sub-li"><a href="/afya-portal/web_pages/afya_services/care-provider-services.html">Care Provider</a></li>';
           var pricingPlan='';
           var joinIn= '';
           var memberDashboardFooterHTML='<a href="/afya-portal/web_pages/member_area/member-account-dashboard.html">Member Dashboard</a>';
            if(readCookieFromUserName("username")==null)
            {
                patientAccount = '<a href="#" data-toggle="modal" data-target="#member_login_spring">Patient</a>';
                providerAccount = '<a href="#" data-toggle="modal" data-target="#member_login_spring">Care Provider</a>';
                pricingPlan = '<a href="#" data-toggle="modal" data-target="#member_login_spring">Pricing Plan</a>';
                joinIn='<li><a href="/afya-portal/join-in.html">Not a Member? Join In</a></li>';
//                afyaServicesPatientAccount='<a href="/afya-portal/web_pages/afya_services/afya-patient-service.html">Patient</a>';
//                afyaServicesProviderAccount='<a href="/afya-portal/web_pages/afya_services/care-provider-services.html">Care Provider</a>';
            }
            else
            {
                if (readCookieFromUserName("role") != "ROLE_PATIENT")
                {
                    pricingPlan = '<a href="/afya-portal/pricing-plan.html">Pricing Plan</a>';
                    providerAccount = '<a id="lnkCareProvider" href="#">Care Provider</a>';
                    patientAccount = 'Patient';
                    joinIn='<li>Not a Member? Join In</li>';
                    afyaServicesPatientAccount='Patient';
                }
                else
                {
                    pricingPlan = 'Pricing Plan';
                    providerAccount = 'Care Provider';
                    afyaServicesProviderAccount= 'Care Provider';
                    patientAccount = '<a href="/afya-portal/web_pages/member_area/patient/Patient_account.html">Patient</a>';
                    memberDashboardFooterHTML='<a href="/afya-portal/web_pages/member_area/patient/Patient_account.html">Member Dashboard</a> ';
                    joinIn='<li>Not a Member? Join In</li>';
                }




            }
          footerCode = '<div class="container">'+
                        '<div class="row">'+
                            '<div class="col-md-3 col-sm-6">'+
                                '<div class="widget">'+
                                   '<h3><a href="/afya-portal/web_pages/ExperienceAfya/experience-afya.html">Experience Afya</a></h3>'+
                                   '<ul>'+
                                      '<li><a href="/afya-portal/web_pages/ExperienceAfya/afya-ecosystem.html">Afya Ecosystem</a></li>'+
                                      '<li class="sub-li"><a href="/afya-portal/web_pages/ExperienceAfya/smart-care-community.html">Smart Care Community</a></li>'+
                                     '<li class="sub-li"><a href="/afya-portal/web_pages/ExperienceAfya/social-innovation.html">Social Innovation</a></li>'+
                                     ' <li class="sub-li"><a href="/afya-portal/web_pages/ExperienceAfya/future-care.html">Future of Care</a></li>'+
                                      '<li><a href="/afya-portal/web_pages/ExperienceAfya/afya-care-experience.html">Afya Care Experience</a></li>'+
                                      '<li><a href="/afya-portal/web_pages/ExperienceAfya/afya-services.html">Afya Services</a></li>'+
                                     '<li><a href="/afya-portal/web_pages/faq/faq.html">FAQ</a></li>'+
                                   '</ul>'+
                                '</div>'+
                           '</div>'+
                           '<!--/.col-md-3-->'+
                           '<div class="col-md-2 col-sm-6">'+
                               '<div class="widget">'+
                                 '<h3><a href="/afya-portal/web_pages/ExperienceAfya/afya-services.html">Afya Services</a></h3>'+
                                 '<ul>'+
                                    //'<li class="sub-li">'+
                                    afyaServicesPatientAccount +
                                    //'</li>'+
                                    // '<li class="sub-li">'+
                                     afyaServicesProviderAccount +
                                     //'</li>'+
                                     '<li>'+
                                     pricingPlan +
                                      '</li>'+
                                      '<li><a href="/afya-portal/web_pages/afya_services/afya-mobile-app.html">Afya Mobile App</a></li>'+
                                      '<li><a href="/afya-portal/ecosystem-adaptation.html">Ecosystem Adaptation</a></li>'+
                                      '<li><a href="/afya-portal/web_pages/afya_services/scalability-of-afya.html">Scalability of Afya</a></li>'+
                                      '<li><a href="/afya-portal/web_pages/afya_services/member-support.html">Member Support</a></li>'+
                                      '<li><a href="/afya-portal/web_pages/afya_services/security-confidentiality.html">Security &amp; Confidentiality</a></li>'+
                                    '</ul>'+
                               '</div>'+
                           '</div>'+
                            '<!--/.col-md-3-->'+

                           '<div class="col-md-2 col-sm-6">'+
                              '<div class="widget">'+
                                    '<h3><a href="/afya-portal/web_pages/member_area/member-area.html">Member Area</a></h3>'+
                                  '<ul>'+
                                  '<li>'+
                                     patientAccount +
                                     '</li>'+
                                     '<li>'+
                                     providerAccount +
                                      '</li>'+

                                      joinIn +
                                     '<li><a href="/afya-portal/web_pages/afya_services/member-support.html">Member Support</a></li>'+
                                      '<li>'+
                                      memberDashboardFooterHTML+
                                      '</li>'+
                                      '<!--<li><a href="members-speak.html">Member Speak</a></li>-->'+
                                  '</ul>'+
                             '</div>'+
                         '</div>'+
                           '<!--/.col-md-3-->'+
                           '<div class="col-md-3 col-sm-6">'+
                              '<div class="widget">'+
                                   '<h3><a href="/afya-portal/web_pages/about_afya/about-afya.html">About Afya</a></h3>'+
                                  '<ul>'+
                                    '<li><a href="/afya-portal/web_pages/about_afya/about-afya.html">Overview &amp; Leadership</a></li>'+
                                    '<!--<li><a href="/afya-portal/web_pages/about_afya/press-news.html">Press News</a></li>'+
                                     '<li><a href="/afya-portal/web_pages/about_afya/careers.html">Careers</a></li>-->'+
                                      '<li><a class="submenuheader" href="#">Trust</a>'+
                                        '<div class="submenu">'+
                                          '<ul>'+
                                                 '<li class="sub-li"><a href="/afya-portal/web_pages/about_afya/security.html">Security</a></li>'+
                                                  '<li class="sub-li"><a href="/afya-portal/web_pages/about_afya/membership-agreement.html">Membership agreement</a></li>'+
                                                  '<li class="sub-li"><a href="/afya-portal/web_pages/about_afya/subscription-agreement.html">Subscription agreement</a></li>'+
                                                  '<li class="sub-li"><a href="/afya-portal/web_pages/about_afya/privacy-confidentiality.html">Privacy &amp; confidentiality</a></li>'+
                                                  '<li class="sub-li"><a href="/afya-portal/web_pages/about_afya/terms-of-use.html">Terms of Use</a></li>'+
                                               '</ul>'+
                                           '</div>'+
                                    '</li>'+
                                     '<li><a href="/afya-portal/web_pages/about_afya/contact-afya.html">Contact Us</a></li>'+
                                   '<li><a href="/afya-portal">Afya Home</a></li>'+
                                    '</ul>'+
                               '</div>'+
                           '</div>'+
                           '<!--/.col-md-3-->'+
                           '<div class="col-md-2 col-sm-6">'+
                              '<div class="widget text-center xs-text-left">'+
                                 '<ul>'+
                                      '<li><a href="/afya-portal"><img src="/afya-portal/static/images/foot-logo.png" alt="footer logo"></a></li>'+
                                       '<li class="mtb25"><a class="footer-adj-text">Connect to <br/>Afya Social Network</a></li>'+
                                       '<li>'+
                                         '<a href="#"><i class="fa fa-facebook circle-icon"></i></a>'+
                                         '<a href="#"><i class="fa fa-twitter circle-icon"></i></a>'+
                                         '<a href="#"><i class="fa fa-google-plus circle-icon"></i> </a>'+
                                      '</li>'+
                                 '</ul>'+
                              '</div>'+
                          '</div>'+
                        '<!--/.col-md-3-->'+
                     '</div>'+
                   '</div>';
                     return footerCode;
                     }


                     function copyrightFooterElement()
                     {

                        var copyrightFooter= '<div class="container">'+
                               '    <div class="row">'+
                               '        <div class="col-sm-6 text-right"> &copy; 2015 All Rights Reserved. </div>'+
                               '        <div class="col-sm-6 text-left"> <a href="/afya-portal/privacy-disclaimer.html">Privacy Disclaimer</a> </div>'+
                               '    </div>'+
                              ' </div>';

                           return copyrightFooter;

                   }



            //All js file
             $('head').append('<script src="/afya-portal/static/js/jquery_cookie.js"></script>');
             $('head').append('<script src="/afya-portal/static/js/MemberProfileDetails.js"></script>');
             $('head').append('<script src="/afya-portal/static/js/sweet-alert.min.js"></script>');
              $('head').append('<script src="/afya-portal/static/js/bootstrap.min.js" type="text/javascript"></script>');


              $('head').append('<script src="/afya-portal/static/js/main.js" type="text/javascript"></script>');
              $('head').append('<script src="/afya-portal/static/js/wow.min.js" type="text/javascript"></script>');

             //All css file
             $('head').append('<link href="/afya-portal/static/css/sweet-alert.css" rel="stylesheet" type="text/css">');
              $('head').append('<link href="/afya-portal/static/css/custom.css" rel="stylesheet" type="text/css">');

      ddaccordion.init({
                      headerclass: "submenuheader", //Shared CSS class name of headers group
                      contentclass: "submenu", //Shared CSS class name of contents group
                      revealtype: "click", //Reveal content when user clicks or onmouseover the header? Valid value: "click", "clickgo", or "mouseover"
                      mouseoverdelay: 200, //if revealtype="mouseover", set delay in milliseconds before header expands onMouseover
                      collapseprev: true, //Collapse previous content (so only one open at any time)? true/false
                      defaultexpanded: [], //index of content(s) open by default [index1, index2, etc] [] denotes no content
                      onemustopen: false, //Specify whether at least one header should be open always (so never all headers closed)
                      animatedefault: false, //Should contents open by default be animated into view?
                      persiststate: true, //persist state of opened contents within browser session?
                      toggleclass: ["", ""], //Two CSS classes to be applied to the header when it's collapsed and expanded, respectively ["class1", "class2"]
                      togglehtml: ["suffix", "<img src='/afya-portal/static/images/plus.jpg' class='statusicon' />", "<img src='/afya-portal/static/images/minus.jpg' class='statusicon' />"], //Additional HTML added to the header when it's collapsed and expanded, respectively  ["position", "html1", "html2"] (see docs)
                      animatespeed: "fast", //speed of animation: integer in milliseconds (ie: 200), or keywords "fast", "normal", or "slow"
                      oninit: function (headers, expandedindices) { //custom code to run when headers have initalized
                          //do nothing

                      },
                      onopenclose: function (header, index, state, isuseractivated) { //custom code to run whenever a header is opened or closed
                          //do nothing
                      }
                  })

             $(document).ready(function (){

                  var footer = copyrightFooterElement();
                  $("#footer").append(footer);

                 if(readCookieFromUserName("username")!=null && (readCookieFromUserName("role") != "ROLE_PATIENT")) {
                     $.ajax({
                         url: "/afya-portal/anon/getProviderByUsername?username=" + readCookieFromUserName("username"),
                         type: 'get',
                         dataType: 'json',
                         async: true,
                         success: function (data) {

                             if (data.memberType == 'PREMIUM') {

                                 $('#lnkCareProvider').attr('href',"/afya-portal/web_pages/member_area/provider/Provider_account.html"); //'<a href="/afya-portal/web_pages/member_area/provider/Provider_account.html">Care Provider</a>';
                                        memberDashboardFooterHTML='<a href="/afya-portal/web_pages/member_area/provider/Provider_account.html">Member Dashboard</a> ';

                                  }
                                else{
                               	memberDashboardFooterHTML='<a href="/afya-portal">Member Dashboard</a> ';
                               }
                         }
                     });
                 }
              });