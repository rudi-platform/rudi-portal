package org.rudi.microservice.konsent.storage.entity.treatmentversion;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import org.rudi.microservice.konsent.core.common.SchemaConstants;
import org.rudi.microservice.konsent.storage.entity.common.AbstractMultilangualStampedEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "involved_population_category", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@AssociationOverride(name = "labels", joinTable = @JoinTable(name = "involved_population_category_dictionary_entry", joinColumns = @JoinColumn(name = "involved_population_category_fk"), inverseJoinColumns = @JoinColumn(name = "dictionary_entry_fk")))
public class InvolvedPopulationCategoryEntity extends AbstractMultilangualStampedEntity {
	private static final long serialVersionUID = 4513519721998137462L;

}
