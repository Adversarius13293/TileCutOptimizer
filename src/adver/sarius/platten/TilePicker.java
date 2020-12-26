package adver.sarius.platten;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Window;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class TilePicker extends JPanel {

	private static final long serialVersionUID = -4567025229354638653L;
	public final static short BASES_MODE = 1;
	public final static short CUTS_MODE = 2;
	private static Dimension prefInputSize;
	private TileCollectionModel model;
	private List<Tile> tiles;
	private short mode;

	// TODO: Dsiable save if max base count is smaller than sum of minimum.
	// TODO: have min never be higher than max.
	public TilePicker(TileCollectionModel model, List<Tile> tiles, short mode) {
		super();

		if (mode != BASES_MODE && mode != CUTS_MODE) {
			throw new UnsupportedOperationException("This mode is not supported!");
		}
		this.model = model;
		this.tiles = tiles;
		this.mode = mode;
		JFormattedTextField tmp = new JFormattedTextField(NumberFormat.getNumberInstance());
		tmp.setValue(new Double(3000.55));
		prefInputSize = tmp.getPreferredSize(); // TODO: just hard code values?

		setLayout(new BorderLayout());

		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

		JSpinner baseAmount = new JSpinner(new SpinnerNumberModel(model.getMaxBaseTiles(), 1, 99, 1));
		if (mode == BASES_MODE) {
			JPanel tmpTop = new JPanel();
			tmpTop.setLayout(new FlowLayout(FlowLayout.LEFT));
			tmpTop.add(new Label("Anzahl an verfügbaren Basisplatten insgesamt:"));
			baseAmount.addChangeListener(event -> model.setMaxBaseTiles((int) baseAmount.getValue()));
			tmpTop.add(baseAmount);
			limitPanelHeight(tmpTop);
			center.add(tmpTop);
		}

		if (tiles.isEmpty()) {
			tiles.add(new Tile());
		}

		tiles.forEach(t -> center.add(createNewEntry(t)));

		JButton addTile = new JButton("+");
		addTile.setFont(addTile.getFont().deriveFont(16f));
		addTile.setForeground(Color.GREEN);
		addTile.addActionListener(ae -> {
			Tile t = new Tile();
			tiles.add(t);
			center.add(createNewEntry(t), center.getComponentCount() - 1); // TODO: risky, find other solution
			center.getTopLevelAncestor().revalidate();
		});

		JPanel tmpPanel = new JPanel();
		tmpPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(addTile); // can't get button aligned to the left, so pack in in a panel first?...
		limitPanelHeight(tmpPanel);
		center.add(tmpPanel);

		add(center, BorderLayout.CENTER);

		JPanel tmpBottom = new JPanel();
		JButton saveButton = new JButton("Speichern");
		saveButton.addActionListener(ae -> {
			if (mode == BASES_MODE) {
				this.model.setMaxBaseTiles((int) baseAmount.getValue());
				this.model.setBaseTiles(tiles);
			} else {
				this.model.setCutTiles(tiles);
			}
			((Window) getTopLevelAncestor()).dispose(); // unsave cast?
		});
		tmpBottom.add(saveButton);

		JButton cancel = new JButton("Abbrechen");
		cancel.addActionListener(ae -> ((Window) getTopLevelAncestor()).dispose()); // unsave cast?
		tmpBottom.add(cancel);
		add(tmpBottom, BorderLayout.SOUTH);

		setVisible(true);
	}

	private JPanel createNewEntry(Tile tile) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton remove = new JButton("X");
		remove.setForeground(Color.RED);
		remove.addActionListener(ae -> {
			panel.setVisible(false);
			panel.getParent().remove(panel);
			tiles.remove(tile);
		});
		panel.add(remove);

		NumberFormat format = DecimalFormat.getInstance();

		JTextField surfaceField = new JTextField();
		surfaceField.setText((mode == CUTS_MODE && tile.getMinCount() > 1 ? (tile.getMinCount() + "x ") : "")
				+ (tile.getWidth() * tile.getHeight()));
		surfaceField.setPreferredSize(
				new Dimension((int) (prefInputSize.getWidth() * 1.5), (int) prefInputSize.getHeight()));
		surfaceField.setEnabled(false);

		panel.add(new Label("Höhe:"));
		JFormattedTextField input = new JFormattedTextField(format);
		input.setValue(new Double(tile.getHeight()));
		input.setPreferredSize(prefInputSize);
		input.addPropertyChangeListener("value", event -> {
			tile.setRect(tile.getX(), tile.getY(), tile.getWidth(), Double.parseDouble("" + event.getNewValue()));
			surfaceField.setText((mode == CUTS_MODE && tile.getMinCount() > 1 ? (tile.getMinCount() + "x ") : "")
					+ (tile.getWidth() * tile.getHeight()));
		});
		panel.add(input);

		panel.add(new Label("Breite:"));
		input = new JFormattedTextField(format);
		input.setValue(new Double(tile.getWidth()));
		input.setPreferredSize(prefInputSize);
		input.addPropertyChangeListener("value", event -> {
			tile.setRect(tile.getX(), tile.getY(), Double.parseDouble("" + event.getNewValue()), tile.getHeight());
			surfaceField.setText((mode == CUTS_MODE && tile.getMinCount() > 1 ? (tile.getMinCount() + "x ") : "")
					+ (tile.getWidth() * tile.getHeight()));
		});
		panel.add(input);

		if (mode == CUTS_MODE) {
			panel.add(new Label("Anzahl:"));
			JSpinner amount = new JSpinner(new SpinnerNumberModel(tile.getMinCount(), 0, 99, 1));
			amount.addChangeListener(e -> {
				tile.setMinCount((int) amount.getValue());
				surfaceField.setText((mode == CUTS_MODE && tile.getMinCount() > 1 ? (tile.getMinCount() + "x ") : "")
						+ (tile.getWidth() * tile.getHeight()));
			});
			// Currently disabled. Does not work well with algorithm and final display.
			// panel.add(amount);

			panel.add(new Label("Farbe:"));
			JButton buttonColor = new JButton(" ");
			buttonColor.setBackground(tile.getColor());
			buttonColor.addActionListener(ae -> {
				Color pickedColor = JColorChooser.showDialog(null, "Farbauswahl", tile.getColor());
				if (pickedColor != null) {
					buttonColor.setBackground(pickedColor);
					tile.setColor(pickedColor);
				}
			});
			panel.add(buttonColor);

		} else {
			panel.add(new Label("Minimum Anzahl:"));
			JSpinner min = new JSpinner(new SpinnerNumberModel(tile.getMinCount(), 0, 99, 1));
			min.addChangeListener(event -> tile.setMinCount((int) min.getValue()));
			panel.add(min);

			panel.add(new Label("Maximum Anzahl:"));
			JSpinner max = new JSpinner(new SpinnerNumberModel(tile.getMaxCount(), 0, 99, 1));
			max.addChangeListener(event -> tile.setMaxCount((int) max.getValue()));
			panel.add(max);
		}

		panel.add(new Label("Fläche:"));
		panel.add(surfaceField);

		limitPanelHeight(panel);

		return panel;
	}

	private void limitPanelHeight(JPanel panel) {
		panel.setMaximumSize(new Dimension(panel.getMaximumSize().width, panel.getPreferredSize().height));
	}
}
