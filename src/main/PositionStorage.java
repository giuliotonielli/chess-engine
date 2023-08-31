//package main;
//
//import java.awt.Point;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class PositionStorage {
//	HashMap<Double, ArrayList<Point>> storage = new HashMap<Double, ArrayList<Point>>();
//
//	void setUp(double[][] board) {
//		double value;
//		ArrayList<Point> points;
//		Point point;
//		
//		for (int i = 0; i < 8; i++) {
//		    for (int j = 0; j < 8; j++) {
//		        value = board[i][j];
//		        
//		        if (value == 0)
//		        	continue;
//		        
//		        point = new Point(j, i);
//		        
//		        if (storage.containsKey(value)) {
//		            points = storage.get(value);
//		            points.add(point);
//		        } 
//		        
//		        else {
//		            points = new ArrayList<Point>();
//		            points.add(point);
//		            storage.put(value, points);
//		        }
//		        storage.put(value, points);
//		     }
//		}
//	}
//	
//	void update(Move move, double valueCaptured) {
//		boolean queening = whiteQueening(move) | blackQueening(move);
//		ArrayList<Point> points = storage.get(move.value);
//		
//		if (points == null) {
//			System.out.println(move.value);
//			System.out.println(storage);
//		}
//		for (int i = 0; i < points.size(); i++) 
//			if (points.get(i).x == move.a & points.get(i).y == move.b) {
//				if (!queening)
//					points.set(i, new Point(move.c, move.d));
//				else {
//					points.remove(i);
//					storage.get(9*move.value).add(new Point(move.c, move.d));
//				}
//			}
//		
//		if (valueCaptured == 0)
//			return;
//		
//		points = storage.get(valueCaptured);
//
//		for (int i = 0; i < points.size(); i++) 
//			if (points.get(i).x == move.c & points.get(i).y == move.d) 
//				points.remove(i);
//	}
//	
//	void undo(Move move, double valueCaptured) {
//		boolean queening = whiteQueening(move) | blackQueening(move);
//		ArrayList<Point> points = storage.get(move.value);
//		
//		if (queening) {
//			points.add(new Point(move.a, move.b));
//			points = storage.get(9*move.value);
//		}
//		
//		for (int i = 0; i < points.size(); i++) {
//			if (points.get(i).x == move.c & points.get(i).y == move.d) {
//				if (queening)
//					points.remove(i);
//				else 
//					points.set(i, new Point(move.a, move.b));
//			}
//		}
//		
//		if (valueCaptured != 0)
//			storage.get(valueCaptured).add(new Point(move.c, move.d));
//	}
//	
//	public boolean whiteQueening(Move m) {
//		if (true) 
//			return true;
//		
//		return false;
//	}
//	
//	public boolean blackQueening(Move m) {
//		if (false) 
//			return true;
//		
//		return false;
//	}
//	
//	public void removePiece(double v, int x, int y) {
//		for (Point p : storage.get(v)) {
//			if (p.x == x & p.y == y) {
//				storage.get(v).remove(p);
//				return;
//			}
//		}
//	}
//}
//
