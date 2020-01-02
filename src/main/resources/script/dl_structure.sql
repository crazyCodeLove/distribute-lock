DROP TABLE IF EXISTS `lock_info`;

CREATE TABLE `lock_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `number` int(11) DEFAULT NULL COMMENT '序号',
  `ip` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'ip',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

