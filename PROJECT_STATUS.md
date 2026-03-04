# Class Pets Backend - 项目状态（覆盖版）

> 更新时间: 2026-03-03（含删组事务修复与注册码文案统一）
> 范围: 历史版本记录 + 最新增量归档

## 0、最新增量（2026-03-03 夜间）

- 小组删除链路修复：`GroupService.delete` 改为事务执行，顺序为“删除组历史 -> 清学生 group_id -> 删除小组”，避免半成功状态。
- 已适配线上外键场景（`group_event.group_id -> group_info.id`），删除有历史的小组不再触发系统异常。
- 注册/找回密码相关错误文案统一：`激活码` 全量替换为 `注册码`。
- 排行分类统计持续采用双轨：`student_event.rule_id` 优先，`reason` 兜底（兼容历史记录）。

## 0、最新增量（2026-03-01）

- 抽奖/背包链路：抽中奖励统一先入背包，改为手动使用生效；新增全班抽奖记录查询口径。
- 撤销链路：历史记录返回 `revoked/undoable`，前后端统一支持“已撤销/不可撤销”展示与拦截。
- 道具闭环：`CANDY/MEAT/BIG_BURST` 在背包使用后若触发满级，补齐图鉴解锁与宠物重置。
- 数据回滚精度：`student_event` 增加 `exp_change` 用于撤销时精确回滚经验。
- 线上稳定性：生产口径调整为 Flyway 默认关闭，采用手工 SQL 执行策略以降低启动风险。

## 一、今天完成了什么（已完成）

### 1) 商城模块修正（核心）
- 修复商品删除/上架语义：
  - 删除：逻辑删除（`is_deleted=1`）
  - 上下架：`is_active` 切换
- 新增商品状态切换接口：
  - `PATCH /api/v1/store/items/{itemId}/active`
- 商品查询按班级与删除标记过滤，支持编辑态看到下架商品
- 商城权限校验加固：所有操作校验班级归属（老师权限）

### 2) 积分流水修正
- 兑换行为写入学生事件流水（`student_event`）
- 历史接口返回兑换变动字段（`redeemChange`）
- 保证前端历史可展示“兑换商品”扣减记录

### 3) 删除链路与一致性
- 修复删除学生系统异常（先删关联流水/兑换记录，再删学生）
- 删除相关异常统一走业务异常码（避免误判成功）

### 4) 排行口径修正
- 小组周/月榜统计口径与学生榜统一：
  - 时间字段使用 `timestamp`
  - 过滤 `revoked=0`

### 5) 成长体系（极速版后端）
- 学生积分变动时同步成长：
  - 加分增加经验
  - 扣分不减少经验
- 等级按经验阈值自动计算
- `StudentVO` 增加成长字段：
  - `exp`
  - `levelStartExp`
  - `nextLevelExp`
  - `expToNext`
- 阈值配置化：
  - `growth.level-thresholds`
  - `growth.overflow-step`

## 二、当前关键接口状态（真实）

### 认证
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/auth/me`

### 班级/学生/小组/规则
- 班级：查询、创建、修改、清空积分
- 学生：列表、新增、编辑、删除、加扣分、批量分组
- 小组：列表、新增、编辑、删除、加扣分、历史
- 规则：列表、新增、删除

### 排行/分析/流水
- 排行榜：个人榜、小组榜（总/周/月）
- 分析：趋势/Top/雷达/占比/预警数据
- 历史：班级历史 + 学生历史（含兑换扣减）

### 商城
- `GET /api/v1/classes/{classId}/store/items`
- `POST /api/v1/classes/{classId}/store/items`
- `PUT /api/v1/store/items/{itemId}`
- `PATCH /api/v1/store/items/{itemId}/active`
- `DELETE /api/v1/store/items/{itemId}`（逻辑删除）
- `POST /api/v1/store/redeem`

## 三、配置说明

### 成长阈值配置
在 `application.yml` 可直接配置：

```yaml
growth:
  level-thresholds: [0, 100, 250, 450, 700, 1000, 1400, 1900, 2500, 3200, 4000]
  overflow-step: 800
```

- `level-thresholds`：每级经验下限（Lv1 对应首个值）
- `overflow-step`：超过最后阈值后的每级步长

## 四、构建状态

- 后端编译通过：`mvn -DskipTests compile`

## 五、待办（下一迭代）

1. 成长体系标准版：升级日志、阈值管理接口、奖励机制
2. 数据管理：导入/导出接口（学生、规则、流水）
3. 商城运营能力：兑换记录查询、筛选、导出
4. 自动化测试：权限、兑换事务、排行榜口径回归
