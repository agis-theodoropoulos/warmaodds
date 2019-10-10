package com.eureton.warmaodds.services.impl;

import java.util.LinkedList;
import java.util.List;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.RangeService;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.types.Range;
import com.eureton.warmaodds.util.Util;

public class RangeServiceImpl implements RangeService {

	private static final String TAG = RangeServiceImpl.class.getSimpleName();
	
	@Override
	public Range getFull(InputMeta meta) {
		AttackStats s = meta.mInput.attacks[0];
		int max = s.maxDamageRoll;
		int min = s.isAutoHit(meta.mInput) ?  s.minDamageRoll : Util.MISS;

		return new Range(max, min);
	}

	@Override
	public List<Range> getRanges(InputMeta meta, int count) {
		List<Range> rs = new LinkedList<Range>();
		Range r = getFull(meta);

		int totalSize = getTotalRangeSize(meta);
		count = Math.min(count, totalSize);
		int itemSize = totalSize / count;
		int boostCount = totalSize % count;
		int curMax = r.max;

		for (int i = 0; i < count - 1; ++i) {
			int min = curMax - itemSize + 1;

			if (boostCount-- > 0) min--;
			rs.add(new Range(curMax, min));
			curMax = min - 1;
		}
		if (curMax < meta.mInput.attacks[0].minDamageRoll) curMax = r.min;
		rs.add(new Range(curMax, r.min));

		return rs;
	}
	
	@Override
	public int getRangeSize(Input input, AttackStats attackStats) {
		return attackStats.maxDamageRoll -
				attackStats.minDamageRoll + 
				(attackStats.isAutoHit(input) ? 1 : 2);
	}

	private int getTotalRangeSize(InputMeta meta) {
		AttackStats s = meta.mInput.attacks[0];

		return getRangeSize(meta.mInput, s);
	}
}

