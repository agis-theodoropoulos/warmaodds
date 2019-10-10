package com.eureton.warmaodds.services.impl;

import java.util.Locale;
import java.util.Map;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.FormatterService;
import com.eureton.warmaodds.types.Combination;
import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.types.Range;
import com.eureton.warmaodds.util.Util;
import com.eureton.warmaodds.views.ModifiersView;

public class PercentageFormatter implements FormatterService {

	@Override
	public String boxes(int n) { return simple(n); }

	@Override
	public String focus(int n) { return simple(n); }

	@Override
	public String transfers(int n) { return simple(n); }

	@Override
	public String count(int n) { return simple(n); }

	@Override
	public String probability(float p) {
		return String.format(Locale.US, "%6.2f%%", p * 100);
	}

	@Override
	public String damage(float n) {
		return String.format(Locale.US, "%.1f", n);
	}

	@Override
	public String combination(Combination c) {
		StringBuilder sb = new StringBuilder();
		int size = c.getSize();
		int span = c.getSpan();
		
		sb.append("{");
		for (int i = 0; i < size - 1; ++i) {
			int r = c.getRoll(i);

			sb.append(rollToString(r, i == span - 1, c.getMitigator(i)));
			sb.append(", ");
		}
		if (size > 0) {
			int r = c.getRoll(size - 1);

			sb.append(rollToString(r, size == span, c.getMitigator(size - 1)));
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public String input(Input i) {
		StringBuilder sb = new StringBuilder();
		final String nl = System.lineSeparator();
		final String cnl = "," + nl;

		sb.append(nl).
			append("{").append(nl).
				append("\tattacks: [").append(nl);
		for (int j = 0; j < i.attacks.length; ++j) {
			sb.append(attackStats(i.attacks[j], 2, j < i.attacks.length - 1));
		}
				sb.append("\t],").append(nl).
				append("\tboxes: ").append(i.boxes).append(cnl).
				append("\tfocus: ").append(i.focus).append(cnl).
				append("\tfury: ").append(i.fury).append(cnl).
				append("\tkds: ").append(i.kds).append(cnl).
				append("\ttough: ").append(i.tough).append(nl).
			append("}").append(nl);

		return sb.toString();
	}

	@Override
	public String range(Range r) {
		return String.format(
			Locale.US, 
			"[%s, %s]",
			rollToString(r.max),
			rollToString(r.min)
		);
	}

	@Override
	public String attackStats(AttackStats s) {
		return attackStats(s, 0, false);
	}

	@Override
	public int attackModifier(Constants.AttackModifier modifier) {
		int pos = 0;

		switch (modifier) {
		case NONE:					pos = 0; break;
		case REROLL:				pos = 1; break;
		case DISCARD_LOWEST:		pos = 2; break;
		case DISCARD_HIGHEST:		pos = 3; break;
		//case DISCARD_ANY_1:	pos = 4; break;
		//case REROLL_1S_AND_2S:		pos = 5; break;
		//case ADD_2_DISCARD_ANY_2:	pos = 6; break;
		default: throw new IllegalArgumentException();
		}

		return pos;
	}

	@Override
	public int damageModifier(Constants.DamageModifier modifier) {
		int pos = 0;

		switch (modifier) {
		case NONE:					pos = 0; break;
		case REROLL:				pos = 1; break;
		case DISCARD_LOWEST:		pos = 2; break;
		//case DISCARD_ANY_1:	pos = 3; break;
		//case REROLL_1S_AND_2S:		pos = 4; break;
		default: throw new IllegalArgumentException();
		}

		return pos;
	}

	@Override
	public int onHitModifier(Constants.OnHitModifier modifier) {
		int pos = 0;

		switch (modifier) {
		case NONE:				pos = 0; break;
		case DOUBLE_DAMAGE:		pos = 1; break;
		case MIN_1_DAMAGE:		pos = 2; break;
		case NO_ROLL_1_DAMAGE:	pos = 3; break;
		case NO_ROLL_3_DAMAGE:	pos = 4; break;
		case D3_PLUS_3_DAMAGE:	pos = 5; break;
		case KNOCKDOWN:			pos = 6; break;
		default: throw new IllegalArgumentException();
		}

		return pos;
	}

	@Override
	public int onCritModifier(Constants.OnCritModifier modifier) {
		int pos = 0;
	
		switch (modifier) {
		case NONE:		pos = 0; break;
		case EXTRA_DIE:	pos = 1; break;
		case KNOCKDOWN:	pos = 2; break;
		//case HALVE_ARM:	pos = 2; break;
		//case RFP:		pos = 3; break;
		default: throw new IllegalArgumentException();
		}

		return pos;
	}

	@Override
	public int onKillModifier(Constants.OnKillModifier modifier) {
		int pos = 0;

		switch (modifier) {
		case NONE:		pos = 0; break;
		case NO_TOUGH:	pos = 1; break;
		default: throw new IllegalArgumentException();
		}

		return pos;
	}

	private String simple(int n) {
		return String.format(Locale.US, "%2d", n);
	}

	private String rollToString(int roll) {
		return rollToString(roll, false, Combination.Mitigator.NONE);
	}

	private String rollToString(int roll, boolean isAggregate,
			Combination.Mitigator mitigator) {
		String s, m = "";

		switch (mitigator) {
		case NONE: m = " "; break;
		case FOCUS: m = "!"; break;
		case FURY: m = "*"; break;
		}
		
		switch (roll) {
		case Util.ANY: s = " ANY"; break;
		case Util.MISS: s = "MISS"; break;
		default:
			s = String.format("%s%2d%s", m, roll, isAggregate ? "+" : " ");
			break;
			
		}
		
		return s;
	}

	private String attackStats(AttackStats s, int tabCount, boolean comma) {
		StringBuilder sb = new StringBuilder();
		final String nl = System.lineSeparator();
		final String cnl = "," + nl;
		String type = "";
		String braceLead = "";
		for (int i = 0; i < tabCount; ++i) braceLead += "\t";
		final String attrLead = braceLead + "\t";

		switch (s.type) {
		case AttackStats.MAT: type = "MAT"; break;
		case AttackStats.RAT: type = "RAT"; break;
		case AttackStats.MGC: type = "MGC"; break;
		}

		sb.append(braceLead).append("{").append(nl).
			append(attrLead).append("type: ").append(type).append(cnl).
			append(attrLead).append("mat: ").append(mat(s)).append(cnl).
			append(attrLead).append("def: ").append(def(s)).append(cnl).
			append(attrLead).append("attackDice: ").append(s.attackDice).append(cnl).
			append(attrLead).append("pow: ").append(pow(s)).append(cnl).
			append(attrLead).append("arm: ").append(s.arm).append(cnl).
			append(attrLead).append("damageDice: ").append(s.damageDice).append(cnl).
			append(attrLead).append("attackModifier: ").append(s.attackModifier).append(cnl).
			append(attrLead).append("damageModifier: ").append(s.damageModifier).append(cnl).
			append(attrLead).append("onHitModifier: ").append(s.onHitModifier).append(cnl).
			append(attrLead).append("onCritModifier: ").append(s.onCritModifier).append(cnl).
			append(attrLead).append("onKillModifier: ").append(s.onKillModifier).append(cnl).
			append(attrLead).append("hitProbability: ").append(s.hitProbability).append(cnl).
			append(attrLead).append("critProbability: ").append(s.critProbability).append(cnl).
			append(attrLead).append("damage: ").append(array(s.damage)).append(cnl).
			append(attrLead).append("killRolls: ").append(array(s.killRolls)).append(cnl).
			append(attrLead).append("focusKillRolls: ").append(array(s.focusKillRolls)).append(cnl).
			append(attrLead).append("furyKillRolls: ").append(array(s.furyKillRolls)).append(cnl).
			append(attrLead).append("betterAttacks: ").append(array(s.betterAttacks)).append(nl).
		append(braceLead).append("}").append(comma ? cnl : nl);

		return sb.toString();
	}

	private String mat(AttackStats attackStats) {
		return attackStats.mat != Util.AUT ?
				String.valueOf(attackStats.mat) :
				"AUTO";
	}

	private String def(AttackStats attackStats) {
		return attackStats.def != Util.KD ?
				String.valueOf(attackStats.def) :
				"KD";
	}

	private String pow(AttackStats attackStats) {
		return attackStats.pow != Util.ND ?
				String.valueOf(attackStats.pow) :
				"ND";
	}

	private String array(int[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");

		int i = 0;
		for (int r : array) {
			sb.append(r);
			if (++i < array.length) sb.append(", ");
		}

		return sb.append("]").toString();
	}
}

