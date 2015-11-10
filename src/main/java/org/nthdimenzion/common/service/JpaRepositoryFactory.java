/*
 * Copyright (c) 1/22/15 8:50 PM.Nth Dimenzion, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package org.nthdimenzion.common.service;

import com.google.common.base.Preconditions;
import org.nthdimenzion.common.crud.ICrudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */

@Service
public class JpaRepositoryFactory {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> T getCrudRepository(Class<? extends ICrudEntity> crudEntity) {
        Preconditions.checkNotNull(entityManager);
        Preconditions.checkNotNull(crudEntity);
        T crudRepository = (T)new SimpleJpaRepository(crudEntity, entityManager);
        return crudRepository;
    }

}
