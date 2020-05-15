USE `two-factor-auth` ;

alter table `two-factor-auth`.`invitations`
ADD COLUMN `verification_code` VARCHAR(6) NOT NULL;