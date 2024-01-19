
package pl.at.bpmnprojekt.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.at.bpmnprojekt.domain.model.AppUser;
import pl.at.bpmnprojekt.domain.repository.AppUserRepository;
import pl.at.bpmnprojekt.payload.AuthenticatePayload;
import pl.at.bpmnprojekt.payload.RegisterPayload;

@Service
@Transactional
public class AuthenticationService {

    private static Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final AppUserRepository appUserRepository;

    public AuthenticationService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public void register(RegisterPayload payload) throws IllegalArgumentException, IllegalStateException {
        if (null == payload) {
            LOGGER.error("'payload' was null");
            throw new IllegalArgumentException("'payload' was null");
        }

        boolean present = appUserRepository.findByEmail(payload.email()).isPresent();
        if (present) {
            throw new IllegalStateException(String.format("User with email '%s' already exists", payload.email()));
        }

        AppUser appUser = new AppUser(null, payload.name(), payload.email(), payload.password());
        appUserRepository.save(appUser);
    }

    public boolean authenticate(AuthenticatePayload payload) throws EntityNotFoundException {
        if (null == payload) {
            LOGGER.error("'payload' was null");
            throw new IllegalArgumentException("'payload' was null");
        }

        AppUser appUser = appUserRepository.findByEmail(payload.email())
            .orElseThrow(() -> new EntityNotFoundException(String.format("User with email '%s' not found", payload.email())));

        return payload.password().equals(appUser.getPassword());
    }

}
