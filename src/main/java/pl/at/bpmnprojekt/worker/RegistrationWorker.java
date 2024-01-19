package pl.at.bpmnprojekt.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.at.bpmnprojekt.payload.AuthenticatePayload;
import pl.at.bpmnprojekt.payload.RegisterPayload;
import pl.at.bpmnprojekt.service.AuthenticationService;

import java.util.Map;

@Component
public class RegistrationWorker {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationWorker.class);

    private final ZeebeClient client;
    private final AuthenticationService authenticationService;

    public RegistrationWorker(ZeebeClient client, AuthenticationService authenticationService) {
        this.client = client;
        this.authenticationService = authenticationService;
    }

    @JobWorker(type = "create-account")
    public void handleCreatingAccount(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: create-account");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
        }

        final String name = (String) jobVariables.get("name");
        final String email = (String) jobVariables.get("email");
        final String password = (String) jobVariables.get("password");

        try {
            final RegisterPayload payload = new RegisterPayload(name, email, password);
            authenticationService.register(payload);
            jobClient.newCompleteCommand(job).send().join();
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.error("Error: ", e);
            client.newThrowErrorCommand(job).errorCode("registrationFailedError").send().join();
        } catch (Exception e) {
            LOGGER.error("Unexpected error ahs occurred", e);
            jobClient.newFailCommand(job).retries(3).errorMessage(e.getMessage()).send().join();
        }
    }

    @JobWorker(type = "complete-registration")
    public void handleCompleteRegistration(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: complete-registration");
        authProcessSucceeded(jobClient, job);
    }

    @JobWorker(type = "fail-registration")
    public void handleFailRegistration(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: fail-registration");
        authProcessFailed(jobClient, job);
    }

    @JobWorker(type = "verify-login-data")
    public void handleVerifyLoginData(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: verify-login-data");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
        }

        final String email = (String) jobVariables.get("email");
        final String password = (String) jobVariables.get("password");

        try {
            final AuthenticatePayload payload = new AuthenticatePayload(email, password);
            boolean result = !authenticationService.authenticate(payload);
            jobVariables.put("error", result);
            jobClient.newCompleteCommand(job).variables(jobVariables).send().join();
        } catch (EntityNotFoundException e) {
            LOGGER.error("Entity not found: ", e);
            jobVariables.put("error", true);
            jobClient.newCompleteCommand(job).variables(jobVariables).send().join();
        } catch (Exception e) {
            LOGGER.error("Unexpected error ahs occurred", e);
            jobClient.newFailCommand(job).retries(3).errorMessage(e.getMessage()).send().join();
        }
    }

    @JobWorker(type = "complete-login")
    public void handleCompleteLogin(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: complete-login");
        authProcessSucceeded(jobClient, job);
    }

    private void authProcessSucceeded(JobClient jobClient, ActivatedJob job) {
        final Map<String, Object> variables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
        }

        final String messageName = "authSucceededMessage";
        final String correlationKey = (String) variables.get("authProcessId");
        client.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationKey)
            .send().join();

        LOGGER.info("Message name (sent): " + messageName);
        LOGGER.info("Correlation key (created and sent): " + correlationKey);
        LOGGER.info("Then process instance goes into subscription mode with use of catching message event.");

        jobClient.newCompleteCommand(job).send().join();
    }

    @JobWorker(type = "fail-login")
    public void handleFailLogin(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: fail-login");
        authProcessFailed(jobClient, job);
    }

    private void authProcessFailed(JobClient jobClient, ActivatedJob job) {
        final Map<String, Object> variables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
        }

        final String messageName = "authFailedMessage";
        final String correlationKey = (String) variables.get("authProcessId");
        client.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationKey)
            .send().join();

        LOGGER.info("Message name (sent): " + messageName);
        LOGGER.info("Correlation key (created and sent): " + correlationKey);
        LOGGER.info("Then process instance goes into subscription mode with use of catching message event.");

        jobClient.newCompleteCommand(job).send().join();
    }

}
