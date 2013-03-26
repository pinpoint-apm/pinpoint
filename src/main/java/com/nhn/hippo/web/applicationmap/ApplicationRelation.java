package com.nhn.hippo.web.applicationmap;

/**
 * application map에서 application간의 관계를 담은 클래스
 * 
 * @author netspider
 */
public class ApplicationRelation {
	protected final String id;

	protected final Application from;
	protected final Application to;
	private final ResponseHistogram histogram;

	public ApplicationRelation(Application from, Application to, ResponseHistogram histogram) {
		if (from == null) {
			throw new NullPointerException("from must not be null");
		}
		if (to == null) {
			throw new NullPointerException("to must not be null");
		}
		this.id = from.getId() + to.getId();
		this.from = from;
		this.to = to;
		this.histogram = histogram;
	}

	public boolean isSelfCalled() {
		return from.getSequence() == to.getSequence();
	}

	public String getId() {
		return id;
	}

	public Application getFrom() {
		return from;
	}

	public Application getTo() {
		return to;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	public ApplicationRelation mergeWith(ApplicationRelation relation) {
		if (this.from.equals(relation.getFrom()) && this.to.equals(relation.getTo())) {
			this.histogram.mergeWith(relation.getHistogram());
		} else {
			throw new IllegalArgumentException("Can't merge.");
		}
		return this;
	}

	@Override
	public String toString() {
		return "ApplicationRelation [from=" + from + ", to=" + to + ", histogram=" + histogram + "]";
	}
}
