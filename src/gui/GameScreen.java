package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;


public class GameScreen extends JPanel {

	private GUI gui;
	private MyMouseListener myMouseListener;

	public GameScreen(GUI gui) {
		this.gui = gui;
		setPreferredSize(new Dimension(850, 800));
	}
	
	public void initInputs() {
		myMouseListener = new MyMouseListener(gui);

		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);

		requestFocus();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		gui.getRender().render(g);
	}

}
