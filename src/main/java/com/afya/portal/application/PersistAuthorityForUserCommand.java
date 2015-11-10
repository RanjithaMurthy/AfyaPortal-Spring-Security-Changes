package com.afya.portal.application;

import com.google.common.collect.Sets;
import com.nzion.dto.UserLoginDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * Created by Mohan Sharma on 7/22/2015.
 */
@AllArgsConstructor
@Getter
public class PersistAuthorityForUserCommand {
    private UserLoginDto userLoginDto;
    private Set<String> authority = Sets.newHashSet();
}
