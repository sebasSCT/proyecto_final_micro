package co.edu.uniquindio.talleruno.services.implementacion;

import co.edu.uniquindio.talleruno.documentos.Usuario;
import co.edu.uniquindio.talleruno.dtos.general.EmailDTO;
import co.edu.uniquindio.talleruno.dtos.usuario.ItemUsuarioDTO;
import co.edu.uniquindio.talleruno.excepciones.NoHayUsuariosException;
import co.edu.uniquindio.talleruno.repositorios.UsuarioRepository;
import co.edu.uniquindio.talleruno.services.interfaces.EmailService;
import co.edu.uniquindio.talleruno.services.interfaces.GeneralService;
import co.edu.uniquindio.talleruno.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralServiceImpl implements GeneralService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailServicio;
    private final JWTUtils jwtUtils;

    @Override
    public Page<ItemUsuarioDTO> listarUsuarios(Pageable pageable) throws Exception {
        Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);

        if (usuariosPage.isEmpty()) {
            throw new NoHayUsuariosException("No hay usuarios");
        }

        List<ItemUsuarioDTO> usuariosDTO = usuariosPage.getContent().stream()
                .map(usuario -> new ItemUsuarioDTO(
                        usuario.getCodigo(),
                        usuario.getEmail()))
                .collect(Collectors.toList());

        return new PageImpl<>(usuariosDTO, pageable, usuariosPage.getTotalElements());
    }

    @Override
    public void enviarLinkRecuperacion(String email) throws Exception {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if(usuario.isEmpty()){
            throw new Exception("No existe el usuario con el email " + email);
        }else if(!usuario.get().isEstado()){
            throw new Exception("Estás inactivo");
        }else{

            String token = jwtUtils.generarTokenPassword(usuario.get().getCodigo());
            emailServicio.enviarEmail(new EmailDTO("Recupera tu cuenta", email, token));
        }
    }
}
