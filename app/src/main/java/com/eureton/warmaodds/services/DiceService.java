package com.eureton.warmaodds.services;

public interface DiceService {

	float hit(int r, int n);
	float hitRr(int r, int n);
	float hitDl(int r, int n);
	float hitDh(int r, int n);

	float dmg(int r, int n);
	float dmgRr(int r, int n);
	float dmgDl(int r, int n);
	float dmgCum(int r, int n);
	float dmgCumRr(int r, int n);
	float dmgCumDl(int r, int n);

	float crit(int r, int n);
	float crit_rr(int r, int n);
	float crit_dl(int r, int n);
	float crit_dh(int r, int n);
	
	int min(int n);
	int max(int n);
	
	int min(int n, int s);
	int max(int n, int s);
}

