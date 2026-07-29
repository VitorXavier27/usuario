package com.xavier.usuario.business;

import com.xavier.usuario.business.converter.UsuarioConverter;
import com.xavier.usuario.business.dto.UsuarioDTO;
import com.xavier.usuario.infrastructure.entity.Usuario;
import com.xavier.usuario.infrastructure.exceptions.ConflictException;
import com.xavier.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.xavier.usuario.infrastructure.repository.UsuarioRepository;
import com.xavier.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioDTO salvarUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(
                usuarioRepository.save(usuario)
        );
    }


    public void emailExiste(String email){
        try{
            boolean existe = verificarEmailExistente(email);
            if(existe){
                throw new ConflictException("Email ja cadastrado"+ email);
            }
        } catch (ConflictException e){
            throw new ConflictException("Email ja cadastrado" + e.getCause());
        }
    }

    public boolean verificarEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não Encontrado" + email)
        );
    }

    public void deletarUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token,UsuarioDTO dto){
        // busca email atraves do token
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        // criptografia senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        // busca usuario no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado"));
        // mesclagem de dados com o dto e o banco de dados para verificar a busca
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);
        // salvou os dados no banco de dados e depois converte para usuario dto
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }
}
