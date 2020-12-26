package adver.sarius.platten;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

public class TileDrawer extends JPanel {

	private static final long serialVersionUID = 1834242967233176490L;
	private List<Tile> tiles;
	private Tile base;
	private double zoom;
	private static int stringMargin = 12;

	public TileDrawer(List<Tile> tiles, Tile base, double zoom) {
		super();
		this.tiles = tiles;
		this.base = base;
		this.zoom = zoom;
		setPreferredSize(new Dimension((int) base.getBounds().getWidth(), (int) base.getBounds().getHeight()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		// g2.fill(base);
		g2.fillRect((int) (base.getX() * zoom), (int) (base.getY() * zoom), (int) (base.getWidth() * zoom),
				(int) (base.getHeight() * zoom));
		tiles.forEach(t -> {
			g2.setColor(t.getColor());
			g2.fillRect((int) (t.getX() * zoom), (int) (t.getY() * zoom), (int) (t.getWidth() * zoom),
					(int) (t.getHeight() * zoom));
			// g2.fill(t);
			g2.setColor(Color.WHITE);
			g2.drawString(t.getWidth() + " x " + t.getHeight(), (float) (t.getX() * zoom),
					(float) (t.getY() * zoom) + stringMargin);
		});
	}
}