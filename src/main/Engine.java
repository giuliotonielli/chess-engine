// Created 29/12/2022 

// TODO
// starting now is too late

// TODO finish chessboard eval tobcaptred algo with pawn moves

// TODO
// does ai know how to en passant? check in pawn moves

// TODO
// might make everything faster: create a graph structure with nodes when going
// down the moves tree so that we don't have to research moves already explored again
// each time we will only have to search a move deeper and look determine the best one.

// TODO 
// make a defended by list, since capture till end isn't working
// OR only check captured of value >= 3 (or both)

// TODO put whiteTurn boolean back in place?

// TODO AI not castling 

// TODO after a few moves down on rough eval don't evaluate future moves anymore

package main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import gui.GUI;
import webdriver.URLreader;
import webdriver.Webdriver;

public class Engine {
	Scanner input = new Scanner(System.in);
	boolean outOfTheory = false, mate = false; 
	int COLOR;
	
	long evalTime = 0, totalTime, findTime, applyRevTime;
	int moveApply = 0;
	
	String nextURL = "https://www.365chess.com/opening.php";
	URLreader url;
	Chessboard chessboard;
	ArrayList<Move> moveList;
	static GUI gui;
	
	double currentEval = 0;
	double[] finalEvals;
	Move finalMove;
	
	double[][] board;
	
	HashMap<Character, Integer> charToInt = new HashMap<Character, Integer>();
	HashMap<String, double[]> fenToEval;
	HashMap<String, Integer> fenToOccurencies = new HashMap<String, Integer>();
	Webdriver w = new Webdriver();
	
	Node startingNode;
	
	int POSITIONS_EVALUATED;    
	
	int INTERFACE = 0;
	// 0: only console
	// 1: chess.com
	// 2: GUI
	
	int ENGINEDEPTH = 5; //TODO: enginedepth is increases when there are less pieces/less moves
	// most reasonable depth: 3 (23/04/2023)
	//                        4 (24/04/2023)
	//                        5 (23/07/2023)
	//                        6 (05/08/2023)
	//                        3 (07/08/2023) introduction of check till no more captures
	//                        5 (11/08/2023) 
	
	public Engine() {}
	
	public static void main(String[] args) {
		Engine engine = new Engine();
		gui = new GUI(engine);
		engine.start();
	}

	private void start() {
		if (INTERFACE == 1)
			w.start();
		
		processInitialInput();  
		setChessboardUp();
		setHashmapsUp();
		game();
	}

	private void setHashmapsUp() {
		charToInt.put('a', 0);
		charToInt.put('b', 1);
		charToInt.put('c', 2);
		charToInt.put('d', 3);
		charToInt.put('e', 4);
		charToInt.put('f', 5);
		charToInt.put('g', 6);
		charToInt.put('h', 7);
	}

	private void game() {
		if (COLOR == 1) 
			AI();
	
		while (!mate) {
			waitTurn();
			AI();
		}
	}

	private void AI() {
		POSITIONS_EVALUATED = 0;
		
		Move output = null;
		
		if (moveList.size() < 5 & !outOfTheory) { //without false
			String move = getMoveFromTheory();
			if (move != null) {
				System.out.println("from theory");
				output = algebraicToICCF(move);
				output.print();
				applyMove(output);
				updateGUI(output);
			}
		}
		
		if (output == null) 
			output = computeMove();
		
		if (INTERFACE == 1)
			w.sendMoveToDriver(output);

		updateGUI(output);
		}
	
	private Move computeMove() {
		fenToEval = new HashMap<String, double[]>();
		startingNode = new Node(currentEval, board, null, new ArrayList<Node>(), null, null);
		evalTime = 0;
		applyRevTime = 0;
		findTime = 0;
		moveApply = 0;
		totalTime = System.currentTimeMillis();
		
		System.out.println("STARTING NOW");
		
		if (COLOR == 1)
			currentEval = findMax(findAllLegalMoves(1, true), ENGINEDEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, startingNode); 
		else 
			currentEval = findMin(findAllLegalMoves(-1, true), ENGINEDEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, startingNode); 
		
		applyMove(finalMove);
		finalMove.print();
		
		System.out.println("time evaluating: " + evalTime + "ms");		
		System.out.println("pos evaluatd: " + POSITIONS_EVALUATED);
		System.out.println("find time: " + findTime + "ms");	
		System.out.println("apply rev time: " + applyRevTime + "ms");
		System.out.println("moves applied: " + moveApply);
		System.out.println("time total: " + (System.currentTimeMillis() - totalTime) + "ms");	
		
		return finalMove;  
	}
	
	private double findMin(Move[] moves, int depth, double a, double b, Node node) {
		double eval, beta = Double.POSITIVE_INFINITY, valueCaptured, min = 1500; 
		Move bestMove = new Move(0, 0, 0, 0, 0);
		
		for (Move m : moves) { 
			if (m == null)
				continue;
			
			valueCaptured = board[m.d][m.c];
			
			if (valueCaptured == 100.0)
				return Double.NEGATIVE_INFINITY;
			
			applyMove(m);  
			
			Node nextNode = new Node(0, board, null, new ArrayList<Node>(), m, node);
			node.nextNodes.add(nextNode);
			eval = minMax(1, depth-1, a, b, nextNode);
			
			reverseMove(m, valueCaptured); 
			
			// alpha beta pruning
			beta = Math.min(beta, eval);
			
			if (beta < a) 
				return eval; 
						
			b = Math.min(b, beta);
						
			if (eval < min) {
				bestMove = m;
				min = eval;
			}
		}
		
		node.bestNextMove = bestMove;
		
		if (depth == ENGINEDEPTH) 
			finalMove = bestMove;
		
		return min;
	}

	private double findMax(Move[] moves, int depth, double a, double b, Node node) {
		double eval, alpha = Double.NEGATIVE_INFINITY, valueCaptured, max = -1500;
		Move bestMove = new Move(0, 0, 0, 0, 0);
		
		for (Move m : moves) {
			if (m == null)
				continue;
			
			valueCaptured = board[m.d][m.c];
		
			if (valueCaptured == -100.0) 
				return Double.POSITIVE_INFINITY;
			
			applyMove(m);  
			
			Node nextNode = new Node(0, board, null, new ArrayList<Node>(), m, node);
			node.nextNodes.add(nextNode);
			eval = minMax(-1, depth-1, a, b, nextNode);
			
			reverseMove(m, valueCaptured); 
			
			// alpha beta pruning
			alpha = Math.max(alpha, eval);
			
			if (alpha > b) 
				return eval; 
			
			a = Math.max(a, alpha);
			
			if (eval > max) {
				bestMove = m;
				max = eval;
			}
		}
		
		node.bestNextMove = bestMove;
		
		if (depth == ENGINEDEPTH) 
			finalMove = bestMove;
		
		return max;
	}

	private double minMax(int color, int depth, double a, double b, Node node) {
		String fen = getFenNotation(board, color == 1);
		
		if (fenToOccurencies.get(fen) == 3) {
			node.eval = 0;
			return 0;
		}
		
		double[] fenInfo = fenToEval.get(fen);
		double eval = 0;
		
		if (fenInfo != null) {
			// old fen of >= depth than current one & we have an eval for that fen
			if (fenInfo[1] >= (double)depth) {
				node.eval = fenInfo[0];
				return fenInfo[0];
			}
		}
		
		if (depth <= 0) {
			//
			long t0 = System.currentTimeMillis();
			
			eval = chessboard.evaluate();
			
			evalTime += System.currentTimeMillis() - t0;
			POSITIONS_EVALUATED ++;
			//
			
			updateFenToEval(fen, eval, depth);
			
			node.eval = eval;
			return eval;
		}
		
		long t0 = System.currentTimeMillis();
		Move[] moves = findAllLegalMoves(color, depth > 0);
		findTime += System.currentTimeMillis() - t0;
		
		// TODO LENGTH AINT GONNA BE ZERO, BUT ALL MOVES WILL BE NULL PROBABLY
		if (moves.length == 0) {
			if (depth > 0) {
				// TODO make STALL work! define it without check somehow?
				// stall
				return 0;
			}
			
			// no more captures
			else {
				System.out.println("shouldnt be seeing me");
				eval = chessboard.evaluate();
				POSITIONS_EVALUATED ++;
				
				//eval *= 1 + color*moves.size()/150;	define moves before
			}
		}
		
		else if (color == 1) 
			eval = findMax(moves, depth, a, b, node);
		else 
			eval = findMin(moves, depth, a, b, node);
		
		
		updateFenToEval(fen, eval, depth);
		
		node.eval = eval;
		
		return eval;
	}

	private void updateFenToEval(String fen, double eval, double depth) {
		double[] info = new double[2];
		info[0] = eval;
		info[1] = depth;
		fenToEval.put(fen, info);
	}
	
	private Move[] findAllLegalMoves(int color, boolean all) {
		Move[] output = new Move[218];
		Move[] m; // = new Move[27];
		int added = 0;
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j]*color > 0) {
					m = getMovesAt(board[i][j], j, i); 
					
					for (Move move : m) {
						if (move == null)
							continue;
						
						if (all | board[move.d][move.c] != 0) {
							output[added] = move;
							added++;
						}
					}
				}
			}
		}
		
		return output;
	}
	
	private void applyMove(Move m) {
		moveApply++;
		long t0 = System.currentTimeMillis();
				
		double valueCaptured = board[m.d][m.c];
			
		board[m.b][m.a] = 0;
		
		// queening
		if (m.value == 1 & m.d == 0)
			board[m.d][m.c] = 9;
		else if (m.value == -1 & m.d == 7)
			board[m.d][m.c] = -9;
		else 
			board[m.d][m.c] = m.value;
			
		// castling
		if (Math.abs(m.value) == 100.0 & Math.abs(m.c-m.a) == 2) {
			if (m.value == 100.0 & m.c-m.a == 2) 
				rookCastle(new Move(5.0, 7, 7, 5, 7), true);
			else if (m.value == 100.0 & m.c-m.a == -2)
				rookCastle(new Move(5.0, 0, 7, 3, 7), true);
			else if (m.value == -100.0 & m.c-m.a == 2) 
				rookCastle(new Move(-5.0, 7, 0, 5, 0), true);
			else if (m.value == -100.0 & m.c-m.a == -2)
				rookCastle(new Move(-5.0, 0, 0, 3, 0), true);
		}
		
		// en passant
		if ((m.value == 1 | m.value == -1) & valueCaptured == 0 & m.c-m.a != 0) 
			board[m.d + (int)m.value][m.c] = 0;
			
		String fen = getFenNotation(board, moveList.size()%2 != 0);
		
		if (fenToOccurencies.get(fen) == null) 
			fenToOccurencies.put(fen, 0);
		
		fenToOccurencies.put(fen, fenToOccurencies.get(fen) + 1);
		
		moveList.add(m);
		
		applyRevTime += System.currentTimeMillis() - t0;
	}
	
	private void reverseMove(Move m, double valueCaptured) {
		long t0 = System.currentTimeMillis();
		
		String fen = getFenNotation(board, moveList.size()%2 == 0);
		
		board[m.d][m.c] = valueCaptured;
		board[m.b][m.a] = m.value;
		
		// undo castling
		if (Math.abs(m.value) == 100.0 & Math.abs(m.c-m.a) == 2) {
			if (m.value == 100.0 & m.c-m.a == 2) 
				rookCastle(new Move(5.0, 7, 7, 5, 7), false);
			else if (m.value == 100.0 & m.c-m.a == -2)
				rookCastle(new Move(5.0, 0, 7, 3, 7), false);
			else if (m.value == -100.0 & m.c-m.a == 2) 
				rookCastle(new Move(-5.0, 7, 0, 5, 0), false);
			else if (m.value == -100.0 & m.c-m.a == -2)
				rookCastle(new Move(-5.0, 0, 0, 3, 0), false);
		}
		
		// reverse en passant
		if ((m.value == 1 | m.value == -1) & valueCaptured == 0 & m.c-m.a != 0) 
			board[m.d + (int)m.value][m.c] = -m.value;
		
		if (fenToOccurencies.get(fen) == 1)
			fenToOccurencies.remove(fen);
		else
			fenToOccurencies.put(fen, fenToOccurencies.get(fen) - 1);
		
		moveList.remove(moveList.size() - 1);
		
		applyRevTime += System.currentTimeMillis() - t0;
	}
	
	private void rookCastle(Move m, boolean update) {
		if (update) {
			board[m.d][m.c] = m.value;
			board[m.b][m.a] = 0;
		}
			
		else {
			board[m.b][m.a] = m.value;
			board[m.d][m.c] = 0;
		}
	}
	
	private void applyMove2(Move m) {
		
		if (Math.abs(m.value) == 100.0 & Math.abs(m.c-m.a) == 2) {
			if (m.value == 100.0 & m.c-m.a == 2) 
				applyMove2(new Move(5.0, 7, 7, 5, 7));
			else if (m.value == 100.0 & m.c-m.a == -2)
				applyMove2(new Move(5.0, 0, 7, 3, 7));
			else if (m.value == -100.0 & m.c-m.a == 2) 
				applyMove2(new Move(-5.0, 7, 0, 5, 0));
			else if (m.value == -100.0 & m.c-m.a == -2)
				applyMove2(new Move(-5.0, 0, 0, 3, 0));
		}
		
		board[m.b][m.a] = 0;
		
		if (m.value == 1 & m.d == 0)
			board[m.d][m.c] = 9;
		else if (m.value == -1 & m.d == 7)
			board[m.d][m.c] = -9;
		else 
			board[m.d][m.c] = m.value;
	}

	private void reverseMove2(Move m, double valueCaptured) {
		if (Math.abs(m.value) == 100.0 & Math.abs(m.c-m.a) == 2) {
			if (m.value == 100.0 & m.c-m.a == 2) 
				reverseMove2(new Move(5.0, 5, 7, 7, 7), 0);
			else if (m.value == 100.0 & m.c-m.a == -2)
				reverseMove2(new Move(5.0, 3, 7, 0, 7), 0);
			else if (m.value == -100.0 & m.c-m.a == 2) 
				reverseMove2(new Move(-5.0, 5, 0, 7, 0), 0);
			else if (m.value == -100.0 & m.c-m.a == -2)
				reverseMove2(new Move(-5.0, 3, 0, 0, 0), 0);
		}
		
		board[m.d][m.c] = valueCaptured;
		board[m.b][m.a] = m.value;
	}

	private String getMoveFromTheory() {
		url = new URLreader(nextURL);
		
		if (url.doc == null) {
			outOfTheory = true;
			return null;
		}
		
		String[] moves = url.getMoves(moveList.size());
		
		if (moves.length == 1) {
			outOfTheory = true; 
			return null;
		}
			
		int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
		
		if (moveList.size() != 4) {
			nextURL = url.nextGivenIndex(randomNum);
			if (nextURL.length() == 0)
				outOfTheory = true;
		}
		
		return moves[randomNum];
	}

	private boolean inCheck(int color) {
		//TODO: am i even needed as a funciton :) ?
		Point p = new Point(0, 0); 
		
		int x = p.x;
		int y = p.y;
		
		boolean output = attackKing(knightMoves(3*color, x, y), color) | attackKing(bishopMoves(3.2*color, x, y), color)
				| attackKing(rookMoves(5*color, x, y), color) | attackKing(queenMoves(9*color, x, y), color);
		
		if (color == 1)
			return attackKing(pawnMoves(1, x, y), color) | output;
		
		return attackKing(pawnMoves(-1, x, y), color) | output;
	}
	
	private boolean attackKing(Move[] moves, int color) {
		for (Move move : moves) 
			if (move != null)
				if (board[move.d][move.c] == -move.value & !((int)move.value == color & move.a == move.c)) 
					return true;
		
		return false;
	}
	
	private Move[] concatArray(Move[] a, Move[] b) {
		Move[] output = new Move[a.length + b.length];
		
		for (int i = 0; i < a.length; i++) 
			output[i] = a[i];
		
		for (int i = a.length; i < a.length + b.length; i ++) 
			output[i] = b[i - a.length];
		
		return output;
	}

	private void waitTurn() {
		Move move; 
		int a = 0, b = 0, c = 0, d = 0;
		
		if (startingNode != null) {
			printNodeSequence(startingNode);
		}
			
		System.out.println("Make your move:");
		
		String opponentsMove = "";
		
		if (INTERFACE == 0) {
			opponentsMove = input.nextLine();
			
			a = charToInt.get(opponentsMove.charAt(0));
			b = 8 - Character.getNumericValue(opponentsMove.charAt(1));
			c = charToInt.get(opponentsMove.charAt(2));
			d = 8 - Character.getNumericValue(opponentsMove.charAt(3));
		}
		
		else if (INTERFACE == 1) {
			opponentsMove = w.getOpponentsMove();
			
			a = Character.getNumericValue(opponentsMove.charAt(0)) - 1;
			b = 8 - Character.getNumericValue(opponentsMove.charAt(1));
			c = Character.getNumericValue(opponentsMove.charAt(2)) - 1;
			d = 8 - Character.getNumericValue(opponentsMove.charAt(3));
		}
		
		else if (INTERFACE == 2) {
			int[] m = gui.getScene().getOpponentsMove();
			
			a = m[0];  
			b = m[1];
			c = m[2];
			d = m[3];
		}
		
		if (board[b][a]*COLOR >= 0) 
			move = new Move(board[d][c], c, d, a, b);
		else 
			move = new Move(board[b][a], a, b, c, d);
		
		move.print();
		applyMove(move);
		updateGUI(move);
		
		if (moveList.size() < 5 & !outOfTheory)
			getNextURL(move);
	}

	// FOLLOWING FUNCTION IS NOT NECESSARY! just for analyses
	private void printNodeSequence(Node node) {
		Move bestMove = node.bestNextMove;
		Node bestNode = null;
		
		if (node.nextNodes.size() == 0) 
			return;
		
		for (Node n : node.nextNodes) {
			Move m = n.move;
			System.out.println(n.eval + " " + m.value + " " + m.a + " " + m.b + " " + m.c + " " + m.d);
			
			if (bestMove == m) {
				bestNode = n;
			}
		}
		
		System.out.println("best: " + bestNode.eval + " " + bestMove.value + " " + bestMove.a + " " + bestMove.b + " " + bestMove.c + " " + bestMove.d);
		System.out.println();
		
		printNodeSequence(bestNode);
		return;
	}

	private void updateGUI(Move move) {
		if (INTERFACE == 2) {
			gui.getScene().updateBoard();
			
			if (move != null) 
				gui.getScene().highlightLastMove(move);
			
			gui.run();
		}
	}

	private void getNextURL(Move m) {
		url = new URLreader(nextURL);
		nextURL = url.getNextUrlGivenPlayersMove(m, charToInt, moveList.size());
			
		if (nextURL == null)
			outOfTheory = true;
	}
	
	public Move algebraicToICCF(String move) {
		switch (move.charAt(0)) {
		case 'N': return findEndSquare(move.substring(1), 3);
		case 'B': return findEndSquare(move.substring(1), 3.2);
		case 'R': return findEndSquare(move.substring(1), 5);
		case 'Q': return findEndSquare(move.substring(1), 9);
		case 'K': return findEndSquare(move.substring(1), 100);
		case 'O': return algToICCF(move);
		default: return findEndSquare(move, 1);
		}
	}
	
	private Move findEndSquare(String move, double value) {
		int c, d, coordinatesIndex;
			
		if (charToInt.get(move.charAt(0)) != null & move.length() == 2) 
			coordinatesIndex = 0;
		else if (move.charAt(0) == 'x') 
			coordinatesIndex = 1;
		else 
			coordinatesIndex = 2;
		
		c = charToInt.get(move.charAt(coordinatesIndex));
		d = 8 - Character.getNumericValue(move.charAt(coordinatesIndex+1));
		
		if (coordinatesIndex == 2)
			return findStartingSquare(charToInt.get(move.charAt(0)), value, c, d);
		else 
			return findStartingSquare(9, value, c, d);
	}

	private Move findStartingSquare(int info, double value, int c, int d) {
		
		int p, v = (int)value;
		Move[] moves = new Move[35];
		
		if (moveList.size()%2 == 0)
			p = -1;
		else
			p = 1;
		
		if (value == 3.2) 
			moves = bishopMoves(value*p, c, d);
		
		else {
			switch (v) {
			case 3: moves = knightMoves(v*p, c, d); break;
			case 5: moves = rookMoves(v*p, c, d); break;
			case 9: moves = queenMoves(v*p, c, d); break;
			case 100: moves = kingMoves(v*p, c, d); break;}
		}
		
		if (info == 9) {
			if (v == 1) {
				if ((int)board[d - p][c] == -v*p)
					return new Move(-v*p, c, d - p, c, d);
				else 
					return new Move(-v*p, c, d - 2*p, c, d);
			}
			
			for (Move m : moves) {
				if (board[m.d][m.c] == -value*p) 
					return new Move(-value*p, m.c, m.d, c, d);
			}
		}
		
		else {
			if (v == -p) 
				return new Move(v, info, d - p, c, d);
			
			for (Move m : moves) {
				if (board[m.d][m.c] == -value*p & (int)m.c == info)
					return new Move(-value*p, m.c, m.d, c, d);
			}
		}
		
		return null;
	}

	private Move algToICCF(String move) {
		if (move.length() < 5 & moveList.size()%2 == 0) 
			return new Move(100, 4, 7, 6, 7);
		
		else if (move.length() < 5) 
			return new Move(-100, 4, 0, 6, 0);
		
		else if (move.length() > 4 & moveList.size()%2 == 0) 
			return new Move(100, 4, 7, 2, 7);
		
		else 
			return new Move(-100, 4, 0, 2, 0);
	}
	
	private Move[] getMovesAt(double v1, int a, int b) {
		if (v1 == 3.2 || v1 == -3.2)
			return bishopMoves(v1, a, b);
		
		else {
			int v = (int)v1;
			
			switch (v) {
			case -1: return pawnMoves(v, a, b);
			case 1: return pawnMoves(v, a, b); 
			case -3: return knightMoves(v, a, b); 
			case 3: return knightMoves(v, a, b); 
			case -5: return rookMoves(v, a, b); 
			case 5: return rookMoves(v, a, b); 
			case -9: return queenMoves(v, a, b); 
			case 9: return queenMoves(v, a, b); 
			case -100: return kingMoves(v, a, b); 
			case 100: return kingMoves(v, a, b); 
			}
		}
		
		return null;
	}
	
	private Move[] pawnMoves(int v, int a, int b) {
		Move[] output = new Move[4];
		
		// pawn forward
		if (board[b - v][a] == 0) {
			output[0] = new Move(v, a, b, a, b - v);
				
			// double pawn jump
			if ((b == 1 & v == -1) | (b == 6 & v == 1))
				if (board[b - 2*v][a] == 0)
					output[1] = new Move(v, a, b, a, b - 2*v);
		}
		
		// pawn capture
		if (a != 7)
			if (board[b - v][a+1]*v < 0)
				output[2] = new Move(v, a, b, a+1, b - v);
				
		if (a != 0)
			if (board[b - v][a-1]*v < 0)
				output[3] = new Move(v, a, b, a-1, b - v);
		
		// en passant
		if ((b == 4 & v == -1) | (b == 3 & v == 1)) { //TODO the two b values were inversed
			Move lastMove = moveList.get(moveList.size() - 1);
			
			if (lastMove.d - lastMove.b == 2*v & lastMove.value == -1.0*v) {
				if (lastMove.c == a + 1) 
					output[2] = new Move(v, a, b, a+1, b - v);
				else if (lastMove.c == a - 1) 
					output[2] = new Move(v, a, b, a-1, b - v);
			}
		}
		
		return output;
	}
	
	private Move[] knightMoves(int v, int a, int b) {
		Move[] output = new Move[8];
		
		if (a - 2 >= 0 & b - 1 >= 0)
			if (board[b - 1][a - 2]*v <= 0)
				output[0] = new Move(v, a, b, a - 2, b - 1);
		if (a - 2 >= 0 & b + 1 < 8)
			if (board[b + 1][a - 2]*v <= 0)
				output[1] = new Move(v, a, b, a - 2, b + 1);
		if (a + 2 < 8 & b - 1 >= 0)
			if (board[b - 1][a + 2]*v <= 0)
				output[2] = new Move(v, a, b, a + 2, b - 1);
		if (a + 2 < 8 & b + 1 < 8)
			if (board[b + 1][a + 2]*v <= 0)
				output[3] = new Move(v, a, b, a + 2, b + 1);
		if (b - 2 >= 0 & a - 1 >= 0)
			if (board[b - 2][a - 1]*v <= 0)
				output[4] = new Move(v, a, b, a - 1, b - 2);
		if (b - 2 >= 0 & a + 1 < 8)
			if (board[b - 2][a + 1]*v <= 0)
				output[5] = new Move(v, a, b, a + 1, b - 2);
		if (b + 2 < 8 & a - 1 >= 0)
			if (board[b + 2][a - 1]*v <= 0)
				output[6] = new Move(v, a, b, a - 1, b + 2);
		if (b + 2 < 8 & a + 1 < 8)
			if (board[b + 2][a + 1]*v <= 0)
				output[7] = new Move(v, a, b, a + 1, b + 2);
		
		return output;
	}
	
	private Move[] bishopMoves(double v, int a, int b) {
		Move[] output = new Move[13];
		int added = 0;
		
		// up right
		int x = a;
		int y = b;
		
		while (x < 7 & y > 0) {
			x++;
			y--;
			
			if (board[y][x]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, x, y);
			added++;
			if (board[y][x]*v < 0) 
				break;
		}
		
		// up left
		x = a;
		y = b;
		
		while (x > 0 & y > 0) {
			x--;
			y--;
			
			if (board[y][x]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, x, y);
			added++;
			if (board[y][x]*v < 0) 
				break;
		}
		
		// down right
		x = a;
		y = b;
		
		while (x < 7 & y < 7) {
			x++;
			y++;
			
			if (board[y][x]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, x, y);
			added++;
			if (board[y][x]*v < 0) 
				break;
		}
		
		// down left
		x = a;
		y = b;
				
		while (x > 0 & y < 7) {
			x--;
			y++;
					
			if (board[y][x]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, x, y);
			added++;
			if (board[y][x]*v < 0) 
				break;
		}
		
		return output;
	}
	
	private Move[] rookMoves(int v, int a, int b) {
		Move[] output = new Move[14];
		int added = 0;
		
		int x = a;
		int y = b;
		
		// up
		while (y > 0) {
			y--;
			
			if (board[y][a]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, a, y);
			added++;
			if (board[y][a]*v < 0) 
				break;
		}
		
		// down
		y = b;
		while (y < 7) {
			y++;
			
			if (board[y][a]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, a, y);
			added++;
			if (board[y][a]*v < 0) 
				break;
		}
		
		// right
		while (x < 7) {
			x++;
			
			if (board[b][x]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, x, b);
			added++;
			if (board[b][x]*v < 0) 
				break;
		}
		
		// left
		x = a;
		while (x > 0) {
			x--;
			
			if (board[b][x]*v > 0)
				break;
			
			output[added] = new Move(v, a, b, x, b);
			added++;
			if (board[b][x]*v < 0) 
				break;
		}
		
		return output;
	}
	
	private Move[] queenMoves(int v, int a, int b) {
		Move[] output = concatArray(rookMoves(v, a, b), bishopMoves(v, a, b));
		
		return output;
	}
	
	private Move[] kingMoves(int v, int a, int b) {
		Move[] output = new Move[10];
		
		if (a > 0) {
			if (board[b][a - 1]*v <= 0)
				output[0] = new Move(v, a, b, a - 1, b);
			
			if (b > 0) 
				if (board[b-1][a-1]*v <= 0)
					output[1] = new Move(v, a, b, a - 1, b - 1); 
			if (b < 7) 
				if (board[b+1][a-1]*v <= 0)
					output[2] = new Move(v, a, b, a - 1, b + 1); 
		}
		
		if (a < 7) {
			if (board[b][a+1]*v <= 0)
				output[3] = new Move(v, a, b, a + 1, b);
			
			if (b > 0) 
				if (board[b-1][a+1]*v <= 0)
					output[4] = new Move(v, a, b, a + 1, b - 1); 
			if (b < 7) 
				if (board[b+1][a+1]*v <= 0)
					output[5] = new Move(v, a, b, a + 1, b + 1); 
		}
		
		if (b > 0)
			if (board[b-1][a]*v <= 0)
				output[6] = new Move(v, a, b, a, b - 1);
		
		if (b < 7)
			if (board[b+1][a]*v <= 0)
				output[7] = new Move(v, a, b, a, b + 1);
		
		
		Move e;
		if (board[b][5] == 0 & board[b][6] == 0) {
			e = new Move(v, 4, b, 6, b);
			if (castlingIsPossible(e, v/100))
				output[8] = e;
		}
		if (board[b][1] == 0 & board[b][2] == 0 & board[b][3] == 0) {
			e = new Move(v, 4, b, 2, b);
			if (castlingIsPossible(e, v/100))
				output[9] = e;
		}
		
		return output;
	}
	
	private boolean castlingIsPossible(Move e, double color) {
		
		// first, check if rook or king ever moved and if rook has ever been captured
		for (Move m : moveList) {
			if (m.value*color == 100.0 | (m.value*color == 5.0 & ((e.c == 2 & m.a == 0) | (e.c == 6 & m.a == 7))) | (e.d == m.d & ((m.c == 0 & e.c == 2) | (m.c == 7 & e.c == 6))))
				return false;
		}
		
		// TODO not considering if the king will be in check, simply not convenietn
//		applyMove(e);    //TODO: there was applymove2 here, now that function is deleted. maybe you don't need to apply all things to see if the move is good
//		
//		if (!inCheck(color)) 
//			output = true;
//
//		reverseMove(e, 0);
		
		return true;
	}
	
	private String getFenNotation(double[][] position, boolean white) {
		String output = "";
		double v;
		int counter = 0;
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				v = position[i][j];
				
				if (v == 0) {
					counter++;
					continue;
				}
				
				if (counter != 0) {
					output += counter;
					counter = 0;
				}
				
				output += getLetter(v);
			}
			
			if (counter != 0) {
				output += counter;
				counter = 0;
			}
		}
		
		if (white)
			output += 'w';
		else 
			output += 'b';
		
		// to add: en passant, ing and 50 move rule TODO
		
		return output;
	}

	private char getLetter(double w) {
		if (w == 3.2)
			return 'B';
		if (w == -3.2)
			return 'b';
		
		switch ((int)w) {
			case -1: return 'p';
			case -3: return 'n';
			case -5: return 'r';
			case -9: return 'q';
			case -100: return 'k';
			case 1: return 'P';
			case 3: return 'N';
			case 5: return 'R';
			case 9: return 'Q';
			case 100: return 'K';
		}
		
		return 'g';
	}

	private void setChessboardUp() {
		chessboard = new Chessboard(null, 0, new ArrayList<Move>());
		board = chessboard.board;
		moveList = chessboard.moveList;
		
		if (INTERFACE == 2) {
			gui.getScene().importImages();
			updateGUI(null);
		}
	}

	private void processInitialInput() {
		if (INTERFACE == 0)
			COLOR = getColor();
		else if (INTERFACE == 1)
			COLOR = w.getColor();
		else if (INTERFACE == 2)
			COLOR = -1;
	}

	private int getColor() {
		System.out.println("What color am I, black or white?");
		
		String color = input.nextLine();
		
		if (color.equals("white")) 
			return 1;
		
		if (color.equals("black"))
			return -1;
		
		System.out.println("make sure the spelling is right and you did not use capital letters!");
		return getColor();
	}
	
	public double[][] getBoard() {
		return board;
	}
	
	public double getEval() { 
		return currentEval;
	}
	
	public int getPlayersColor() {
		return -COLOR;
	}
}
