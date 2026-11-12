package com.lau.api.mappers;

import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.Rol;
import com.lau.api.dominio.enums.TipoIdentificacion;
import com.lau.api.dominio.enums.TipoVinculacion;
import com.lau.api.dto.personal.PersonalDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PersonalMapper {

    public Personal requestToPersonal(PersonalDTO p) {
        return new Personal(
                TipoIdentificacion.fromString(String.valueOf(p.tipoIdentificacion())),
                p.numeroIdentificacion(),
                p.nombres(),
                p.apellidos(),
                Rol.fromString(p.rol().toString()),
                TipoVinculacion.fromString(String.valueOf(p.tipoVinculacion())),
                p.email(),
                LocalDate.now(),
                p.password()
        );
    }

//    public PersonalDTO toResponse(Personal p) {
//        return new PersonalDTO(
//                p.getTipoIdentificacion(),
//                p.getNumeroIdentificacion(),
//                p.getNombres(),
//                p.getApellidos(),
//                p.getTelefono(),
//                p.getEmail(),
//                p.getRol(),
//                p.getTipoVinculacion()
//        )
//    }
}
