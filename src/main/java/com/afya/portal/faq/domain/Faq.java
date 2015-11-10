package com.afya.portal.faq.domain;

import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/2/15
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Getter
@Setter
public class Faq implements ICrudEntity {

    @Id
    private String questionId;

    @Column(length = 1000)
    private String question;

    @Column(length = 1000)
    private String answer;

    @Enumerated(EnumType.STRING)
    private QuestionCategory questionCategory;

    @Enumerated(EnumType.STRING)
    private QuestionLevel questionLevel;


    enum QuestionCategory{
        PATIENT("Patient"),CARE_PROVIDER("Care Provider"),CARE_PAYOR("Care Payer");
        private String name;
        QuestionCategory(String name){
          this.name = name;
        }
        public String getName(){
            return name;
        }
    }

    enum QuestionLevel{
        HIGH("High"),MEDIUM("Medium"),LOW("Low");
        private String name;
        QuestionLevel(String name){
          this.name = name;
        }
        public String getName(){
            return name;
        }
    }

}
