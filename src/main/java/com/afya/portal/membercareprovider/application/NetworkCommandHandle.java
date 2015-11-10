package com.afya.portal.membercareprovider.application;

import com.afya.portal.domain.model.clinic.Clinic;
import com.afya.portal.membercareprovider.domain.Network;
import com.afya.portal.util.UtilValidator;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/4/15
 * Time: 8:33 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class NetworkCommandHandle {

    @Autowired
    private Repository<Network> networkRepository;

    @Autowired
    private Repository<Clinic> clinicRepository;

    @CommandHandler
    public Network handle(NetworkCreateCommand cmd) throws Exception {
        Network network = null;
        if(UtilValidator.isEmpty(cmd.getNetworkId())){
            Clinic clinic = clinicRepository.load(cmd.getFromClinicId());
            Clinic toClinic = clinicRepository.load(cmd.getClinicId());
            network = new Network(clinic,toClinic,"");
            networkRepository.add(network);
        }else{
            network = networkRepository.load(cmd.getNetworkId());
            network.setStatus(Network.STATUS.valueOf(cmd.getNetworkStatus()));
        }
        return network;
    }
}
