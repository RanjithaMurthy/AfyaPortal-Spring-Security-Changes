package com.afya.portal.faq.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/2/15
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class FaqDto {

    private String questionId;

    private String question;

    private String answer;

    private String questionCategory;

    private String questionLevel;
}
