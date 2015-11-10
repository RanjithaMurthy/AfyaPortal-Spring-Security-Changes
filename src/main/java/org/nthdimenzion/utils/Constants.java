package org.nthdimenzion.utils;

public interface Constants {
	String DEFAULT_SECURITY_GROUP = "ADMIN";
	String SUPER_ADMIN = "SUPER_ADMIN";
	String ADMIN = "ADMIN";
	String FRONTDESK = "FRONTDESK";
	String PROVIDER = "PROVIDER";
	String PATIENT = "PATIENT";
	String CASEMANAGER = "CASEMANAGER";
	String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
	String ROLE_ADMIN = "ROLE_ADMIN";
	String ROLE_APPOINTMENT_CREATE = "ROLE_APPOINTMENT_CREATE";
	String ROLE_PROVIDER = "ROLE_PROVIDER";
	String ROLE_PATIENT = "ROLE_PATIENT";
	String PREFERENCE_COMPONENT_PREFIX = "comp";
	String LABEL_PREFIX = "lbl";
	String TEXTBOX_PREFIX = "txt";
	String SINGLE_SPACE = " ";
	String NAME_SEPERATOR = SINGLE_SPACE;
	Integer PRIMARY_POLICY = Integer.valueOf(1);
	String EMAIL_PATTERN = ".+@.+\\.[a-z]+";
	String FIELD_TYPE = "fieldtype";
	String FIELD_VALUE = "fieldvalue";
	String DEFAULT_FIELD_VALUE = "defaultfieldvalue";
	String SEQNUM = "seqnum";
	String REQUIRED_COLUMN = "requiredcolumn";
	String SEARCH_COLUMN = "searchcolumn";
	String DISPLAY_COLUMN = "displaycolumn";
	String ID = "id";
	String LISTENER_EVENT = "listenerevent";
	String NEW_LABEL =  "newlabel";
	String LABEL = "label";
	String ISMANDATORY = "ismandatory";

	Integer MINUTES_PER_DAY = 24 * 60;
	Integer SECONODS_PER_DAY = MINUTES_PER_DAY * 60;
	Integer MILLI_SECONODS_PER_DAY = SECONODS_PER_DAY * 1000;
	Integer PAGE_SIZE = 10;

	String DELIMITER = "_";

	String LOAD = "_LOAD";
	String CREATE = "_CREATE";
	String UPDATE = "_UPDATE";
	String DELETE = "_DELETE";
	String[] DML_OPS = new String[]{LOAD, CREATE, UPDATE, DELETE};

	String COMMA = ",";
	String NEWLINE = "\r\n";

	String COMMON_SECURITY_GROUP = "COMMON_SECURITY_GROUP";
	char[] ALPHABETS = new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	char[] DIGITS = new char[]{'0','1','2','3','4','5','6','7','8','9'};
	char BLANK_CHAR = ' '; 
	String BLANK = " "; 
	String YES = "Yes";
	String NO = "No";
	
	// COLORS
	String WHITE = "#FFFFFF";
}
