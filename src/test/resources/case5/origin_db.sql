CREATE TABLE activity_reward_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  activity_key VARCHAR(255),
  details TEXT,
  rewards TEXT,
  user_account_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE activity_schedule (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  activity_key VARCHAR(255),
  auto_start BIT,
  description VARCHAR(255),
  display VARCHAR(255),
  open BIT,
  period_end_time BIGINT,
  period_start_time BIGINT,
  phase_rewards TEXT,
  reward_trigger VARCHAR(255),
  reward_trigger_params TEXT,
  rewards TEXT,
  valid_count BIGINT,
  valid_duration BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE authenticate (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  access_expires_at BIGINT,
  access_token TEXT,
  access_token_id VARCHAR(255),
  login_ip VARCHAR(255),
  login_platform VARCHAR(255),
  login_type VARCHAR(255) NOT NULL,
  logout_timestamp BIGINT,
  refresh_expires_at BIGINT,
  refresh_token TEXT,
  refresh_token_id VARCHAR(255),
  user_id VARCHAR(255),
  username VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE chat_conversation (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  assistant_msg_id VARCHAR(255),
  assistant_text TEXT,
  chat_context TEXT,
  completion_id VARCHAR(255) NOT NULL,
  deleted BIT,
  feedback TEXT,
  language VARCHAR(255),
  session_id VARCHAR(255) NOT NULL,
  thinking_assistant_text TEXT,
  thinking_elapsed_secs INT,
  user_id VARCHAR(255) NOT NULL,
  deep_qa_finish BIT,
  deep_qa_idx INT,
  deep_qa_text VARCHAR(255),
  mode VARCHAR(255) DEFAULT 'normal',
  video_identity VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE chat_session (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  deleted BIT,
  initial_context TEXT,
  ip_address VARCHAR(255),
  language VARCHAR(255),
  user_id VARCHAR(255) NOT NULL,
  video_summary_id VARCHAR(255),
  expired_time BIGINT,
  mode VARCHAR(255) DEFAULT 'normal',
  PRIMARY KEY (id)
);

CREATE TABLE chat_shared (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  author VARCHAR(255),
  chat_session_id VARCHAR(255),
  deleted BIT,
  duration BIGINT,
  snapshoot TEXT,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE contact_us (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  business_email VARCHAR(255),
  company_name VARCHAR(255),
  company_size VARCHAR(255),
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  message_content VARCHAR(255),
  company_social_channel VARCHAR(255),
  user_id VARCHAR(255),
  source VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE customer (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  email VARCHAR(255) NOT NULL,
  last_checkout_session_id VARCHAR(255),
  type VARCHAR(255) NOT NULL,
  user_account_id VARCHAR(255) NOT NULL,
  pay_platform VARCHAR(255),
  pay_platform_uid VARCHAR(255),
  stripe_id VARCHAR(255),
  apple_account_token VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE deep_video_summary (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  completion_id VARCHAR(255) NOT NULL,
  feedback TEXT,
  highlights TEXT,
  identity VARCHAR(255) NOT NULL,
  idx INT,
  link VARCHAR(10240) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  video_summary_id VARCHAR(255),
  expired_time BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE external_file (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  checksum VARCHAR(255),
  file_source VARCHAR(255) NOT NULL,
  name VARCHAR(255),
  origin_name VARCHAR(255),
  rex VARCHAR(255),
  size BIGINT,
  status VARCHAR(255),
  upload_end_time BIGINT,
  upload_start_time BIGINT,
  url VARCHAR(255),
  user_id VARCHAR(255) NOT NULL,
  identify VARCHAR(255) NOT NULL,
  meta TEXT,
  resource_type VARCHAR(255),
  expired_time BIGINT,
  tmp_resource_file BIT DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE highlight_moment (
  id VARCHAR(255) NOT NULL,
  data TEXT,
  deleted BIT,
  event VARCHAR(255),
  favorite BIT NOT NULL,
  feedback TEXT,
  idx INT NOT NULL,
  hm_task_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  origin_begin_time VARCHAR(255),
  origin_end_time VARCHAR(255),
  default_moment BIT,
  url_source VARCHAR(255),
  expired_time BIGINT,
  video_platform VARCHAR(255),
  clipped BIT,
  act_as VARCHAR(255),
  task_info TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE highlight_moment_canvas (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  bboxes_src TEXT,
  cc_aligned TEXT,
  cc_style TEXT,
  end_ms BIGINT,
  end_ms_boundary BIGINT,
  full_video_stored VARCHAR(255),
  moment_id VARCHAR(255) NOT NULL,
  ratio VARCHAR(255),
  ratio_bboxes TEXT,
  rendered_video_stored VARCHAR(255),
  start_ms BIGINT,
  start_ms_boundary BIGINT,
  user_id VARCHAR(255) NOT NULL,
  rendered_version VARCHAR(255),
  cc_source_lang VARCHAR(255),
  cc_target_lang VARCHAR(255),
  caption_display VARCHAR(255),
  data_version VARCHAR(255),
  meta TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE highlight_moment_task (
  id VARCHAR(255) NOT NULL,
  default_task BIT,
  deleted BIT,
  identity VARCHAR(255) NOT NULL,
  outcome BIGINT,
  status VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  video_meta TEXT NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  completed_cost BIGINT,
  first_moment_cost BIGINT,
  error_count INT,
  url_source VARCHAR(255),
  expired_time BIGINT,
  video_platform VARCHAR(255),
  error_code VARCHAR(255),
  ratio VARCHAR(255),
  moment_count INT,
  query VARCHAR(6000),
  root_task_id VARCHAR(255),
  outcomes TEXT,
  full_video_stored VARCHAR(255),
  cc_aligned TEXT,
  language VARCHAR(255),
  source_language VARCHAR(255),
  caption_display VARCHAR(255),
  act_as VARCHAR(255),
  summary_meta TEXT,
  full_audio_stored VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE highlight_task_caption (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  caption_display VARCHAR(255),
  cc_aligned TEXT,
  cc_source_lang VARCHAR(255),
  cc_target_lang VARCHAR(255),
  origin_cc_aligned TEXT,
  task_id VARCHAR(255),
  user_id VARCHAR(255) NOT NULL,
  cc_aligned_speakers TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE hm_cc_translation (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  cc TEXT NOT NULL,
  hm_id VARCHAR(255) NOT NULL,
  language VARCHAR(255),
  user_id VARCHAR(255) NOT NULL,
  source_cc BIT,
  PRIMARY KEY (id)
);

CREATE TABLE hm_download_log (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  begin_time VARCHAR(255),
  cost BIGINT,
  end_time VARCHAR(255),
  hm_id VARCHAR(255) NOT NULL,
  identity VARCHAR(255) NOT NULL,
  res VARCHAR(255),
  source_ip VARCHAR(255),
  status VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE hm_shared (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  author VARCHAR(255),
  deleted BIT,
  duration BIGINT,
  hm_id VARCHAR(255),
  snapshot TEXT,
  user_id VARCHAR(255) NOT NULL,
  origin_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE marketing_unsubscribe (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  email VARCHAR(255) NOT NULL,
  user_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE multimodal_completion (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  assistant_message TEXT,
  duration BIGINT,
  error_code VARCHAR(255),
  error TEXT,
  ip_address VARCHAR(255),
  status INT,
  streaming BIT,
  user_id VARCHAR(255) NOT NULL,
  user_message TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE note (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  author VARCHAR(255),
  duration BIGINT,
  favorite BIT NOT NULL,
  highlight_display BIT NOT NULL,
  highlight_fragment_display TEXT,
  highlight_frame_display TEXT,
  highlights TEXT,
  language VARCHAR(255),
  mindmap TEXT,
  mindmap_display BIT NOT NULL,
  saved BIT,
  summary TEXT,
  summary_display BIT NOT NULL,
  thumbnail VARCHAR(255),
  title TEXT,
  user_account_id VARCHAR(255) NOT NULL,
  link VARCHAR(10240) NOT NULL,
  video_summary_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE note_shared (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  deleted BIT,
  note_id VARCHAR(255),
  snapshoot TEXT,
  user_account_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE notify_log (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  data TEXT,
  receiver VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  viewed BIT,
  PRIMARY KEY (id)
);

CREATE TABLE payment_entitlement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  active BIT NOT NULL,
  feature VARCHAR(255) NOT NULL,
  num BIGINT,
  platform VARCHAR(255),
  price VARCHAR(255) NOT NULL,
  svip_price VARCHAR(255) NOT NULL,
  vip_price VARCHAR(255) NOT NULL,
  ssvip_price VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE payment_record (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  currency VARCHAR(255) NOT NULL,
  customer_id VARCHAR(255) NOT NULL,
  deleted BIT,
  pay_platform VARCHAR(255) NOT NULL,
  price BIGINT NOT NULL,
  product_id VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  transaction_id VARCHAR(255) NOT NULL,
  unknown_fields TEXT,
  user_id VARCHAR(255) NOT NULL,
  entitlements TEXT,
  unit_amount BIGINT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE plan_entitlement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  active BIT NOT NULL,
  feature VARCHAR(255),
  the_interval VARCHAR(255),
  usage_limit BIGINT,
  plan VARCHAR(255),
  platform VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE project_job (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  data TEXT,
  error_code VARCHAR(255),
  moment_id VARCHAR(255),
  status VARCHAR(255) NOT NULL,
  task_id VARCHAR(255),
  type VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  video_meta TEXT NOT NULL,
  expired_time BIGINT,
  deleted BIT DEFAULT 'b'0'',
  system_job BIT DEFAULT 'b'0'',
  retry_params TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE strategy_rule (
  id VARCHAR(255) NOT NULL,
  black_list_exclude_chat TEXT,
  black_list_strategy_exclude_chat VARCHAR(255),
  default_strategy VARCHAR(255) NOT NULL,
  platform VARCHAR(255) NOT NULL,
  priority INT,
  white_list_exclude_chat TEXT,
  white_list_strategy_exclude_chat VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE subscription (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  cancel_at BIGINT,
  cancel_at_period_end BIT,
  checkout_session_id VARCHAR(255),
  current_period_end BIGINT,
  current_period_start BIGINT,
  latest_invoice VARCHAR(255),
  latest_invoice_status VARCHAR(255),
  plan VARCHAR(255),
  platform VARCHAR(255),
  request_cancel_at BIGINT,
  status VARCHAR(255),
  trial_end BIGINT,
  trial_start BIGINT,
  user_account_id VARCHAR(255) NOT NULL,
  next_period_plan VARCHAR(255),
  pay_platform VARCHAR(255),
  pay_platform_sid VARCHAR(255),
  last_renew_plan_entitlement BIGINT,
  reward_trial_plan VARCHAR(255),
  reward_trial_plan_end BIGINT,
  reward_trial_plan_start BIGINT,
  quantity BIGINT,
  next_period_quantity BIGINT,
  stripe_id VARCHAR(255),
  customer_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE suggestion_feedback (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  deleted BIT,
  email VARCHAR(255),
  plain_text TEXT,
  rich_text TEXT,
  type VARCHAR(255),
  user_id VARCHAR(255),
  username VARCHAR(255),
  details TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE system_coming_soon (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  email VARCHAR(255) NOT NULL,
  types TEXT,
  user_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE system_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  conf_key VARCHAR(255) NOT NULL,
  conf_value LONGTEXT,
  value_type VARCHAR(255),
  desc VARCHAR(2048),
  PRIMARY KEY (id)
);

CREATE TABLE system_version (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  cur_version VARCHAR(255) NOT NULL,
  cur_version_val BIGINT NOT NULL,
  download_address VARCHAR(255) NOT NULL,
  enabled BIT NOT NULL,
  update_content VARCHAR(255),
  update_type VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE uninstall_reason (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  details TEXT,
  email VARCHAR(255),
  reasons TEXT,
  user_id VARCHAR(255),
  signup_time BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE user_account (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  deleted BIT,
  email VARCHAR(255) NOT NULL,
  enabled BIT,
  inviter_code VARCHAR(255),
  locked BIT,
  password VARCHAR(255),
  platform VARCHAR(255),
  signup_ip VARCHAR(255),
  third_party_id VARCHAR(255),
  type VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  internal_code VARCHAR(255),
  format_email VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE user_activity (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  activity_key VARCHAR(255),
  admin_data TEXT,
  data TEXT,
  end_time BIGINT,
  last_reward_time BIGINT,
  phase_progress INT,
  phase_rewards TEXT,
  rewards TEXT,
  start_time BIGINT,
  user_account_id VARCHAR(255) NOT NULL,
  valid_count BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE user_entitlement (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  entitlements TEXT NOT NULL,
  user_account_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE user_portrait (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  tags TEXT,
  user_account_id VARCHAR(255) NOT NULL,
  jobs TEXT,
  usages TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE user_profile (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  avatar VARCHAR(255),
  invitation_code VARCHAR(255),
  language VARCHAR(255),
  user_account_id VARCHAR(255) NOT NULL,
  zernio_profile_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE video_find_clip (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  clips TEXT,
  identity VARCHAR(255) NOT NULL,
  language VARCHAR(255),
  link VARCHAR(10240) NOT NULL,
  outcome BIGINT,
  query VARCHAR(255),
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE video_highlight (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  expired_time BIGINT,
  highlights TEXT,
  identity VARCHAR(255) NOT NULL,
  language VARCHAR(255),
  outcome BIGINT,
  user_id VARCHAR(255) NOT NULL,
  video_id VARCHAR(255),
  video_task_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE video_mindmap (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  completion_id VARCHAR(255) NOT NULL,
  feedback TEXT,
  identity VARCHAR(255) NOT NULL,
  link VARCHAR(10240) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  expired_time BIGINT,
  video_summary_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE video_summary (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  completion_id VARCHAR(255) NOT NULL,
  deleted BIT,
  feedback TEXT,
  highlights TEXT,
  identity VARCHAR(255) NOT NULL,
  language VARCHAR(255),
  link VARCHAR(10240) NOT NULL,
  outcome BIGINT,
  summary TEXT,
  tags TEXT,
  title TEXT,
  user_id VARCHAR(255) NOT NULL,
  video_id VARCHAR(255) NOT NULL,
  video_source VARCHAR(255) NOT NULL,
  video_thumbnail VARCHAR(255),
  expired_time BIGINT,
  video_task_id VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE video_summary_change_log (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  changed TEXT,
  completion_id VARCHAR(255),
  user_id VARCHAR(255) NOT NULL,
  video_summary_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE waiting_list (
  id VARCHAR(255) NOT NULL,
  created BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  version BIGINT NOT NULL,
  age VARCHAR(255) NOT NULL,
  approved BIT,
  email VARCHAR(255) NOT NULL,
  gender VARCHAR(255) NOT NULL,
  how_hear VARCHAR(255) NOT NULL,
  most_like_feature TEXT,
  name VARCHAR(255) NOT NULL,
  occupation VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);
