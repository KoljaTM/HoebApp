package de.vanmar.android.hoebapp.bo;

import java.util.Date;

/**
 * An object of this class represents a single media item (book, CD, etc.)
 *
 * @author Kolja
 */
public class Media {

	private String author;
	private String title;
	private Date loanDate;
	private Date dueDate;
	private String signature;
	private boolean canRenew;
	private String noRenewReason;
	private int numRenews;
	private String mediumId;
	private String type;
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

	public boolean isCanRenew() {
		return canRenew;
	}

	public void setCanRenew(boolean canRenew) {
		this.canRenew = canRenew;
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

	public String getMediumId() {
		return mediumId;
	}

	public void setMediumId(final String mediumId) {
		this.mediumId = mediumId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Media media = (Media) o;

		if (canRenew != media.canRenew) return false;
		if (numRenews != media.numRenews) return false;
		if (author != null ? !author.equals(media.author) : media.author != null) return false;
		if (dueDate != null ? !dueDate.equals(media.dueDate) : media.dueDate != null) return false;
		if (imgUrl != null ? !imgUrl.equals(media.imgUrl) : media.imgUrl != null) return false;
		if (loanDate != null ? !loanDate.equals(media.loanDate) : media.loanDate != null) return false;
		if (mediumId != null ? !mediumId.equals(media.mediumId) : media.mediumId != null) return false;
		if (noRenewReason != null ? !noRenewReason.equals(media.noRenewReason) : media.noRenewReason != null)
			return false;
		if (signature != null ? !signature.equals(media.signature) : media.signature != null) return false;
		if (title != null ? !title.equals(media.title) : media.title != null) return false;
		if (type != null ? !type.equals(media.type) : media.type != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = author != null ? author.hashCode() : 0;
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (loanDate != null ? loanDate.hashCode() : 0);
		result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
		result = 31 * result + (signature != null ? signature.hashCode() : 0);
		result = 31 * result + (canRenew ? 1 : 0);
		result = 31 * result + (noRenewReason != null ? noRenewReason.hashCode() : 0);
		result = 31 * result + numRenews;
		result = 31 * result + (mediumId != null ? mediumId.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (imgUrl != null ? imgUrl.hashCode() : 0);
		return result;
	}
}
