package com.eureton.warmaodds.tasks;

import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.tasks.Task;

public class Merger implements Task.Merger2 {

	@Override
	public State call(State allButKillState, State killState) {
		State merged = new State(allButKillState);

		merged.output.kill = killState.output.kill;

		return merged;
	}
}

