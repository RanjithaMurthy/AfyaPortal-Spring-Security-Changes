package com.afya.portal.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Mohan Sharma on 6/22/2015.
 */
@Getter
@Setter
public class PersistCivilDataImageCommand {
    private String civilId;
    private MultipartFile image;

    public PersistCivilDataImageCommand(String civilId, MultipartFile image) {
        this.civilId = civilId;
        this.image = image;
    }
}
