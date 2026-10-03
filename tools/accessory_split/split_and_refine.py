#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
饰品精灵表拆分 + PerfectPixel 完美像素优化
============================================

输入 : 一张 1536x1024 的 AI 生成饰品集合图（10 列 x 7 行 = 70 个饰品，带 alpha 通道）
输出 : 70 个独立、背景透明、像素对齐的图标 PNG

流程
----
1. 把源图按 alpha 还原成真实颜色（反预乘），同时生成"白底合成图"用于网格检测。
2. 调用 https://github.com/theamusing/perfectPixel 的 detect_grid_scale + refine_grids
   自动检测原图的像素块大小，并用 Sobel 边缘把网格线对齐到真实像素边界。
3. 用对齐后的网格采样，得到 304x199 的"完美像素"画布（每个采样点 = 1 个美术像素）。
4. 在采样结果上用 alpha 做前景掩膜，按 行投影 -> 列投影 -> 单列纵向投影 三级切分，
   稳定地切出 10x7 共 70 个饰品的外接矩形（能正确处理上下两行互相"咬合"的情况）。
5. 每个图标按外接矩形裁剪，alpha 二值化为硬边（像素画不需要半透明边缘），
   透明像素填成最近的不透明像素颜色（避免游戏内缩放出现黑边）。

依赖: numpy, opencv-python(-headless), perfectPixel 源码 (--perfect-pixel 指定)
用法: python split_and_refine.py --src source.png --out icons/ [--perfect-pixel path/to/src]
"""
import argparse, json, os, sys
import numpy as np
import cv2

NAMES = """ruby_pendant sapphire_crystal_pendant amethyst_star_pendant bone_fang_pendant crimson_eye_pendant azure_shard_pendant violet_cross_pendant golden_gem_pendant blood_crystal_pendant obsidian_drop_pendant
ruby_ring silver_sapphire_ring thorn_amethyst_ring golden_ruby_ring emerald_ring mithril_sapphire_ring amethyst_ring silver_ring black_iron_ring golden_pearl_ring
cross_sword_charm crimson_lantern_charm sapphire_teardrop_charm amethyst_shuriken bat_charm bone_feather_charm amethyst_butterfly black_cat_charm cursed_tome_charm compass_watch_charm
blood_crown frost_crown amethyst_crown bone_crown obsidian_gold_crown golden_crown silver_crown crimson_white_crown shadow_crown ice_crown
red_potion blue_potion purple_potion emerald_lantern empty_vial amethyst_amulet ruby_amulet sapphire_amulet arcane_tome crimson_knapsack
white_feather black_feather bone_fang obsidian_shard blood_crystal_cluster amethyst_spike_orb runed_obsidian_block ancient_scroll herbal_leaf blood_star
azure_cross grey_skull white_skull bat_wing sapphire_diadem amethyst_star snow_crystal violet_eye_amulet crimson_torii white_fox_mask""".split()

ROWS, COLS = 7, 10
ALPHA_THR = 100      # 采样点 alpha 高于该值算作"有内容"
MIN_RUN = 2          # 投影切分时的最小连续长度


def _runs(v, minlen=1, gap=0):
    """把布尔数组切成若干连续 True 段。"""
    out, s = [], None
    for i, b in enumerate(v):
        if b and s is None:
            s = i
        elif not b and s is not None:
            out.append([s, i]); s = None
    if s is not None:
        out.append([s, len(v)])
    merged = []
    for c in out:
        if merged and c[0] - merged[-1][1] <= gap:
            merged[-1][1] = c[1]
        else:
            merged.append(c)
    return [c for c in merged if c[1] - c[0] >= minlen]


def refine_sheet(src_rgba, pp):
    """用 PerfectPixel 的网格检测/对齐，把源图重采样成完美像素画布。返回 (rgb, alpha)。"""
    rgb = src_rgba[:, :, :3][:, :, ::-1].astype(np.float32)   # BGR -> RGB
    a = src_rgba[:, :, 3].astype(np.float32) / 255.0
    # 白底合成图：网格检测用它最稳（反预乘后的图噪声会干扰 FFT）
    comp = np.clip(np.rint(rgb * a[..., None] + 255.0 * (1 - a[..., None])), 0, 255).astype(np.uint8)
    # 反预乘还原真实颜色，保留边缘饱和度
    a_safe = np.clip(a, 0.35, 1.0)[..., None]
    true_rgb = np.clip((rgb - 255.0 * (1.0 - a_safe)) / a_safe, 0, 255)
    true_rgb = np.clip(np.rint(np.where(a[..., None] > 0.05, true_rgb, 255.0)), 0, 255).astype(np.uint8)
    alpha8 = np.clip(np.rint(a * 255), 0, 255).astype(np.uint8)

    gw, gh = pp["detect_grid_scale"](comp, peak_width=6, max_ratio=1.5, min_size=4.0)
    x_coords, y_coords = pp["refine_grids"](comp, int(round(gw)), int(round(gh)), 0.3)
    print("[perfectPixel] 自动网格 %dx%d -> 对齐后网格线 %d x %d" % (gw, gh, len(x_coords), len(y_coords)))
    rx = pp["sample_center"](true_rgb, x_coords, y_coords)
    ra = pp["sample_center"](alpha8[..., None], x_coords, y_coords)[..., 0]
    print("[perfectPixel] 完美像素画布: %d x %d" % (rx.shape[1], rx.shape[0]))
    return rx, ra


def find_boxes(ra):
    """三级投影切分，返回 grid[row][col] = (x0,y0,x1,y1)。"""
    H, W = ra.shape
    mask = ra > ALPHA_THR
    rp = mask.sum(axis=1)
    bands, s = [], None
    for i, v in enumerate(rp):
        if v > 0 and s is None:
            s = i
        elif v == 0 and s is not None:
            bands.append([s, i]); s = None
    if s is not None:
        bands.append([s, len(rp)])

    pitch = H / float(ROWS)
    cells, row_cursor = {}, 0
    for (y0, y1) in bands:
        nrows = max(1, int(round((y1 - y0) / pitch)))       # 这一条带里其实叠了几行
        cols = _runs(mask[y0:y1].any(axis=0), minlen=MIN_RUN, gap=2)
        assert len(cols) == COLS, "列数异常: %r" % (cols,)
        found = []
        for ci, (x0, x1) in enumerate(cols):
            cell = mask[y0:y1, x0:x1]
            vr = _runs(cell.any(axis=1), minlen=MIN_RUN, gap=1)
            if len(vr) != nrows:
                prof = cell.sum(axis=1).astype(float)
                if len(vr) == 1 and nrows == 2:            # 上下两个图标贴在一起，按最暗行切
                    cut = int(np.argmin(prof[2:-2])) + 2
                    vr = [[vr[0][0], cut], [cut, vr[0][1]]]
                else:
                    vr = sorted(sorted(vr, key=lambda r: -(r[1] - r[0]))[:nrows])
            for k, (cy0, cy1) in enumerate(vr):
                sub = mask[y0 + cy0:y0 + cy1, x0:x1]
                ys, xs = np.where(sub)
                found.append((k, ci, [x0 + int(xs.min()), x0 + int(xs.max()) + 1,
                                      y0 + cy0 + int(ys.min()), y0 + cy0 + int(ys.max()) + 1]))
        found.sort(key=lambda t: (t[0], t[1]))
        for k, ci, b in found:
            cells[(row_cursor + k, ci)] = b
        row_cursor += nrows
    assert len(cells) == ROWS * COLS, "图标数量异常: %d" % len(cells)
    return [[cells[(r, c)] for c in range(COLS)] for r in range(ROWS)]


def fill_transparent(rgba):
    """把完全透明的像素填成最近的不透明像素颜色，避免游戏内出现黑边/暗晕。"""
    alpha = rgba[:, :, 3]
    if (alpha == 0).sum() == 0:
        return rgba
    inv = (alpha == 0).astype(np.uint8)
    _, labels = cv2.distanceTransformWithLabels(inv, cv2.DIST_L2, 3,
                                                labelType=cv2.DIST_LABEL_PIXEL)
    zeros = np.argwhere(inv == 0)          # label-1 -> 该零像素在 raster order 中的下标
    if len(zeros) == 0:
        return rgba
    src = zeros[np.clip(labels - 1, 0, len(zeros) - 1)]
    out = rgba.copy()
    out[inv == 1] = rgba[src[inv == 1][:, 0], src[inv == 1][:, 1]]
    out[:, :, 3] = alpha
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", required=True, help="源精灵表 PNG")
    ap.add_argument("--out", required=True, help="输出图标目录")
    ap.add_argument("--perfect-pixel", default="perfectPixel/src",
                    help="perfectPixel 仓库的 src 目录")
    ap.add_argument("--sheet", default=None, help="额外输出整张优化后的精灵表")
    ap.add_argument("--preview", default=None, help="额外输出 4x 预览图")
    args = ap.parse_args()

    sys.path.insert(0, args.perfect_pixel)
    from perfect_pixel.perfect_pixel import refine_grids, sample_center, detect_grid_scale
    pp = {"refine_grids": refine_grids, "sample_center": sample_center,
          "detect_grid_scale": detect_grid_scale}

    src = cv2.imread(args.src, cv2.IMREAD_UNCHANGED)
    if src is None:
        raise SystemExit("无法读取: %s" % args.src)
    if src.ndim == 3 and src.shape[2] == 3:
        src = np.dstack([src, np.full(src.shape[:2], 255, np.uint8)])
    print("[input] %dx%d" % (src.shape[1], src.shape[0]))

    rx, ra = refine_sheet(src, pp)
    grid = find_boxes(ra)

    mask = (ra > ALPHA_THR).astype(np.uint8) * 255
    n, lab, stats, _ = cv2.connectedComponentsWithStats(mask, connectivity=8)
    clean = np.zeros_like(mask)
    for i in range(1, n):
        if stats[i, cv2.CC_STAT_AREA] >= 2:      # 去掉 1px 噪点
            clean[lab == i] = 255

    os.makedirs(args.out, exist_ok=True)
    manifest = []
    for r in range(ROWS):
        for c in range(COLS):
            i = r * COLS + c
            x0, y0, x1, y1 = grid[r][c][0], grid[r][c][2], grid[r][c][1], grid[r][c][3]
            rgba = np.dstack([rx[y0:y1, x0:x1], clean[y0:y1, x0:x1]]).copy()
            rgba[rgba[:, :, 3] == 0] = 0
            rgba = fill_transparent(rgba)
            name = "%02d_%s" % (i + 1, NAMES[i])
            cv2.imwrite(os.path.join(args.out, name + ".png"), rgba[:, :, [2, 1, 0, 3]])  # RGBA -> BGRA
            manifest.append({"index": i + 1, "row": r, "col": c, "name": NAMES[i],
                             "file": name + ".png", "width": x1 - x0, "height": y1 - y0})
    json.dump(manifest, open(os.path.join(args.out, "manifest.json"), "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)
    print("[output] 已写出 %d 个图标 -> %s" % (len(manifest), args.out))

    if args.sheet:
        sheet = np.dstack([rx, clean]).copy()
        sheet[sheet[:, :, 3] == 0] = 0
        cv2.imwrite(args.sheet, sheet[:, :, [2, 1, 0, 3]])  # RGBA -> BGRA
        print("[output] 优化后精灵表 -> %s" % args.sheet)
    if args.preview:
        s = 4
        cw = max(x1 - x0 for row in grid for (x0, _, x1, _) in row) + 2
        ch = max(y1 - y0 for row in grid for (_, y0, _, y1) in row) + 2
        prev = np.full((ROWS * ch * s, COLS * cw * s, 3), 255, np.uint8)
        for r in range(ROWS):
            for c in range(COLS):
                x0, y0, x1, y1 = grid[r][c][0], grid[r][c][2], grid[r][c][1], grid[r][c][3]
                ic = np.dstack([rx[y0:y1, x0:x1], clean[y0:y1, x0:x1]]).copy()
                ic[ic[:, :, 3] == 0] = [255, 255, 255, 255]
                big = cv2.resize(ic, ((x1 - x0) * s, (y1 - y0) * s), interpolation=cv2.INTER_NEAREST)
                oy = r * ch * s + (ch * s - big.shape[0]) // 2
                ox = c * cw * s + (cw * s - big.shape[1]) // 2
                prev[oy:oy + big.shape[0], ox:ox + big.shape[1]] = big[:, :, :3]
        cv2.imwrite(args.preview, prev[:, :, ::-1])
        print("[output] 4x 预览 -> %s" % args.preview)


if __name__ == "__main__":
    main()
