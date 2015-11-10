DROP FUNCTION IF EXISTS `getInvoiceItemNetAmount`$$

CREATE DEFINER=`root`@`localhost` FUNCTION `getInvoiceItemNetAmount`(invoiceItemId BIGINT(20)) RETURNS DECIMAL(19,3)
BEGIN
	DECLARE netAmount DECIMAL(19,3);
	DECLARE AMOUNT DECIMAL(19,3);
	DECLARE CONCESSION_APPLIED BIT(1);
	DECLARE CONCESSION_TYPE VARCHAR(255);
	DECLARE CONCESSION_AMOUNT DECIMAL(19,3);

	-- select necessary fields from invoice_item record and patient record
	SELECT itm.AMOUNT
		, itm.CONCESSION_APPLIED, itm.CONCESSION_TYPE, itm.CONCESSION_AMOUNT
	INTO AMOUNT
		, CONCESSION_APPLIED, CONCESSION_TYPE, CONCESSION_AMOUNT
	FROM invoice_item itm
	WHERE itm.ID = invoiceItemId;

	-- compute NetAmout
	SET netAmount = AMOUNT - (/*CASE CONCESSION_APPLIED
			WHEN 1 THEN */(CASE CONCESSION_TYPE WHEN 'PERCENTAGE' THEN (IFNULL(CONCESSION_AMOUNT,0) * AMOUNT / 100) ELSE IFNULL(CONCESSION_AMOUNT,0) END)
			/*ELSE 0 END*/);

	-- return Patient Payable amount
	RETURN IFNULL(netAmount,0);
    END$$

-- -----------------------------------------

DROP FUNCTION IF EXISTS `getInvoiceItemPatientPayable`$$

CREATE DEFINER=`root`@`localhost` FUNCTION `getInvoiceItemPatientPayable`(invoiceItemId BIGINT(20)) RETURNS DECIMAL(19,3)
BEGIN
	DECLARE netAmount DECIMAL(19,3);
	DECLARE netDeductableAmount DECIMAL(19,3);
	DECLARE netCopayAmount DECIMAL(19,3);
	DECLARE netPatientPayableAmount DECIMAL(19,3);
	DECLARE PATIENT_TYPE VARCHAR(255);
	DECLARE AMOUNT DECIMAL(19,3);
	DECLARE CONCESSION_APPLIED BIT(1);
	DECLARE CONCESSION_TYPE VARCHAR(255);
	DECLARE CONCESSION_AMOUNT DECIMAL(19,3);

	DECLARE DEDUCTABLE_AMOUNT DECIMAL(19,3);
	DECLARE DEDUCTABLE_PERCENTAGE DECIMAL(19,3);

	DECLARE COPAY_AMOUNT DECIMAL(19,3);
	DECLARE COPAY_PERCENTAGE DECIMAL(19,3);

	-- select necessary fields from invoice_item record and patient record
	SELECT patient.PATIENT_TYPE, itm.AMOUNT
		, itm.CONCESSION_APPLIED, itm.CONCESSION_TYPE, itm.CONCESSION_AMOUNT
		, itm.DEDUCTABLE_AMOUNT, itm.DEDUCTABLE_PERCENTAGE
		, itm.COPAY_AMOUNT, itm.COPAY_PERCENTAGE
	INTO PATIENT_TYPE, AMOUNT
		, CONCESSION_APPLIED, CONCESSION_TYPE, CONCESSION_AMOUNT
		, DEDUCTABLE_AMOUNT, DEDUCTABLE_PERCENTAGE
		, COPAY_AMOUNT, COPAY_PERCENTAGE
	FROM invoice_item itm
	INNER JOIN invoice ON invoice.ID = itm.INVOICE_ID
	INNER JOIN patient ON patient.ID = invoice.PATIENT
	WHERE itm.ID = invoiceItemId;

	-- compute NetAmout
	SET netAmount = AMOUNT - (/*CASE CONCESSION_APPLIED
			WHEN 1 THEN */(CASE CONCESSION_TYPE WHEN 'PERCENTAGE' THEN (IFNULL(CONCESSION_AMOUNT,0) * AMOUNT / 100) ELSE IFNULL(CONCESSION_AMOUNT,0) END)
			/*ELSE 0 END*/);

	-- compute Deductable Amount
	SET netDeductableAmount = IFNULL(DEDUCTABLE_AMOUNT, 0) + (IFNULL(DEDUCTABLE_PERCENTAGE, 0) * netAmount / 100);

	-- compute Copay Amount
	SET netCopayAmount = IFNULL(COPAY_AMOUNT,0) + (IFNULL(COPAY_PERCENTAGE,0) * (netAmount - netDeductableAmount) / 100);

	-- compute Patient Payable
	CASE PATIENT_TYPE
		WHEN 'INSURANCE' THEN SET netPatientPayableAmount = netDeductableAmount + netCopayAmount;
		-- WHEN 'CASH' THEN SET netPatientPayableAmount = netAmount;
		ELSE SET netPatientPayableAmount = netAmount;
	END CASE;
	-- return Patient Payable amount
	RETURN IFNULL(netPatientPayableAmount,0);
	/* Previous Code as on 2015-08-15
	DECLARE patientPayableAmount DECIMAL(19,3);
	SELECT patientPayable
	INTO patientPayableAmount
	FROM
	(SELECT
		CASE
			WHEN patient.PATIENT_TYPE = 'INSURANCE' THEN
				invoice_item.AMOUNT - (IFNULL(invoice_item.COPAY_AMOUNT,0) + (IFNULL(invoice_item.COPAY_PERCENTAGE,0) * invoice_item.AMOUNT / 100)
					+ IFNULL(invoice_item.DEDUCTABLE_AMOUNT, 0) + (IFNULL(invoice_item.DEDUCTABLE_PERCENTAGE, 0) * invoice_item.AMOUNT / 100))
			WHEN patient.PATIENT_TYPE = 'CASH' THEN invoice_item.AMOUNT
			ELSE NULL
		END AS patientPayable
	FROM invoice_item
	INNER JOIN invoice ON invoice.ID = invoice_item.INVOICE_ID
	INNER JOIN patient on patient.ID = invoice.PATIENT
	WHERE invoice_item.ID = invoiceItemId) tmp;

	RETURN IFNULL(patientPayableAmount,0);*/
    END$$

-- ----------------------------

DROP FUNCTION IF EXISTS `getRcmBillableAmount`$$

CREATE DEFINER=`root`@`localhost` FUNCTION `getRcmBillableAmount`(visitType VARCHAR(255), billableAmount DECIMAL(19,3) ) RETURNS DECIMAL(19,3)
BEGIN
	DECLARE rcmBillableAmount DECIMAL(19,3);
	DECLARE advanceAmount DECIMAL(19,3);
	DECLARE convenienceFee DECIMAL(19,3);
	DECLARE scpVisitType VARCHAR(255);

	SET scpVisitType =
		CASE visitType
			WHEN 'Premium Visit' THEN 'PREMIUM_APPOINTMENT'
			WHEN 'Tele Consultation Visit' THEN 'TELE_CONSULT_APPOINTMENT'
			WHEN 'Home Visit' THEN 'HOME_VISIT_APPOINTMENT'
			WHEN 'Consult Visit' THEN 'CONSULT_VISIT'
			ELSE NULL
		END;


	SELECT IFNULL(scp.ADVANCE_AMOUNT, (IFNULL(scp.ADVANCE_AMOUNT_PERCENT, 0) * billableAmount / 100)) AS advanceAmount
		, IFNULL(scp.CONVENIENCE_FEE, (IFNULL(scp.CONVENIENCE_FEE_PERCENT, 0) * billableAmount / 100)) AS convenienceFee
	INTO advanceAmount, convenienceFee
	FROM scheduling_preference scp
	WHERE scp.VISIT_TYPE = scpVisitType;

	SET rcmBillableAmount = (CASE advanceAmount WHEN 0 THEN billableAmount ELSE advanceAmount END) + convenienceFee;

	RETURN IFNULL(rcmBillableAmount, billableAmount);

    END$$

-- ----------------------

DROP FUNCTION IF EXISTS `getTotalBillableAmount`$$

CREATE DEFINER=`root`@`localhost` FUNCTION `getTotalBillableAmount`(visitType VARCHAR(255), billableAmount DECIMAL(19,3)) RETURNS DECIMAL(19,3)
BEGIN
	-- DECLARE rcmBillableAmount DECIMAL(19,3);
	-- DECLARE advanceAmount DECIMAL(19,3);
	DECLARE totalBillableAmount DECIMAL(19,3);
	DECLARE convenienceFee DECIMAL(19,3);
	DECLARE scpVisitType VARCHAR(255);

	SET scpVisitType =
		CASE visitType
			WHEN 'Premium Visit' THEN 'PREMIUM_APPOINTMENT'
			WHEN 'Tele Consultation Visit' THEN 'TELE_CONSULT_APPOINTMENT'
			WHEN 'Home Visit' THEN 'HOME_VISIT_APPOINTMENT'
			WHEN 'Consult Visit' THEN 'CONSULT_VISIT'
			ELSE NULL
		END;


	/*SELECT IFNULL(scp.ADVANCE_AMOUNT, (IFNULL(scp.ADVANCE_AMOUNT_PERCENT, 0) * billableAmount / 100)) AS advanceAmount
		, IFNULL(scp.CONVENIENCE_FEE, (IFNULL(scp.CONVENIENCE_FEE_PERCENT, 0) * billableAmount / 100)) AS convenienceFee*/
	SELECT IFNULL(scp.CONVENIENCE_FEE, (IFNULL(scp.CONVENIENCE_FEE_PERCENT, 0) * billableAmount / 100)) AS convenienceFee
	INTO convenienceFee
	FROM scheduling_preference scp
	WHERE scp.VISIT_TYPE = scpVisitType;

	SET totalBillableAmount = billableAmount + IFNULL(convenienceFee,0);

	RETURN totalBillableAmount;

    END$$

-- ----------------------

DROP FUNCTION IF EXISTS `getRcmVisitTypeName`$$

CREATE DEFINER=`root`@`localhost` FUNCTION `getRcmVisitTypeName`(slotTypeName VARCHAR(255)) RETURNS VARCHAR(255) CHARSET utf8
BEGIN
	DECLARE rcmVisitType VARCHAR(255);

	SET rcmVisitType =
		CASE slotTypeName
		WHEN 'Premium Visit' THEN 'PREMIUM_APPOINTMENT'
		WHEN 'Tele Consultation Visit' THEN 'TELE_CONSULT_APPOINTMENT'
		WHEN 'Home Visit' THEN 'HOME_VISIT_APPOINTMENT'
		WHEN 'Consult Visit' THEN 'CONSULT_VISIT'
		ELSE NULL
	END;
	RETURN rcmVisitType;
    END$$

-- ----------------------

DROP FUNCTION IF EXISTS `getRcmConvenienceFee`$$

CREATE DEFINER=`root`@`localhost` FUNCTION `getRcmConvenienceFee`(visitType VARCHAR(255), billableAmount DECIMAL(19,3) ) RETURNS DECIMAL(19,3)
BEGIN
	DECLARE convenienceFee DECIMAL(19,3);
	DECLARE scpVisitType VARCHAR(255);
	SELECT `getRcmVisitTypeName`(visitType) INTO scpVisitType;
	SELECT IFNULL(scp.CONVENIENCE_FEE, (IFNULL(scp.CONVENIENCE_FEE_PERCENT, 0) * billableAmount / 100)) AS convenienceFee
	INTO convenienceFee
	FROM scheduling_preference scp
	WHERE scp.VISIT_TYPE = scpVisitType;
	RETURN convenienceFee;
    END$$

