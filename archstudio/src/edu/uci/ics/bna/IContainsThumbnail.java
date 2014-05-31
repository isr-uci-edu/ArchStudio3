package edu.uci.ics.bna;

import edu.uci.ics.bna.thumbnail.*;

public interface IContainsThumbnail extends Thing{

	public static final String THUMBNAIL_PROPERTY_NAME = "$$thumbnail";
	public static final String THUMBNAIL_INSET_PROPERTY_NAME = "$$thumbnailInset";
	
	public void setThumbnail(Thumbnail thumbnail);
	public Thumbnail getThumbnail();
	public void setThumbnailInset(int inset);
	public int getThumbnailInset();
	
}