package de.vanmar.android.hoebapp.bo;

import java.util.Date;

/**
 * An object of this class represents a single media item (book, CD, etc.)
 * 
 * @author Kolja
 * 
 */
public class Media {

	private String author;
	private String title;
	private Date loanDate;
	private Date dueDate;
	private String signature;
	private String renewLink;
	private String noRenewReason;
	private int numRenews;

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

	public Date getLoanDate() {
		return loanDate;
	}

	public void setLoanDate(final Date loanDate) {
		this.loanDate = loanDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(final Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getRenewLink() {
		return renewLink;
	}

	public void setRenewLink(final String renewLink) {
		this.renewLink = renewLink;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(final String signature) {
		this.signature = signature;
	}

	public String getNoRenewReason() {
		return noRenewReason;
	}

	public void setNoRenewReason(final String noRenewReason) {
		this.noRenewReason = noRenewReason;
	}

	public int getNumRenews() {
		return numRenews;
	}

	public void setNumRenews(final int numRenews) {
		this.numRenews = numRenews;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result
				+ ((loanDate == null) ? 0 : loanDate.hashCode());
		result = prime * result
				+ ((noRenewReason == null) ? 0 : noRenewReason.hashCode());
		result = prime * result + numRenews;
		result = prime * result
				+ ((renewLink == null) ? 0 : renewLink.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		final Media other = (Media) obj;
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (dueDate == null) {
			if (other.dueDate != null) {
				return false;
			}
		} else if (!dueDate.equals(other.dueDate)) {
			return false;
		}
		if (loanDate == null) {
			if (other.loanDate != null) {
				return false;
			}
		} else if (!loanDate.equals(other.loanDate)) {
			return false;
		}
		if (noRenewReason == null) {
			if (other.noRenewReason != null) {
				return false;
			}
		} else if (!noRenewReason.equals(other.noRenewReason)) {
			return false;
		}
		if (numRenews != other.numRenews) {
			return false;
		}
		if (renewLink == null) {
			if (other.renewLink != null) {
				return false;
			}
		} else if (!renewLink.equals(other.renewLink)) {
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
		return true;
	}

}
