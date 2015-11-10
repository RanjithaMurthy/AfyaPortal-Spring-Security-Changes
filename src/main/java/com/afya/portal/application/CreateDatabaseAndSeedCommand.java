package com.afya.portal.application;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Mohan Sharma on 3/13/2015.
 */
@Data
@AllArgsConstructor
public class CreateDatabaseAndSeedCommand {
    @NotNull
    public String databaseName;
    @NotNull
    public String userName;
    @NotNull
    public String password;
}
