INSERT INTO transaction_category (transaction_category_id, category_name)
VALUES (2,'GROCERIES'),
       (3,'ENTERTAINMENT'),
       (4,'UTILITIES'),
       (5,'CAFE_AND_RESTAURANT'),
       (6,'TRANSPORTATION'),
       (7,'MEDICAL_CARE'),
       (8,'EDUCATION'),
       (9,'SHOPPING'),
       (10,'PHARMACY'),
       (11,'HEALTH_AND_BEAUTY'),
       (12,'SPORTS_AND_FITNESS'),
       (13,'ELECTRONICS'),
       (14,'FEES_AND_CHARGES'),
       (15,'OTHER')

ON CONFLICT DO NOTHING;

INSERT INTO product_category (product_category_id, category_name)
VALUES (1, 'CHECKING_ACCOUNT'),
       (2, 'SAVINGS_ACCOUNT'),
       (3, 'CREDIT_CARD'),
       (4, 'LOAN'),
       (5, 'MORTGAGE'),
       (6, 'INVESTMENT'),
       (7, 'PENSION'),
       (8, 'INSURANCE'),
       (9, 'OTHER'),
       (10, 'SHORT_TERM_DEPOSIT')

ON CONFLICT DO NOTHING;