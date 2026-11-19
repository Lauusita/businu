package com.lau.api.dominio.auditoria;

import com.lau.api.config.JpaConfig;
import org.hibernate.envers.RevisionListener;

/** Pone el autor (usuario del token) en cada revisión de Envers. */
public class AutorRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revision) {
        JpaConfig.usuarioActual().ifPresent(((Revision) revision)::setPersonalId);
    }
}
