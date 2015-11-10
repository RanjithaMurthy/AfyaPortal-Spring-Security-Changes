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
      <td align="left"><img src="${baseUrl}/static/images/logo.png" width="129" height="126" /></td>
    </tr>
    <tr>
      <td align="center" bgcolor="#26ABE2"><font size="5" color="#FFFFFF">Quotation</font></td>
    </tr>
    <tr>
      <td><table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr>
          <td width="11%" height="18"><b>Membership ID</b></td>
          <td width="30%">${membershipId}</td>
          <td width="10%"><b>Member Type</b></td>
          <td width="34%">${memberType}</td>
          <td width="7%"><b>Date</b></td>
          <td width="8%">${dateFormated}</td>
        </tr>
        <tr>
          <td><b>Member Name</b></td>
          <td>${memberName}</td>
          <td><b>Referred By</b></td>
          <td>${referredBy}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td colspan="6"><span><font color="#000000">We thank you for your interest in Afya Ecosystem and look forward for your continued participation in the journey of building a SMART CARE Community.<br />
            <!--<font color="#000000">Details of the pack you have chosen are as below</font>-->
            </font></span></td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
      </table>
      <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr bordercolor="#F3F3F3">
          <td width="46%" height="" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Pack</font></td>
          <td width="10%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Subscription Period</font></td>
          <td width="13%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Payment (<span>${paymentCurrency}</span>)</font></td>
          <td width="16%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Date of Subscription</font></td>
          <td width="7%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Subscription end on</font></td>
          <td width="8%" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Days</font></td>
        </tr>
        <tr bordercolor="#F3F3F3">
          <td height="24" align="center" valign="middle" bgcolor="#558ED5"><font face="Arial" size="2" color="#FFFFFF"><span>${packName}</span></font></td>
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
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td align="left" valign="bottom" style="border-bottom: 1px solid #F3F3F3"><b><font color="#000000">Smart Solutions</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom"><font color="#000000"><span>${smartSolutions}</span></font></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td align="left" valign="bottom" style="border-bottom: 1px solid #F3F3F3"><b><font color="#000000">Smart Services that you can render to your Patients</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom"><font color="#000000"><span>${smartServicesPatient}</span></font></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td align="left" valign="bottom" style="border-bottom: 1px solid #F3F3F3"><b><font color="#000000">Smart Services that you can render to Other Care Providers</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="bottom"><font color="#000000"><span>${smartServicesCareProviders}</span></font></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td align="left" valign="bottom" style="border-bottom: 1px solid #F3F3F3"><b><font color="#000000">Notification Service</font></b></td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
      </table>
      <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr bordercolor="#F3F3F3">
          <td width="46%" height="" align="center" valign="middle" bgcolor="#215968"><font face="Arial" size="2" color="#FFFFFF">Payment (<span>${paymentCurrency}</span>)</font></td>
        </tr>
        <tr bordercolor="#F3F3F3">
          <td height="24" align="center" valign="middle" bgcolor="#558ED5"><font face="Arial" size="2" color="#FFFFFF">SMS Notification (Pack of <span>${notificationServicePacks}</span>)</font></td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td align="left" valign="bottom"><b><font color="#000000">Self Learning</font></b></td>
        </tr>
        <tr>
          <td align="left" valign="bottom"><font color="#000000">Self Learning on Afya Ecosystem is delivered on Learning Management Platform. <span>${notificationServicePacks}</span> Hours of Training is included in the pack <b>${packName}</b> that you have chosen.</font></td>
        </tr>
        <tr>
          <td align="left" valign="bottom"><font color="#000000">Please communicate to community care to book your slots for free online training sessions.</font></td>
        </tr>
        <tr>
          <td align="left" valign="bottom">
          <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;" >
            <tr><th>Afya Smart Pack</th><th>Training Sessions</th><th>Training Duration (minutes)</th><th>Practice Duration</th><th>Assessment Duration</th></tr>
            <#list trainingSchedules as schedule>
              <tr><td>${(schedule.session)!}</td><td>${(schedule.trainingSessionDescription)!}</td><td style="text-align:center;">${(schedule.trainingDuration)!}</td><td style="text-align:center;">${(schedule.practiceDuration)!}</td><td style="text-align:center;">${(schedule.assessmentDuration)!}</td></tr>
            </#list>
            <tr><td colspan="2" style="margin-left:30px;">Total Time in Minutes</td><td style="text-align:center;">${totalTrainingDurationInMins}</td><td style="text-align:center;">${totalPracticeDurationInMins}</td><td style="text-align:center;">${totalAssessmentDurationInMins}</td></tr>
            <tr><td colspan="2" style="margin-left:30px;">Total Time in Hours</td><td style="text-align:center;">${totalTrainingDurationInHours}</td><td style="text-align:center;">${totalPracticeDurationInHours}</td><td style="text-align:center;">${totalAssessmentDurationInHours}</td></tr>
          </table>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td align="left" valign="bottom" bordercolor="#F3F3F3"><b><font size="4" color="#000000">Encl: Annexure to Quotation</font></b></td>
        </tr>
      </table>
      <table width="100%" border="1" cellpadding="2" cellspacing="2"  bordercolor="#CCCCCC" style=" padding:10px; border-collapse: collapse; font-family:Arial, Helvetica, sans-serif; font-size:12px;">
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td height="18" colspan="6" align="center" bgcolor="#558ED5"><strong><font size="4" color="#000000">Annexure to Quotation</font></strong></td>
          </tr>
        <tr>
          <td width="11%" height="18"><b>Membership ID</b></td>
          <td width="30%">${membershipId}</td>
          <td width="10%"><b>Member Type</b></td>
          <td width="34%">${memberType}</td>
          <td width="7%"><b>Date</b></td>
          <td width="8%">${dateFormated}</td>
        </tr>
        <tr>
          <td><b>Member Name</b></td>
          <td>${memberName}</td>
          <td><b>Referred By</b></td>
          <td>${referredBy}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000"><strong>1.Afya's policy for premium members</strong>&nbsp;&nbsp;
            <!--<a href="web_pages\member_area\provider\Provider_account_policies_for_quotation.html" target="_blank">(link)</a>-->
            <br /></font></span></td>
        </tr>

        <tr>
          <td colspan="1"><font color="#000000">1. Time Period</font></td>
          <td colspan="5">Trial period starts from the time member subscribes for Afya Services.<br>
            Scope of Afya Services during Trial Period - All Services included in the selected pack are available for effective use during Trial Period<br>
            Trial period will be valid for  30 days from date of subscription.<br>
            When the member decided to go for full subscription by paying the subscription fees then the Member gets the benefit of the pending Trail days being added to the subscription days.<br>
            Trial period is not applicable for any future subscription renewals or upgrades. This is an initial one-time facility provided to the member.<br>
          </td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">2. Subscription</font></td>
          <td colspan="5">
            A member can subscribe by making on-line payment through www.afyaarabia.com's payment gateway.<br>
            The subscription fees has to be paid in advance for either half yearly or an annual subscription - as per members choice.<br>
            A subscription once made, cannot be cancelled, transferred or exchanged with any manner.<br>
            Kindly accept the 'Subscription Agreement' as directed to on Afya's portal.<br>
          </td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">3. Additional Training Service</font></td>
          <td colspan="5">
            Upon subscribing, the member is requested  to ensure that the users undergo self learning sessions to get themselves acquainted and trained for the effective usage of Afya services.<br>
            Afya will provide a pack of FREE online training & support hours for enabling smooth activation of Afya Services.<br>
            The member can request for Additional Training Services through Afya.<br>
            Additional Training Services once paid cannot be cancelled , but can be rescheduled 48 Hrs prior to the appointment time.<br></td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">4. Notification Pack</font></td>
          <td colspan="5">
            A member needs to subscribe for the Notification Pack, if the member wants to send SMS notification, that are enabled by Afya's services, as a part of optimizing the members workflows.<br>
            Notification pack is not required if only emails are to be sent.<br>
            Afya provides the facility to its members to have a personalized touch to the SMS, that are enabled through Afya. For this, the member needs to state the Sender ID, while registering or it can be updated subsequently. However, till the member does not provide the Sender ID, the member still enjoys the privilege of sending SMS with the Sender ID as 'afyaarabia'.<br>
          </td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">5. Renewal</font></td>
          <td colspan="5">
            In order to ensure smooth operations and continued services of Afya, the member is adviced to renew their subscription any time before the active subscription expires.<br>
            Upon renewal, the member would enjoy the privileges & benefits as per Afya's policy prevailing at the time of renewal. Like-wise the members responsibility & the charges payable would be as applicable, as per the Afya's policy prevailing at the time of renewal.<br>
            The member has the option to redeem the loyalty points, if any, while renewing the subscription.<br>
            If the member has loyalty points and the member does not renew the services, then Afya will provide the member the option to request to convert the loyalty points thereby extending the validity of the services, by a proportionate number of days. However, if
            the member does not request such conversion and if the membership expires, then the loyalty points get forfeited.<br>
            It is advise to renew the Smart Solutions Service at least 30 days prior to the date of expiry, incase of non renewal of Smart Solutions Service then Smart Services facility would be stopped 7 days prior to the expiry of the subscription of SMART Solutions Service.<br>
            Once the subscription is expired due to non renewal, the Premium member automatically becomes a Community Member. This Community Member would be able to login into the portal MY Account &  download his DB backup only, within 15 days of expiry.<br>
            For any reason, if the member is unable to renew the subscription on time, but later decides to re-join Afya's community, then the member is most welcome back to Afya Community Care.
            In such event of a break and rejoining thereafter, the member can choose to be considered as a new member or  to request for the past data, while continuing the services.
            Afya will be able to provide the past data with a additional fees
            1) for only activating the masters or
            2) activating with all past records. Re-training, will be charged separately.
            The re-activation fees does not include free re-training. <br>
          </td>
        </tr>
        <!--<tr>
          <td colspan="6"><font color="#000000"><font color="#000000">&lt;should be coming from master&gt;</font></font></td>
        </tr>-->
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">2. Afya's Policy for Smart Services</font></b><font color="#000000"><br />
          </font></span></td>
        </tr>

        <tr>
          <td colspan="1"><font color="#000000">1. Appointment Premium - Doctors </font></td>
          <td colspan="5">
            Appointment Booking is possible only 7 days in advance.<br>
            Cancellation by Patient - Can be done up to 48 hrs prior to appointment time. Afya will refund the Advance less Cancellation Charges to the Patient in 10 days from cancellation.<br>
            Cancellation by Clinic/Doctor -  On behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor.<br>
            Rescheduling by Patient - Can be done untill 48 Hrs prior only. <br>
            Rescheduling by Clinic / Doctor - If the Patient agrees for the rescheduling proposed by the Clinic/Doctor, then rescheduling by the doctor is valid. Else, this would be considered as cancellation by Clinic/Doctor. Thus, on behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            No Show of Patient - The advance is non-refundable to the Patient. <br>
            No Show of Doctor (Not Rescheduled) - 100% advance is to be refunded to the Patient by the Clinic/Doctor. The Patient would be provided the refund by the Clinic. <br>
            Cancellation Fees from Patient : These are the Value Added Fees which is termed as Cancellation Fees when recovered from the patient on cancellation of the Smart Services. <br>
          </td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">2. Appointment Request  - Doctors</font></td>
          <td colspan="5">
            Appointment Booking is possible in advance. <br>
            Cancellation by Patient - Can be done up to 48 hrs prior to appointment time. Afya will refund the Advance less Cancellation Charges to the Patient in 10 days from cancellation. <br>
            Cancellation by Clinic/Doctor -  On behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            Rescheduling by Patient - Can be done untill 48 Hrs prior only. <br>
            Rescheduling by Clinic / Doctor - If the Patient agrees for the rescheduling proposed by the Clinic/Doctor, then rescheduling by the doctor is valid. Else, this would be considered as cancellation by Clinic/Doctor. Thus, on behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            No Show of Patient - The advance is non-refundable to the Patient. <br>
            No Show of Doctor (Not Rescheduled) - 100% advance is to be refunded to the Patient by the Clinic/Doctor. The Patient would be refund by the Clinic. <br>
            Cancellation Fees from Patient : These are the Value Added Fees which is termed as Cancellation Fees when recovered from the patient on cancellation of the Smart Services. <br></td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">3. Appointment - Home Visit Doctor</font></td>
          <td colspan="5">
            Appointment Booking for Home Visit of Doctor is possible only 7 in advance. <br>
            Cancellation by Patient - Can be done up to 48 hrs prior to appointment time. Afya will refund the Advance less Cancellation Charges to the Patient in 10 days from cancellation.<br>
            Cancellation by Clinic/Doctor -  On behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            Rescheduling by Patient - Can be done untill 48 Hrs prior only. <br>
            Rescheduling by Clinic / Doctor - If the Patient agrees for the rescheduling proposed by the Clinic/Doctor, then rescheduling by the doctor is valid. Else, this would be considered as cancellation by Clinic/Doctor. Thus, on behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            No Show of Patient - The advance is non-refundable to the Patient. <br>
            No Show of Doctor (not communicated to Patient) -  On behalf of the Clinic / Doctor, Afya will refund 100% advance to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            Cancellation Fees from Patient : These are the Value Added Fees which is termed as Cancellation Fees when recovered from the patient on cancellation of the Smart Services. <br></td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">4. Tele Consultation</font></td>
          <td colspan="5">
            Booking is possible for 7 days in advance. <br>
            Cancellation by Patient - Cancellation facility is not available for Tele Consultation. <br>
            Cancellation by Clinic/Doctor -  Cancellation facility is not available for Tele Consultation. <br>
            Rescheduling by Patient - Can be done until 24 Hrs prior only. <br>
            Rescheduling by Clinic / Doctor - If the Patient agrees for the rescheduling proposed by the Clinic/Doctor, then rescheduling by the doctor is valid. Else, this would be considered as cancellation by Clinic/Doctor.  Thus, on behalf of Clinic / Doctor, Afya will refund Full advance  to the Patient. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
            No Show of Patient - The advance is non-refundable. <br>
            No Show of Doctor (not communicated to Patient) - On written confirmation from the Clinic/Doctor. Full advance will be refunded to the patient by Afya. Value Added Fees for this transaction will be deducted / Charged to the Clinic/Doctor. <br>
          </td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">5. Home Pharmacy</font></td>
          <td colspan="5">
            Home Pharmacy can be rendered in multiple ways
            1. Refill Request is possible directly by the Patient
            2. Enabled by the  Doctor while prescribing.
            The Pharmacy is expected to accept the order only if the they can deliver all medicines in a given prescription. i.e partial orders cannot be accepted. <br>
            Cancellation by Patient - Cancellation facility is not available for this service.<br>
            Cancellation by Pharmacy -  Cancellation facility is not available for this service.<br>
          </td>
        </tr>
        <tr>
          <td colspan="1"><font color="#000000">6. Afya Payment Policy - Revenue Collected from all Smart Services</font></td>
          <td colspan="5">
            Payment received from Patients will be transferred to the Provider members bank account, on a weekly basis net of Afya's Value Added fees. <br>
            Value Add Fees are the charges payable to Afya for enabling the Smart Service. This would be visible to the member while they set their charges for Smart Services. This is not to be disclosed to the Patients.<br>
            Afya’s Smart Services are for Cash Paying Patients only.<br>
            Afya’s policies are applicable as per the local standard time.<br>
            Payment Status of Smart Services can be verified by the member by accessing "My Account" section in the afyaarabia portal.<br>
            In case of any clarification on the payment status, the member may send an email at info@afyaarabia.com<br></td>
        </tr>
        <!--
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">For the Smart Services included in the selected pack Afya's policy will be printed</font><font color="#000000"><br />
          </font></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">Service wise Afya policy is given in next sheet, this policy should come from Smart service master</font><font color="#000000"><br />
          </font></span></td>
        </tr>-->
        <tr><td colspan="6">&nbsp;</td></tr>
        <!--<tr>-->
          <!--<td colspan="6"><strong>3.Loyalty Terms</strong></td>-->
        <!--</tr>-->
        <!--<tr style="display: none;">-->
          <!--<td colspan="6"><span>&nbsp;&lt;!&ndash;should be coming from master&ndash;&gt;</span></td>-->
        <!--</tr>-->
        <!--<tr>-->
          <!--<td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">4.Privacy &amp; Confidentiality Statement</font></b></span>&nbsp;&nbsp;<a href="web_pages\about_afya\privacy-confidentiality.html" target="_blank">(link)</a></td>-->
        <!--</tr>-->
        <!--<tr style="display: none;">-->
          <!--<td colspan="6">&nbsp;</td>-->
        <!--</tr>-->
        <!--<tr>-->
          <!--<td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">5. Terms of Usage</font></b></span>&nbsp;&nbsp;<a href="web_pages\about_afya\terms-of-use.html" target="_blank">(link)</a></td>-->
        <!--</tr>-->
        <!--<tr style="display: none;">-->
          <!--<td colspan="6">content of terms of usage as available on the portal</td>-->
        <!--</tr>-->
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">3. Disclaimer</font></b></span></td>
        </tr>
        <tr>
          <td colspan="6">We appreciate and value your professional practice and welcome you to render and provide service by being part of Afya. </td>
        </tr>
        <tr>
          <td colspan="6">This Quotation is not a contract or bill for services, but merely an estimate based on your specifications and/ or choice of services.</td>
        </tr>
        <tr>
          <td colspan="6">Actual fees may vary from the quote with or without change in specifications and/ or choice of services, and are subject to change, without any prior notice. </td>
        </tr>
        <tr>
          <td colspan="6">In case of any questions or clarifications, please communicate with community care team by writing to <span style="color:red">info@afyaarabia.com</span>, giving reference of the membership ID and date of quotation. Thank you.</td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">4.Payment Details</font></b></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">The payment for Afya Services can be made online.</font></span></td>
        </tr>
        <tr><td colspan="6">&nbsp;</td></tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><b><font color="#000000">5. Validity of Quotation</font></b></span></td>
        </tr>
        <tr>
          <td colspan="6"><span style="border-left: 0px solid #000000"><font color="#000000">This quotation is valid for 24 hours</font></span></td>
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
