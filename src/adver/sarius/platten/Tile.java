package adver.sarius.platten;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Tile extends Rectangle2D.Double {

	private static final long serialVersionUID = 3032976828817371660L;
	private static final Random r = new Random();
	private Color color;
	private int minCount = 0;
	private int maxCount = 99;
	private boolean remaining = true;
	private boolean fitting = false;
	private int base = -1;

	public Tile() {
		super();
		this.color = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
	}

	public Tile(double width, double height) {
		this();
		setRect(0, 0, width, height);
	}

	public Tile(Tile tile) {
		super(tile.x, tile.y, tile.width, tile.height);
		this.color = tile.color; // TODO: is the color object final?
		this.minCount = tile.minCount;
		this.maxCount = tile.maxCount;
		this.remaining = tile.remaining;
		this.fitting = tile.fitting;
		this.base = tile.base;
	}

	public void rotate() {
		this.setRect(this.getX(), this.getY(), this.getHeight(), this.getWidth());
	}

	public void setLocation(double posX, double posY) {
		this.setRect(posX, posY, this.getWidth(), this.getHeight());
	}

	public double getSurface() {
		return getWidth() * getHeight();
	}

	public int getMinCount() {
		return minCount;
	}

	public void setMinCount(int minCount) {
		this.minCount = minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isRemaining() {
		return remaining;
	}

	public void setRemaining(boolean remaining) {
		this.remaining = remaining;
	}

	public boolean isFitting() {
		return fitting;
	}

	public void setFitting(boolean fitting) {
		this.fitting = fitting;
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
	}

	@Override
	public String toString() {
		return getWidth() + " x " + getHeight();
	}
}
