var payitHelperPlugin = {
	computeHash : function(successCallback, errorCallback, original, dataToHash, baseUrlForService){
		var isMobile = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry|IEMobile)/);
		
		// alert("isMobile: " + isMobile);
		
		if(isMobile)
			computeHashMobile();
		else
			computeHashWeb();
		
		function computeHashWeb(){
			/*if(baseUrlForService == undefined || baseUrlForService == null)
				baseUrlForService = "http://5.9.249.196:7879/afya-portal/anon/";*/

			var url = baseUrlForService + "computeHashForPaymentGateway?original=" + original + "&dataToComputeHash=" + dataToHash;
			
			var xmlhttp=new XMLHttpRequest();
			xmlhttp.onreadystatechange=function(){
				if (xmlhttp.readyState == 4){
					if(xmlhttp.status==200){
						var hash = xmlhttp.responseText.substring(1,xmlhttp.responseText.length - 1);
						successCallback(hash);
					}else{
						var errMsg = "Error in getting Hash from service: " + url;
					    // alert(errMsg);
						errorCallback(errMsg);
					}
				}
			};
			xmlhttp.open("GET",url,true);
			xmlhttp.setRequestHeader("Accept", "application/json");
			xmlhttp.send();
		};
		
		function computeHashMobile(){
			var isMobile = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry|IEMobile)/);
			// alert('calling cordova.exec');
			cordova.exec(
				successCallback, // success callback function
				errorCallback, // error callback function
				'PayitHelperPlugin', // mapped to our native Java class called "PayitHelperPlugin"
				'computeHash', // with this action name
				[{
					'original' : original,
					'dataToHash' : dataToHash,
				}]
			);
		}
	}
}