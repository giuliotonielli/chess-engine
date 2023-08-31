package main;

import java.util.ArrayList;

// IMPORTANT: this class is not strictly necessary for the bot to work, 
// but it is useful to analyse the moves chosen by the engine.

public class Node {
	public double eval;
	public double[][] board;
	public ArrayList<Node> nextNodes;
	public Move bestNextMove;
	public Move move;
	public Node prevNode;
	
	public Node(double eval, double[][] board, Move bestNextMove, ArrayList<Node> nextNodes, Move move, Node prevNode) {
		this.eval = eval;
		this.board = board;
		this.bestNextMove = bestNextMove;
		this.nextNodes = nextNodes;
		this.move = move;
		this.prevNode = prevNode;
	}

}
