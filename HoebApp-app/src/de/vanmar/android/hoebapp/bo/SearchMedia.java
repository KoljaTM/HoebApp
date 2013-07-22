package de.vanmar.android.hoebapp.bo;


/**
 * An object of this class represents a single media item (book, CD, etc.)
 * returned from search
 * 
 * @author Kolja
 * 
 */
public class SearchMedia {

	private String author;
	private String title;
	private String signature;
	private String id;
	private String type;
	private String year;
	private String imgUrl;

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

	public String getYear() {
		return year;
	}

	public void setYear(final String year) {
		this.year = year;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(final String imgUrl) {
		this.imgUrl = imgUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((imgUrl == null) ? 0 : imgUrl.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
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
		final SearchMedia other = (SearchMedia) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
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
		if (year == null) {
			if (other.year != null) {
				return false;
			}
		} else if (!year.equals(other.year)) {
			return false;
		}
		return true;
	}
}
