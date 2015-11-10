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
        akey: "kQV9ST8bpb0vDoIc", 	// Fixed value (provided by ISYS)
        timestamp: "", 				// Blank as suggested by ISYS
        rnd: "", 					// Blank as suggested by ISYS
        original: 'oQLmOazWoZFPhdza', // Fixed value (provided by ISYS)
        pollStartTimestamp: null
    };

    var channelConstant = {
        "KNET" : "Kwknetonedc",
        "VISAMASTER": "Kwkfhcc",
    };

    // settings
    var settings = {
        statusPollIntervalInSeconds: 5,
        //timeoutInMillisecs: 10 * 60 * 1000  // TO DO : ENABLE THIS FOR PRODUCTION (5 Minutes timeout)
        timeoutInMillisecs: 0.5 * 60 * 1000	//TEMP - FOR TESTING (30 Seconds timeout)
    };

    // public method to initiate payment
    app.payit.initiatePayment = function (options) {	// options -> {afyaId: <afya id>, clinicId: <clinic id>, doctorId: <doctor id>, transactionType: <APPOINTMENT_REQUEST, APPOINTMENT_BOOKING, TELECONSULTATION_BOOKING, HOME_VISIT>, apptSlot: <appointment slot date-time>, paymentAmount: <amount>, paymentChannel: <KNET/VISAMASTER>, description: <description>, description2: <description>, errorCB: <error Callback>, pollCallback: <callback to capture Payment Gateway Polled status>, baseUrlForService: <baseUrlForService> }
        // default value for options.baseUrlForService
        if(options.baseUrlForService == undefined || options.baseUrlForService == null)
            options.baseUrlForService = 'http://5.9.249.196:7879/afya-portal/anon/';
            // options.baseUrlForService = 'http://localhost:7878/afya-portal/anon/';

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
        if(options.paymentChannel == undefined)
            options.paymentChannel = 'VISAMASTER';
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
			    // constructed gateway url
			    var gatewayUrl = basePath +
					"?paymentchannel=" + request.paymentchannel + "&isysid=" + request.isysid + "&amount=" + request.amount + "&description=" + request.description + "&description2="
					+ request.description2 + "&tunnel=" + request.tunnel + "&currency=" + request.currency + "&language=" + request.language + "&country=" + request.country + "&merchant_name="
					+ request.merchant_name + "&akey=" + request.akey + "&original=" + encodeURI(request.original) + "&responseurl="
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
        invokePollcallback(pollCallback, 'Pending', trackId, timestamp);
        request.pollStartTimestamp = new Date();
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
                // check for transaction complete resonse
                var isysPaymentStatus = '';
                if (responseText.indexOf('PaymentStatus=Declined') >= 0)
                    isysPaymentStatus = 'Declined';
                else if (responseText.indexOf('PaymentStatus=Captured') >= 0)
                    isysPaymentStatus = 'Captured';
                else if (responseText.indexOf('PaymentStatus=Approved') >= 0)
                    isysPaymentStatus = 'Approved';
                else if (responseText.indexOf('PaymentStatus=pending') >= 0)
                    isysPaymentStatus = 'Pending';

                /*if(responseText.indexOf('PaymentStatus=Declined') >=0 
                || responseText.indexOf('PaymentStatus=Captured') >=0 
                || responseText.indexOf('PaymentStatus=Approved') >=0){*/
                if (isysPaymentStatus.length > 0 && isysPaymentStatus != 'Pending') {
                    // -- Payment is either 'Sucessful' or 'Declined' --
                    // callback for update success
                    function onUpdateSuccess1() {
                        // close inappbrowser - transaction is complete
                        windowRef.close();
                        // reset isysid (ready for next payment)
                        request.isysid = null;
                        // invoke poll callback
                        invokePollcallback(pollCallback, isysPaymentStatus, trackId, timestamp);
                    }
                    // updated Payment Transaction Status
                    app.svc.updatePaymentTransactionWithIsysStatus({ "paymentId": paymentId, isysTrackingRef: request.isysid, 'isysPaymentStatus': isysPaymentStatus, isysPaymentStatusTimestamp: timestamp, isysHttpResponseStatus: statusText, success: onUpdateSuccess1 });
                } else if (((new Date()) - request.pollStartTimestamp) > settings.timeoutInMillisecs) {
                    // -- Payment Processing TIMED-OUT --
                    // callback for update success
                    function onUpdateSuccess2() {
                        // close the transaction, timed-out (it is taking too-much time)
                        windowRef.close();
                        // reset isysid (ready for next payment)
                        request.isysid = null;
                        // invoke poll callback
                        invokePollcallback(pollCallback, isysPaymentStatus, trackId, timestamp, true); // TIMED OUT
                    }
                    // update Payment Transaction Status
                    app.svc.updatePaymentTransactionWithIsysStatus({ "paymentId": paymentId, isysTrackingRef: request.isysid, 'isysPaymentStatus': isysPaymentStatus, isysPaymentStatusTimestamp: timestamp, isysHttpResponseStatus: statusText, success: onUpdateSuccess2 });
                } else {
                    // -- Payment status is 'Pending'
                    // invoke poll callback
                    invokePollcallback(pollCallback, isysPaymentStatus, trackId, timestamp);
                    // monitor again
                    pollPaymentStatus(paymentId, akey, merchantName, trackId, windowRef, pollCallback, baseUrlForService);
                }
            }, baseUrlForService);
        }, (settings.statusPollIntervalInSeconds * 1000));
    }

    // fetch Payit payment status
    function fetchPayitStatus(akey, merchantName, trackId, windowRef, gotResponseCB, baseUrlForService) {

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
    function invokePollcallback(pollCallback, isysPaymentStatus, trackId, timestamp, isTimedOut) {
        if (pollCallback != undefined) {
            // user friendly status
            var status;
            if (isTimedOut == true) {
                status = 'Timed Out';
                status = 'Success';	// FOR TESTING - TO DO: REMOVE THIS IN PRODUCTION
            } else {
                switch (isysPaymentStatus) {
                    case 'Captured':
                    case 'Approved':
                        status = 'Success';
                        break;
                    case 'Declined':
                        status = 'Declined';
                        break;
                    case 'Pending':
                        status = 'Waiting';
                        break;
                    default:
                        status = 'Waiting'; 	// TO DO : CHECK IF THIS IS FINE
                        // throw 'Unknown Isys Status ' + isysPaymentStatus;
                }
            }
            // invoke
            pollCallback({ 'status': status, isysStatus: isysPaymentStatus, 'timestamp': timestamp, 'trackId': trackId });
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
