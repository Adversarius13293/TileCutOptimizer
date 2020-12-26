package adver.sarius.platten;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TileCollectionModel {

	private List<Tile> baseTiles;
	private int maxBaseTiles = 1;
	private List<Tile> cutTiles;
	private double zoom = 1;

	public TileCollectionModel() {
		baseTiles = new ArrayList<>();
		cutTiles = new ArrayList<>();
	}

	public List<Tile> getBaseTiles() {
		return baseTiles;
	}

	public List<Tile> getBaseTilesCopy() {
		return baseTiles.stream().map(Tile::new).collect(Collectors.toList());
	}

	public void setBaseTiles(List<Tile> baseTiles) {
		this.baseTiles = baseTiles;
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
	}

	public List<Tile> getCutTiles() {
		return cutTiles;
	}

	public List<Tile> getCutTilesCopy() {
		return cutTiles.stream().map(Tile::new).collect(Collectors.toList());
	}

	public void setCutTiles(List<Tile> cutTiles) {
		this.cutTiles = cutTiles;
	}

	public int getMaxBaseTiles() {
		return maxBaseTiles;
	}

	public void setMaxBaseTiles(int maxBaseTiles) {
		this.maxBaseTiles = maxBaseTiles;
	}
}
