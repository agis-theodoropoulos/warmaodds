package com.eureton.warmaodds.models;

public class State {

	public Input input;
	public Output output;
	
	public State(Input input, Output output) {
		this.input = new Input(input);
		this.output = new Output(output);
	}

	public State(State other) {
		input = new Input(other.input);
		output = new Output(other.output);
	}
}

