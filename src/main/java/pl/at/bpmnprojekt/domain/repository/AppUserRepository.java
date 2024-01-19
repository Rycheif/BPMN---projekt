package pl.at.bpmnprojekt.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.at.bpmnprojekt.domain.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

}
