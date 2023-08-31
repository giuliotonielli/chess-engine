package gui;

import java.awt.Graphics;


public class Render {

	private GUI gui;

	public Render(GUI gui) {
		this.gui = gui;
	}

	public void render(Graphics g) {
		gui.getScene().render(g);
	}
}