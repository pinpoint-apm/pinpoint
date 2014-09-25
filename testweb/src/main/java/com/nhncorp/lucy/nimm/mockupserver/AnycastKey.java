package com.nhncorp.lucy.nimm.mockupserver;

public class AnycastKey {

		private final int domainId;
		private final int idcId;

		AnycastKey(int domainId, int idcId) {
			this.domainId = domainId;
			this.idcId = idcId;
		}

		int getDomainId() {
			return domainId;
		}

		int getIdcId() {
			return idcId;
		}


		@Override
		public boolean equals(Object o) {
			if (null == o) return true;
			if (!(o instanceof AnycastKey)) return false;

			AnycastKey that = (AnycastKey) o;

			if (domainId != that.domainId) return false;
			if (idcId != that.idcId) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = domainId;
			result = 31 * result + idcId;
			return result;
		}

}