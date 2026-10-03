# 饰品图标拆分 / 完美像素优化

把一张 AI 生成的饰品集合图（10 列 × 7 行 = 70 个饰品）拆成 70 个独立、背景透明、
像素严格对齐的图标，并使用 [perfectPixel](https://github.com/theamusing/perfectPixel)
做完美像素优化。

## 产物

| 位置 | 说明 |
| :--- | :--- |
| `src/main/resources/assets/life_contract/textures/item/accessory/` | **70 个成品图标 PNG**（RGBA，透明背景，原始像素分辨率） |
| `tools/accessory_split/split_and_refine.py` | 可复现的完整处理脚本 |
| `tools/accessory_split/source.png` | 原始输入图（1536×1024） |
| `tools/accessory_split/accessory_sheet.png` | 优化后的整张精灵表（304×199，透明背景） |
| `tools/accessory_split/preview_4x.png` | 优化后精灵表的 4× 最近邻放大预览 |
| `tools/accessory_split/contact_sheet.png` | 70 个图标的编号对照图（6× 放大） |
| `tools/accessory_split/manifest.json` | 编号 → 文件名 / 行列 / 尺寸 的清单 |

## 处理流程

1. **颜色还原**：源图是 AI 生成、带软 alpha 的 PNG（alpha 最大只有 254）。
   先按 alpha 反预乘还原出真实颜色，避免边缘被白底冲淡；
   同时生成一张"白底合成图"专门给网格检测用（反预乘后的噪声会干扰 FFT）。
2. **网格检测**：调用 perfectPixel 的 `detect_grid_scale`，
   通过 FFT 幅度谱自动测出原图的像素块大小（检测结果 **4.60 px/像素**），
   再用 Sobel 边缘把每条网格线吸附到真实像素边界（`refine_grids`），
   得到 305×200 条网格线 → **304×199 的完美像素画布**。
3. **重采样**：用对齐后的网格中心采样（`sample_center`），
   每个采样点 = 1 个美术像素；颜色和 alpha 用同一套网格坐标采样，保证完全对齐。
4. **拆分**：在采样结果的 alpha 掩膜上做三级投影切分
   （整图行投影 → 条带内列投影 → 单列内纵向投影），
   稳定切出 10×7 = 70 个图标的外接矩形。
   第 4/5 行和第 6/7 行在原图里上下"咬合"在一起（挂坠垂饰越过了行边界），
   脚本会按列分别找纵向间隙来切，不会把图标切坏。
5. **后处理**：
   - alpha 二值化成硬边（像素画不需要半透明边缘）；
   - 去掉 1px 孤立噪点；
   - 透明像素填成**最近的不透明像素颜色**，避免游戏内缩放出现黑边/暗晕。

## 重新运行

```bash
# 需要 numpy + opencv-python，以及 perfectPixel 源码
git clone --depth 1 https://github.com/theamusing/perfectPixel.git temp/perfectPixel

python tools/accessory_split/split_and_refine.py \
    --src tools/accessory_split/source.png \
    --out src/main/resources/assets/life_contract/textures/item/accessory \
    --perfect-pixel temp/perfectPixel/src \
    --sheet tools/accessory_split/accessory_sheet.png \
    --preview tools/accessory_split/preview_4x.png
```

## 图标清单

| 行 | 内容 | 编号 |
| :--- | :--- | :--- |
| 1 | 项链 / 吊坠（红宝石、蓝水晶、紫星、骨牙、红眼、蓝晶、紫十字、金框宝石、血晶、黑曜垂饰） | 01–10 |
| 2 | 戒指（红宝石、银蓝、荆棘紫晶、金红、翡翠、秘银蓝晶、紫晶、银白、黑铁、金珍珠） | 11–20 |
| 3 | 护符 / 挂件（十字剑、红灯笼、蓝水滴、紫星镖、蝙蝠、骨羽、紫蝶、黑猫、咒文书、怀表） | 21–30 |
| 4 | 头冠（血红、冰霜、紫晶、骨冠、黑金、金冠、银冠、红白十字、暗影、寒冰） | 31–40 |
| 5 | 药水与器物（红/蓝/紫药水、绿灯笼、空瓶、紫晶护符、红晶护符、蓝晶护符、魔典、行囊） | 41–50 |
| 6 | 材料（白羽、黑羽、骨牙、黑曜碎片、血晶簇、紫刺球、符文黑石、卷轴、草药叶、血色星芒） | 51–60 |
| 7 | 杂项（蓝十字、灰骷髅、白骷髅、蝠翼、蓝宝冠饰、紫四芒星、雪晶、紫眼护符、红鸟居、白狐面具） | 61–70 |

## 尺寸

单个图标约 **12–27 × 18–33 像素**（原始像素分辨率，未做任何缩放），
在 Minecraft 里作为物品贴图可直接使用（建议按 16×16 或 32×32 的观感缩放显示）。
