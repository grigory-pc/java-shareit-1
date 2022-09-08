DROP TABLE IF EXISTS USERS, REQUESTS, ITEMS, BOOKINGS, COMMENTS;

CREATE TABLE IF NOT EXISTS `USERS`
(
    `ID`    LONG PRIMARY KEY AUTO_INCREMENT,
    `NAME`  VARCHAR(50),
    `EMAIL` VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS `REQUESTS`
(
    `ID`           LONG PRIMARY KEY AUTO_INCREMENT,
    `DESCRIPTION`  VARCHAR(200),
    `REQUESTER_ID` LONG,
    `CREATED`   DATETIME
);

CREATE TABLE IF NOT EXISTS `ITEMS`
(
    `ID`          LONG PRIMARY KEY AUTO_INCREMENT,
    `NAME`        VARCHAR(50),
    `DESCRIPTION` VARCHAR(200),
    `AVAILABLE`   VARCHAR(20),
    `OWNER_ID`    LONG,
    `REQUEST_ID`  LONG
);

CREATE TABLE IF NOT EXISTS `BOOKINGS`
(
    `ID`         LONG PRIMARY KEY AUTO_INCREMENT,
    `START_DATE` DATETIME,
    `END_DATE`   DATETIME,
    `STATUS`     VARCHAR(20),
    `ITEM_ID`    LONG,
    `BOOKER_ID`  LONG
);

CREATE TABLE IF NOT EXISTS `COMMENTS`
(
    `ID`        LONG PRIMARY KEY AUTO_INCREMENT,
    `TEXT`      VARCHAR(200),
    `ITEM_ID`   LONG,
    `AUTHOR_ID` LONG,
    `CREATED`   DATE
);

ALTER TABLE `REQUESTS`
    ADD FOREIGN KEY (`REQUESTER_ID`) REFERENCES `USERS` (`ID`);

ALTER TABLE `ITEMS`
    ADD FOREIGN KEY (`OWNER_ID`) REFERENCES `USERS` (`ID`);

ALTER TABLE `ITEMS`
    ADD FOREIGN KEY (`REQUEST_ID`) REFERENCES `REQUESTS` (`ID`);

ALTER TABLE `BOOKINGS`
    ADD FOREIGN KEY (`ITEM_ID`) REFERENCES `ITEMS` (`ID`);

ALTER TABLE `BOOKINGS`
    ADD FOREIGN KEY (`BOOKER_ID`) REFERENCES `USERS` (`ID`);

ALTER TABLE `COMMENTS`
    ADD FOREIGN KEY (`ITEM_ID`) REFERENCES `ITEMS` (`ID`);

ALTER TABLE `COMMENTS`
    ADD FOREIGN KEY (`AUTHOR_ID`) REFERENCES `USERS` (`ID`);

