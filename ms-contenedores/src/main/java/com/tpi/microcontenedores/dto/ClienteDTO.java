package com.tpi.microcontenedores.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;

    @JsonAlias({"email"})
    private String mail;

    @JsonAlias({"telefono", "phone", "celular"})
    private String numero;
}
// si te queres ahorrar configuracion, pones los mismos nombres que en la entidad del otro microservicio
// y spring lo mapea automaticamente con feign o resttemplate
// Si queres cambiar algun nombre, podes usar @JsonProperty("nombre_en_otro_microservicio"), iria arriba del atributo