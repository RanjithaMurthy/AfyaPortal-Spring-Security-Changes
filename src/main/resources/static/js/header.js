function headerElement(){

var userDivHTML='';
var pricingPlan='';
var memberDashboardHTML='<a href="/afya-portal/web_pages/member_area/member-account-dashboard.html">Member Dashboard</a>';
var JoinIn='<a href="/afya-portal/join-in.html">Join In &nbsp; <i class="fa fa-chevron-right"></i></a>';

		if(readCookieFromUserName("username")==null)
		{
			userDivHTML='<a href="#" data-toggle="modal" data-target="#member_login"><i class="fa fa-user"></i>'+
						  ' &nbsp; &nbsp;<strong> Member Log in</strong></a></li>';
			pricingPlan = '<a href="#" data-toggle="modal" data-target="#member_login">Pricing Plan</a>';
			memberDashboardHTML='<a href="/afya-portal/web_pages/member_area/member-account-dashboard.html">Member Dashboard</a> ';
		}
		else
		{
			JoinIn='';
			userDivHTML= '<i class="fa fa-user"></i> <strong> <span id="userNameText">Member Log in </span>&nbsp;'+
						  '<a href="#" onclick="removeAllCookies()">Logout</a></strong></li>';
			pricingPlan = '<a href="/afya-portal/pricing-plan.html">Pricing Plan</a>';

			if(readCookieFromUserName("username")!=null && (readCookieFromUserName("role") != "ROLE_PATIENT")) {
                 $.ajax({
                         url: "/afya-portal/anon/getProviderByUsername?username=" + readCookieFromUserName("username"),
                         type: 'get',
                         dataType: 'json',
                         async: true,
                         success: function (data) {

                                if (data.memberType == 'PREMIUM') {
									memberDashboardHTML='<a href="/afya-portal/web_pages/member_area/provider/Provider_account.html">Member Dashboard</a> ';

                                }
                                else{
                                	memberDashboardHTML='<a href="/afya-portal">Member Dashboard</a> ';
                                }
                         }
                 });
            }
            if((readCookieFromUserName("role") == "ROLE_PATIENT")) {
           		memberDashboardHTML='<a href="/afya-portal/web_pages/member_area/patient/Patient_account.html">Member Dashboard</a> ';

            }
		}
	  headerCode = '<div class="top-bar">'+
	  '<div class="container">'+
	  '<div class="row">'+
	  '<div class="col-sm-12 col-md-12 hidden-xs">'+
	  '<div class="social">'+
	  '<ul class="social-share"><li>'+
	 userDivHTML +
	  '<li><a href="/afya-portal/web_pages/afya_services/member-support.html "><i class="fa fa-phone"></i> '+
	  '&nbsp; &nbsp;  Community Support</a></li><li class="hidden-sm"><a href="/afya-portal/web_pages/about_afya/contact-afya.html">Contact Us</a></li>'+
	  '<li class="dropdown"> <a href="#" class="dropdown-toggle" data-toggle="dropdown">'+
	  '<!--<img src="../../images/usa.jpg" alt="usa"> --> English &nbsp;<i class="fa fa-caret-down"></i></a>'+
	  '<!-- <ul class="dropdown-menu sub-navi"><li><a href="#"><img src="../../images/lang-en.jpg" alt="Eng"></a></li></ul>-->'+
	  '</li></ul>'+
	  '<div class="hidden-sm pull-right">'+
	  '<div class="search "><input type="button" class="search-btn">'+
	  '<form role="form"><input type="text" class="search-form" autocomplete="off" placeholder="Search"></form>'+
	  '</div>'+
	  '<a href="#"><i class="fa fa-facebook top-fb-circle-icon"></i></a> <a href="#"><i class="fa fa-twitter top-tw-circle-icon"></i></a>'+
	  ' </div>'+
	  '</div>'+
	  '</div>'+
	  '</div>'+
	  '</div><!--/.container--></div>'+
	 // '<div class="clearfix">'+
	 // '</div>'+
	  '<div class="navbar yamm navbar-default">'+
	  '<div class="container">'+
	  '<div class="navbar-header">'+
	  '<button type="button" data-toggle="collapse" data-target="#navbar-collapse-1" class="navbar-toggle"><span class="icon-bar"></span>'+
	  '<span class="icon-bar"></span><span class="icon-bar"></span></button><a class="navbar-brand" href="/afya-portal	  ">'+
	  '<img src="/afya-portal/static/images/logo.png" alt="logo"></a> '+
	  '</div>'+
	  '<div id="navbar-collapse-1" class="navbar-collapse collapse">'+
	  '<ul class="nav navbar-nav"><li class="dropdown yamm-fw">'+
	  '<a href="/afya-portal/web_pages/ExperienceAfya/experience-afya.html" class="dropdown-toggle nmt">Experience Afya<b class="caret">'+
	  '</b></a>'+
	  '<ul class="dropdown-menu"><li>'+
	  '<div class="yamm-content">'+
	  '<div class="row">'+
	  '<div class="col-xs-12 col-sm-4 col-md-4">'+
	  '<ul class="sub-menu"><li><p><a href="/afya-portal/web_pages/ExperienceAfya/afya-ecosystem.html">Afya Ecosystem</a></p></li><li class="list-styled-blue"><p>'+
	  '<a href="/afya-portal/web_pages/ExperienceAfya/smart-care-community.html">Smart Care Community</a></p></li><li class="list-styled-blue"><p>'+
	  '<a href="/afya-portal/web_pages/ExperienceAfya/social-innovation.html">Social Innovation</a></p></li></ul>'+
	  '</div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4">'+
	  '<ul class="sub-menu"><li class="list-styled-blue"><p><a href="/afya-portal/web_pages/ExperienceAfya/future-care.html">Future of Care</a></p></li><li><p>'+
	  '<a href="/afya-portal/web_pages/ExperienceAfya/afya-care-experience.html">Afya Care Experience</a></p></li><li><p>'+
	  '<a href="/afya-portal/web_pages/ExperienceAfya/afya-services.html">Afya Services</a>'+
	  '</p></li></ul>'+
	  '</div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4"><ul class="sub-menu"><li><p><a href="/afya-portal/web_pages/faq/faq.html">FAQ</a></p></li></ul>'+
	  '</div>'+
	  '</div>'+
	  '</div>'+
	  '</li></ul></li><li class="dropdown yamm-fw">'+
	  '<a href="/afya-portal/web_pages/ExperienceAfya/afya-services.html" class="dropdown-toggle nmt">Afya Services<b class="caret"></b></a><ul class="dropdown-menu"><li>'+
	  '<div class="yamm-content">'+
	  '<div class="row">'+
	  '<div class="col-xs-12 col-sm-4 col-md-4"><ul class=" sub-menu"><li><p>'+
	  pricingPlan +
	  '</p></li><li><p><a href="/afya-portal/web_pages/afya_services/member-support.html">Member Support</a></p></li></ul></div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4"><ul class=" sub-menu"><li><p><a href="/afya-portal/web_pages/afya_services/afya-mobile-app.html">Afya Mobile App</a></p></li>'+
	  '<li><p><a href="/afya-portal/ecosystem-adaptation.html">Ecosystem Adaptation</a></p></li></ul>'+
	  '</div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4">'+
	  '<ul class=" sub-menu"><li><p><a href="/afya-portal/web_pages/afya_services/scalability-of-afya.html">Scalability of Afya</a></p></li><li><p>'+
	  '<a href="/afya-portal/web_pages/afya_services/security-confidentiality.html">Security &amp; Confidentiality</a></p></li></ul>'+
	  '</div>'+
	  '</div>'+
	  '</div></li></ul></li>'+
	  '<li class="dropdown yamm-fw"><a href="/afya-portal/web_pages/member_area/member-area.html" class="dropdown-toggle nmt">Member Area <b class="caret"></b></a>'+
	  '<ul class="dropdown-menu"><li>'+
	  '<div class="yamm-content">'+
	  '<div class="row">'+
	  '<div class="col-xs-12 col-sm-4 col-md-4">'+
	  '<ul class=" sub-menu"><li><p>'+
	  memberDashboardHTML +
	  '</p></li><li><p>'+
	  '<a href="/afya-portal/web_pages/afya_services/member-support.html">Member Support</a></p></li></ul>'+
	  '</div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4">'+
	  '<ul class=" sub-menu"><li><!--<p><a href="member-support.html">Member Speak</a></p>--></li></ul>'+
	  '</div>'+
	  '</div>'+
	  '</div>'+
	  '</li></ul></li>'+
	  '<li class="dropdown yamm-fw"><a href="/afya-portal/web_pages/about_afya/about-afya.html"  class="dropdown-toggle nmt"> About Afya<b class="caret"></b></a>'+
	  '<ul class="dropdown-menu"><li>'+
	  '<div class="yamm-content">'+
	  '<div class="row">'+
	  '<div class="col-xs-12 col-sm-4 col-md-4">'+
	  '<ul class=" sub-menu"><li><p><a href="/afya-portal/web_pages/about_afya/about-afya.html">Overview &amp; Leadership</a></p></li><li><p>'+
	  '<a href="/afya-portal/web_pages/about_afya/afya-trust.html">Trust</a></p></li><li class="list-styled-blue"><p><a href="/afya-portal/web_pages/about_afya/security.html">Security</a></p></li></ul></div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4"><ul class=" sub-menu">'+
	  '<!--<li><p><a href="/afya-portal/web_pages/about_afya/press-news.html">Press &amp; News</a></p></li><li><p><a href="careers.html">Careers</a></p></li>-->'+
	  '<li class="list-styled-blue"><p><a href="/afya-portal/web_pages/about_afya/membership-agreement.html">Membership Agreement</a></p></li>'+
	  '<li class="list-styled-blue"><p><a href="/afya-portal/web_pages/about_afya/subscription-agreement.html">Subscription Agreement</a></p></li></ul>'+
	  '</div>'+
	  '<div class="col-xs-12 col-sm-4 col-md-4"><ul class=" sub-menu"><li class="list-styled-blue"><p>'+
	  '<a href="/afya-portal/web_pages/about_afya/privacy-confidentiality.html">Privacy &amp; Confidentiality</a></p></li><li class="list-styled-blue"><p>'+
	  '<a href="/afya-portal/web_pages/about_afya/terms-of-use.html">Terms Of Use</a></p></li></ul>'+
	  '</div>'+
	  '</div>'+
	  '</div>'+
	  '</li></ul></li>'+
	  '<!--<li class="dropdown yamm-fw"><a href="Events.html" class="nmt"> Events </a></li>-->'+
	  '<li class="dropdown yamm-fw"><li class="btn-regi">'+

	  JoinIn +

	   '</li>'+
	  '</ul>'+
	  '</div>'+
	  '</div>'+
	  '</div>';


	  return headerCode;  
  };
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

