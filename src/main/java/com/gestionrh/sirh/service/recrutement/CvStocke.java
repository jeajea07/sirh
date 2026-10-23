package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.TypeCv;

public record CvStocke(String nomOriginal, String chemin, TypeCv type, long tailleOctets) { }
