# Class Pets - Controller 与 DTO 设计

## 1. AuthController
- `POST /auth/register` -> `register(RegisterRequestDTO)`
- `POST /auth/login` -> `login(LoginRequestDTO)`
- `POST /auth/logout` -> `logout()`
- `GET /auth/me` -> `me()`

DTO:
- `RegisterRequestDTO { username, password, nickname, activationCode }`
- `LoginRequestDTO { username, password }`
- `LoginResponseVO { token, expiresIn, teacher }`

## 2. ActivationCodeAdminController
- `GET /admin/activation-codes` -> `page(ActivationCodePageQueryDTO)`
- `POST /admin/activation-codes` -> `generate(ActivationCodeGenerateRequestDTO)`
- `PATCH /admin/activation-codes/{id}/disable` -> `disable(Long id)`

DTO:
- `ActivationCodeGenerateRequestDTO { count, prefix, length }`
- `ActivationCodePageQueryDTO { page, size, used }`

## 3. ClassController
- `GET /classes` -> `list()`
- `POST /classes` -> `create(ClassCreateRequestDTO)`
- `GET /classes/{classId}` -> `detail(Long classId)`
- `PUT /classes/{classId}` -> `update(Long classId, ClassUpdateRequestDTO)`
- `DELETE /classes/{classId}` -> `delete(Long classId)`

DTO:
- `ClassCreateRequestDTO { name, teacherName }`
- `ClassUpdateRequestDTO { name, teacherName }`

## 4. ClassConfigController
- `GET /classes/{classId}/config` -> `getConfig(Long classId)`
- `PUT /classes/{classId}/config` -> `updateConfig(Long classId, ClassConfigUpdateRequestDTO)`

DTO:
- `ClassConfigUpdateRequestDTO { levels, pets, evolution }`

## 5. StudentController
- `GET /classes/{classId}/students` -> `page(Long classId, StudentPageQueryDTO)`
- `POST /classes/{classId}/students` -> `create(Long classId, StudentCreateRequestDTO)`
- `GET /students/{studentId}` -> `detail(Long studentId)`
- `PUT /students/{studentId}` -> `update(Long studentId, StudentUpdateRequestDTO)`
- `DELETE /students/{studentId}` -> `delete(Long studentId)`

回收站:
- `GET /classes/{classId}/trash/students` -> `trashPage(Long classId, PageQueryDTO)`
- `POST /classes/{classId}/trash/students/{trashId}/restore` -> `restore(Long classId, Long trashId)`
- `DELETE /classes/{classId}/trash/students/{trashId}` -> `hardDelete(Long classId, Long trashId)`

DTO:
- `StudentPageQueryDTO { page, size, keyword, groupId, sort }`
- `StudentCreateRequestDTO { name, studentNo, gender, groupId, avatarImage, petId }`
- `StudentUpdateRequestDTO { name, studentNo, gender, groupId, avatarImage, petId, totalPoints, redeemPoints, exp, level, title }`

## 6. StudentEventController
- `POST /students/{studentId}/score` -> `score(Long studentId, StudentScoreRequestDTO)`
- `POST /classes/{classId}/students/score/batch` -> `batchScore(Long classId, BatchScoreRequestDTO)`
- `GET /students/{studentId}/events` -> `eventPage(Long studentId, StudentEventPageQueryDTO)`
- `POST /student-events/{eventId}/revoke` -> `revoke(Long eventId)`

DTO:
- `StudentScoreRequestDTO { mode, ruleId, points, reason, note, context }`
- `BatchScoreRequestDTO { studentIds, points, reason, note }`
- `StudentEventPageQueryDTO { page, size, from, to, revoked }`

## 7. GroupController
- `GET /classes/{classId}/groups` -> `list(Long classId)`
- `POST /classes/{classId}/groups` -> `create(Long classId, GroupCreateRequestDTO)`
- `PUT /groups/{groupId}` -> `update(Long groupId, GroupUpdateRequestDTO)`
- `DELETE /groups/{groupId}` -> `delete(Long groupId)`

DTO:
- `GroupCreateRequestDTO { name, icon, note, points }`
- `GroupUpdateRequestDTO { name, icon, note, points }`

## 8. GroupEventController
- `POST /groups/{groupId}/score` -> `score(Long groupId, GroupScoreRequestDTO)`
- `GET /classes/{classId}/group-events` -> `eventPage(Long classId, GroupEventPageQueryDTO)`
- `POST /group-events/{eventId}/revoke` -> `revoke(Long eventId)`

DTO:
- `GroupScoreRequestDTO { mode, points, reason, note }`
- `GroupEventPageQueryDTO { page, size, groupId, range }`

## 9. RuleController
- `GET /classes/{classId}/rules` -> `list(Long classId, RulePageQueryDTO)`
- `POST /classes/{classId}/rules` -> `create(Long classId, RuleCreateRequestDTO)`
- `PUT /rules/{ruleId}` -> `update(Long ruleId, RuleUpdateRequestDTO)`
- `DELETE /rules/{ruleId}` -> `delete(Long ruleId)`

DTO:
- `RulePageQueryDTO { enabled, category, type }`
- `RuleCreateRequestDTO { content, points, type, category, enabled, cooldownHours, stackable }`
- `RuleUpdateRequestDTO { content, points, type, category, enabled, cooldownHours, stackable }`

## 10. ShopController
- `GET /classes/{classId}/shop-items` -> `itemList(Long classId)`
- `POST /classes/{classId}/shop-items` -> `createItem(Long classId, ShopItemCreateRequestDTO)`
- `PUT /shop-items/{itemId}` -> `updateItem(Long itemId, ShopItemUpdateRequestDTO)`
- `DELETE /shop-items/{itemId}` -> `deleteItem(Long itemId)`
- `POST /shop-items/{itemId}/redeem` -> `redeem(Long itemId, RedeemRequestDTO)`
- `GET /classes/{classId}/redeem-records` -> `redeemRecordPage(Long classId, RedeemRecordPageQueryDTO)`

DTO:
- `ShopItemCreateRequestDTO { name, cost, stock, icon, enabled }`
- `ShopItemUpdateRequestDTO { name, cost, stock, icon, enabled }`
- `RedeemRequestDTO { studentId, qty }`
- `RedeemRecordPageQueryDTO { page, size, studentId, itemId, range }`

## 11. RankingController
- `GET /classes/{classId}/rankings/students` -> `studentRanking(Long classId, StudentRankingQueryDTO)`
- `GET /classes/{classId}/rankings/groups` -> `groupRanking(Long classId, GroupRankingQueryDTO)`

DTO:
- `StudentRankingQueryDTO { range, mode }`
- `GroupRankingQueryDTO { type, range }`

## 12. RollCallController
- `GET /classes/{classId}/roll-call/status` -> `status(Long classId)`
- `POST /classes/{classId}/roll-call/pick` -> `pick(Long classId, RollCallPickRequestDTO)`
- `POST /classes/{classId}/roll-call/reset` -> `reset(Long classId)`

DTO:
- `RollCallPickRequestDTO { groupId, count, dedup }`

## 13. ExportImportController
- `GET /classes/{classId}/export/leaderboard.xlsx` -> `exportLeaderboard(Long classId)`
- `GET /classes/{classId}/export/redeem-balance.xlsx` -> `exportRedeemBalance(Long classId)`
- `GET /classes/{classId}/export/reason-stats.xlsx` -> `exportReasonStats(Long classId)`
- `GET /classes/{classId}/export/all.xlsx` -> `exportAll(Long classId)`
- `GET /classes/{classId}/export/filtered.csv` -> `exportFilteredCsv(Long classId, ExportFilterQueryDTO)`
- `GET /classes/{classId}/export/backup.json` -> `exportBackupJson(Long classId)`
- `POST /classes/{classId}/import/backup-json` -> `importBackupJson(Long classId, MultipartFile file)`
- `POST /classes/{classId}/import/students-template` -> `importStudentsTemplate(Long classId, MultipartFile file)`

## 14. LogController
- `GET /classes/{classId}/logs` -> `page(Long classId, LogPageQueryDTO)`

DTO:
- `LogPageQueryDTO { page, size, type, from, to }`

## 通用建议
- 统一返回结构: `ApiResponse<T>`
- 分页结构: `PageResult<T>`
- 鉴权后从 `SecurityContext` 提取 `teacherId`
- Service 层全部做 `teacherId` 与 `classId` 的归属校验
