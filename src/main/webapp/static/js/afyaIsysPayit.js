if(window.afyaApp == undefined)
    window.afyaApp = {};

(function (app) {
    app.payit = {};

    // private request object
    var request = {
        paymentchannel: null,      // KNET: Kwknetonedc, VISA/MasterCard: Kwkfhcc
        isysid: null, 					// Dynamically generated unique id (also referred as the track-id and receipt number)
        amount: null, 				// Amount in KD (dynamic input)
        description: null, 			// Description 1 (dynamic input)
        description2: null, 		// Description 2 (dynamic input)
        tunnel: "isys", 				// Fixed value (provided by ISYS)
        currency: "414", 			// Fixed value (for KD)
        language: "EN", 				// Fixed value (for English)
        country: "kwt", 				// Fixed value (for Kuwait)
        merchant_name: "AFYAARABIA",  //"nthdimenzion", // "H3 Systems Co. W.L.L.", // Fixed value (provided by ISYS)
        akey: "VcYCDMw93gDGRRUC", 	// Fixed value (provided by ISYS)
        timestamp: "", 				// Blank as suggested by ISYS
        rnd: "", 					// Blank as suggested by ISYS
        original: 'oQLmOazWoZFPhdza', // Fixed value (provided by ISYS)
        pollStartTimestamp: null
    };

    // ENABLE OR BYPASS PAYMENT GATEWAY (SET TO 'true' FOR PAYMENT BYPASS, SET TO 'false' TO ENABLE PAYMENT (IN PRODUCTION), SET TO null TO AUTO DETERMINE BASED ON ENVIRONMENT)
    var bypassPG = null;       // Auto-Determine based on Environment
    // var bypassPG = true;    // FOR DEVELOPMENT AND TESTING (Bypassed - will NOT hit the Payment Gateway)
    // var bypassPG = false;   // FOR PRODUCTION (will RE-DIRECT to Payment Gateway)

    // auto-determine if bypass required based on environment
    if(bypassPG == null){
        // determine if this is a test/development environment
        if(window.location.hostname.indexOf("localhost") >=0
            || window.location.hostname.indexOf("5.9.249.196") >= 0
            || window.location.hostname.indexOf("5.9.249.197") >= 0){
            bypassPG = true;    // Bypass Payments in Development and Test environments
        }else{
            bypassPG = false;   // Enable Payments in all other Environments (including Production)
        }
    }

    var channelConstant = {
        "KNET" : "Kwknetonedc",
        "VISAMASTER": "Kwkfhcc",
    };

    // settings
    var settings = {
        statusPollIntervalInSeconds: 5,
        timeoutInMillisecs: 10 * 60 * 1000  // TO DO : ENABLE THIS FOR PRODUCTION (5 Minutes timeout)
        //timeoutInMillisecs: 0.5 * 60 * 1000	//TEMP - FOR TESTING (30 Seconds timeout)
    };

    // public method to initiate payment
    app.payit.initiatePayment = function (options) {	// options -> {afyaId: <afya id>, clinicId: <clinic id>, doctorId: <doctor id>, transactionType: <APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT>, apptSlot: <appointment slot date-time>, paymentAmount: <amount>, paymentChannel: <KNET/VISAMASTER>, description: <description>, description2: <description>, errorCB: <error Callback>, pollCallback: <callback to capture Payment Gateway Polled status>, baseUrlForService: <baseUrlForService> }

        if(bypassPG == true)
            writeToConsole("Payment Gateway by-passed");

        // default value for options.baseUrlForService
        if(options.baseUrlForService == undefined || options.baseUrlForService == null)
            options.baseUrlForService = 'http://5.9.249.196:7879/afya-portal/anon/';
            // options.baseUrlForService = 'http://localhost:7878/afya-portal/anon/';

        // prepare baseUrlForPortal
        if(options.baseUrlForPortal == undefined)
            options.baseUrlForPortal = options.baseUrlForService.substring(0, (options.baseUrlForService.indexOf('anon')));
        if(options.baseUrlForPortal.indexOf('http') != 0)
            options.baseUrlForPortal = window.location.origin + options.baseUrlForPortal;

        // check if a request is already under process
        if (request.isysid != null) {
            // a request is under processing
            options.errorCB('Request is under process with Track-Id: ' + request.isysid);
        } else {
            initiatePaymentInternal(options);
        }

        // generate URL and send the request
        // log the request in the server
        // initiate polling for response
    }

    // private method to initiate payment
    function initiatePaymentInternal(options) {	// options -> {afyaId: <afya id>, clinicId: <clinic id>, doctorId: <doctor id>, transactionType: <APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT>, apptSlot: <appointment slot date-time (string)>, paymentAmount: <amount>, paymentChannel: <KNET/VISAMASTER>, description: <description>, description2: <description>, errorCB: <error Callback>, pollCallback: <callback to capture Payment Gateway Polled status>, baseUrlForService: <baseUrlForService> }
        // generate isysid

        // invokePollcallback(options.pollCallback, 'Approved', 'testing-trackid', getTimestamp());
        // return;

        // validate payment channel
        if(options.paymentChannel != 'VISAMASTER' && options.paymentChannel != 'KNET')
            options.errorCB('Unknown paymentChannel ' + options.paymentChannel);

        // validate payer type
        if(options.payerType != 'CONSUMER' && options.payerType != 'MERCHANT')
            options.errorCB('Unknown payerType ' + options.payerType);

        // Assign Payment Channel to the request
        // if(options.paymentChannel == undefined)
        //    options.paymentChannel = 'VISAMASTER';
        request.paymentchannel = channelConstant[options.paymentChannel];

        // generate isysid (from current date-time)
        var dt = new Date();
        request.isysid = dt.getFullYear().toString() + (dt.getMonth() + 1).toString() + (dt.getDate()).toString() + (dt.getHours()).toString() + (dt.getMinutes()).toString() + (dt.getSeconds()).toString() + (dt.getMilliseconds()).toString();

        writeToConsole(request.isysid);

        // update the 'request' object from data in 'options' input object 
        request.amount = options.paymentAmount;
        request.description = options.description || '';
        request.description2 = options.description2 || '';

        // prepare input data to compute Hash 
        var dataToComputeHash = request.paymentchannel + "paymentchannel" + request.isysid + "isysid" + request.amount + "amount"
                + request.timestamp + "timestamp" + request.description + "description" + request.rnd + "rnd" + request.original + "original";
        writeToConsole('dataToComputeHash: ' + dataToComputeHash);

        // initiate computation of the Hash
        writeToConsole('invoking computeHash');
        computeHash(request.original, dataToComputeHash
			, function (hash) {	// computeHash Success Callback
			    // -- got the Hash successfully --
			    // base path
			    var basePath = "https://Pay-it.mobi/globalpayit/pciglobal/WebForms/Payitcheckoutservice%20.aspx";
			    // construct response URL
			    var responseUrl = options.baseUrlForPortal + "merchant_page.html";
			    // constructed gateway url
			    var gatewayUrl = basePath +
					"?paymentchannel=" + request.paymentchannel + "&isysid=" + request.isysid + "&amount=" + request.amount + "&description=" + request.description + "&description2="
					+ request.description2 + "&tunnel=" + request.tunnel + "&currency=" + request.currency + "&language=" + request.language + "&country=" + request.country + "&merchant_name="
					+ request.merchant_name + "&akey=" + request.akey + "&original=" + encodeURI(request.original) + "&responseurl=" + responseUrl
					+ "&hash=" + hash;
			    writeToConsole('gatewayUrl: ' + gatewayUrl);
			    //alert(gatewayUrl);
			    //alert('opening in new window');

			    // open url in system browser
			    // window.open(gatewayUrl, '_system');

			    // default values for parameters not-used for this transaction
			    //options.pharmacyOrderId = options.pharmacyOrderId || '';
			    //options.apptDoctorId = options.apptDoctorId || '';
			    //options.apptSlot = options.apptSlot || '';

			    // call service to record the request
			    var timestamp = getTimestamp();
			    app.svc.addPaymentTransactionForAppointment({ transactionType: options.transactionType, transactionAmount: options.paymentAmount
					, transactionTimestamp: timestamp, isysTrackingRef: request.isysid, afyaId: options.afyaId, apptClinicId: options.clinicId
					, apptDoctorId: options.doctorId, apptSlot: options.apptSlot, pharmacyOrderId: options.pharmacyOrderId
                    , packageId: options.packageId, username: options.username, processingFees: options.processingFees
                    , paymentChannel: options.paymentChannel, payerType: options.payerType
					, success: function (data) {	// success Callback for addPaymentTransactionForAppointment
					    var paymentId = data.paymentId;
					    writeToConsole('paymentId from addPaymentTransactionForAppointment: ' + paymentId);

					    if(bypassPG == true){   // Payment bypassed
                            // start monitor response
                            var windowRef = {close: function(){}}; // dummy object
                            startToPollPaymentStatus(paymentId, request.akey, request.merchant_name, request.isysid, windowRef, options.pollCallback, options.baseUrlForService);
                            return;
					    }

                        // open in inappbrowser
                        var windowRef = window.open(gatewayUrl, '_blank', 'location=yes');
                        writeToConsole('windowRef : ' + windowRef);

                        // check if Popup window is opened successfully
                        if (windowRef == undefined) {
                            options.errorCB('Unable to open Popup. Please enable Popup and try again');
                            request.isysid = null;
                            return;
                        }
					    // start monitor response
					    startToPollPaymentStatus(paymentId, request.akey, request.merchant_name, request.isysid, windowRef, options.pollCallback, options.baseUrlForService);
					} 
			    });
			},
			function (errorMessage) {		// Error Callback
			    // -- error computing the Hash --
			    // reset isysid to null, to enable another payment
			    request.isysid = null;
			    // inform the error back to the caller
			    options.errorCB(errorMessage);
			},
            options.baseUrlForService);
    }

    // private method to Compute Hash with native code throug the payitHelper plugin 
    function computeHash(original, dataToHash, hashCallback, errorCallback, baseUrlForService) {

        /*// -- TESTING -- <
        hashCallback('b3212e6854f3c2c96be4cebc82653536e786bd25fb674f61be1097bfa5822d2c');
        return;
        // -- TESTING -- >*/

        var successCB = function (hash) { writeToConsole('computeHash Success - hash : ' + hash); hashCallback(hash); };
        var errorCB = function (message) { writeToConsole('Error - message :' + message); errorCallback(message); };

        //alert('calling computeHash plugin..');
        writeToConsole('calling computeHash plugin..');
        // payitHelperPlugin.computeHash(successCB, errorCB, 'oQLmOazWoZFPhdza', 'Kwkfhccpaymentchannel201507201232284304isysid1amounttimestampsomethingdescriptionrndoQLmOazWoZFPhdzaoriginal');
        payitHelperPlugin.computeHash(successCB, errorCB, original, dataToHash, baseUrlForService);
        //alert('finished calling computeHash plugin, waiting for callback response');
    }

    function startToPollPaymentStatus(paymentId, akey, merchantName, trackId, windowRef, pollCallback, baseUrlForService) {
        // current formated timestamp
        var timestamp = getTimestamp();
        invokePollcallback(pollCallback, paymentId, 'Pending', trackId, timestamp);
        request.pollStartTimestamp = new Date();
        request.isFirstPoll = true;
        pollPaymentStatus(paymentId, akey, merchantName, trackId, windowRef, pollCallback, baseUrlForService);
    }

    // private function to poll for payment status
    function pollPaymentStatus(paymentId, akey, merchantName, trackId, windowRef, pollCallback, baseUrlForService) {
        writeToConsole("in pollPaymentStatus (" + getTimestamp() + ")");
        setTimeout(function () {	// timeout callback
            fetchPayitStatus(akey, merchantName, trackId, windowRef, function (status, statusText, responseText) {	// callback for fetch ISYS Payit status
                writeToConsole("pollPaymentStatus - response from Gateway - " + responseText);

                // current formated timestamp
                var timestamp = getTimestamp();
                // isys Payment Status
                var isysPaymentStatus = '';

                // check response status is 200-OK
                if(status != 200){
                    reportHttpError(status);
                    return;
                }

                /*
                // determine the payment status
                if (responseText.indexOf('PaymentStatus=Declined') >= 0 || responseText.indexOf('PaymentStatus=declined') >= 0)
                    isysPaymentStatus = 'Declined';
                else if (responseText.indexOf('PaymentStatus=Captured') >= 0 || responseText.indexOf('PaymentStatus=captured') >= 0)
                    isysPaymentStatus = 'Captured';
                else if (responseText.indexOf('PaymentStatus=Approved') >= 0 || responseText.indexOf('PaymentStatus=approved') >= 0)
                    isysPaymentStatus = 'Approved';
                else if (responseText.indexOf('PaymentStatus=Pending') >= 0 || responseText.indexOf('PaymentStatus=pending') >= 0)
                    isysPaymentStatus = 'Pending';
                */

                // retrieve isysPaymentStatus
                var statusIdx = responseText.indexOf('PaymentStatus=');
                var statusKeyLength = 'PaymentStatus='.length;
                var statusEndIdx = responseText.indexOf('||', statusIdx + statusKeyLength);
                isysPaymentStatus = responseText.substring(statusIdx + statusKeyLength, statusEndIdx);
                // isysPaymentStatus = isysPaymentStatus.toLowerCase();

                // get the Merchant Transaction Ref
                var merchantTransRefKey = "||MerchantTransRef=";
                var idxMerchantTrans = responseText.indexOf(merchantTransRefKey);
                var merchantTransRef = "";
                if(idxMerchantTrans >= 0){
                    var startIdx = idxMerchantTrans + merchantTransRefKey.length;
                    merchantTransRef = responseText.substring(startIdx, responseText.indexOf('||', startIdx));
                }


                /*if(responseText.indexOf('PaymentStatus=Declined') >=0 
                || responseText.indexOf('PaymentStatus=Captured') >=0 
                || responseText.indexOf('PaymentStatus=Approved') >=0){*/
                if (/*isysPaymentStatus.length > 0 && */isysPaymentStatus.toLowerCase() != 'pending') {
                    // -- Payment is either 'Sucessful' or 'Declined' --
                    // callback for update success
                    function onUpdateSuccess1() {
                        // close inappbrowser - transaction is complete
                        windowRef.close();
                        // reset isysid (ready for next payment)
                        request.isysid = null;
                        // invoke poll callback
                        invokePollcallback(pollCallback, paymentId, isysPaymentStatus, trackId, timestamp);
                    }
                    // callback for update error
                    function onUpdateError1(status){
                        reportHttpError(status);
                    }
                    // updated Payment Transaction Status
                    app.svc.updatePaymentTransactionWithIsysStatus({ "paymentId": paymentId, isysTrackingRef: request.isysid, 'isysPaymentStatus': isysPaymentStatus, isysPaymentStatusTimestamp: timestamp, isysHttpResponseStatus: statusText, isysMerchantRef: merchantTransRef, success: onUpdateSuccess1, error: onUpdateError1 });
                } else if (((new Date()) - request.pollStartTimestamp) > settings.timeoutInMillisecs) {
                    // -- Payment Processing TIMED-OUT --
                    // callback for update success
                    function onUpdateSuccess2() {
                        // close the transaction, timed-out (it is taking too-much time)
                        windowRef.close();
                        // reset isysid (ready for next payment)
                        request.isysid = null;
                        // invoke poll callback
                        invokePollcallback(pollCallback, paymentId, isysPaymentStatus, trackId, timestamp, true); // TIMED OUT
                    }
                    // callback for update error
                    function onUpdateError2(status){
                        reportHttpError(status);
                    }
                    // update Payment Transaction Status
                    app.svc.updatePaymentTransactionWithIsysStatus({ "paymentId": paymentId, isysTrackingRef: request.isysid, 'isysPaymentStatus': isysPaymentStatus, isysPaymentStatusTimestamp: timestamp, isysHttpResponseStatus: statusText, isysMerchantRef: merchantTransRef, success: onUpdateSuccess2, error: onUpdateError2  });
                } else {
                    // perform Pending status stuff
                    function doProcess(){
                        request.isFirstPoll = false;
                        // invoke poll callback
                        invokePollcallback(pollCallback, paymentId, isysPaymentStatus, trackId, timestamp);
                        // monitor again
                        pollPaymentStatus(paymentId, akey, merchantName, trackId, windowRef, pollCallback, baseUrlForService);
                    }
                    // -- Payment status is 'Pending'
                    if(request.isFirstPoll == true){
                        app.svc.updatePaymentTransactionWithIsysStatus({ "paymentId": paymentId, isysTrackingRef: request.isysid, 'isysPaymentStatus': isysPaymentStatus, isysPaymentStatusTimestamp: timestamp, isysHttpResponseStatus: statusText, isysMerchantRef: merchantTransRef, success: function(){doProcess();}, error: function(status){reportHttpError(status);}  });
                    }else{
                        doProcess();
                    }
                }

                // common error handling routine  NOTE: before calling ensure that 'isysPaymentStatus' and 'timestamp' are initialized
                function reportHttpError(status){
                        // close inappbrowser - transaction is complete
                        windowRef.close();
                        // reset isysid (ready for next payment)
                        request.isysid = null;
                        // invoke poll callback
                        invokePollcallback(pollCallback, paymentId, isysPaymentStatus, trackId, timestamp, false, status == 0 ? 'ERROR_CONNECTIVITY' : 'ERROR_UNKNOWN'); // Error
                }
            }, baseUrlForService);
        }, (settings.statusPollIntervalInSeconds * 1000));
    }

    // fetch Payit payment status
    function fetchPayitStatus(akey, merchantName, trackId, windowRef, gotResponseCB, baseUrlForService) {

        if(bypassPG == true){   // Payment bypassed
            gotResponseCB(200, "OK", "PaymentStatus=dummyCaptured||MerchantTransRef=||");
            return;
        }

        var pollUrl = "https://pay-it.mobi/globalpayit/pciglobal/WebForms/Status.aspx?merchantname=" + merchantName + "&authkey=" + akey + "&trackid=" + trackId;

        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4) {
                if (xmlhttp.status == 200) {
                    // windowRef.close(); // FOR TESTING
                    var dt = new Date();
                    var updateTime = dt.getFullYear() + " " + (dt.getMonth() + 1) + " " + (dt.getDate()) + " " + (dt.getHours()) + " " + (dt.getMinutes()) + " " + (dt.getSeconds());
                    // document.getElementById("txtResponse").innerHTML= updateTime + "		" + xmlhttp.responseText;
                    gotResponseCB(xmlhttp.status, xmlhttp.statusText, xmlhttp.responseText);
                } else {
                    // document.getElementById("txtResponse").innerHTML="Error (Status: " + xmlhttp.status + ")";
                    gotResponseCB(xmlhttp.status, xmlhttp.statusText, xmlhttp.responseText);
                }
            }
        };
        // construct PollUrl to be invoked through Wrapper Service
        if(baseUrlForService != undefined && baseUrlForService != null)
            pollUrl = baseUrlForService + 'getResponseFromUrl?requestUrl=' + encodeURIComponent(pollUrl);
        // invoke service
        xmlhttp.open("GET", pollUrl, true);
        xmlhttp.send();
    }

    // Helper to invoke Pollcallback
    function invokePollcallback(pollCallback, paymentId, isysPaymentStatus, trackId, timestamp, isTimedOut, errorCode) {
        if (pollCallback != undefined) {
            // user friendly status
            var status;
            if(errorCode != undefined && errorCode != null){
                switch(errorCode){
                    case 'ERROR_CONNECTIVITY':
                        status = 'ERROR_CONNECTIVITY';
                    break;
                    case 'ERROR_UNKNOWN':
                    default:
                        status = 'ERROR_UNKNOWN';
                    break;
                }
            } else if (isTimedOut == true) {
                status = 'Timed Out';
                // status = 'Success';	// FOR TESTING - TO DO: REMOVE THIS IN PRODUCTION
            } else if (bypassPG == true && isysPaymentStatus.toLowerCase() == "dummycaptured"){
                // PAYMENT BYPASSED
                status = 'Success';
            } else {
                switch (isysPaymentStatus.toLowerCase()) {
                    //case 'Captured':
                    //case 'Approved':
                    case 'captured':
                    case 'approved':
                        status = 'Success';
                        break;
                    //case 'Declined':
                    case 'declined':
                        status = 'Declined';
                        break;
                    //case 'Pending':
                    case 'pending':
                        status = 'Waiting';
                        break;
                    //case 'Unspecified Failure':
                    case 'unspecified failure':
                        status = 'Unspecified Failure';
                        break;
                    default:
                        status = isysPaymentStatus;
                        // status = 'ERROR_UNKNOWN_PAYMENT_STATUS';
                }
            }
            // invoke
            pollCallback({ 'status': status, 'paymentId': paymentId, isysStatus: isysPaymentStatus, 'timestamp': timestamp, 'trackId': trackId });
        }
    }

    // Helper function to construct Timestamp string in the standard format (yyyy-MM-dd HH:mm:ss)
    function getTimestamp() {
        var dt = new Date();
        var year = dt.getFullYear();
        var month = (dt.getMonth() + 1);
        if (month < 10)
            month = '0' + month;
        var date = dt.getDate();
        if (date < 10)
            date = '0' + date;
        var hours = dt.getHours();
        if (hours < 10)
            hours = '0' + hours;
        var minutes = dt.getMinutes();
        if (minutes < 10)
            minutes = '0' + minutes;
        var seconds = dt.getSeconds();
        if (seconds < 10)
            seconds = '0' + seconds;

        var timestamp = year + '-' + month + '-' + date + ' ' + hours + ':' + minutes + ':' + seconds;
        return timestamp;
    }

    function writeToConsole(message) {
        console.log(message);
        // alert(message);
    }

})(afyaApp);
