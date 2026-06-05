# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20077
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: localhost (MySQL 8.0.46-0ubuntu0.24.04.2)
# 数据库: target_db
# 生成时间: 2026-06-05 11:45:24 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# 转储表 activity_reward_log
# ------------------------------------------------------------

CREATE TABLE `activity_reward_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `activity_key` varchar(255) DEFAULT NULL,
  `details` json DEFAULT NULL,
  `rewards` json DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXk19mxlcyma049xacphrhyddou` (`activity_key`),
  KEY `IDXiu3kjjwl7ysjweu6uj76k016g` (`user_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
