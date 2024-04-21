SET NAMES utf8mb4;

DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock`
(
    `id`      int AUTO_INCREMENT,
    `name`    varchar(50),
    `count`   int,
    `sold`    int,
    `version` int,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `stock_order`;
CREATE TABLE `stock_order`
(
    `id`         int AUTO_INCREMENT,
    `sid`        int,
    `name`       varchar(50),
    `createTime` timestamp,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`       int AUTO_INCREMENT,
    `name`     varchar(50),
    `password` varchar(50),
    PRIMARY KEY (`id`)
);

-- -----------------------------------------------

INSERT INTO `stock` VALUES (1, 'iPhone 15', 10000, 0, 0);
INSERT INTO `stock` VALUES (2, 'iPad 9', 5000, 0, 0);
INSERT INTO `stock` VALUES (3, 'Vision Pro', 1000, 0, 0);

INSERT INTO `user` VALUES (1, 'admin', '123456');
INSERT INTO `user` VALUES (2, 'test', '12345678');
