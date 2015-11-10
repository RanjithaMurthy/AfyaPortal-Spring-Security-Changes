package com.afya.portal.controller;

import com.afya.portal.application.PersistFacilityCommand;
import com.afya.portal.query.PortalFinder;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Controller
public class ExampleController {


    @RequestMapping(value = "/example", method = RequestMethod.GET)
    public ResponseEntity<String> example() {
         return new ResponseEntity<String>("{message : 'success'}",null, HttpStatus.OK);
    }

    @RequestMapping(value = "/test")
    public String test (Model model) {
        model.addAttribute("name", "Ajay");
        return "test";
    }
}
