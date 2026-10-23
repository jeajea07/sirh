// Bloc 3
package com.gestionrh.sirh.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_type_decision")
@AttributeOverride(name = "id", column = @Column(name = "id_type_decision"))
public class TypeDecisionModel extends ReferenceModel { protected TypeDecisionModel() {} }
