package org.alku.life_contract.accessory;

import java.util.List;

/**
 * 击杀充能：击杀生物累积层数，每层提供一份加成；受伤掉层、超时衰减。
 *
 * @param max         层数上限
 * @param perStack    每层提供的效果
 * @param decayTicks  每多少 tick 衰减 1 层（0 表示不衰减）
 * @param loseOnHurt  每次受伤损失几层
 */
public record AccessoryCharge(int max, List<AccessoryEffect> perStack, int decayTicks, int loseOnHurt) {

    public static final AccessoryCharge NONE = new AccessoryCharge(0, List.of(), 0, 0);

    public boolean isEmpty() {
        return max <= 0 || perStack.isEmpty();
    }
}
