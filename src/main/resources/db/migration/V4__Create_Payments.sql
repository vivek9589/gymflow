CREATE TABLE `payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,

  `member_id` BIGINT NOT NULL,
  `subscription_id` BIGINT NOT NULL,
  `gym_id` BIGINT NOT NULL,

  `amount` DECIMAL(10,2) NOT NULL,
  `payment_mode` VARCHAR(50),
  `status` VARCHAR(50),

  `transaction_ref` VARCHAR(255),

  `created_at` DATETIME(6),

  PRIMARY KEY (`id`),

  FOREIGN KEY (`member_id`) REFERENCES `members`(`id`),
  FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions`(`id`),
  FOREIGN KEY (`gym_id`) REFERENCES `gyms`(`id`)
);