package org.alku.life_contract.mutation;

public enum MutationNode {
    BLADE("活性激化：锋刃", 1, new int[]{5,10,20}, "己方感染生物攻击力 +15% / +30% / +50%"),
    ARMOR("外壳增生：重铠", 1, new int[]{5,10,20}, "护甲 +2/+4/+6，最大生命 +20%/+40%/+60%"),
    NEST("生态蔓延：温床", 1, new int[]{8,15}, "污染方块上速度 I；玩家生命恢复 I"),
    MARK("目标解构：标记", 1, new int[]{12}, "攻击目标后，己方生物集火 10 秒"),
    SWARM("群体协同：蜂群", 2, new int[]{20,30,40}, "生成上限提高，生命降低；死亡毒素爆炸"),
    BEHEMOTH("基因重构：巨兽", 2, new int[]{20,35,50}, "基础生物替换为精英变异体"),
    SENSE("神经突触：感知", 2, new int[]{15,25}, "索敌范围翻倍；发现敌人时高亮"),
    PARASITE("宿主侵染：寄生", 2, new int[]{40}, "击杀敌对目标时 50% 转化"),
    CALAMITY("代号：天灾巨兽", 3, new int[]{120}, "召唤 Boss 级变异体并全服播报"),
    PURIFICATION("生态重塑：大净化", 3, new int[]{100}, "获得 5 分钟冷却的大净化主动道具"),
    AIRDROP("信息战：空投劫留", 3, new int[]{80}, "空投落点提前刷新己方精英守卫");

    public final String title;
    public final int tier;
    public final int[] costs;
    public final String effect;
    MutationNode(String title, int tier, int[] costs, String effect) {
        this.title = title; this.tier = tier; this.costs = costs; this.effect = effect;
    }
    public int maxLevel() { return costs.length; }
    public int costForNext(int current) { return current >= maxLevel() ? -1 : costs[current]; }
    public int requiredLevels() { return tier == 2 ? 5 : tier == 3 ? 12 : 0; }
    public MutationNode conflict() { return this == SWARM ? BEHEMOTH : this == BEHEMOTH ? SWARM : null; }
}
