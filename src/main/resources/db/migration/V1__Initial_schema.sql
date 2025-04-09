DROP TABLE IF EXISTS cron_task_detail;
CREATE TABLE cron_task_detail (
  task_name varchar(255) NOT NULL,
  task_group varchar(255) NOT NULL,
  task_class varchar(255) NOT NULL,
  description varchar(1024),
  initial_parameter text,
  cron_expression blob NOT NULL,
  cron varchar(255) NOT NULL,
  next_fired_datetime timestamp,
  prev_fired_datetime timestamp,
  task_status varchar(45) NOT NULL,
  max_retry_count int,
  timeout bigint,
  last_modified timestamp DEFAULT CURRENT_TIMESTAMP
);