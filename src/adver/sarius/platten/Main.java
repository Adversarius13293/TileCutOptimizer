package adver.sarius.platten;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Main {
	// TODO: display dimension string of rotations in initial order
	// TODO: Use better class organization, not everything in static.
	// TODO: Zooming currently deletes other tabs, like not fitting tiles. Also
	// switches back to first tab.
	// TODO: clone button for tilePicker

	private static boolean consoleOutput = false;
	private static int debugCounter = 0;
	private static double minWasted = Double.MAX_VALUE;
	private static List<Tile> bestBases = new ArrayList<>();
	private static List<Tile> bestSetup = new ArrayList<>();

	public static void main(String[] args) {
		if(args.length > 0 && args[0].equals("debug")) {
			consoleOutput = true;
		}
		if (consoleOutput) {
			System.out.println("Unit Test Result: " + doSomeTests());
		}

		JFrame mainFrame = new JFrame("Plattenzuschnitt");
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		TileCollectionModel model = new TileCollectionModel();
		initExampleModel(model);

		JTabbedPane tab = new JTabbedPane();
		mainFrame.add(tab, BorderLayout.CENTER);

		JToolBar toolbar = new JToolBar();
		mainFrame.add(toolbar, BorderLayout.NORTH);
		JButton startButton = new JButton("Start");
		startButton.addActionListener(ae -> {
			minWasted = Double.MAX_VALUE;
			bestBases.clear();
			bestSetup.clear();

			tab.removeAll();
			tab.add(new JLabel("Bitte warten... Dies kann mehrere Minuten dauern."));

			if (consoleOutput) {
				System.out.println(new Date() + "Start computation.");
			}
			SwingUtilities.invokeLater(() -> startComputation(model, tab));
		});
		toolbar.add(startButton);

		JButton buttonBase = new JButton("Basisplatten");
		buttonBase.addActionListener(ae -> {
			JDialog d = new JDialog(mainFrame, "Einstellungen: Basisplatten");

			d.setPreferredSize(new Dimension((int) (0.5 * mainFrame.getWidth()), (int) (0.5 * mainFrame.getHeight())));
			JScrollPane scroll = new JScrollPane(
					new TilePicker(model, model.getBaseTilesCopy(), TilePicker.BASES_MODE));
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			d.add(scroll);

			d.setModal(true);
			d.pack();

			d.setVisible(true);
		});
		toolbar.add(buttonBase);

		JButton buttonCuts = new JButton("Zuschnitte");
		buttonCuts.addActionListener(ae -> {
			JDialog d = new JDialog(mainFrame, "Einstellungen: Zuschnitte");

			d.setPreferredSize(new Dimension((int) (0.5 * mainFrame.getWidth()), (int) (0.5 * mainFrame.getHeight())));
			JScrollPane scroll = new JScrollPane(new TilePicker(model, model.getCutTilesCopy(), TilePicker.CUTS_MODE));
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			d.add(scroll);

			d.setModal(true);
			d.pack();

			d.setVisible(true);
		});
		toolbar.add(buttonCuts);

		JPanel zoomPanel = new JPanel();
		zoomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JSpinner zoom = new JSpinner(new SpinnerNumberModel(model.getZoom(), 0.1, 10, 0.1));
		zoom.setEditor(new JSpinner.NumberEditor(zoom, "0%"));
		zoom.setToolTipText("Zoom");
		zoom.addChangeListener(event -> {
			model.setZoom((double) zoom.getValue());
			redrawTabs(tab, model.getZoom());
		});
		zoomPanel.add(zoom);
		toolbar.add(zoomPanel);

		mainFrame.setVisible(true);
		mainFrame.pack();
		mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	private static void initExampleModel(TileCollectionModel model) {
		model.getBaseTiles().add(new Tile(250.0, 125.0));
		model.getBaseTiles().add(new Tile(300.0, 150.0));

		model.getCutTiles().add(new Tile(204.5, 57.3));
		model.getCutTiles().add(new Tile(204.5, 57.3));
		model.getCutTiles().add(new Tile(138.7, 61.2));
		model.getCutTiles().add(new Tile(129.4, 60.3));
		model.getCutTiles().add(new Tile(138.7, 65.6));
		model.getCutTiles().add(new Tile(129.4, 29.0));
		model.getCutTiles().add(new Tile(175.0, 127.0));

		model.setMaxBaseTiles(3);
	}

	private static void tryEachBaseCombination(TileCollectionModel model, int currentBaseIndex,
			List<Tile> pickedBaseTiles) {
		if (currentBaseIndex >= model.getBaseTiles().size()) {
			// tried all bases and did not reach maximum. May still be enough for cutting
			if (pickedBaseTiles.size() > 0
					&& !(bestSetup.stream().filter(Tile::isFitting).count() == model.getCutTiles().size()
							&& pickedBaseTiles.stream().mapToDouble(Tile::getSurface).sum() > bestBases.stream()
									.mapToDouble(Tile::getSurface).sum())) {
				if (consoleOutput) {
					System.out.println(new Date() + " Try " + pickedBaseTiles.size() + " base combi " + ++debugCounter);
				}
				tryNewTile(pickedBaseTiles, 0, model.getCutTilesCopy());
			}
			return;
		} else {
			Tile currentBase = model.getBaseTiles().get(currentBaseIndex);

			// has no minimum amount, so try others first.
			if (currentBase.getMinCount() <= 0) {
				tryEachBaseCombination(model, currentBaseIndex + 1, pickedBaseTiles);
			}

			// add up to MaxCount of this base
			for (int i = 1; i <= currentBase.getMaxCount(); i++) {
				pickedBaseTiles.add(currentBase);

				if (bestSetup.stream().filter(Tile::isFitting).count() == model.getCutTiles().size()
						&& pickedBaseTiles.stream().mapToDouble(Tile::getSurface).sum() > bestBases.stream()
								.mapToDouble(Tile::getSurface).sum()) {
					// can't be a better solution than current best
					break;
				} else if (currentBase.getMinCount() <= i) {
					if (pickedBaseTiles.size() == model.getMaxBaseTiles() && pickedBaseTiles.size() > 0) {
						// Only, if there are no more bases that would have to be picked.
						for (int j = currentBaseIndex + 1; j < model.getBaseTiles().size(); j++) {
							if (model.getBaseTiles().get(j).getMinCount() > 0) {
								break;
							}
						}
						if (consoleOutput) {
							System.out.println(
									new Date() + " Try " + pickedBaseTiles.size() + " base combi " + ++debugCounter);
						}
						tryNewTile(pickedBaseTiles, 0, model.getCutTilesCopy());
						break;
					} else if (pickedBaseTiles.size() > model.getMaxBaseTiles()) {
						// would need more tiles of this type than allowed.
						break;
					} else {
						// added minimum amount of this tile, now may try others
						tryEachBaseCombination(model, currentBaseIndex + 1, pickedBaseTiles);
					}
				}
			}
			// tried all combinations. Remove everything again, to let the higher call try
			// without this base.
			pickedBaseTiles.removeIf(t -> t == currentBase);
		}
	}

	private static void redrawTabs(JTabbedPane tab, double zoom) {
		tab.removeAll();
		for (int i = 0; i < bestBases.size(); i++) {
			int x = i; // for the stream.
			TileDrawer drawer = new TileDrawer(
					bestSetup.stream().filter(p -> p.getBase() == x && p.isFitting()).collect(Collectors.toList()),
					bestBases.get(x), zoom);

			JScrollPane scroll = new JScrollPane(drawer);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			tab.add("Platte " + (i + 1) + " (" + bestBases.get(i).getWidth() + " x " + bestBases.get(i).getHeight()
					+ ")", scroll);
		}
	}

	private static void startComputation(TileCollectionModel model, JTabbedPane tab) {
		tryEachBaseCombination(model, 0, new ArrayList<>());

		if (consoleOutput) {
			System.out.println(new Date() + " Displaying result: Fitting "
					+ (bestSetup.stream().filter(Tile::isFitting).count()) + " of " + bestSetup.size());
		}
		if (bestSetup.stream().filter(Tile::isFitting).count() == bestSetup.size()) {
			redrawTabs(tab, model.getZoom());
		} else {
			tab.removeAll();
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			panel.add(new JLabel("Konnte nicht alle Zuschnitte auf die Basisplatten aufteilen."));

			JButton button = new JButton("Beste Ergebnis dennoch anzeigen.");
			button.addActionListener(ae -> {
				redrawTabs(tab, model.getZoom());
				button.setEnabled(false);
				tab.add(panel);
			});
			panel.add(button);
			panel.add(new JLabel("Fehlende Zuschnitte: " + bestSetup.stream().filter(t -> !t.isFitting())
					.map(Tile::toString).collect(Collectors.joining(", "))));
			tab.add(panel);
		}
	}

	public static void tryNewTile(List<Tile> pickedBaseTiles, int pickedBaseIndex, List<Tile> toCut) {
		if (toCut.stream().allMatch(t -> !t.isRemaining())) {
			if (!toCut.stream().anyMatch(b -> b.isFitting() && b.getBase() == pickedBaseIndex)) {
				// don't leave a base empty. Assume that every given base has to be filled.
				return;
			}
			if (pickedBaseIndex == pickedBaseTiles.size() - 1) {
				// compute new min waste
				double tmp = toCut.stream().filter(Tile::isFitting).map(Tile::getBase).distinct()
						.map(i -> pickedBaseTiles.get(i).getSurface()).reduce(0., (a, b) -> a + b);
				double tmp2 = tmp - toCut.stream().filter(t -> t.isFitting()).mapToDouble(t -> t.getSurface()).sum();
				// get every cut is more important than minimizing waste.
				if (toCut.stream().filter(Tile::isFitting).count() > bestSetup.stream().filter(Tile::isFitting).count()
						|| (toCut.stream().filter(Tile::isFitting).count() == bestSetup.stream().filter(Tile::isFitting)
								.count() && tmp2 < minWasted)) {
					minWasted = tmp2;
					bestSetup = toCut.stream().map(Tile::new).collect(Collectors.toList());
					bestBases = pickedBaseTiles.stream().map(Tile::new).collect(Collectors.toList());
				}
			} else {
				// first base finished, try next base with remainings.
				toCut.stream().filter(t -> !t.isFitting()).forEach(t -> t.setRemaining(true));
				tryNewTile(pickedBaseTiles, pickedBaseIndex + 1, toCut);
				toCut.stream().filter(t -> !t.isFitting()).forEach(t -> t.setRemaining(false));
			}
			return;
		}

		Tile firstRemainingTile = null;
		for (Tile tile : toCut) {
			if (!tile.isRemaining()) {
				continue;
			}
			firstRemainingTile = firstRemainingTile == null ? tile : firstRemainingTile;
			for (int r = 0; r < 2; r++) {
				if (r == 1) {
					tile.rotate();
				}
				Tile base = pickedBaseTiles.get(pickedBaseIndex);
				tile.setLocation(base.getX(), base.getY()); // initial alignment
				if (!base.contains(tile)) {
					continue; // test other orientation since it is too big.
				}

				for (int c = 0; c < 4; c++) { // Align at base corners
					// TODO: double loop and 0 and 1 multiplication instead?
					switch (c) {
					case 0:
						tile.setLocation(base.getMinX(), base.getMinY());
						break;
					case 1:
						tile.setLocation(base.getMinX(), base.getMaxY() - tile.getHeight());
						break;
					case 2:
						tile.setLocation(base.getMaxX() - tile.getWidth(), base.getMinY());
						break;
					case 3:
						tile.setLocation(base.getMaxX() - tile.getWidth(), base.getMaxY() - tile.getHeight());
						break;
					}

					if (toCut.stream().filter(p -> p.isFitting() && p.getBase() == pickedBaseIndex)
							.anyMatch(p -> p.intersects(tile))) {
						// test other alignments
						continue;
					}
					tile.setRemaining(false);
					tile.setFitting(true);
					tile.setBase(pickedBaseIndex);
					tryNewTile(pickedBaseTiles, pickedBaseIndex, toCut);
					tile.setFitting(false);
					tile.setRemaining(true);
				}

				// Align at every existing tile.
				// toCut.stream().filter(t -> t.getBase() == pickedBaseIndex &&
				// t.isFitting()).forEach(fit -> {
				for (Tile fit : toCut) { // for-loop is better with eclipse debugging
					if (fit.getBase() != pickedBaseIndex || !fit.isFitting()) {
						continue;
					}
					for (int i = 0; i < 8; i++) {
						// find all positions. Currently only existing corners.
						// TODO: May need some more positions
						switch (i) {
						case 0:
							tile.setLocation(fit.getMinX() - tile.getWidth(), fit.getMinY());
							break;
						case 1:
							tile.setLocation(fit.getMinX() - tile.getWidth(), fit.getMaxY() - tile.getHeight());
							break;
						case 2:
							tile.setLocation(fit.getMinX(), fit.getMaxY());
							break;
						case 3:
							tile.setLocation(fit.getMaxX() - tile.getWidth(), fit.getMaxY());
							break;
						case 4:
							tile.setLocation(fit.getMaxX(), fit.getMaxY() - tile.getHeight());
							break;
						case 5:
							tile.setLocation(fit.getMaxX(), fit.getMinY());
							break;
						case 6:
							tile.setLocation(fit.getMaxX() - tile.getWidth(), fit.getMinY() - tile.getHeight());
							break;
						case 7:
							tile.setLocation(fit.getMinX(), fit.getMinY() - tile.getHeight());
							break;
						}

						if (!base.contains(tile)) {
							continue;
						}
						if (toCut.stream().filter(p -> p.isFitting() && p.getBase() == pickedBaseIndex)
								.anyMatch(p -> p.intersects(tile))) {
							continue;
						}

						tile.setRemaining(false);
						tile.setFitting(true);
						tile.setBase(pickedBaseIndex);
						tryNewTile(pickedBaseTiles, pickedBaseIndex, toCut);
						tile.setFitting(false);
						tile.setRemaining(true);
					}
				} // );
			}
			// tried this tile in any position. No need to try it again on this base after
			// placing others.
			// TODO: With too many tiles I will need to try again. If the tile needs to be
			// in the middle for example.
			tile.setRemaining(false);
		}
		// if last one did not fit try again to find optimum, since only now everything
		// is tested. This way it won't get stuck, if first base is always too small.
		tryNewTile(pickedBaseTiles, pickedBaseIndex, toCut);
		// this will set all newly not fitting ones back to remaining, so the higher
		// call can try them again.
		boolean startSwitch = false;
		for (Tile t : toCut) {
			if (t == firstRemainingTile) {
				startSwitch = true;
			}
			if (startSwitch && !t.isFitting()) {
				t.setRemaining(true);
			}
		}
	}

	public static int doSomeTests() {
		Tile a = new Tile(2, 2);
		Tile b = new Tile(2, 2); // identical
		if (!a.intersects(b) || !b.intersects(a))
			return 1;

		b = new Tile(2, 2); // corner inside
		b.setLocation(1, 1);
		if (!a.intersects(b) || !b.intersects(a))
			return 2;

		b = new Tile(2, 2); // on line
		b.setLocation(2, 0);
		if (a.intersects(b) || b.intersects(a))
			return 3;

		b = new Tile(2, 3); // contains, only one side taller
		if (!a.intersects(b) || !b.intersects(a))
			return 4;
		if (a.contains(b) || !b.contains(a))
			return 5;

		b = new Tile(4, 4); // fully contains
		b.setLocation(-1, -1);
		if (!a.intersects(b) || !b.intersects(a))
			return 6;
		if (a.contains(b) || !b.contains(a))
			return 7;

		b = new Tile(4, 4); // far away
		b.setLocation(5, 4);
		if (a.intersects(b) || b.intersects(a))
			return 8;
		if (a.contains(b) || b.contains(a))
			return 9;

		b = new Tile(4, 2); // contains, sides longer
		b.setLocation(-1, 0);
		if (!a.intersects(b) || !b.intersects(a))
			return 10;
		if (a.contains(b) || !b.contains(a))
			return 11;

		b = new Tile(4, 4); // on line but bigger
		b.setLocation(-1, 2);
		if (a.intersects(b) || b.intersects(a))
			return 12;

		b = new Tile(2, 2); // on line and offset
		b.setLocation(1, 2);
		if (a.intersects(b) || b.intersects(a))
			return 13;

		return 0;
	}
}
