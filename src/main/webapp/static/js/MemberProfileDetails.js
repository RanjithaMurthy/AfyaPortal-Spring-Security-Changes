
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
    //remove all session store variables
     afyaSessionStore().removeAll();
    if(result)
        window.location.href = '/afya-portal';
}

//Removes all cookies : Called from static pages where authentication is not required

function removeAllCookiesStaticPages(){

    var cookies = $.cookie();
    var result = false;
    for(var cookie in cookies){

        if($.removeCookie(cookie, { path: '/afya-portal/'})||$.removeCookie(cookie, { path: '/afya-portal'})){

            result = true;
        }
    }
    //remove all session store variables
     afyaSessionStore().removeAll();
    //Re-paint the header and footer
    initializePage();
}

function redirectClinic(){
    var pvdrData = getProviderByUsername();// get Provider obj Details to Filter for Community/Visitors
    //alert(pvdrData.memberType);// VISITOR | PREMIUM
    if(pvdrData.memberType == "VISITOR"){
        $('#mySmallModalMyFacilitySubscribe').modal('show');// show coming soon modal
        /*sweetAlert({
          title: "",
          text: "Subscribe immediately Afya Services & enjoy the power of being part of the Community",
          imageUrl: '/afya-portal/static/images/logo.png'
        });*/
    }
    else
        //var username = readCookie("username");
        $(location).attr('href','/afya-portal/redirect_direct');


}




