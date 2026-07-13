package org.alku.life_contract.mutation;

public enum MutationNode {
    BLADE("活性激化：锋刃", 1, new int[]{5, 10, 20}, new String[]{
            "己方感染生物攻击力 +15%",
            "己方感染生物攻击力 +30%",
            "己方感染生物攻击力 +50%"
    }),
    ARMOR("外壳增生：重铠", 1, new int[]{5, 10, 20}, new String[]{
            "己方生物护甲 +2，最大生命值 +20%",
            "己方生物护甲 +4，最大生命值 +40%",
            "己方生物护甲 +6，最大生命值 +60%"
    }),
    NEST("生态蔓延：温床", 1, new int[]{8, 15}, new String[]{
            "己方生物在己方模组污染方块上获得【速度 I】",
            "玩家在己方模组污染方块上获得【生命恢复 I】"
    }),
    MARK("目标解构：标记", 1, new int[]{12}, new String[]{
            "玩家攻击过的敌对目标，会在 10 秒内遭到周围所有己方生物强制集火"
    }),
    SWARM("群体协同：蜂群", 2, new int[]{20, 30, 40}, new String[]{
            "基础生物生成数量上限 +50%，生命值 -10%",
            "基础生物生成数量上限 +100%，生命值 -10%",
            "数量上限 +100%，且生物死亡时触发小范围毒素爆炸"
    }),
    BEHEMOTH("基因重构：巨兽", 2, new int[]{20, 35, 50}, new String[]{
            "停止刷新基础生物，改为每 60 秒刷新一只精英变异体",
            "精英变异体体型增大 1.5 倍，攻击附带强力击飞效果",
            "精英变异体免疫绝大多数控制和负面效果"
    }),
    SENSE("神经突触：感知", 2, new int[]{15, 25}, new String[]{
            "己方生物索敌范围从 16 格扩大至 32 格",
            "索敌范围内发现敌人时，为玩家提供透视高亮（发光 Buff）"
    }),
    PARASITE("宿主侵染：寄生", 2, new int[]{40}, new String[]{
            "己方生物击杀敌对生物或玩家时，有 50% 几率将其转化为己方基础感染生物"
    }),
    CALAMITY("代号：天灾巨兽", 3, new int[]{120}, new String[]{
            "在玩家位置召唤 Boss 级变异体，降临时进行全服文本与音效播报"
    }),
    PURIFICATION("生态重塑：大净化", 3, new int[]{100}, new String[]{
            "获得大净化主动道具：转化半径 40 格内的生态，并降下仅伤害敌方的感染雨（冷却 5 分钟）"
    }),
    AIRDROP("信息战：空投劫留", 3, new int[]{80}, new String[]{
            "空投箱即将刷新时，在落点 10 格范围提前生成一圈己方精英生物进行防守"
    });

    public final String title;
    public final int tier;
    public final int[] costs;
    private final String[] effects;

    MutationNode(String title, int tier, int[] costs, String[] effects) {
        if (costs.length != effects.length) {
            throw new IllegalArgumentException(title + " 的费用等级与效果等级数量不一致");
        }
        this.title = title;
        this.tier = tier;
        this.costs = costs;
        this.effects = effects;
    }

    public int maxLevel() {
        return costs.length;
    }

    public int costForNext(int current) {
        return current >= maxLevel() ? -1 : costs[current];
    }

    public String effectAt(int level) {
        if (level < 1 || level > maxLevel()) {
            throw new IllegalArgumentException("无效词条等级: " + level);
        }
        return effects[level - 1];
    }

    public int requiredLevels() {
        return tier == 2 ? 5 : tier == 3 ? 12 : 0;
    }

    public MutationNode conflict() {
        return this == SWARM ? BEHEMOTH : this == BEHEMOTH ? SWARM : null;
    }
}
