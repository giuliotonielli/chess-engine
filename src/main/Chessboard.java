package main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

// better to castle, worse if u can't castle unless reaching the end game

// TODO check value that could be captured by pieces, the higher the better
// TODO only check moves of pieces that are gonna have the next move

public class Chessboard {
	public double[][] board; 
	public double adv;
	public ArrayList<Move> moveList = new ArrayList<Move>();
	double toBeCaptured;
	
	ArrayList<Point> passedPawns;
	Hashtable<Point, Double> pawnIncr = new Hashtable<Point, Double>();
	Hashtable<Point, Double> knightIncr = new Hashtable<Point, Double>();
	Hashtable<Integer, Boolean> openFiles;
	
	public Chessboard(double[][] board, double adv, ArrayList<Move> moveList) {
		this.board = board;
		this.adv = adv;
		this.moveList = moveList;
		
		if (board == null) {
			setBoardUp(board);
			setHashtablesUp();
		}
	}

	private void setHashtablesUp() {
		// pawn increase in value
		pawnIncr.put(new Point(2, 5), 1.03);
		pawnIncr.put(new Point(3, 5), 1.03);
		pawnIncr.put(new Point(4, 5), 1.03);
		pawnIncr.put(new Point(5, 5), 1.03);
		pawnIncr.put(new Point(2, 4), 1.03);
		pawnIncr.put(new Point(5, 4), 1.03);
		pawnIncr.put(new Point(2, 3), 1.03);
		pawnIncr.put(new Point(5, 3), 1.03);
		pawnIncr.put(new Point(5, 2), 1.03);
		pawnIncr.put(new Point(3, 2), 1.03);
		pawnIncr.put(new Point(4, 2), 1.03);
		pawnIncr.put(new Point(2, 2), 1.03);
		pawnIncr.put(new Point(3, 4), 1.1);
		pawnIncr.put(new Point(4, 4), 1.1);
		pawnIncr.put(new Point(3, 3), 1.1);
		pawnIncr.put(new Point(4, 3), 1.1);
		
		// knights increase in value
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (i == 0 || j == 0 || i == 7 || j == 7)
					knightIncr.put(new Point(j, i), 0.95);
				else if (i<6 & j<6 & i>1 & j>1) 
					knightIncr.put(new Point(j, i), 1.03);
			}
		}
		
		knightIncr.put(new Point(3, 3), 1.04);
		knightIncr.put(new Point(4, 3), 1.04);
		knightIncr.put(new Point(3, 4), 1.04);
		knightIncr.put(new Point(4, 4), 1.04);
		
	}

	private void setBoardUp(double[][] board) {
		board = new double[8][8];
		
		// pawns
		for (int i = 0; i < 8; i++) {
			board[1][i] = -1;
			board[6][i] = 1;
		}
		
		// knights
		board[0][1] = -3;
		board[0][6] = -3;
		board[7][1] = 3;
		board[7][6] = 3;
		
		// bishops
		board[0][2] = -3.2;
		board[0][5] = -3.2;
		board[7][2] = 3.2;
		board[7][5] = 3.2;
		
		// rooks
		board[0][0] = -5;
		board[0][7] = -5;
		board[7][0] = 5;
		board[7][7] = 5;
		
		// queens
		board[0][3] = -9;
		board[7][3] = 9;
		
		// kings
		board[0][4] = -100;
		board[7][4] = 100;
		
		this.board = board;
	}
	
	public double evaluate() { 
		double eval = 0, value;
		toBeCaptured = 0;
		passedPawns = new ArrayList<Point>();
		openFiles = new Hashtable<Integer, Boolean>();
		
		//this.print();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				value = board[i][j];
				
				if (value == 0)
					continue;
				
				if (value == 3.2 || value == -3.2) {
					eval += evalBishop(value, j, i);
					continue;
				}
				
				double e = 0;
				switch((int)value) {
				case 1: e = evalPawn(1, j, i); break;
				case -1: e = evalPawn(-1, j, i); break;
				case 3: e = evalKnight(3, j, i); break;
				case -3: e = evalKnight(-3, j, i); break;
				case 5: e = evalRook(5, j, i); break;
				case -5: e = evalRook(-5, j, i); break;
				case 9: e = evalQueen(9, j, i); break;
				case -9: e = evalQueen(-9, j, i); break;
				case 100: e = evalKing(100, j, i); break;
				case -100: e = evalKing(-100, j, i); break;
				}
				
//				System.out.println(p);
//				System.out.println(e);
				
				eval += e;
			}
		}
		
//		this.print();
//		System.out.println(eval);
		if (moveList.size()%2 == 0) 
			eval += toBeCaptured/4;
		else
			eval -= toBeCaptured/4;
			
		
		return eval;
	}

	private double evalKnight(double v, int x, int y) {
		if (!(x == 1 | x == 6 | y == 1 | y == 6))
			v *= knightIncr.get(new Point(x,y));
		
		if (startingSquare(v, y)) 
			v *= 0.95;
		
		
		if (checkMoves(v))
			v = knightMoves(v, x, y);
		
		return v;
	}

	private boolean checkMoves(double v) {
		if ((moveList.size()%2 == 0 & v > 0) | (moveList.size()%2 != 0 & v < 0))
			return true;
		return false;
	}

	private boolean startingSquare(double v, int y) {
		if (((v > 0 & y == 7) | (v < 0 & y == 0)) & moveList.size() < 25)
			return true;
		return false;
	}

	private double evalPawn(double v, int x, int y) {
		if (isPassedPawn((int)v, x, y)) {
			passedPawns.add(new Point(x, y)); //TODO what does passed pawns do?
			v *= 1.18;
		}
		
		if (pawnIncr.keySet().contains(new Point(x,y)))
			v *= pawnIncr.get(new Point(x,y));
		
		if (checkMoves(v))
			pawnMoves((int)v, x, y);
			
		return v;
	}

	private boolean isPassedPawn(int v, int x, int y) {
		if (fileIsOpen(x - 1, y - v, -v) & fileIsOpen(x, y - v, -v) & fileIsOpen(x + 1, y - v, -v))
			return true;
		// TODO STORE OPEN and CLOSED FILES better than searching 8 times for open ones
		return false;
	}

	private double evalBishop(double v, int x, int y) {
		if (startingSquare(v, y)) 
			v *= 0.95;
		
		return elaborateMoves(v, bishopMoves(v, x, y, checkMoves(v)));
	}
	
	private double evalKing(double v, int x, int y) {
		
		// opening and middle game
		if (moveList.size() < 40) {
			
			// better if king is castled 
			if ((v > 0 & y == 7) || (v < 0 & y == 0)) {
				if (x == 1 || x == 6)
					v *= 1.008;
				if (x == 2)
					v *= 1.006;
			}
			
			// worse if king has moved but not for castling
			for (Move m : moveList) {
				if (m.value == v & Math.abs(m.a-m.c) != 2) {
					v *= 0.998;
				}
			}
			
			// worse if king is not on the last row
			if ((y != 0 & v < 0) || (y != 7 & v > 0))
				v *= 0.996;
			
			// worse if king is open
			double openSquares;
			openSquares = bishopMoves(v, x, y, false)[0];
			openSquares += rookMoves(v, x, y, false)[0];
			v *= 1 - openSquares/2200;
		}
		
		return v;
	}

	private double evalQueen(double v, int x, int y) {
		v = (elaborateMoves(v, rookMoves(v, x, y, checkMoves(v))) + elaborateMoves(v, bishopMoves(v, x, y, checkMoves(v))))/2;
			
		return v;
	}

	private double evalRook(double v, int x, int y) {
		// better if on open files
		if ((v > 0 & fileIsOpen(x, 0, 1)) || (v < 0 & fileIsOpen(x, 7, -1)))
			v *= 1.02;
		
		//TODO not doing the same for white? (see below)
		// find passed pawns in front of the rook if the rook is black
		if (v < 0) {
			for (int b = 0; b < 7; b++) {
				if (board[b][x] == -1.0 & isPassedPawn(-1, x, y))
					passedPawns.add(new Point(x, y));
			}
		}
		
		// better behind own or opponents passed pawns
		for (Point pawn : passedPawns) {
			if (pawn.x == x & ((board[pawn.y][x] == 1.0 & pawn.y < y) || (board[pawn.y][x] == -1.0 & pawn.y > y))) 
				v *= 1.02;
		}
			
		// better if on 2nd/7th rank
		if ((y == 1 & v > 0) || (y == 6 & v < 0))
			v *= 1.02;
		
		// better if on central files
		if (x == 3 || x == 4)
			v *= 1.015;
		
		return elaborateMoves(v, rookMoves(v, x, y, checkMoves(v)));
	}

	private boolean fileIsOpen(int x, int y, int v) {
		if (x == -1 | x == 8)
			return true;
		
		for (int b = y; b < 7 & b > 0; b += v) {
			if ((int)board[b][x] == v)
				return false;
		}
		
		return true;
	}

	private double[] rookMoves(double v, int x, int y, boolean myTurn) {
		int a = x;
		int b = y;
		int legalMoves = 0;
		double seeThroughValue = 0;
		double toAdd = 0;
		boolean countingLegals = true;
		
		while (a > 0) {
			a--;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}	
		
		a = x;
		countingLegals = true;
		while (a < 7) {
			a++;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		a = x;
		countingLegals = true;
		while (b > 0) {
			b--;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		b = y;
		countingLegals = true;
		while (b < 7) {
			b++;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		double[] output = new double[2];
		output[0] = (double)legalMoves;
		output[1] = seeThroughValue;
		
		toBeCaptured -= toAdd/v;
		
		return output;
	}
	
	private double[] bishopMoves(double v, int x, int y, boolean myTurn) {
		int a = x;
		int b = y;
		int legalMoves = 0;
		double seeThroughValue = 0;
		double toAdd = 0;
		boolean countingLegals = true;
		
		while (a < 7 & b < 7) {
			a++;
			b++;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		a = x;
		b = y;
		countingLegals = true;
		while (a < 7 & b > 0) {
			a++;
			b--;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		a = x;
		b = y;
		countingLegals = true;
		while (a > 0 & b > 0) {
			a--;
			b--;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		a = x;
		b = y;
		countingLegals = true;
		while (a > 0 & b < 7) {
			a--;
			b++;
			
			if (board[b][a]*v > 0)
				countingLegals = false;
			
			if (countingLegals) {
				if (myTurn)
					toAdd += board[b][a];
				legalMoves++;
			}
			
			if (board[b][a]*v < 0) {
				countingLegals = false;
				seeThroughValue += board[b][a];
			}
		}
		
		double[] output = new double[2];
		output[0] = (double)legalMoves;
		output[1] = seeThroughValue;
		
		toBeCaptured -= toAdd/v;
		
		return output;
	}
	
	private double knightMoves(double v, int a, int b) {
		int movesNumber = 0;
		double toAdd = 0;
		
		try {
			if (board[b - 1][a - 2]*v < 0)
				toAdd += board[b - 1][a - 2];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b + 1][a - 2]*v < 0)
				toAdd += board[b + 1][a - 2];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b - 1][a + 2]*v < 0)
				toAdd += board[b - 1][a + 2];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b + 1][a + 2]*v < 0)
				toAdd += board[b + 1][a + 2];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b - 2][a - 1]*v < 0)
				toAdd += board[b - 2][a - 1];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b - 2][a + 1]*v < 0)
				toAdd += board[b - 2][a + 1];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b + 2][a - 1]*v < 0)
				toAdd += board[b + 2][a - 1];
			movesNumber++;
		} catch (Exception e) {}
		
		try {
			if (board[b + 2][a + 1]*v < 0)
				toAdd += board[b + 2][a + 1];
			movesNumber++;
		} catch (Exception e) {}
		
		v *= 1 + movesNumber/150;
		
		toBeCaptured -= toAdd/v;
		
		return v;
	}
	
	private double pawnMoves(int v, int a, int b) {
		// not interested in pawn forward moves
		
		// pawn capture
		if (a != 7) 
			if (board[b - v][a+1]*v < 0) 
				toBeCaptured += Math.abs(board[b - v][a + 1]);
		
		if (a != 0) 
			if (board[b - v][a-1]*v < 0) 
				toBeCaptured += Math.abs(board[b - v][a - 1]);
		
		
		// en passant
		if ((b == 4 & v == -1) | (b == 3 & v == 1)) { //TODO the two b values were inversed
			Move lastMove = moveList.get(moveList.size() - 1);
			
			if (lastMove.d - lastMove.b == 2*v & lastMove.value == -1.0*v) {
				if (lastMove.c == a + 1) 
					toBeCaptured += Math.abs(lastMove.value);
				
				else if (lastMove.c == a - 1) 
					toBeCaptured += Math.abs(lastMove.value);
			}
		}
		
		return v;
	}
	
	private double elaborateMoves(double v, double[] moves) {
		double legalMoves = moves[0];
		double seeThroughValue = moves[1];
		
		// if it controls many squares better
		legalMoves *= 0.02;
		
		// if piece x-rays lots of value better
		if (seeThroughValue >= 100)
			seeThroughValue -= 82;
		else if (seeThroughValue <= -100)
			seeThroughValue += 82;
		
		seeThroughValue /= 120;
		
		if (v > 0)
			v -= seeThroughValue - legalMoves;
		else 
			v -= seeThroughValue + legalMoves;
		
		// if on the edge of the board slightly worse
		v *= 0.99;
				
		return v;
	}
	
	public void print() {
		for (int i = 0; i < 8; i++) {
			System.out.println(Arrays.toString(this.board[i]));
		}
		System.out.println();
	}
}
