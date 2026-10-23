package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.Classement;
import java.util.List;

public record ResultatClassement(Classement classement, String motif, List<ResultatCritereClassement> obligatoires,
        List<ResultatCritereClassement> souhaites) { }
