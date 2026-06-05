# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20077
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: localhost (MySQL 8.0.46-0ubuntu0.24.04.2)
# 数据库: way2_edu_db
# 生成时间: 2026-06-05 11:41:06 +0000
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 activity_schedule
# ------------------------------------------------------------

CREATE TABLE `activity_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `activity_key` varchar(255) DEFAULT NULL,
  `auto_start` bit(1) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `display` varchar(255) DEFAULT NULL,
  `open` bit(1) DEFAULT NULL,
  `period_end_time` bigint DEFAULT NULL,
  `period_start_time` bigint DEFAULT NULL,
  `phase_rewards` json DEFAULT NULL,
  `reward_trigger` varchar(255) DEFAULT NULL,
  `reward_trigger_params` json DEFAULT NULL,
  `rewards` json DEFAULT NULL,
  `valid_count` bigint DEFAULT NULL,
  `valid_duration` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1kntydqqpcx7b3no6salgtfyx` (`activity_key`),
  KEY `IDXlp4i795esdppyy9bq00hxdng3` (`reward_trigger`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 authenticate
# ------------------------------------------------------------

CREATE TABLE `authenticate` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `access_expires_at` bigint DEFAULT NULL,
  `access_token` text,
  `access_token_id` varchar(255) DEFAULT NULL,
  `login_ip` varchar(255) DEFAULT NULL,
  `login_type` enum('DEFAULT','OAUTH2') NOT NULL,
  `logout_timestamp` bigint DEFAULT NULL,
  `refresh_expires_at` bigint DEFAULT NULL,
  `refresh_token` text,
  `refresh_token_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `login_platform` enum('IOS','WEB') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXrt6i9j76epa9kdwujbncs57s9` (`user_id`),
  KEY `IDXc01qxburbkfgaprt72fjqdl52` (`username`),
  KEY `IDXrplgur5vo3j3qcebg0ph8ubya` (`access_token_id`),
  KEY `IDX1kuv4nu4e2nqepedmm4deweq5` (`refresh_token_id`),
  KEY `IDXsymrxsvvh79vfopjf9nbd7nth` (`user_id`,`login_platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 chat_conversation
# ------------------------------------------------------------

CREATE TABLE `chat_conversation` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `assistant_msg_id` varchar(255) DEFAULT NULL,
  `assistant_text` text,
  `chat_context` json DEFAULT NULL,
  `completion_id` varchar(255) NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `feedback` json DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `session_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `thinking_assistant_text` text,
  `thinking_elapsed_secs` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXlh0g30y76xykfrh0ogya29ma0` (`session_id`),
  KEY `IDXhhf4s7vjr0vl1cmgu9k7vyqt2` (`user_id`),
  KEY `IDXjvuw7x6s3513166pj208xn4w1` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 chat_session
# ------------------------------------------------------------

CREATE TABLE `chat_session` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `initial_context` json DEFAULT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXbtiycf7at8q98lm757jq7nqrh` (`user_id`),
  KEY `IDX5ltksk2er1tbnytc7e06kobk` (`created`),
  KEY `IDXm2jq13en6vr6dcu9isgdd0l8m` (`video_summary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 chat_shared
# ------------------------------------------------------------

CREATE TABLE `chat_shared` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `chat_session_id` varchar(255) DEFAULT NULL,
  `snapshoot` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXeqdxi8lrq084h795ybu7fgvc0` (`user_id`),
  KEY `IDX2e53aqryh35kfapqkpx8r2ffn` (`chat_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 chat_video
# ------------------------------------------------------------

CREATE TABLE `chat_video` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `favorite` bit(1) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_meta` json NOT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
  `video_summary_language` varchar(255) DEFAULT NULL,
  `mode` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXoeb6yjnbd111p3wqwbo9nllbc` (`user_id`),
  KEY `IDXchw3m5vucljq4k8f6skbgb3n2` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 customer
# ------------------------------------------------------------

CREATE TABLE `customer` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `email` varchar(255) NOT NULL,
  `last_checkout_session_id` varchar(255) DEFAULT NULL,
  `stripe_id` varchar(255) DEFAULT NULL,
  `type` enum('FREE','LAPSED_SUBSCRIPTION','PAYMENT','SUBSCRIPTION') NOT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `apple_account_token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4qm52n85kwhmxj2nktmj1kv9h` (`user_account_id`),
  UNIQUE KEY `UKdwk6cx0afu8bs9o4t536v1j5v` (`email`),
  UNIQUE KEY `UKsqa5ejoa652ln8nxyr6q6xi1e` (`apple_account_token`),
  KEY `IDXmuvtldex930iffxglqp0dqn0o` (`stripe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 deep_video_summary
# ------------------------------------------------------------

CREATE TABLE `deep_video_summary` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `completion_id` varchar(255) NOT NULL,
  `feedback` json DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `link` varchar(10240) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `highlights` json DEFAULT NULL,
  `idx` int DEFAULT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7kp0gywo5j9yh6nxgaejr0afj` (`user_id`,`identity`),
  KEY `IDXpa4isj77b7ldxht9mgqh65fuu` (`user_id`),
  KEY `IDX7apn5tvqvawx7on7aulg5qud9` (`identity`),
  KEY `IDXlv8a47a7m2pw61f1rjxgtc29t` (`video_summary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 external_file
# ------------------------------------------------------------

CREATE TABLE `external_file` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `checksum` varchar(255) DEFAULT NULL,
  `file_source` enum('GOOGLE_DRIVE','UPLOAD') NOT NULL,
  `identify` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `origin_name` varchar(255) DEFAULT NULL,
  `rex` varchar(255) DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `status` enum('DONE','ERROR','EXPIRE','FINALLY','START','UPLOADED') DEFAULT NULL,
  `upload_end_time` bigint DEFAULT NULL,
  `upload_start_time` bigint DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXokbli2mrhio8dllvr0hc9sga7` (`user_id`,`file_source`,`checksum`),
  KEY `IDX6sgds571444bg674gpqfk6jw9` (`user_id`,`identify`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 favorite
# ------------------------------------------------------------

CREATE TABLE `favorite` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `entity_id` varchar(255) DEFAULT NULL,
  `snapshot` json DEFAULT NULL,
  `type` enum('SEARCH_VIDEO') DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKc6si27o87oi09f9c3s888kkdy` (`user_account_id`,`type`,`entity_id`),
  KEY `IDX8jxd9o54qnrrud7yd5sm62utm` (`user_account_id`),
  KEY `IDXa0pxqupxivnid94nn490fr0yp` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 log_record
# ------------------------------------------------------------

CREATE TABLE `log_record` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `event` enum('CLICK_TIMESTAMP','REQUEST_CHAT','REQUEST_HIGHLIGHT','REQUEST_MINDMAP','REQUEST_SUMMARY','VIDEO_VISIT','MASTER_VIDEO_BUTTON_CLICK') NOT NULL,
  `identify` varchar(255) DEFAULT NULL,
  `properties` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 multimodal_completion
# ------------------------------------------------------------

CREATE TABLE `multimodal_completion` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `assistant_message` json DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `error_code` varchar(255) DEFAULT NULL,
  `error` text,
  `ip_address` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `streaming` bit(1) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `user_message` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXixfvg63o4x8c1cirv11wka2kf` (`user_id`),
  KEY `IDXisthohprkm1ddix5m1xaktvrn` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 note
# ------------------------------------------------------------

CREATE TABLE `note` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `favorite` bit(1) NOT NULL,
  `highlight_display` bit(1) NOT NULL,
  `highlight_fragment_display` json DEFAULT NULL,
  `highlight_frame_display` json DEFAULT NULL,
  `highlights` json DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `mindmap` json DEFAULT NULL,
  `mindmap_display` bit(1) NOT NULL,
  `summary` text,
  `summary_display` bit(1) NOT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  `title` text,
  `user_account_id` varchar(255) NOT NULL,
  `link` varchar(10240) NOT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
  `saved` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXpsfjyblo8s0wpe0yommmyjapq` (`user_account_id`),
  KEY `IDXb5too3j3ip527y17mxk18wmjl` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 note_shared
# ------------------------------------------------------------

CREATE TABLE `note_shared` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `note_id` varchar(255) DEFAULT NULL,
  `snapshoot` json DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXnhqhc5nio1ecua43k32q6v6k6` (`user_account_id`),
  KEY `IDX6vvfa0jbcjn9p65lgametvh98` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 payment_entitlement
# ------------------------------------------------------------

CREATE TABLE `payment_entitlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `active` bit(1) NOT NULL,
  `feature` enum('EXPORT_SUMMARY_NO_WATERMARK','EXPORT_SUMMARY_WITH_WATERMARK','IMAGE_CHAT','REAL_TIME_WEB_ACCESS','TEXT_CHAT','VIDEO_DEEP_SUMMARY','VIDEO_SUMMARY') NOT NULL,
  `num` bigint DEFAULT NULL,
  `price` varchar(255) NOT NULL,
  `svip_price` varchar(255) NOT NULL,
  `vip_price` varchar(255) NOT NULL,
  `platform` enum('IOS','WEB') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKibd20jw8r0betwbygw4x8ru` (`price`,`feature`),
  UNIQUE KEY `UKkw9mhgk6ad8v3hj8nx8vvm84s` (`platform`,`price`,`feature`),
  KEY `IDXpb5irssd4nvc0hmf2f14ssepf` (`price`),
  KEY `IDXru2fiv0gelc4b8wu9w6infm2h` (`vip_price`),
  KEY `IDXsue3iq6s9evrudfkpydtwpohc` (`svip_price`)
) ENGINE=InnoDB AUTO_INCREMENT=367 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 plan_entitlement
# ------------------------------------------------------------

CREATE TABLE `plan_entitlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `active` bit(1) NOT NULL,
  `feature` enum('EXPORT_SUMMARY_NO_WATERMARK','EXPORT_SUMMARY_WITH_WATERMARK','IMAGE_CHAT','REAL_TIME_WEB_ACCESS','TEXT_CHAT','VIDEO_DEEP_SUMMARY','VIDEO_SUMMARY','FILE_STORAGE') DEFAULT NULL,
  `the_interval` enum('DAY','MONTH','WEEK','YEAR') DEFAULT NULL,
  `usage_limit` bigint DEFAULT NULL,
  `plan` varchar(255) DEFAULT NULL,
  `platform` enum('IOS','WEB') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKf1mk14xngocqrns7a1nar2sd3` (`plan`,`feature`),
  UNIQUE KEY `UKin1g004isqj3961ma3ub6qj9` (`platform`,`plan`,`feature`),
  KEY `IDXlom5vmsd0qshpxt34te4947su` (`plan`)
) ENGINE=InnoDB AUTO_INCREMENT=556 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 strategy_rule
# ------------------------------------------------------------

CREATE TABLE `strategy_rule` (
  `id` varchar(255) NOT NULL,
  `priority` int DEFAULT NULL,
  `black_list_exclude_chat` json DEFAULT NULL,
  `black_list_strategy_exclude_chat` varchar(255) DEFAULT NULL,
  `default_strategy` varchar(255) NOT NULL,
  `platform` varchar(255) NOT NULL,
  `white_list_exclude_chat` json DEFAULT NULL,
  `white_list_strategy_exclude_chat` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 subscription
# ------------------------------------------------------------

CREATE TABLE `subscription` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `cancel_at` bigint DEFAULT NULL,
  `cancel_at_period_end` bit(1) DEFAULT NULL,
  `checkout_session_id` varchar(255) DEFAULT NULL,
  `current_period_end` bigint DEFAULT NULL,
  `current_period_start` bigint DEFAULT NULL,
  `customer_id` varchar(255) NOT NULL,
  `latest_invoice` varchar(255) DEFAULT NULL,
  `latest_invoice_status` varchar(255) DEFAULT NULL,
  `plan` varchar(255) DEFAULT NULL,
  `request_cancel_at` bigint DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stripe_id` varchar(255) DEFAULT NULL,
  `trial_end` bigint DEFAULT NULL,
  `trial_start` bigint DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `platform` enum('IOS','WEB') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKb0x9r9nv5k1lqfqew2jfxlm3y` (`user_account_id`),
  UNIQUE KEY `UK25s692po480f2ggj5gkgks5rn` (`customer_id`),
  KEY `IDXlh45n72a8s6l55lm924ddamxr` (`stripe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 suggestion_feedback
# ------------------------------------------------------------

CREATE TABLE `suggestion_feedback` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `plain_text` text,
  `rich_text` text,
  `type` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `details` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX7k4aan5rwjxv6ghsxtv1g70rc` (`user_id`),
  KEY `IDXkc17dnwtjup0oxirc8futbq49` (`username`),
  KEY `IDXaib22qp66ja50hqyejil1c02l` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 system_config
# ------------------------------------------------------------

CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `conf_key` varchar(255) NOT NULL,
  `conf_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `value_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs1m1uqu5cn06tudbkjfxte49s` (`conf_key`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 system_version
# ------------------------------------------------------------

CREATE TABLE `system_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `cur_version` varchar(255) NOT NULL,
  `cur_version_val` bigint NOT NULL,
  `download_address` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `update_content` varchar(255) DEFAULT NULL,
  `update_type` enum('FORCE','NON_FORCE','NON_NEED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbetcxp467mdlagq1rp7yplpsg` (`cur_version_val`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 team
# ------------------------------------------------------------

CREATE TABLE `team` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `admin_account_id` varchar(255) DEFAULT NULL,
  `admin_email` varchar(255) DEFAULT NULL,
  `init_deep_video_summary_limit` bigint DEFAULT NULL,
  `init_entitlement_valid_period` bigint DEFAULT NULL,
  `init_text_chat_limit` bigint DEFAULT NULL,
  `init_video_summary_limit` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_count_limit` int DEFAULT NULL,
  `deep_video_summary_limit` bigint DEFAULT NULL,
  `entitlement_valid_period` bigint DEFAULT NULL,
  `text_chat_limit` bigint DEFAULT NULL,
  `video_summary_limit` bigint DEFAULT NULL,
  `internal_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5l82o0t08gk130nfbp300qkp2` (`admin_email`),
  UNIQUE KEY `UKjv0e3fcxcabp9rg509a3g2wg3` (`admin_account_id`),
  UNIQUE KEY `UKg2l9qqsoeuynt4r5ofdt1x2td` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 team_user
# ------------------------------------------------------------

CREATE TABLE `team_user` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `is_admin` bit(1) NOT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','BLOCKED','PENDING') DEFAULT NULL,
  `team_id` varchar(255) DEFAULT NULL,
  `user_account_id` varchar(255) DEFAULT NULL,
  `deep_video_summary_limit` bigint DEFAULT NULL,
  `text_chat_limit` bigint DEFAULT NULL,
  `video_summary_limit` bigint DEFAULT NULL,
  `edcity_user` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjaomj7q6mnoc0bkcshkxp0ux1` (`team_id`,`email`),
  UNIQUE KEY `UKrinb9fw1jrsmhukefi111voyr` (`user_account_id`),
  KEY `IDXde9x6bc3lw2hv9nr3vvviwd7w` (`team_id`),
  KEY `IDX8af7q1ntivf9d46q907c04lx2` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 uninstall_reason
# ------------------------------------------------------------

CREATE TABLE `uninstall_reason` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `reasons` json DEFAULT NULL,
  `details` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_account
# ------------------------------------------------------------

CREATE TABLE `user_account` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `inviter_code` varchar(255) DEFAULT NULL,
  `locked` bit(1) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `signup_ip` varchar(255) DEFAULT NULL,
  `third_party_id` varchar(255) DEFAULT NULL,
  `type` enum('DEFAULT','OAUTH2_FACEBOOK','OAUTH2_GITHUB','OAUTH2_GOOGLE','OAUTH2_EDCITY') NOT NULL,
  `username` varchar(255) NOT NULL,
  `platform` enum('IOS','WEB') DEFAULT NULL,
  `internal_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhl02wv5hym99ys465woijmfib` (`email`),
  UNIQUE KEY `UKod5wr1qyul6adh9oajxmpsovs` (`third_party_id`),
  KEY `IDXcastjbvpeeus0r8lbpehiu0e4` (`username`),
  KEY `IDXm6e706cno3luf4r4mq8gjcwll` (`inviter_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_activity
# ------------------------------------------------------------

CREATE TABLE `user_activity` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `activity_key` varchar(255) DEFAULT NULL,
  `admin_data` json DEFAULT NULL,
  `data` json DEFAULT NULL,
  `end_time` bigint DEFAULT NULL,
  `last_reward_time` bigint DEFAULT NULL,
  `phase_progress` int DEFAULT NULL,
  `phase_rewards` json DEFAULT NULL,
  `rewards` json DEFAULT NULL,
  `start_time` bigint DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `valid_count` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK38p6o4e9a8doxi665tyhg0hia` (`user_account_id`,`activity_key`),
  KEY `IDX8pu68fvc2nj89grgxitllpntt` (`user_account_id`),
  KEY `IDXti95ib0wtf0gr8ps41ldo11yi` (`activity_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_entitlement
# ------------------------------------------------------------

CREATE TABLE `user_entitlement` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `entitlements` json NOT NULL,
  `user_account_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKaf9wv026udbtt21bwevkgohoy` (`user_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_portrait
# ------------------------------------------------------------

CREATE TABLE `user_portrait` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `tags` json DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9a2k4iyg9ple25xox7vjeq42o` (`user_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_profile
# ------------------------------------------------------------

CREATE TABLE `user_profile` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `invitation_code` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK71obh3wae6hyns1a0bx1roufy` (`user_account_id`),
  UNIQUE KEY `UKllyy73p0k58t791w174gtsmw2` (`invitation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 video_find_clip
# ------------------------------------------------------------

CREATE TABLE `video_find_clip` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `clips` json DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `link` varchar(10240) NOT NULL,
  `outcome` bigint DEFAULT NULL,
  `query` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXjn0d192bguspelpfx0x53r9xi` (`user_id`,`identity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 video_mindmap
# ------------------------------------------------------------

CREATE TABLE `video_mindmap` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `completion_id` varchar(255) NOT NULL,
  `feedback` json DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `link` varchar(10240) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjfe50tn4vqwy1xpll1woysjy0` (`user_id`,`identity`),
  KEY `IDXr9lltpyb6prphc12fttsc3rrd` (`user_id`),
  KEY `IDX75u5p5xwr9jq9w5lx8bqdmut9` (`identity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 video_summary
# ------------------------------------------------------------

CREATE TABLE `video_summary` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `completion_id` varchar(255) NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `feedback` json DEFAULT NULL,
  `highlights` json DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `link` varchar(10240) NOT NULL,
  `summary` text,
  `title` text,
  `user_id` varchar(255) NOT NULL,
  `video_id` varchar(255) NOT NULL,
  `video_source` varchar(255) NOT NULL,
  `video_thumbnail` varchar(255) DEFAULT NULL,
  `outcome` bigint DEFAULT NULL,
  `tags` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtgtunp2y9ixol1am7y5rdusob` (`user_id`,`identity`),
  KEY `IDXc0hf7bmrmsf8oxlpfuc6wctm4` (`user_id`),
  KEY `IDXgfvppndekwvjmbc2f6kbshulg` (`identity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 video_summary_change_log
# ------------------------------------------------------------

CREATE TABLE `video_summary_change_log` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `changed` json DEFAULT NULL,
  `completion_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_summary_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXpug6hdxokv3h1uo2o1beqlb8t` (`user_id`),
  KEY `IDXobx3edc9gnnkvf8s4ck5ey6kq` (`video_summary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 waiting_list
# ------------------------------------------------------------

CREATE TABLE `waiting_list` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `age` varchar(255) NOT NULL,
  `approved` bit(1) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `gender` varchar(255) NOT NULL,
  `how_hear` varchar(255) NOT NULL,
  `most_like_feature` text,
  `name` varchar(255) NOT NULL,
  `occupation` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmv321lcjbnr47y9pljn3tkl9y` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
