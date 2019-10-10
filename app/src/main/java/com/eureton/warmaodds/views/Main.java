package com.eureton.warmaodds.views;

import java.util.List;

public class Main {

	public final AttacksView attacksView;
	public final List<AttackView> attackViews;
	public final GlobalsView globalsView;
	public final UiView uiView;
	public final TotalsView totals;

	public Main(AttacksView attacksView, List<AttackView> attackViews,
			GlobalsView globalsView, UiView uiView, TotalsView totals) {
		this.attacksView = attacksView;
		this.attackViews = attackViews;
		this.globalsView = globalsView;
		this.uiView = uiView;
		this.totals = totals;
	}
}

