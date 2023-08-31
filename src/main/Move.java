package main;

public class Move {
	public int a, b, c, d;
	public double value, valueCaptured;
	
	// CASTLING: value = +/-100 for white/black, a = +2/-2 for short/long, b = -1, c = 0, d = 0
	// CASTLING IN ICCF: treat it as a "normal" king move
	
	public Move(double value, int a, int b, int c, int d) {
		this.value = value;
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public void print() {
		System.out.println(this.value + ", " + this.a + " " + this.b + " " + this.c + " " + this.d);
	}

	public void printWithDepth(int depth) {
		if (depth <= 2)
			return;
		System.out.println(" - ".repeat(depth) + this.value + ", " + this.a + " " + this.b + " " + this.c + " " + this.d);
	}
}
