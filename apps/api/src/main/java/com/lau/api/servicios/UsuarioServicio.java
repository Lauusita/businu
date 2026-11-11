package com.lau.api.servicios;

import com.lau.api.dominio.Usuario;
import com.lau.api.repositorio.UsuarioRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServicio {
    private UsuarioRepositorio usuarioRepositorio;

    public void guardar(Usuario usuario) {
        usuarioRepositorio.save(usuario);
    }

    public List<Usuario> findAll() {
        return usuarioRepositorio.findAll();
    }
}
