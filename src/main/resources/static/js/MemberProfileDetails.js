/**
 * Created by Mohan Sharma on 8/2/2015.
 */

 //updated by Shreedhar
var nameDiv = $("#nameDiv");
var userNameTxt = $("#userNameText");

var nameWelcome='';
var nameHeader='';


$(document).ready(function(){
 var username = readCookie("username");
 if(username!=null)
 {
    var role =readCookie("role");
    if(role=="ROLE_FACILITY_ADMIN")
    {
     $.getJSON('/afya-portal/anon/getProviderByUsername?username='+username, function(data){
           nameWelcome= "<h2>Welcome, "+(data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '')+"<h2>";
           nameHeader=(data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '');
        console.log(nameHeader + ' ' + Date());
        console.log(nameWelcome + ' ' + Date() );
         $(nameWelcome).appendTo(nameDiv);
                     $("#userNameText").text(nameHeader);
        });
    }
    else
    {
    $.getJSON('/afya-portal/anon/getPatientByUsername?username='+username, function(data){
         nameWelcome= "<h2>Welcome, "+(data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '')+"<h2>";
         nameHeader=(data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '');

        console.log(nameHeader + ' ' + Date());
        console.log(nameWelcome + ' ' + Date() );
         $(nameWelcome).appendTo(nameDiv);
                     $("#userNameText").text(nameHeader);
    });

    }

   // alert(nameHeader);
}
    })
//nameDiv.ready(function(){
//    $(nameWelcome).appendTo(nameDiv);
//    console.log(nameHeader + ' ' + Date());
//    console.log(nameWelcome + ' ' + Date() );
//});
//
//$("#userNameText").ready(function(){
////alert(nameHeader);
//console.log(nameHeader + ' ' + Date());
//console.log(nameWelcome + ' ' + Date() );
//                              $("#userNameText").text(nameHeader);
//
//
//
//
//});



function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function removeAllCookies(){

    var cookies = $.cookie();
    var result = false;
    for(var cookie in cookies){

        if($.removeCookie(cookie, { path: '/afya-portal/'})||$.removeCookie(cookie, { path: '/afya-portal'})){

            result = true;
        }
    }
    if(result)
        window.location.href = '/afya-portal';
}

function redirectClinic(){
    //var username = readCookie("username");
    $(location).attr('href','/afya-portal/anon/redirect_clinic');
}



//
//
//var nameDiv = $("#nameDiv");
//var userNameTxt = $("#userNameText");
//var headerUserNameDiv = $("#headerUserNameDiv");
//nameDiv.ready(function(){
//    var username = readCookie("username");
//    var role =readCookie("role");
//    if(role=="ROLE_FACILITY_ADMIN")
//    {
//     $.getJSON('/afya-portal/anon/getProviderByUsername?username='+username, function(data){
//            $("<h2>Welcome, "+(data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '')+"<h2>").appendTo(nameDiv);
//
//        });
//    }
//    else
//    {
//    $.getJSON('/afya-portal/anon/getPatientByUsername?username='+username, function(data){
//        $("<h2>Welcome, "+(data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '')+"<h2>").appendTo(nameDiv);
//
//    });
//    }
//});
//
//userNameTxt.ready(function(){
//if(readCookie("username")!= null)
//{
//    var username = readCookie("username");
//    var role =readCookie("role");
//        if(role=="ROLE_FACILITY_ADMIN")
//        {
//         $.getJSON('/afya-portal/anon/getProviderByUsername?username='+username, function(data){
//                 userNameTxt.text((data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || ''));
//                alert((data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || ''));
//                               $((data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || '')).appendTo(userNameTxt);
//            });
//        }
//        else
//        {
//        $.getJSON('/afya-portal/anon/getPatientByUsername?username='+username, function(data){
//               // userNameTxt.text((data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || ''));
//                alert($("#userNameText").text())
//               alert((data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || ''));
//                              $("#userNameText").text((data.salutation || '')+" "+(data.firstName || '') +" "+(data.lastName || ''));
//                              alert($("#userNameText").text())
//           });
//        }
//
//    }
//});
//
//
//
//function readCookie(name) {
//    var nameEQ = name + "=";
//    var ca = document.cookie.split(';');
//    for (var i = 0; i < ca.length; i++) {
//        var c = ca[i];
//        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
//        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
//    }
//    return null;
//}
//
//function removeAllCookies(){
//
//    var cookies = $.cookie();
//    var result = false;
//    for(var cookie in cookies){
//
//        if($.removeCookie(cookie, { path: '/afya-portal/'})||$.removeCookie(cookie, { path: '/afya-portal'})){
//
//            result = true;
//        }
//    }
//    if(result)
//        window.location.href = '/afya-portal';
//}
//
//function redirectClinic(){
//    $(location).attr('href','/afya-portal/redirect_direct');
//}


