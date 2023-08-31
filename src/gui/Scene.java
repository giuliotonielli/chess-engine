package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import main.Engine;
import main.Move;

public class Scene {
	private Piece[][] pieces = new Piece[8][8];
	private Engine engine;
	private int clicks = 0, a = -1, b, c, d;
	private int[] opponentsMove;
	private double[][] board;
	private boolean waitingForMove = false, draggingMouse = false;
	Graphics2D g2;
	
	private BufferedImage bb, wb, br, wr, bk, wk, bq, wq, bp, wp, bn, wn, chessboard;
	
	public Scene(Engine engine) {
		this.engine = engine;
	}

	public void render(Graphics g) {
		g.drawImage(chessboard, 0, 0, 800, 800, null);
		drawPieces(g);
		drawHighlightedSquares(g);
		drawEvalBar(g);
	}

	private void drawEvalBar(Graphics g) {
		double eval = engine.getEval();
		double delta = Math.pow(Math.cbrt(eval), 2)*100;
		
		if (eval < 0)
			g.fillRect(800, 0, 50, (int)delta + 400);
		else
			g.fillRect(800, 0, 50, -(int)delta + 400);
		
		g.setFont(new Font("TimesRoman", Font.BOLD, 20)); //PLAIN
		
		if (delta >= 0) 
			 g.drawString("" + Math.round(eval*100.0)/100.0, 800, 780);
		else
			 g.drawString("" + Math.round(eval*100.0)/100.0, 800, 20);
	}

	private void drawHighlightedSquares(Graphics g) {
		if (a == -1)
			return;
		
		g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke((float) 5));
		g2.setColor(Color.DARK_GRAY);
		g2.drawRect(a, b, 98, 98);
		g2.setColor(Color.BLACK);
		g2.drawRect(c, d, 98, 98);
	}

	private void drawPieces(Graphics g) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (pieces[i][j] != null) {
					g.drawImage(pieces[i][j].img, j*100+10, i*100+10, 80, 80, null);
				}
			}
		}
	}


	public void mouseDragged(int x, int y) {
		if (!draggingMouse) {
			draggingMouse = true;
			mouseClicked(x, y);
		}
	}
	
	public void mouseReleased(int x, int y) {
		draggingMouse = false;
		mouseClicked(x, y);
	}


	public void mouseMoved(int x, int y) {
		// TODO Auto-generated method stub
		
	}
	

	public void mouseClicked(int x, int y) {
		if (waitingForMove) {
			if (clicks == 0) {
				opponentsMove = new int[4];
				opponentsMove[0] = (int) x/100;
				opponentsMove[1] = (int) y/100;
				
				if (board[opponentsMove[1]][opponentsMove[0]]*engine.getPlayersColor() <= 0) 
					return;
			}
			
			else if (clicks == 1) {
				opponentsMove[2] = (int) x/100;
				opponentsMove[3] = (int) y/100;
				
				if (board[opponentsMove[3]][opponentsMove[2]]*engine.getPlayersColor() > 0) {
					clicks = 0;
					mouseClicked(x, y);
					return;
				}
			}
			
			clicks++;
		}
	}
	
	public void importImages() {
		try {
			bb = ImageIO.read(getClass().getResourceAsStream("/bb.png"));
			wb = ImageIO.read(getClass().getResourceAsStream("/wb.png"));
			bk = ImageIO.read(getClass().getResourceAsStream("/bk.png"));
			wk = ImageIO.read(getClass().getResourceAsStream("/wk.png"));
			bq = ImageIO.read(getClass().getResourceAsStream("/bq.png"));
			wq = ImageIO.read(getClass().getResourceAsStream("/wq.png"));
			br = ImageIO.read(getClass().getResourceAsStream("/br.png"));
			wr = ImageIO.read(getClass().getResourceAsStream("/wr.png"));
			bp = ImageIO.read(getClass().getResourceAsStream("/bp.png"));
			wp = ImageIO.read(getClass().getResourceAsStream("/wp.png"));
			bn = ImageIO.read(getClass().getResourceAsStream("/bn.png"));
			wn = ImageIO.read(getClass().getResourceAsStream("/wn.png"));
			chessboard = ImageIO.read(getClass().getResourceAsStream("/chessboard.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateBoard() {
		board = engine.getBoard();
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] != 0)
					pieces[i][j] = new Piece(j, i, getImage(board[i][j]));
				else 
					pieces[i][j] = null;
			}
		}
	}

	private BufferedImage getImage(double d) {
		if (d == 3.2) 
			return wb;
		
		if (d == -3.2) 
			return bb;
		
		switch ((int)d) {
		case 1: return wp;
		case -1: return bp;
		case 3: return wn;
		case -3: return bn;
		case 5: return wr;
		case -5: return br;
		case 9: return wq;
		case -9: return bq;
		case 100: return wk;
		case -100: return bk;
		default: return null;
		}
	}
	

	public int[] getOpponentsMove() {
		waitingForMove = true;
		
		while (clicks < 2) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		waitingForMove = false;
		clicks = 0;
		
		return opponentsMove;
	}

	public void highlightLastMove(Move move) {
		a = move.a*100;
		b = move.b*100;
		c = move.c*100;
		d = move.d*100;
	}
}
