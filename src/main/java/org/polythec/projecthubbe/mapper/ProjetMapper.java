package org.polythec.projecthubbe.mapper;

import org.polythec.projecthubbe.dto.ProjetDTO;
import org.polythec.projecthubbe.dto.UserSummaryDTO;
import org.polythec.projecthubbe.entity.Projet;
import org.polythec.projecthubbe.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjetMapper {

    public ProjetDTO toDto(Projet projet) {
        if (projet == null) {
            return null;
        }

        ProjetDTO dto = new ProjetDTO();
        dto.setIdprojet(projet.getIdprojet());
        dto.setNom(projet.getNom());
        dto.setDescription(projet.getDescription());
        dto.setCreatedDate(projet.getCreatedDate());

        // Map owner to UserSummaryDTO
        if (projet.getOwner() != null) {
            UserSummaryDTO ownerDto = new UserSummaryDTO();
            ownerDto.setId(projet.getOwner().getId());
            ownerDto.setFirstName(projet.getOwner().getFirstName());
            ownerDto.setLastName(projet.getOwner().getLastName());
            ownerDto.setEmail(projet.getOwner().getEmail());
            dto.setOwner(ownerDto);
        }

        // Map members to set of UserSummaryDTO
        if (projet.getMembers() != null) {
            Set<UserSummaryDTO> memberDtos = projet.getMembers().stream()
                    .map(user -> {
                        UserSummaryDTO memberDto = new UserSummaryDTO();
                        memberDto.setId(user.getId());
                        memberDto.setFirstName(user.getFirstName());
                        memberDto.setLastName(user.getLastName());
                        memberDto.setEmail(user.getEmail());
                        return memberDto;
                    })
                    .collect(Collectors.toSet());
            dto.setMembers(memberDtos);
        }

        return dto;
    }

    public List<ProjetDTO> toDtoList(List<Projet> projets) {
        return projets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // You can also add a fromDto method if needed for creating/updating entities
    public Projet fromDto(ProjetDTO dto, UserRepository userRepository) {
        if (dto == null) {
            return null;
        }

        Projet projet = new Projet();
        projet.setIdprojet(dto.getIdprojet());
        projet.setNom(dto.getNom());
        projet.setDescription(dto.getDescription());

        // You'd need to fetch the real User entities
        if (dto.getOwner() != null) {
            userRepository.findById(dto.getOwner().getId())
                    .ifPresent(projet::setOwner);
        }

        return projet;
    }
}
