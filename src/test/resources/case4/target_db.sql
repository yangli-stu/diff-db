# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20077
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: localhost (MySQL 8.0.46-0ubuntu0.24.04.2)
# 数据库: vh_db_v4
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
) ENGINE=InnoDB AUTO_INCREMENT=312 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 ai_broll_generate
# ------------------------------------------------------------

CREATE TABLE `ai_broll_generate` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `error_message` varchar(1024) DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  `moment_id` varchar(255) NOT NULL,
  `request` json DEFAULT NULL,
  `resource_type` varchar(255) DEFAULT NULL,
  `results` json DEFAULT NULL,
  `retry_count` int DEFAULT NULL,
  `status` enum('DONE','ERROR','IN_PROGRESS','START','WAIT_ASYNC') DEFAULT NULL,
  `transaction_identities` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX3r84k47l72jbfsqhricotsgg` (`user_id`),
  KEY `IDX45eys8ieu17porj233lcg1kqp` (`moment_id`),
  KEY `IDXqms86gtx6shp1s9xbcmyqkllh` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 ai_speech_enhance
# ------------------------------------------------------------

CREATE TABLE `ai_speech_enhance` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `resource_item` json DEFAULT NULL,
  `enhance_audio_url` varchar(1024) DEFAULT NULL,
  `error_code` varchar(255) DEFAULT NULL,
  `error_message` varchar(1024) DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  `moment_id` varchar(255) NOT NULL,
  `origin_audio_url` varchar(1024) DEFAULT NULL,
  `status` enum('DONE','ERROR','IN_PROGRESS','START','WAIT_ASYNC') DEFAULT NULL,
  `task_context` json DEFAULT NULL,
  `transaction_identities` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXd3tsbq0u7jn1k8gh8c7a9pr8s` (`user_id`),
  KEY `IDXmrq52l3lynul3di132skxv33n` (`moment_id`),
  KEY `IDXirulpeks245hfiknh8hm6241x` (`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 api_key
# ------------------------------------------------------------

CREATE TABLE `api_key` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `expire_at` bigint DEFAULT NULL,
  `sk` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtepsl6cddhci3qiim6tiqvgri` (`sk`),
  KEY `IDXigm287ynn3c0yfn41xiy8ittm` (`user_id`)
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
  `login_platform` enum('IOS','WEB','PLUGIN') DEFAULT NULL,
  `login_type` enum('DEFAULT','OAUTH2') NOT NULL,
  `logout_timestamp` bigint DEFAULT NULL,
  `refresh_expires_at` bigint DEFAULT NULL,
  `refresh_token` text,
  `refresh_token_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXsymrxsvvh79vfopjf9nbd7nth` (`user_id`,`login_platform`),
  KEY `IDXc01qxburbkfgaprt72fjqdl52` (`username`),
  KEY `IDXrplgur5vo3j3qcebg0ph8ubya` (`access_token_id`),
  KEY `IDX1kuv4nu4e2nqepedmm4deweq5` (`refresh_token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 auto_top_up_attempt
# ------------------------------------------------------------

CREATE TABLE `auto_top_up_attempt` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `failure_message` varchar(255) DEFAULT NULL,
  `finished_at` bigint DEFAULT NULL,
  `price` varchar(255) NOT NULL,
  `status` enum('CREATED','FAILED','PAID') NOT NULL,
  `stripe_invoice_id` varchar(255) DEFAULT NULL,
  `stripe_payment_intent_id` varchar(255) DEFAULT NULL,
  `trigger_balance` bigint NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXhl4g3dcmxwer1rp1qg9nvad4c` (`user_id`),
  KEY `IDX948ovyut7d13bd6ajgtsfa2lo` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 auto_top_up_config
# ------------------------------------------------------------

CREATE TABLE `auto_top_up_config` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `consecutive_failure_count` int NOT NULL,
  `last_setup_session_id` varchar(255) DEFAULT NULL,
  `monthly_limit_amount` bigint DEFAULT NULL,
  `price` varchar(255) NOT NULL,
  `status` enum('ACTIVE','DISABLED','PAYMENT_FAILED','PENDING_SETUP') NOT NULL,
  `stripe_payment_method_id` varchar(255) DEFAULT NULL,
  `threshold_credits` bigint NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcj5l1o7q3ejyvckuomi5imf9y` (`user_id`),
  KEY `IDX3h08f6ba51c3w4vlcsgqyjd8u` (`status`)
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
  `thinking_assistant_text` text,
  `thinking_elapsed_secs` int DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `deep_qa_finish` bit(1) DEFAULT NULL,
  `deep_qa_idx` int DEFAULT NULL,
  `deep_qa_text` varchar(255) DEFAULT NULL,
  `mode` varchar(255) DEFAULT 'normal',
  `video_identity` varchar(255) DEFAULT NULL,
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
  `expired_time` bigint DEFAULT NULL,
  `mode` varchar(255) DEFAULT 'normal',
  PRIMARY KEY (`id`),
  KEY `IDXbtiycf7at8q98lm757jq7nqrh` (`user_id`),
  KEY `IDXm2jq13en6vr6dcu9isgdd0l8m` (`video_summary_id`),
  KEY `IDX5ltksk2er1tbnytc7e06kobk` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 chat_shared
# ------------------------------------------------------------

CREATE TABLE `chat_shared` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `chat_session_id` varchar(255) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `snapshoot` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXeqdxi8lrq084h795ybu7fgvc0` (`user_id`),
  KEY `IDX2e53aqryh35kfapqkpx8r2ffn` (`chat_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 contact_us
# ------------------------------------------------------------

CREATE TABLE `contact_us` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `business_email` varchar(255) DEFAULT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  `company_size` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `company_social_channel` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 customer
# ------------------------------------------------------------

CREATE TABLE `customer` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `email` varchar(255) NOT NULL,
  `last_checkout_session_id` varchar(255) DEFAULT NULL,
  `type` enum('FREE','LAPSED_SUBSCRIPTION','PAYMENT','SUBSCRIPTION','SUBSCRIPTION_AND_PAYMENT') NOT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `pay_platform` enum('APPLE','STRIPE') NOT NULL,
  `pay_platform_uid` varchar(255) NOT NULL,
  `stripe_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `apple_account_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK628gcpp38b55pudvgmlikmxhx` (`user_account_id`,`pay_platform`),
  KEY `IDX4qm52n85kwhmxj2nktmj1kv9h` (`user_account_id`),
  KEY `IDXdwk6cx0afu8bs9o4t536v1j5v` (`email`)
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
  `highlights` json DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `idx` int DEFAULT NULL,
  `link` varchar(10240) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXpa4isj77b7ldxht9mgqh65fuu` (`user_id`),
  KEY `IDX7apn5tvqvawx7on7aulg5qud9` (`identity`),
  KEY `IDXlv8a47a7m2pw61f1rjxgtc29t` (`video_summary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 edu_apply_record
# ------------------------------------------------------------

CREATE TABLE `edu_apply_record` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `verified` bit(1) NOT NULL,
  `verified_email` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnm3sp1nd91wf2v3p2wwsjpiml` (`user_account_id`),
  UNIQUE KEY `UKs98fgbvw9m8l7kk24i6d0jibo` (`verified_email`),
  KEY `IDXrmcdkcbdp54wl2drmlhlgouy` (`verified`),
  KEY `IDXfcad5gdgiu0221qbf5ajn2j0g` (`created`)
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
  `name` varchar(255) DEFAULT NULL,
  `origin_name` varchar(255) DEFAULT NULL,
  `rex` varchar(255) DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `status` enum('DONE','ERROR','UPLOADED','EXPIRE','FINALLY','START') DEFAULT NULL,
  `upload_end_time` bigint DEFAULT NULL,
  `upload_start_time` bigint DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `identify` varchar(255) NOT NULL,
  `resource_type` varchar(255) DEFAULT NULL,
  `meta` json DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  `tmp_resource_file` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `IDXokbli2mrhio8dllvr0hc9sga7` (`user_id`,`file_source`,`checksum`),
  KEY `IDX6sgds571444bg674gpqfk6jw9` (`user_id`,`identify`),
  KEY `IDXguuvumhgj98cbplqx5ov72bvi` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 generate_task
# ------------------------------------------------------------

CREATE TABLE `generate_task` (
  `task_id` varchar(64) NOT NULL,
  `created_at` bigint NOT NULL,
  `duration` bigint DEFAULT NULL,
  `error_code` varchar(32) DEFAULT NULL,
  `error_msg` varchar(512) DEFAULT NULL,
  `extra` text,
  `input` text,
  `output` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `priority` int NOT NULL,
  `status` varchar(32) NOT NULL,
  `task_type` varchar(32) NOT NULL,
  `timeout_at` bigint DEFAULT NULL,
  `trace_id` varchar(64) DEFAULT NULL,
  `updated_at` bigint NOT NULL,
  `user_id` varchar(64) NOT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_generate_task_user_created` (`user_id`,`created_at`),
  KEY `idx_generate_task_status_created` (`status`,`created_at`),
  KEY `idx_generate_task_status_updated` (`status`,`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 highlight_moment
# ------------------------------------------------------------

CREATE TABLE `highlight_moment` (
  `id` varchar(255) NOT NULL,
  `data` json DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `event` varchar(255) DEFAULT NULL,
  `favorite` bit(1) NOT NULL,
  `feedback` json DEFAULT NULL,
  `idx` int NOT NULL,
  `hm_task_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `origin_begin_time` varchar(255) DEFAULT NULL,
  `origin_end_time` varchar(255) DEFAULT NULL,
  `default_moment` bit(1) DEFAULT NULL,
  `url_source` enum('KEYWORD','URL') DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  `video_platform` varchar(255) DEFAULT NULL,
  `clipped` bit(1) DEFAULT NULL,
  `act_as` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6y9ren040ilm0oibobimlfwpb` (`hm_task_id`,`idx`),
  KEY `IDXm0c083qo5yc30m8dksbv4q7fg` (`user_id`),
  KEY `IDXjqpmvhqjcpg1ylpqwvsx8v02f` (`hm_task_id`),
  KEY `IDX87uc5jqi00o1whj085s31ym2t` (`last_modified`),
  KEY `IDX6pgn7li6kmf9wx8gwfvyqbbjc` (`user_id`,`clipped`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 highlight_moment_canvas
# ------------------------------------------------------------

CREATE TABLE `highlight_moment_canvas` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `bboxes_src` json DEFAULT NULL,
  `cc_aligned` json DEFAULT NULL,
  `cc_style` json DEFAULT NULL,
  `end_ms` bigint DEFAULT NULL,
  `end_ms_boundary` bigint DEFAULT NULL,
  `full_video_stored` varchar(2048) DEFAULT NULL,
  `moment_id` varchar(255) NOT NULL,
  `ratio` enum('RATIO_16_9','RATIO_1_1','RATIO_4_5','RATIO_9_16','RATIO_ORIGINAL') DEFAULT NULL,
  `ratio_bboxes` json DEFAULT NULL,
  `rendered_video_stored` varchar(10240) DEFAULT NULL,
  `start_ms` bigint DEFAULT NULL,
  `start_ms_boundary` bigint DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `rendered_version` varchar(255) DEFAULT NULL,
  `cc_source_lang` varchar(255) DEFAULT NULL,
  `cc_target_lang` varchar(255) DEFAULT NULL,
  `caption_display` varchar(255) DEFAULT NULL,
  `meta` json DEFAULT NULL,
  `data_version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfcknnqfl8q1im0ogv4n8aog0m` (`moment_id`),
  KEY `IDX3sxtwecw3yg620iejdljkmqsd` (`user_id`),
  KEY `IDXfcknnqfl8q1im0ogv4n8aog0m` (`moment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 highlight_moment_task
# ------------------------------------------------------------

CREATE TABLE `highlight_moment_task` (
  `id` varchar(255) NOT NULL,
  `default_task` bit(1) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `outcome` bigint DEFAULT NULL,
  `status` enum('DONE','ERROR','IN_PROGRESS','START','WAIT_ASYNC') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_meta` json NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `completed_cost` bigint DEFAULT NULL,
  `first_moment_cost` bigint DEFAULT NULL,
  `error_count` int DEFAULT NULL,
  `url_source` enum('KEYWORD','URL') DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  `video_platform` varchar(255) DEFAULT NULL,
  `error_code` varchar(255) DEFAULT NULL,
  `ratio` enum('RATIO_16_9','RATIO_1_1','RATIO_4_5','RATIO_9_16','RATIO_ORIGINAL') DEFAULT NULL,
  `moment_count` int DEFAULT NULL,
  `query` varchar(6000) DEFAULT NULL,
  `root_task_id` varchar(255) DEFAULT NULL,
  `outcomes` json DEFAULT NULL,
  `full_video_stored` varchar(2048) DEFAULT NULL,
  `cc_aligned` json DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `source_language` varchar(255) DEFAULT NULL,
  `caption_display` varchar(255) DEFAULT NULL,
  `act_as` varchar(255) DEFAULT NULL,
  `summary_meta` json DEFAULT NULL,
  `full_audio_stored` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXbgs3bvr41jr9sh84nc70lytmq` (`user_id`),
  KEY `IDX61eawnslixcgesngdr06phqla` (`identity`),
  KEY `IDXitq307eoqg6ukue2ets0gk4h` (`last_modified`),
  KEY `IDXrenjolilb7b4bity9ls817su3` (`root_task_id`),
  KEY `IDXilwcf0fca0usayexrbliy1kjw` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 highlight_task_caption
# ------------------------------------------------------------

CREATE TABLE `highlight_task_caption` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `caption_display` varchar(255) DEFAULT NULL,
  `cc_aligned` json DEFAULT NULL,
  `cc_aligned_speakers` json DEFAULT NULL,
  `cc_source_lang` varchar(255) DEFAULT NULL,
  `cc_target_lang` varchar(255) DEFAULT NULL,
  `origin_cc_aligned` json DEFAULT NULL,
  `task_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsldh286d48okpfi7m0syacu2u` (`task_id`),
  KEY `IDX2r5dgg7hlqdh2cl67d5pcemlg` (`user_id`),
  KEY `IDXsldh286d48okpfi7m0syacu2u` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 hm_cc_translation
# ------------------------------------------------------------

CREATE TABLE `hm_cc_translation` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `cc` text NOT NULL,
  `hm_id` varchar(255) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `source_cc` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXbwk0blnsh4q7ygte5318o9ekx` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 hm_download_log
# ------------------------------------------------------------

CREATE TABLE `hm_download_log` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `begin_time` varchar(255) DEFAULT NULL,
  `cost` bigint DEFAULT NULL,
  `end_time` varchar(255) DEFAULT NULL,
  `hm_id` varchar(255) NOT NULL,
  `identity` varchar(255) NOT NULL,
  `res` varchar(255) DEFAULT NULL,
  `source_ip` varchar(255) DEFAULT NULL,
  `status` enum('DONE','ERROR','IN_PROGRESS','START') NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXnrqm8178akqearla2b7t7jyvq` (`user_id`),
  KEY `IDXluv7ka2kiuxfeb2icev8ivar0` (`hm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 hm_shared
# ------------------------------------------------------------

CREATE TABLE `hm_shared` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `hm_id` varchar(255) DEFAULT NULL,
  `snapshot` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `origin_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXqyscbsvi1my1im7l0igi08j28` (`user_id`),
  KEY `IDXnt1045vf9m1hxf9hel408355t` (`hm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 marketing_unsubscribe
# ------------------------------------------------------------

CREATE TABLE `marketing_unsubscribe` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `email` varchar(255) NOT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs49kilsffmx1ct1vjm2bskxyp` (`email`),
  UNIQUE KEY `UKd8c3adn480g5ommf1li8iyd3v` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



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
  `saved` bit(1) DEFAULT NULL,
  `summary` text,
  `summary_display` bit(1) NOT NULL,
  `thumbnail` varchar(255) DEFAULT NULL,
  `title` text,
  `user_account_id` varchar(255) NOT NULL,
  `link` varchar(10240) NOT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
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
  `deleted` bit(1) DEFAULT NULL,
  `note_id` varchar(255) DEFAULT NULL,
  `snapshoot` json DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXnhqhc5nio1ecua43k32q6v6k6` (`user_account_id`),
  KEY `IDX6vvfa0jbcjn9p65lgametvh98` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 notify_log
# ------------------------------------------------------------

CREATE TABLE `notify_log` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `data` json DEFAULT NULL,
  `receiver` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `viewed` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX3qfxp7q78hre1101w8r3blbqy` (`receiver`,`viewed`,`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 payment_entitlement
# ------------------------------------------------------------

CREATE TABLE `payment_entitlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `active` bit(1) NOT NULL,
  `feature` enum('CANVAS_EXPORT','CREATE_NOTE','CREDIT','FILE_STORAGE','IMAGE_CHAT','TEXT_CHAT','VH_CAPTION_TRANSLATION','VH_CC_OR_REFRAME','VIDEO_DEEP_SUMMARY','VIDEO_HIGHLIGHT','VIDEO_HIGHLIGHT_QUERY','VIDEO_SUMMARY','VIDEO_SUMMARY_QUERY','ANIMATED_VIDEO_HIGHLIGHT','FREE_TOOL_CREDIT','SUMMARY_CREDIT','API_CREDIT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `num` bigint DEFAULT NULL,
  `platform` enum('IOS','WEB','API') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `price` varchar(255) NOT NULL,
  `svip_price` varchar(255) NOT NULL,
  `vip_price` varchar(255) NOT NULL,
  `ssvip_price` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkw9mhgk6ad8v3hj8nx8vvm84s` (`platform`,`price`,`feature`),
  KEY `IDXpb5irssd4nvc0hmf2f14ssepf` (`price`),
  KEY `IDXru2fiv0gelc4b8wu9w6infm2h` (`vip_price`),
  KEY `IDXsue3iq6s9evrudfkpydtwpohc` (`svip_price`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 payment_record
# ------------------------------------------------------------

CREATE TABLE `payment_record` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `currency` varchar(255) NOT NULL,
  `customer_id` varchar(255) NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `pay_platform` enum('APPLE','STRIPE','WAYIN_STRIPE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` bigint NOT NULL,
  `product_id` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `transaction_id` varchar(255) NOT NULL,
  `unknown_fields` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `entitlements` json DEFAULT NULL,
  `unit_amount` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhyj6px0ykk0horexqyrac3y4j` (`pay_platform`,`transaction_id`),
  KEY `IDXld6v33ysh0ttfvnjwg83p1aq1` (`user_id`),
  KEY `IDXdw0y47muuelj7rfwoatljfcfm` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 plan_entitlement
# ------------------------------------------------------------

CREATE TABLE `plan_entitlement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `active` bit(1) NOT NULL,
  `feature` enum('CANVAS_EXPORT','CREATE_NOTE','CREDIT','FILE_STORAGE','IMAGE_CHAT','TEXT_CHAT','VH_CAPTION_TRANSLATION','VH_CC_OR_REFRAME','VIDEO_DEEP_SUMMARY','VIDEO_HIGHLIGHT','VIDEO_HIGHLIGHT_QUERY','VIDEO_SUMMARY','VIDEO_SUMMARY_QUERY','ANIMATED_VIDEO_HIGHLIGHT','FREE_TOOL_CREDIT','SUMMARY_CREDIT') DEFAULT NULL,
  `the_interval` enum('DAY','MONTH','WEEK','YEAR') DEFAULT NULL,
  `usage_limit` bigint DEFAULT NULL,
  `plan` varchar(255) DEFAULT NULL,
  `platform` enum('IOS','WEB','PLUGIN') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKin1g004isqj3961ma3ub6qj9` (`platform`,`plan`,`feature`),
  KEY `IDXlom5vmsd0qshpxt34te4947su` (`plan`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 platform_publish_task
# ------------------------------------------------------------

CREATE TABLE `platform_publish_task` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `error_code` varchar(64) DEFAULT NULL,
  `error_message` text,
  `platform` enum('tiktok','twitter','youtube','instagram','facebook','linkedin') NOT NULL,
  `platform_video_id` varchar(128) DEFAULT NULL,
  `platform_video_url` varchar(512) DEFAULT NULL,
  `publish_config` json NOT NULL,
  `publish_task_id` varchar(255) NOT NULL,
  `published_at` bigint DEFAULT NULL,
  `retry_count` int DEFAULT NULL,
  `social_account_id` varchar(255) NOT NULL,
  `status` enum('CANCELLED','DONE','ERROR','PARTIAL_DONE','PENDING','PROCESSING','SCHEDULED') NOT NULL,
  `attempt_id` varchar(64) DEFAULT NULL,
  `account_avatar` varchar(1024) DEFAULT NULL,
  `account_name` varchar(256) DEFAULT NULL,
  `error_details` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXcaebgkq7uein45cw7rg46okln` (`publish_task_id`),
  KEY `IDXso2mbpjrd0qv6hknh2q6lakwc` (`social_account_id`,`status`),
  KEY `IDXd3pgbdr7t808v905cmjbbqytn` (`status`,`last_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 project
# ------------------------------------------------------------

CREATE TABLE `project` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `status` enum('CREATED','FAILED','ONGOING','QUEUED','SUCCEEDED') NOT NULL,
  `task_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_identity` varchar(255) NOT NULL,
  `error_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX60d0g900v88hwu1mfng1nbewq` (`user_id`),
  KEY `IDXem5ci6g3oogacwiedbl1lnsbj` (`task_id`),
  KEY `IDXptxroq6kjlj0et9p6ewphppt8` (`video_identity`),
  KEY `IDXcx0cmcaja1raaqx4whso3vb48` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 project_job
# ------------------------------------------------------------

CREATE TABLE `project_job` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `data` json DEFAULT NULL,
  `error_code` varchar(255) DEFAULT NULL,
  `moment_id` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `task_id` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_meta` json NOT NULL,
  `expired_time` bigint DEFAULT NULL,
  `system_job` bit(1) DEFAULT b'0',
  `deleted` bit(1) DEFAULT b'0',
  `retry_params` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXmxcyliafc42xqhahrcmftgrcs` (`user_id`,`type`,`last_modified`),
  KEY `IDXg2hf88ty5j4y745huckdcjmqv` (`task_id`),
  KEY `IDXkosrt0dav76g9qn1ypjheg59c` (`moment_id`),
  KEY `IDXm1coea1tm7s59x67d435qg3lc` (`status`),
  KEY `idx_user_system_deleted_last` (`user_id`,`system_job`,`deleted`,`last_modified` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 project_template
# ------------------------------------------------------------

CREATE TABLE `project_template` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `caption_display` enum('both','original','translation') DEFAULT NULL,
  `enable_ai_reframe` bit(1) DEFAULT NULL,
  `enable_caption` bit(1) DEFAULT NULL,
  `enable_more_results` bit(1) DEFAULT NULL,
  `image_overlays` json DEFAULT NULL,
  `ratio` enum('RATIO_16_9','RATIO_1_1','RATIO_4_5','RATIO_9_16') DEFAULT NULL,
  `resolution` enum('FHD_1080','HD_720','SD_480') DEFAULT NULL,
  `source_lang` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  `target_lang` varchar(255) DEFAULT NULL,
  `text_overlays` json DEFAULT NULL,
  `unique_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8trdymp822g4qco3e80evmb8r` (`unique_id`,`user_id`),
  KEY `IDXdrs9p4nrgbnxajj388jk6nw05` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 publish_schedule
# ------------------------------------------------------------

CREATE TABLE `publish_schedule` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `publish_task_id` varchar(255) DEFAULT NULL,
  `scheduled_at` bigint NOT NULL,
  `status` enum('CANCELLED','EXECUTED','PENDING','SCHEDULED') NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXsp0bmsykf1tmse4x2t91e47fv` (`user_id`,`scheduled_at`),
  KEY `IDXp9bd434om66eovf8efp3l0l8d` (`publish_task_id`),
  KEY `IDX7squmd1wp2bmc8qc24m9r1sct` (`status`,`scheduled_at`),
  KEY `IDXtc1ye4n1d4feammm06k43lc71` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 publish_task
# ------------------------------------------------------------

CREATE TABLE `publish_task` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `export_job_id` varchar(255) DEFAULT NULL,
  `export_resolution` varchar(255) DEFAULT NULL,
  `moment_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_url` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXeqii5jty7a82r0jqbegwmbwus` (`user_id`,`created`),
  KEY `IDXqurtpqeq4ytonpojax56c2sa7` (`export_job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 social_media_account
# ------------------------------------------------------------

CREATE TABLE `social_media_account` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `access_token` text,
  `platform` enum('tiktok','twitter','youtube','instagram','facebook','linkedin') NOT NULL,
  `platform_avatar` varchar(512) DEFAULT NULL,
  `platform_user_id` varchar(255) DEFAULT NULL,
  `platform_username` varchar(255) DEFAULT NULL,
  `refresh_token` text,
  `scopes` json DEFAULT NULL,
  `token_expires_at` bigint DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `zernio_profile_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1po3th3jvoal7s6dydtaurgcx` (`platform_user_id`),
  KEY `IDXnes461psmdvp6dw4wcb688200` (`user_id`,`platform`),
  KEY `IDXk0kxm78b4xu55u5kcynywcuu8` (`token_expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 strategy_rule
# ------------------------------------------------------------

CREATE TABLE `strategy_rule` (
  `id` varchar(255) NOT NULL,
  `black_list_exclude_chat` json DEFAULT NULL,
  `black_list_strategy_exclude_chat` varchar(255) DEFAULT NULL,
  `default_strategy` varchar(255) NOT NULL,
  `platform` varchar(255) NOT NULL,
  `priority` int DEFAULT NULL,
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
  `latest_invoice` varchar(255) DEFAULT NULL,
  `latest_invoice_status` varchar(255) DEFAULT NULL,
  `plan` varchar(255) DEFAULT NULL,
  `platform` enum('IOS','WEB','PLUGIN') DEFAULT NULL,
  `request_cancel_at` bigint DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trial_end` bigint DEFAULT NULL,
  `trial_start` bigint DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `next_period_plan` varchar(255) DEFAULT NULL,
  `pay_platform` enum('APPLE','STRIPE') DEFAULT NULL,
  `pay_platform_sid` varchar(255) DEFAULT NULL,
  `last_renew_plan_entitlement` bigint DEFAULT NULL,
  `stripe_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `customer_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `reward_trial_plan` varchar(255) DEFAULT NULL,
  `reward_trial_plan_end` bigint DEFAULT NULL,
  `reward_trial_plan_start` bigint DEFAULT NULL,
  `quantity` bigint DEFAULT NULL,
  `next_period_quantity` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKb0x9r9nv5k1lqfqew2jfxlm3y` (`user_account_id`),
  KEY `IDXon4ul5j9y6jmt8ohkp8uufwjg` (`plan`)
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



# 转储表 system_coming_soon
# ------------------------------------------------------------

CREATE TABLE `system_coming_soon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `email` varchar(255) NOT NULL,
  `types` json DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX6ydlb7bjnx2ikwemg7cbu4we8` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 system_config
# ------------------------------------------------------------

CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `conf_key` varchar(255) NOT NULL,
  `conf_value` mediumtext NOT NULL,
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



# 转储表 thumbnail_generate
# ------------------------------------------------------------

CREATE TABLE `thumbnail_generate` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `expired_time` bigint DEFAULT NULL,
  `ratio` varchar(255) DEFAULT NULL,
  `ref_image_url` varchar(255) DEFAULT NULL,
  `results` json DEFAULT NULL,
  `task_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `status` enum('DONE','ERROR','IN_PROGRESS','START','WAIT_ASYNC') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXl4o5p4u096prcmqspp5iuo7rj` (`user_id`),
  KEY `IDXgcned1bpjaprdeed8ncg8p4cq` (`last_modified`),
  KEY `IDX1i9qjgfdaw20umjcsecedrrru` (`task_id`),
  KEY `idx_thumbnail_generate_user_task_created` (`user_id`,`task_id`,`created` DESC),
  KEY `idx_thumbnail_generate_task_created` (`task_id`,`created` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 uninstall_reason
# ------------------------------------------------------------

CREATE TABLE `uninstall_reason` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `details` json DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `reasons` json DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `signup_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



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
  `platform` enum('IOS','WEB','PLUGIN') DEFAULT NULL,
  `signup_ip` varchar(255) DEFAULT NULL,
  `third_party_id` varchar(255) DEFAULT NULL,
  `type` enum('DEFAULT','OAUTH2_APPLE','OAUTH2_FACEBOOK','OAUTH2_GITHUB','OAUTH2_GOOGLE','OAUTH2_TWITCH') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(255) NOT NULL,
  `internal_code` varchar(255) DEFAULT NULL,
  `from_wayin` bit(1) DEFAULT NULL,
  `format_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhl02wv5hym99ys465woijmfib` (`email`),
  UNIQUE KEY `UKod5wr1qyul6adh9oajxmpsovs` (`third_party_id`),
  KEY `IDXcastjbvpeeus0r8lbpehiu0e4` (`username`),
  KEY `IDXm6e706cno3luf4r4mq8gjcwll` (`inviter_code`),
  KEY `IDXtkwp80v7wol3mbo2lgnfr6fqw` (`format_email`)
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



# 转储表 user_asset
# ------------------------------------------------------------

CREATE TABLE `user_asset` (
  `asset_id` varchar(64) NOT NULL,
  `asset_type` varchar(32) NOT NULL,
  `created_at` bigint NOT NULL,
  `description` text,
  `duration` int NOT NULL,
  `expired_at` bigint DEFAULT NULL,
  `extra` text,
  `icon` varchar(512) DEFAULT NULL,
  `resource_url` varchar(4096) NOT NULL,
  `source_task_id` varchar(64) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `title` varchar(256) NOT NULL,
  `updated_at` bigint NOT NULL,
  `user_id` varchar(64) NOT NULL,
  PRIMARY KEY (`asset_id`),
  KEY `idx_user_asset_user_created` (`user_id`,`created_at`),
  KEY `idx_user_asset_user_type_created` (`user_id`,`asset_type`,`created_at`)
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



# 转储表 user_entitlement_log
# ------------------------------------------------------------

CREATE TABLE `user_entitlement_log` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `identity` varchar(255) NOT NULL,
  `outcomes` json DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `details` json DEFAULT NULL,
  `incomes` json DEFAULT NULL,
  `snapshot` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX2ttpb5w5yiv19imqhj57lkwb4` (`user_id`),
  KEY `IDXkfyobmaixik0d88jsm117tccp` (`identity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_invite_log
# ------------------------------------------------------------

CREATE TABLE `user_invite_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `be_invited` varchar(255) NOT NULL,
  `inviter` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX1fsdcdijuwp4fn43uth1r3shk` (`inviter`),
  KEY `IDX6o4wsgj1lly26oflua15u84p2` (`be_invited`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_portrait
# ------------------------------------------------------------

CREATE TABLE `user_portrait` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `tags` json DEFAULT NULL,
  `user_account_id` varchar(255) NOT NULL,
  `jobs` json DEFAULT NULL,
  `usages` json DEFAULT NULL,
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
  `zernio_profile_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK71obh3wae6hyns1a0bx1roufy` (`user_account_id`),
  UNIQUE KEY `UKllyy73p0k58t791w174gtsmw2` (`invitation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 user_quota
# ------------------------------------------------------------

CREATE TABLE `user_quota` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `project_concurrency` int DEFAULT NULL,
  `rate_limit_per_minute` int DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqcjethr6fuln6jvw0dgyej4aq` (`user_id`)
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



# 转储表 video_generate
# ------------------------------------------------------------

CREATE TABLE `video_generate` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `auto_prompt` bit(1) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `model_config` json DEFAULT NULL,
  `optimized_prompt` text,
  `original_prompt` text,
  `results` json DEFAULT NULL,
  `status` enum('DONE','ERROR','IN_PROGRESS','START','WAIT_ASYNC') DEFAULT NULL,
  `task_id` varchar(255) NOT NULL,
  `thinking` text,
  `user_id` varchar(255) NOT NULL,
  `vid` varchar(255) DEFAULT NULL,
  `error_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX1uauelsaukftm5a32by7ebtpo` (`user_id`),
  KEY `IDXch2q4tcnj7ux6fowuqn3hbwyt` (`task_id`),
  KEY `IDXs3iqlhlm0xldgt07mos8qfw76` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 video_highlight
# ------------------------------------------------------------

CREATE TABLE `video_highlight` (
  `id` varchar(255) NOT NULL,
  `created` bigint NOT NULL,
  `last_modified` bigint NOT NULL,
  `version` bigint NOT NULL,
  `expired_time` bigint DEFAULT NULL,
  `highlights` json DEFAULT NULL,
  `identity` varchar(255) NOT NULL,
  `language` varchar(255) DEFAULT NULL,
  `outcome` bigint DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `video_id` varchar(255) DEFAULT NULL,
  `video_task_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX6jo4fc8vlmti8q2es0mfqrhn8` (`user_id`),
  KEY `IDXmsfn2qdnbkesnvlo4xirhf3jb` (`identity`)
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
  `expired_time` bigint DEFAULT NULL,
  `video_summary_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
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
  `outcome` bigint DEFAULT NULL,
  `summary` text,
  `tags` json DEFAULT NULL,
  `title` text,
  `user_id` varchar(255) NOT NULL,
  `video_id` varchar(255) NOT NULL,
  `video_source` varchar(255) NOT NULL,
  `video_thumbnail` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `expired_time` bigint DEFAULT NULL,
  `video_task_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXc0hf7bmrmsf8oxlpfuc6wctm4` (`user_id`),
  KEY `IDXgfvppndekwvjmbc2f6kbshulg` (`identity`),
  KEY `IDX9142n675n0k402gt79iayb837` (`video_task_id`)
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
