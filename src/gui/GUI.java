package gui;

import javax.swing.JFrame;

import main.Engine;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color; 
 
public class GUI extends JFrame implements Runnable{  
	private GameScreen gameScreen;
	private Render render;
	private Scene scene;
	int frames = 0;
	
	public GUI(Engine engine) {
		gameScreen = new GameScreen(this);
		render = new Render(this);
		scene = new Scene(engine);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		add(gameScreen);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
		
		this.gameScreen.initInputs();
	}
	
	@Override
	public void run() {
		repaint();
	}
	
	public Render getRender() {
		return render;
	}
	
	public GameScreen getGameScreen() {
		return gameScreen;
	}
	
	public Scene getScene() {
		return scene;
	} 
}  
 