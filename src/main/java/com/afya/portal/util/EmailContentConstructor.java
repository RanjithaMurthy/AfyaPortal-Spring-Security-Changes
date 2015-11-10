package com.afya.portal.util;

import java.util.Map;

/**
 * Created by Mohan Sharma on 5/29/2015.
 */
public class EmailContentConstructor {
    public static String setBodyForContactUsMail(Map<String, Object> details) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<div>");
        buffer.append("<p>Hi,<br/><br/>" + details.get("content") + "</p>");
        buffer.append("</div>");
        buffer.append("<div>");
        buffer.append("<p><br/>With Regards,<br/>"+details.get("fullName")+"<br/>");
        if(UtilValidator.isNotEmpty(details.get("emailId")))
            buffer.append("Email-Id : "+details.get("emailId")+"<br/>");
        if(UtilValidator.isNotEmpty(details.get("contactNumber")))
            buffer.append("Mobile No : "+details.get("contactNumber"));
        return buffer.toString();
    }

    public static String setSubjectForContactUsMail(Map<String, Object> details) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Purpose : ");
        if(UtilValidator.isNotEmpty(details.get("purpose")))
            buffer.append(details.get("purpose"));
        else
            buffer.append("Feedback");
        return buffer.toString();
    }
}
