package com.afya.portal.application;

import com.nzion.dto.PackageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by pradyumna on 12-06-2015.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscribePackageCommand {
    String userName;
    private List<PackageDto> packageList;
    private String smsSenderId;
    private Long paymentId;
}
