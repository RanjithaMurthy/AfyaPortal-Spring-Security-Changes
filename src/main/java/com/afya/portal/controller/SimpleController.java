package com.afya.portal.controller;

import com.afya.portal.application.PersistFacilityCommand;
import com.afya.portal.query.PortalFinder;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Controller
public class SimpleController {

    public CommandGateway commandGateway;
    PortalFinder portalFinder;

    @Autowired
    public SimpleController(CommandGateway commandGateway, PortalFinder clinicalFinder) {
        this.commandGateway = commandGateway;
        this.portalFinder = clinicalFinder;
    }

    @RequestMapping(value = "/facility", method = RequestMethod.GET)
    public ModelAndView showClinicForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("persistClinicCommand", new PersistFacilityCommand());
        modelAndView.setViewName("clinicForm");
        return modelAndView;
    }

    @ExceptionHandler
    public void handleException(Exception e) {
        System.out.println(" Expetion " + e.getMessage());
    }
}
