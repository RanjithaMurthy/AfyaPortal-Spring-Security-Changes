package org.nthdimenzion.ddd.domain;

import org.nthdimenzion.ddd.domain.annotations.DomainFactory;
import org.nthdimenzion.object.utils.IIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;

@DomainFactory
public class AbstractDomainFactory {

    @Autowired
    protected IIdGenerator idGenerator;


}
