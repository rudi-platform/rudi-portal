package org.rudi.microservice.kalim.storage.entity.mimetype;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.rudi.common.storage.entity.AbstractStampedEntity;
import org.rudi.microservice.kalim.core.common.SchemaConstants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mime_type", schema = SchemaConstants.DATA_SCHEMA)
@AttributeOverride(name = "code", column = @Column(name = AbstractStampedEntity.CODE_COLUMN_NAME, length = 100, nullable = false))
@Setter
@Getter
@ToString
@NoArgsConstructor
public class MimeTypeEntity extends AbstractStampedEntity {

	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_CODE = "code";
}
