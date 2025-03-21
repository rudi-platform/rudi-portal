package org.rudi.common.storage.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.rudi.common.core.Ordered;
import org.rudi.common.core.Stamped;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entité technique administrable via un BackOffice
 * @author FNI18300
 */
@MappedSuperclass
@Setter
@Getter
@ToString
public abstract class AbstractStampedEntity extends AbstractLabelizedEntity implements Stamped, Ordered, Serializable {

	private static final long serialVersionUID = 2106712668832334687L;

	@Column(name = "opening_date", nullable = false)
	private LocalDateTime openingDate;

	@Column(name = "closing_date")
	private LocalDateTime closingDate;

	@Column(name = "order_", nullable = false)
	private int order;

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
