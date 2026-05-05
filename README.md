# Create DG-Lab

> *最好不要让应力过载。*

一个 Minecraft NeoForge 模组，将 Create 模组的**应力系统**与 **DG-Lab（郊狼）** 设备联动。当你的动力网络过载时，你不仅会在游戏里看到红字警告，还会在现实中感受到它。

## 功能

- 放置「郊狼传感器」方块，连接到 Create 动力网络
- 实时读取网络应力百分比，映射为 DG-Lab 设备的电流强度
- 应力越高，电流越强；过载时触发急促脉冲
- AB 通道自动同步输出
- 内置 WebSocket 服务器，DG-Lab App 扫码即连
- 左上角 HUD 实时显示连接状态和强度

## 使用方法

【最好不要让应力过载 | Create DG-Lab首次发布！】 https://www.bilibili.com/video/BV1DLR7BqE4o/?share_source=copy_web&vd_source=a9bc5f04731f1d8d3046b435a28fa4e0
1. 安装模组（需要 Create 6.0.x + NeoForge 21.1.200+）
2. 合成：一根木棍 → 郊狼传感器（没错，就是这么简单）
3. 放在动力网络中，连接传动杆
4. 右键方块，扫描二维码连接 DG-Lab App
5. 感受应力

## 波形选择

| 应力区间 | 波形 | 感受 |
|---------|------|------|
| 0% ~ 60% | 无输出 | 安全区，岁月静好 |
| 60% ~ 85% | 呼吸波形 | 逐渐紧张 |
| 85% ~ 100% | 连击波形 | 明显不适 |
| > 100% | 报警脉冲 | 过载！快停手！ |

## 依赖

- Minecraft 1.21.1
- NeoForge 21.1.200+
- Create 6.0.x
- Java 21

## 编译

```bash
./gradlew build
```

编译产物在 `build/libs/` 目录下：

| 文件 | 说明 |
|------|------|
| `createdglab-neoforge-1.21.1-x.x.x.jar` | **使用这个** — 含所有依赖的完整版本 |
| `createdglab-neoforge-1.21.1-x.x.x-slim.jar` | 精简版，不含第三方依赖，不推荐 |

## 协议

本模组通过 WebSocket 与 DG-Lab App 通信，协议参考 [DG-LAB-OPENSOURCE](https://github.com/bilbillm/DG-LAB-OPENSOURCE)。

## 许可

MIT License
