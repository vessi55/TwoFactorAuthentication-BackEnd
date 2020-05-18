USE `two-factor-auth` ;

alter table `two-factor-auth` .`invitations`
ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0;

alter table `two-factor-auth` .`users`
ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0;