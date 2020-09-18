USE `two-factor-auth` ;

-- -----------------------------------------------------
-- Table `two-factor-auth`.`articles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `two-factor-auth`.`articles` (
  `uid` VARCHAR(255) NOT NULL,
  `created_date` BIGINT(20) NOT NULL,
  `image` LONGBLOB NULL DEFAULT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `user_id` VARCHAR(255) NOT NULL,
   PRIMARY KEY (`uid`),
  INDEX `FKlc3sm3utetrj1sx4v9ahwopnr` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FKlc3sm3utetrj1sx4v9ahwopnr`
    FOREIGN KEY (`user_id`)
    REFERENCES `two-factor-auth`.`users` (`uid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4;