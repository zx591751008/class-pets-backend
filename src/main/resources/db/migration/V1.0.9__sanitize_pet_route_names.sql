-- Remove legacy suffixes from pet route names and gallery names
-- Ensures names no longer include: 系统限定 / 宠物限定

UPDATE class_config
SET pets = CAST(
  REPLACE(
    REPLACE(
      REPLACE(
        REPLACE(CAST(pets AS CHAR), ' (系统限定)', ''),
      '(系统限定)', ''),
    '（系统限定）', ''),
  '宠物限定', '') AS JSON)
WHERE CAST(pets AS CHAR) LIKE '%系统限定%'
   OR CAST(pets AS CHAR) LIKE '%宠物限定%';

UPDATE student_pet_gallery
SET pet_name = TRIM(
  REPLACE(
    REPLACE(
      REPLACE(
        REPLACE(pet_name, ' (系统限定)', ''),
      '(系统限定)', ''),
    '（系统限定）', ''),
  '宠物限定', '')
)
WHERE pet_name LIKE '%系统限定%'
   OR pet_name LIKE '%宠物限定%';
