package de.vanmar.android.hoebapp.bo;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * An object of this class represents a single media item (book, CD, etc.)
 * returned from details page
 * 
 * @author Kolja
 * 
 */
public class MediaDetails {

	public static class Stock {
		private String locationName;
		private String locationCode;
		private int inStock = 0;
		private List<Date> outOfStock = new LinkedList<Date>();

		public String getLocationName() {
			return locationName;
		}

		public void setLocationName(final String locationName) {
			this.locationName = locationName;
		}

		public String getLocationCode() {
			return locationCode;
		}

		public void setLocationCode(final String locationCode) {
			this.locationCode = locationCode;
		}

		public int getInStock() {
			return inStock;
		}

		public void setInStock(final int inStock) {
			this.inStock = inStock;
		}

		public List<Date> getOutOfStock() {
			return outOfStock;
		}

		public void setOutOfStock(final List<Date> outOfStock) {
			this.outOfStock = outOfStock;
		}
	}

	private String author;
	private String title;
	private String subTitle;
	private String signature;
	private String id;
	private String type;
	private String imgUrl;
	private String contents;

	private final List<Stock> stock = new LinkedList<MediaDetails.Stock>();

	public String getAuthor() {
		return author;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(final String subTitle) {
		this.subTitle = subTitle;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(final String signature) {
		this.signature = signature;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(final String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(final String contents) {
		this.contents = contents;
	}

	public List<Stock> getStock() {
		return stock;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result
				+ ((contents == null) ? 0 : contents.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((imgUrl == null) ? 0 : imgUrl.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + ((stock == null) ? 0 : stock.hashCode());
		result = prime * result
				+ ((subTitle == null) ? 0 : subTitle.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MediaDetails other = (MediaDetails) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (contents == null) {
			if (other.contents != null) {
				return false;
			}
		} else if (!contents.equals(other.contents)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (imgUrl == null) {
			if (other.imgUrl != null) {
				return false;
			}
		} else if (!imgUrl.equals(other.imgUrl)) {
			return false;
		}
		if (signature == null) {
			if (other.signature != null) {
				return false;
			}
		} else if (!signature.equals(other.signature)) {
			return false;
		}
		if (stock == null) {
			if (other.stock != null) {
				return false;
			}
		} else if (!stock.equals(other.stock)) {
			return false;
		}
		if (subTitle == null) {
			if (other.subTitle != null) {
				return false;
			}
		} else if (!subTitle.equals(other.subTitle)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
}
