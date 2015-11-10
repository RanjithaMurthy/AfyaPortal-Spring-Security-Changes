jQuery(function($) {'use strict',

	//#main-slider
	$(function(){
		$('#main-slider.carousel').carousel({
			interval: 8000
		});
	});

// accordian
	$('.accordion-toggle').on('click', function(){
		$(this).closest('.panel-group').children().each(function(){
		$(this).find('>.panel-heading').removeClass('active');
		 });

	 	$(this).closest('.panel-heading').toggleClass('active');
	});


	//Initiat WOW JS
	new WOW().init();

		
	//goto top
	$('.gototop').click(function(event) {
		event.preventDefault();
		$('html, body').animate({
			scrollTop: $("body").offset().top
		}, 500);
	});	

        $(".patient-li").click(function(){
            $(".nav-tabs").css("border-bottom",'0px');
            $(".nav-tabs > li").css("border-bottom","3px solid #3C9");
        });
        $(".care-provider-li").click(function(){
            $(".nav-tabs").css("border-bottom",'0px');
            $(".nav-tabs > li").css("border-bottom","3px solid #0C9B8C");
        });
        $(".care-payer-li").click(function(){
            $(".nav-tabs").css("border-bottom", '0px');
            $(".nav-tabs > li").css("border-bottom","3px solid #60CDCB ");
        });
});