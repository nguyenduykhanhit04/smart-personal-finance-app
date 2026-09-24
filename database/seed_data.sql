USE personal_finance_app;

INSERT INTO categories
(user_id, category_name, category_type, icon, color, is_default)
VALUES
(NULL, 'Ăn uống', 'expense', 'ic_food', '#FF9800', TRUE),
(NULL, 'Di chuyển', 'expense', 'ic_transport', '#2196F3', TRUE),
(NULL, 'Mua sắm', 'expense', 'ic_shopping', '#E91E63', TRUE),
(NULL, 'Hóa đơn', 'expense', 'ic_bill', '#9C27B0', TRUE),
(NULL, 'Giải trí', 'expense', 'ic_entertainment', '#673AB7', TRUE),
(NULL, 'Sức khỏe', 'expense', 'ic_health', '#4CAF50', TRUE),
(NULL, 'Khác', 'expense', 'ic_other', '#607D8B', TRUE),

(NULL, 'Lương', 'income', 'ic_salary', '#4CAF50', TRUE),
(NULL, 'Thưởng', 'income', 'ic_bonus', '#FFC107', TRUE),
(NULL, 'Đầu tư', 'income', 'ic_investment', '#009688', TRUE),
(NULL, 'Khác', 'income', 'ic_other', '#607D8B', TRUE);
