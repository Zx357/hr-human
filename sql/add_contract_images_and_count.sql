-- 添加合同图片和合同次数字段
-- Add contract images and contract count fields to hr_contract table

USE kadmin;

-- 添加合同图片字段（多张图片，逗号分隔）
ALTER TABLE `hr_contract` 
ADD COLUMN `contract_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '合同图片（多张，逗号分隔）' AFTER `remark`;

-- 添加合同次数字段（第几次合同）
ALTER TABLE `hr_contract` 
ADD COLUMN `contract_count` int NULL DEFAULT NULL COMMENT '合同次数（第几次合同）' AFTER `contract_images`;
