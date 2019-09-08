package features;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Images extends JPanel {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Imagens");
		frame.setContentPane( new Images() );
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		Dimension tamanhoJanela = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation( (tamanhoJanela.width - frame.getWidth()) / 2,
				(tamanhoJanela.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	private final static int IMAGEM_LARGURA = 800;
	private final static int IMAGEM_ALTURA = 600;

	private BufferedImage offScreenImage;
	
	public Images() {
		setPreferredSize(new Dimension(IMAGEM_LARGURA, IMAGEM_ALTURA));
		offScreenImage = new BufferedImage(IMAGEM_LARGURA, IMAGEM_ALTURA,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = offScreenImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, offScreenImage.getWidth(), offScreenImage.getHeight());
		g.dispose();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(offScreenImage,0,0,null);
	}

	public void addImage(BufferedImage image, int x, int y) {
		Graphics g = offScreenImage.getGraphics();
		g.drawImage(image,x,y,null);
		g.dispose();
		repaint();
	}

	public void addImage(BufferedImage image, int x, int y, int largura, int altura) {
		Graphics g = offScreenImage.getGraphics();
		g.drawImage(image,x,y,largura,altura,null);
		g.dispose();
		repaint();
	}

}
