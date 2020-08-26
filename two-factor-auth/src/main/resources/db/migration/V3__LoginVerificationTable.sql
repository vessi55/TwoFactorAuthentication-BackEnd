USE `two-factor-auth` ;

-- -----------------------------------------------------
-- Table `two-factor-auth`.`login_verification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `two-factor-auth`.`login_verification` (
  `uid` VARCHAR(255) NOT NULL,
  `login_date` BIGINT(20) NOT NULL,
  `verification_code` VARCHAR(6) NOT NULL,
  `user_id` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`uid`),
  INDEX `FKf09bsfpmtpvmfa9axd0phmecc` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FKf09bsfpmtpvmfa9axd0phmecc`
    FOREIGN KEY (`user_id`)
    REFERENCES `two-factor-auth`.`users` (`uid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4;