<!--<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
</head>
<body>
This is a Quotation Test
</body>
</html>-->

<!DOCTYPE html>
<html ng-app="SubscriptionQuotationModule">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Quotation</title>
</head>

<body>
<div align="center" ng-controller="SubscriptionQuotationController">
  <table width="80%" border="1" bordercolor="#F5F5F5" style="border-collapse: collapse;">
    <tr>
      <td align="center"><img src="/afya-portal/static/images/logo.png" width="129" height="126" /></td>
    </tr>
    <tr>
      <td align="center" bgcolor="#26ABE2"><font size="5" color="#FFFFFF">Quotation</font></td>
    </tr>
    <tr>
      <td><table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr>
          <td width="11%" height="18">Membership ID</td>
          <td width="30%">${membershipId}</td>
          <td width="10%">Member Type</td>
          <td width="34%">${memberType}</td>
          <td width="7%">Date</td>
          <td width="8%">${dateFormated}</td>
        </tr>
        <tr>
          <td>Member Name</td>
          <td>${memberName}</td>
          <td>Referred By</td>
          <td>${referredBy}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 1px solid #000000"><font color="#000000">We thank you for your interest in Afya Ecosystem and look forward for your continued participation in the journey of building a SMART CARE Community.<br />
            <font color="#000000">Details of the pack you have chosen are as below</font></font></span></td>
          </tr>
      </table>
      <br />
      <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr bordercolor="#F3F3F3">
          <td width="46%" height="" align="center" valign="middle" bgcolor="#215968" style="border-left: 1px solid #000000"><font face="Arial" size="2" color="#FFFFFF">Pack</font></td>
          <td width="10%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Subscription Period</font></td>
          <td width="13%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Payment (<span>${paymentCurrency}</span>)</font></td>
          <td width="16%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Date of Subscription</font></td>
          <td width="7%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Subscription end on</font></td>
          <td width="8%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Days</font></td>
        </tr>
        <tr bordercolor="#F3F3F3">
          <td style="border-left: 1px solid #000000" height="24" align="center" valign="middle" bgcolor="#558ED5"><font face="Arial" size="2" color="#FFFFFF"><span>${packName}</span></font></td>
          <td align="center" valign="middle" bgcolor="#558ED5"><font face="Arial" size="2" color="#FFFFFF"><span>${subscriptionPeriod}</span></font></td>
          <td align="center" valign="middle" bgcolor="#558ED5" sdval="215" sdnum="1033;"><font face="Arial" size="2" color="#FFFFFF"><span>${paymentAmount}</span></font></td>
          <td align="center" valign="middle" bgcolor="#558ED5" sdval="42156" sdnum="1033;16393;[$-4009]DD-MM-YYYY;@"><font face="Arial" size="2" color="#FFFFFF"><span>${subscriptionStartDateFormated}</span></font></td>
          <td align="center" valign="middle" bgcolor="#558ED5" sdval="42369" sdnum="1033;16393;[$-4009]DD-MM-YYYY;@"><font face="Arial" size="2" color="#FFFFFF"><span>${subscriptionEndDateFormated}</span></font></td>
          <td align="center" valign="middle" bgcolor="#558ED5" sdval="243" sdnum="1033;"><font face="Arial" size="2" color="#FFFFFF"><span>${subscriptionDays}</span></font></td>
        </tr>
        <tr>
          <td>Following Afya Services are included in the <b>${packName}</b> that you have opted for:</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><b><font color="#000000">Smart Solutions</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><font color="#000000"><span>${smartSolutions}</span></font></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><b><font color="#000000">Smart Services that you can render to your Patients</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><font color="#000000"><span>${smartServicesPatient}</span></font></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><b><font color="#000000">Smart Services that you can render to Other Care Providers</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><font color="#000000"><span>${smartServicesCareProviders}</span></font></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><b><font color="#000000">Notification Service</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
      </table>
      <br />
      <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr bordercolor="#F3F3F3">
          <td width="46%" height="" align="center" valign="middle" bgcolor="#215968" style="border-left: 1px solid #000000"><font face="Arial" size="2" color="#FFFFFF">Payment (<span>${paymentCurrency}</span>)</font></td>
        </tr>
        <tr bordercolor="#F3F3F3">
          <td style="border-left: 1px solid #000000" height="24" align="center" valign="middle" bgcolor="#558ED5"><font face="Arial" size="2" color="#FFFFFF">SMS Notification (Pack of <span>${notificationServicePacks}</span>)</font></td>
        </tr>

        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><b><font color="#000000">Training</font></b></td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><font color="#000000">Online Training on Afya Ecosystem is delivered on Learning Management Platform. <span>${notificationServicePacks}</span> Hours of Training is included in the pack <b>${packName}</b> that you have chosen.</font></td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><font color="#000000">If you wish to view the details of the training session you may refer afyaarabia.com</font></td>
        </tr>
        <tr style="display: none;">
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><font color="#000000">&lt;depending on the pack selected by the member,  training details will be printed below from pack wise training master. Sample data is given below. </font></td>
        </tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3" style="border-left: 1px solid #000000"><b><font size="4" color="#000000">Encl: Annexure to Quotation</font></b></td>
        </tr>
      </table>
      <br />
      <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr>
          <td height="18" colspan="6" align="center" bgcolor="#558ED5"><strong><font size="4" color="#000000">Annexure to Quotation</font></strong></td>
          </tr>
        <tr>
          <td width="11%" height="18">Membership ID</td>
          <td width="30%">${membershipId}</td>
          <td width="10%">Member Type</td>
          <td width="34%">${memberType}</td>
          <td width="7%">Date</td>
          <td width="8%">${dateFormated}</td>
        </tr>
        <tr>
          <td>Member Name</td>
          <td>${memberName}</td>
          <td>Referred By</td>
          <td>${referredBy}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000"><strong>1.Afya's policy for premium members</strong>&nbsp;&nbsp;<a href="web_pages\member_area\provider\Provider_account_policies_for_quotation.html" target="_blank">(link)</a><br />
          </font></span></td>
        </tr>
        <tr style="display: none;">
          <td colspan="6"><font color="#000000"><font color="#000000">&lt;should be coming from master&gt;</font></font></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">2. Afya's Policy for Smart Services</font></b><font color="#000000"><br />
          </font></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">For the Smart Services included in the selected pack Afya's policy will be printed</font><font color="#000000"><br />
          </font></span></td>
        </tr>
        <tr>
          <td colspan="6"><strong>3.Loyalty Terms</strong></td>
        </tr>
        <tr style="display: none;">
          <td colspan="6"><span>&nbsp;<!--should be coming from master--></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">4.Privacy &amp; Confidentiality Statement</font></b></span>&nbsp;&nbsp;<a href="web_pages\about_afya\privacy-confidentiality.html" target="_blank">(link)</a></td>
        </tr>
        <tr style="display: none;">
          <td colspan="6">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">5. Terms of Usage</font></b></span>&nbsp;&nbsp;<a href="web_pages\about_afya\terms-of-use.html" target="_blank">(link)</a></td>
        </tr>
        <tr style="display: none;">
          <td colspan="6">content of terms of usage as available on the portal</td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">6. Disclaimer</font></b></span></td>
        </tr>
        <tr>
          <td colspan="6">We appreciate and value your professional practice and wish to handle all your service needs. </td>
        </tr>
        <tr>
          <td colspan="6">This Quotation is not a contract or bill for services, but merely an estimate based on your specifications and/ or choice of services.</td>
        </tr>
        <tr>
          <td colspan="6">Actual fees may vary from the quote with or without change in specifications and/ or choice of services, and are subject to change, without any prior notice. </td>
        </tr>
        <tr>
          <td colspan="6">In case of any questions or clarifications, please contact our Sales team at info@afyaarabia.com , giving reference of the membership ID and date of quotation. Thank you.</td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">7.Payment Details</font></b></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">The payment for Afya Services can be made online.</font></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">8. Validity of Quotation</font></b></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">This quotation is valid for 24 hours</font></span></td>
        </tr>
        <tr>
          <td colspan="6">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="6">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
  </table>
</div>
<!--<script src="/afya-portal/static/js/jquery.js" type="text/javascript"></script>
<script src="/afya-portal/static/js/jquery_cookie.js" type="text/javascript"></script>
<script src="/afya-portal/static/js/angular.min.js"></script>
<script src="/afya-portal/static/js/angular-messages.min.js"></script>

<script src="/afya-portal/static/js/subscription_quotation.js"></script>
<script src="/afya-portal/static/js/jquery_cookie.js" type="text/javascript"></script>-->
</body>
</html>
