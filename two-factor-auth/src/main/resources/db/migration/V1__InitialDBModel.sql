USE `two-factor-auth` ;

-- -----------------------------------------------------
-- Table `two-factor-auth`.`invitations`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `two-factor-auth`.`invitations` (
  `uid` VARCHAR(255) NOT NULL,
  `created_date` BIGINT(20) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `role` VARCHAR(10) NOT NULL,
  `status` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE INDEX `UK_9iu98yyhhyc0nsjn0e82g99vi` (`email` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4;

-- -----------------------------------------------------
-- Table `two-factor-auth`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `two-factor-auth`.`users` (
  `uid` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `first_name` VARCHAR(255) NULL DEFAULT NULL,
  `last_name` VARCHAR(255) NULL DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(255) NULL DEFAULT NULL,
  `role` VARCHAR(20) NULL DEFAULT NULL,
  `invitation_id` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE INDEX `UK_6dotkott2kjsp8vw4d0m25fb7` (`email` ASC) VISIBLE,
  INDEX `FKgqerfu7jdu22eud6d8g88sin8` (`invitation_id` ASC) VISIBLE,
  CONSTRAINT `FKgqerfu7jdu22eud6d8g88sin8`
    FOREIGN KEY (`invitation_id`)
    REFERENCES `two-factor-auth`.`invitations` (`uid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4;
