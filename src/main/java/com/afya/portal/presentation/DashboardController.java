package com.afya.portal.presentation;

import com.afya.portal.query.UserLoginFinder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Div;

import java.util.Collections;
import java.util.List;

/**
 * Created by pradyumna on 11-06-2015.
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashboardController extends SelectorComposer<Div> {

    @WireVariable
    private UserLoginFinder userLoginFinder;

    private List<FacilityDto> facilities;

    public List<FacilityDto> getFacilities(){
       /* Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        if (null != currentUser) {
            return userLoginFinder.getFacilities(((UserDetails)currentUser.getPrincipal()).getUsername());
        }*/
        return Collections.emptyList();
    }


    @Override
    public void doAfterCompose(Div component) throws Exception {

    }
}
