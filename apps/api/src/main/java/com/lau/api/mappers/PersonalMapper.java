package com.lau.api.mappers;

import com.lau.api.dominio.Personal;
import com.lau.api.dto.personal.PersonalDTO;
import com.lau.api.dto.personal.PersonalResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PersonalMapper {

    public Personal requestToPersonal(PersonalDTO p) {
        return new Personal(
                p.tipoIdentificacion(),
                p.numeroIdentificacion(),
                p.nombres(),
                p.apellidos(),
                p.rol(),
                p.tipoVinculacion(),
                p.email(),
                LocalDate.now(),
                p.password()
        );
    }

    public PersonalResponse toResponse(Personal p) {
        return new PersonalResponse(
                p.getId(),
                p.getTipoIdentificacion(),
                p.getNumeroIdentificacion(),
                p.getNombres(),
                p.getApellidos(),
                p.getEmail(),
                p.getRol()
        );
    }
}
