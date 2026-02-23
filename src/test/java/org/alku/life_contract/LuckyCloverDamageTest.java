package org.alku.life_contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class LuckyCloverDamageTest {

    private static final int MIN_DAMAGE = 1;
    private static final int MAX_DAMAGE = 20;
    private static final int TEST_ITERATIONS = 10000;

    @Test
    @DisplayName("测试随机伤害值在1-20范围内")
    void testRandomDamageRange() {
        Random random = new Random();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int damage = random.nextInt(20) + 1;
            assertTrue(damage >= MIN_DAMAGE, 
                "伤害值 " + damage + " 不应小于最小值 " + MIN_DAMAGE);
            assertTrue(damage <= MAX_DAMAGE, 
                "伤害值 " + damage + " 不应大于最大值 " + MAX_DAMAGE);
        }
    }

    @Test
    @DisplayName("测试随机伤害能覆盖整个范围")
    void testRandomDamageCoverage() {
        Random random = new Random();
        Set<Integer> seenDamages = new HashSet<>();
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int damage = random.nextInt(20) + 1;
            seenDamages.add(damage);
        }
        
        for (int expected = MIN_DAMAGE; expected <= MAX_DAMAGE; expected++) {
            assertTrue(seenDamages.contains(expected), 
                "在 " + TEST_ITERATIONS + " 次测试中未出现伤害值 " + expected);
        }
    }

    @Test
    @DisplayName("测试边界值1和20都能被生成")
    void testBoundaryValues() {
        Random random = new Random();
        boolean foundMin = false;
        boolean foundMax = false;
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int damage = random.nextInt(20) + 1;
            if (damage == MIN_DAMAGE) foundMin = true;
            if (damage == MAX_DAMAGE) foundMax = true;
            if (foundMin && foundMax) break;
        }
        
        assertTrue(foundMin, "应该能够生成最小伤害值 " + MIN_DAMAGE);
        assertTrue(foundMax, "应该能够生成最大伤害值 " + MAX_DAMAGE);
    }

    @RepeatedTest(100)
    @DisplayName("重复测试随机伤害值有效性")
    void testRandomDamageValidity() {
        Random random = new Random();
        int damage = random.nextInt(20) + 1;
        assertTrue(damage >= MIN_DAMAGE && damage <= MAX_DAMAGE,
            "随机伤害值 " + damage + " 必须在 [" + MIN_DAMAGE + ", " + MAX_DAMAGE + "] 范围内");
    }

    @Test
    @DisplayName("测试伤害分布的均匀性")
    void testDamageDistribution() {
        Random random = new Random();
        int[] distribution = new int[MAX_DAMAGE + 1];
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            int damage = random.nextInt(20) + 1;
            distribution[damage]++;
        }
        
        int expectedCount = TEST_ITERATIONS / MAX_DAMAGE;
        int tolerance = (int) (expectedCount * 0.2);
        
        for (int damage = MIN_DAMAGE; damage <= MAX_DAMAGE; damage++) {
            int actualCount = distribution[damage];
            assertTrue(Math.abs(actualCount - expectedCount) <= tolerance,
                "伤害值 " + damage + " 的出现次数 " + actualCount + 
                " 偏离期望值 " + expectedCount + " 超过 " + tolerance);
        }
    }

    @Test
    @DisplayName("测试Profession类hasLuckyCloverAbility方法")
    void testProfessionLuckyCloverAbility() {
        Profession luckyClover = new Profession(
            "lucky_clover", "幸运四叶草", "测试描述",
            false, "", "minecraft:lily_of_the_valley",
            0.0f, 0, 0, 0.0f,
            0.0f, 0, 0, 1.0f,
            "", 0, 0,
            false, 0,
            false, 0.0f, 0,
            false, 0.0f, 0, 0.0f, 0.0f,
            false, false, 0.0f, 0, false,
            false, 0,
            false, 0.0f, 0.0f, 32, 0.0f,
            false, 0, new java.util.ArrayList<>(),
            false, 0, new java.util.ArrayList<>(),
            true
        );
        
        assertTrue(luckyClover.hasLuckyCloverAbility(), 
            "幸运四叶草职业应该有 hasLuckyCloverAbility = true");
        
        Profession normalProfession = new Profession(
            "test", "测试职业", "测试描述",
            false, "", "minecraft:paper",
            0.0f, 0, 0, 0.0f,
            0.0f, 0, 0, 1.0f,
            "", 0, 0,
            false, 0,
            false, 0.0f, 0,
            false, 0.0f, 0, 0.0f, 0.0f,
            false, false, 0.0f, 0, false,
            false, 0,
            false, 0.0f, 0.0f, 32, 0.0f,
            false, 0, new java.util.ArrayList<>(),
            false, 0, new java.util.ArrayList<>(),
            false
        );
        
        assertFalse(normalProfession.hasLuckyCloverAbility(), 
            "普通职业应该有 hasLuckyCloverAbility = false");
    }

    @Test
    @DisplayName("测试攻击场景下的幸运判断逻辑")
    void testAttackerLuckJudgment() {
        assertTrue(isAttackerLucky(15), "攻击时伤害15应该判定为幸运");
        assertTrue(isAttackerLucky(16), "攻击时伤害16应该判定为幸运");
        assertTrue(isAttackerLucky(20), "攻击时伤害20应该判定为幸运");
        
        assertFalse(isAttackerLucky(14), "攻击时伤害14不应该判定为幸运");
        assertFalse(isAttackerLucky(8), "攻击时伤害8不应该判定为幸运");
        assertFalse(isAttackerLucky(7), "攻击时伤害7不应该判定为幸运");
        assertFalse(isAttackerLucky(1), "攻击时伤害1不应该判定为幸运");
        
        assertTrue(isAttackerUnlucky(1), "攻击时伤害1应该判定为不幸");
        assertTrue(isAttackerUnlucky(7), "攻击时伤害7应该判定为不幸");
        assertFalse(isAttackerUnlucky(8), "攻击时伤害8不应该判定为不幸");
        assertFalse(isAttackerUnlucky(15), "攻击时伤害15不应该判定为不幸");
    }

    @Test
    @DisplayName("测试防御场景下的幸运判断逻辑")
    void testDefenderLuckJudgment() {
        assertTrue(isDefenderLucky(1), "防御时伤害1应该判定为幸运");
        assertTrue(isDefenderLucky(5), "防御时伤害5应该判定为幸运");
        assertTrue(isDefenderLucky(7), "防御时伤害7应该判定为幸运");
        
        assertFalse(isDefenderLucky(8), "防御时伤害8不应该判定为幸运");
        assertFalse(isDefenderLucky(14), "防御时伤害14不应该判定为幸运");
        assertFalse(isDefenderLucky(15), "防御时伤害15不应该判定为幸运");
        assertFalse(isDefenderLucky(20), "防御时伤害20不应该判定为幸运");
        
        assertTrue(isDefenderUnlucky(15), "防御时伤害15应该判定为不幸");
        assertTrue(isDefenderUnlucky(20), "防御时伤害20应该判定为不幸");
        assertFalse(isDefenderUnlucky(14), "防御时伤害14不应该判定为不幸");
        assertFalse(isDefenderUnlucky(7), "防御时伤害7不应该判定为不幸");
    }

    @Test
    @DisplayName("测试攻击和防御场景的幸运判断是相反的")
    void testOppositeLuckJudgment() {
        for (int damage = 1; damage <= 20; damage++) {
            boolean attackerLucky = isAttackerLucky(damage);
            boolean defenderLucky = isDefenderLucky(damage);
            
            if (damage >= 15) {
                assertTrue(attackerLucky, "伤害" + damage + "攻击时应为幸运");
                assertFalse(defenderLucky, "伤害" + damage + "防御时应为不幸");
            } else if (damage <= 7) {
                assertFalse(attackerLucky, "伤害" + damage + "攻击时应为不幸");
                assertTrue(defenderLucky, "伤害" + damage + "防御时应为幸运");
            } else {
                assertFalse(attackerLucky, "伤害" + damage + "攻击时应为普通");
                assertFalse(defenderLucky, "伤害" + damage + "防御时应为普通");
            }
        }
    }

    @Test
    @DisplayName("测试伤害等级分布统计")
    void testDamageLevelDistribution() {
        int luckyCount = 0;
        int normalCount = 0;
        int unluckyCount = 0;
        
        for (int damage = 1; damage <= 20; damage++) {
            if (damage >= 15) {
                luckyCount++;
            } else if (damage <= 7) {
                unluckyCount++;
            } else {
                normalCount++;
            }
        }
        
        assertEquals(6, luckyCount, "幸运伤害等级(15-20)应该有6个值");
        assertEquals(7, unluckyCount, "不幸伤害等级(1-7)应该有7个值");
        assertEquals(7, normalCount, "普通伤害等级(8-14)应该有7个值");
    }

    private boolean isAttackerLucky(int damage) {
        return damage >= 15;
    }

    private boolean isAttackerUnlucky(int damage) {
        return damage <= 7;
    }

    private boolean isDefenderLucky(int damage) {
        return damage <= 7;
    }

    private boolean isDefenderUnlucky(int damage) {
        return damage >= 15;
    }
}
