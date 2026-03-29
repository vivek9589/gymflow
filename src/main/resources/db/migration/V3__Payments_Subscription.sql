CREATE TABLE `payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,

  `member_id` BIGINT NOT NULL,
  `subscription_id` BIGINT NOT NULL,
  `gym_id` BIGINT NOT NULL,

  `amount` DECIMAL(10,2) NOT NULL,
  `payment_mode` VARCHAR(50), -- CASH, UPI, CARD
  `status` VARCHAR(50), -- SUCCESS, PENDING

  `transaction_ref` VARCHAR(255), -- optional UPI ref

  `created_at` DATETIME(6),

  PRIMARY KEY (`id`),

  FOREIGN KEY (`member_id`) REFERENCES `members`(`id`),
  FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions`(`id`),
  FOREIGN KEY (`gym_id`) REFERENCES `gyms`(`id`)
);


CREATE TABLE `subscriptions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `member_id` BIGINT NOT NULL,
  `plan_id` BIGINT NOT NULL,
  `gym_id` BIGINT NOT NULL,

  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,

  `total_amount` DECIMAL(10,2) NOT NULL,
  `paid_amount` DECIMAL(10,2) DEFAULT 0,
  `due_amount` DECIMAL(10,2) DEFAULT 0,

  `status` VARCHAR(50) NOT NULL, -- ACTIVE, EXPIRED, PARTIAL

  `created_at` DATETIME(6),
  `updated_at` DATETIME(6),

  PRIMARY KEY (`id`),
  FOREIGN KEY (`member_id`) REFERENCES `members`(`id`),
  FOREIGN KEY (`plan_id`) REFERENCES `plans`(`id`),
  FOREIGN KEY (`gym_id`) REFERENCES `gyms`(`id`)
);