# 天天衣橱 - TianTianWardrobe

智能衣橱管家 App，基于 AI 的衣物识别与穿搭推荐系统。

## 功能特色

- **AI 衣物识别** — 使用 ML Kit 自动识别衣物类别、颜色、风格
- **智能穿搭推荐** — 基于颜色搭配、季节匹配、风格统一的 AI 推荐引擎
- **二十四节气参考** — 结合黄道节气，给出应季穿搭建议
- **拍照录入** — 点击底部加号拍照或从相册选择，快速录入衣物
- **衣柜管理** — 按类别/季节/风格分类管理你的衣物
- **数据本地存储** — 使用 Room 数据库，数据安全可靠

## 技术栈

| 组件 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 相机 | CameraX |
| 数据库 | Room |
| AI识别 | ML Kit Image Labeling |
| 图片加载 | Coil |
| 架构 | MVVM (ViewModel + Flow) |

## 构建要求

- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Android SDK 34
- Gradle 8.5

## 快速开始

```bash
# 克隆项目
git clone https://github.com/cve-vul/TianTianWardrobe.git

# 使用 Gradle 构建
cd TianTianWardrobe
./gradlew assembleDebug
```

APK 生成路径: `app/build/outputs/apk/debug/app-debug.apk`

## 项目结构

```
TianTianWardrobe/
├── app/
│   ├── src/main/
│   │   ├── java/com/tiantian/wardrobe/
│   │   │   ├── ai/          # AI 模块 (识别/节气/推荐)
│   │   │   ├── data/        # 数据层 (Room DB)
│   │   │   ├── ui/          # UI 界面
│   │   │   └── viewmodel/   # ViewModel
│   │   └── res/             # 资源文件
│   └── build.gradle.kts
└── settings.gradle.kts
```
