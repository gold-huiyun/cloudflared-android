
# Cloudflared Android Tunnel App (v4)

此版本变更：
- Actions: 为 setup-go 增加 `cache-dependency-path: cloudflared-src/go.sum`，消除缓存找不到 go.sum 的警告。
- Manifest: 新增 `FOREGROUND_SERVICE_DATA_SYNC` 与 `POST_NOTIFICATIONS`。
- MainActivity: Android 13+ 主动请求通知运行时权限。
- TunnelService: 增加 `--logfile`、进程退出码展示与可执行/存在性检查。

## 参考
- Gradle/Actions 迁移与缓存：https://github.com/gradle/actions/blob/main/setup-gradle/README.md
- Android 14 前台服务类型与权限要求：
  - https://developer.android.com/about/versions/14/changes/fgs-types-required
  - https://developer.android.com/develop/background-work/services/fgs/service-types
- Android 13 通知运行时权限：
  - https://developer.android.com/develop/ui/views/notifications/notification-permission

## 使用
- 运行 Actions → Build cloudflared Android APK
- 安装 APK → 粘贴 Token → 启动。若失败，请打开通知查看退出码，并把 `filesDir/cloudflared.log` 的关键报错贴给我定位。
