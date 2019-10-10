package com.eureton.warmaodds.views;

public interface AttackView {

	void setHit(String s);
	void setCritical(String s);
	void setDamage(String s);
	void setModified(boolean modified);
	void setStats(int type, int mat, int def, int attackDice, int pow, int arm,
			int damageDice);
}

