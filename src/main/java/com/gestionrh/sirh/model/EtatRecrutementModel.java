// Bloc 3
package com.gestionrh.sirh.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_etat_recrutement")
@AttributeOverride(name = "id", column = @Column(name = "id_etat_recrutement"))
public class EtatRecrutementModel extends ReferenceModel { protected EtatRecrutementModel() {} }
