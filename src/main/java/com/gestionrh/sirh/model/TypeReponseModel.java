// Bloc 3
package com.gestionrh.sirh.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_type_reponse")
@AttributeOverride(name = "id", column = @Column(name = "id_type_reponse"))
public class TypeReponseModel extends ReferenceModel { protected TypeReponseModel() {} }
