/*
SQLyog Community v12.01 (64 bit)
MySQL - 5.6.26-log 
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

create table `provider_refund` (
	`ID` bigint (20),
	`IS_ACTIVE` bit (1),
	`CREATED_BY` varchar (765),
	`CREATE_TX_TIMESTAMP` datetime ,
	`DEACTIVATION_REASON` varchar (765),
	`UPDATE_BY` varchar (765),
	`UPDATED_TX_TIMESTAMP` datetime ,
	`VERSION` bigint (20),
	`REFUND_AMOUNT` Decimal (21),
	`REFUND_NOTE` varchar (765),
	`REFUND_REASON` varchar (765),
	`STATUS` varchar (765),
	`DEACTIVATEDBY` bigint (20),
	`INVOICE` bigint (20),
	`PROVIDER` bigint (20)
);

create table `afya_clinic_deposit` (
	`ID` bigint (20),
	`IS_ACTIVE` bit (1),
	`CREATED_BY` varchar (765),
	`CREATE_TX_TIMESTAMP` datetime ,
	`DEACTIVATION_REASON` varchar (765),
	`UPDATE_BY` varchar (765),
	`UPDATED_TX_TIMESTAMP` datetime ,
	`VERSION` bigint (20),
	`CLINIC_DEPOSIT_TYPE` varchar (765),
	`DEPOSIT_AMOUNT` Decimal (21),
	`DEACTIVATEDBY` bigint (20),
	`INVOICE` bigint (20),
	`PATIENT` bigint (20),
	`PROVIDER` bigint (20)
);

ALTER TABLE `patient_deposit` ADD BANK_NAME VARCHAR(255);
ALTER TABLE `patient_deposit` ADD CHEQUE_DATE DATE;
ALTER TABLE `patient_deposit` ADD TXN_NUMBER VARCHAR(255);

ALTER TABLE slot_type ADD color VARCHAR(255);

ALTER TABLE `clinic_cancellation_preference` ADD CANCELLATION_TIME DECIMAL(19,2);
ALTER TABLE `clinic_rescheduling_preference` ADD RESCHEDULING_TIME DECIMAL(19,2);

ALTER TABLE `invoice` ADD `AMOUNT_REFUNDED_TO_PATIENT` BOOLEAN;
