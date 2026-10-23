// Bloc 3
package com.gestionrh.sirh.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_etat_dossier")
@AttributeOverride(name = "id", column = @Column(name = "id_etat_dossier"))
public class EtatDossierModel extends ReferenceModel { protected EtatDossierModel() {} }
