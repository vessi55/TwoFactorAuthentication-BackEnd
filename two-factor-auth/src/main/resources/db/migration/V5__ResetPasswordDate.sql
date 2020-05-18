USE `two-factor-auth` ;

alter table `two-factor-auth` .`users`
ADD COLUMN `reset_password_date` BIGINT NULL DEFAULT NULL;