package com.afya.portal.domain.model;

import com.afya.portal.domain.model.patient.Patient;
import com.nzion.dto.PatientDto;
import lombok.NoArgsConstructor;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Mohan Sharma on 3/30/2015.
 */
@Component
@NoArgsConstructor
public class PatientEventHandler {

    @PersistenceContext
    private EntityManager entityManager;

    @EventHandler
    public void addPatient(EventMessage<PatientDto> message) {
        System.out.println("Received Message " + message);
        PatientDto patientDto = (PatientDto) message.getPayload();
        String afyaId = patientDto.getAfyaId();
        Patient patient = null;
        if (afyaId != null) {
            patient = entityManager.find(Patient.class, Long.valueOf(afyaId));
        } else {
            patient = new Patient();
        }
        patient.setPropertiesToPatientEntity(patientDto);
        entityManager.persist(patient);
    }

}
