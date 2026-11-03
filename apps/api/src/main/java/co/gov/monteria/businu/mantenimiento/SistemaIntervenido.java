package co.gov.monteria.businu.mantenimiento;

/** Sistema de la embarcación intervenido. Solo MOTOR exige lecturas de horómetro. */
public enum SistemaIntervenido {
    MOTOR,
    CASCO,
    ELECTRICO,
    AIRE_ACONDICIONADO,
    SEGURIDAD,
    OTRO;
}
