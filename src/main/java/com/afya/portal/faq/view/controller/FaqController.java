package com.afya.portal.faq.view.controller;

import com.afya.portal.faq.view.dto.FaqDto;
import com.afya.portal.faq.view.query.FaqFinder;
import com.afya.portal.membercareprovider.application.NetworkCreateCommand;
import com.afya.portal.membercareprovider.domain.Network;
import com.afya.portal.membercareprovider.view.dto.NetworkDto;
import com.afya.portal.membercareprovider.view.dto.YesterdayClinicRevenueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/2/15
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
public class FaqController {

    @Autowired
    private FaqFinder faqFinder;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/getAllFaq")
    public @ResponseBody List<FaqDto> getAllFaq(){
        return faqFinder.getAllFaq();
    }

}
