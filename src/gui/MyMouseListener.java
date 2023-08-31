package gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class MyMouseListener implements MouseListener, MouseMotionListener {

	private GUI gui;

	public MyMouseListener(GUI gui) {
		this.gui = gui;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		gui.getScene().mouseDragged(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		gui.getScene().mouseMoved(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//gui.getScene().mouseClicked(e.getX(), e.getY());
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		gui.getScene().mouseReleased(e.getX(), e.getY());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
