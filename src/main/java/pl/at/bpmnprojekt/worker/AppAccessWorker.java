package pl.at.bpmnprojekt.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class AppAccessWorker {

    private static Logger LOGGER = LoggerFactory.getLogger(AppAccessWorker.class);

    private final ZeebeClient client;

    public AppAccessWorker(ZeebeClient client) {
        this.client = client;
    }

    @JobWorker(type = "login-or-register")
    public void handleLoginOrRegister(final JobClient jobClient, final ActivatedJob job) {
        LOGGER.info("Running: loginOrRegister");
        final Map<String, Object> variables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            LOGGER.info("Process variables & task/job variables (e.g. data submitted by user): " + entry.getKey() + " : " + entry.getValue());
        }

        final String authProcessId = UUID.randomUUID().toString();
        variables.put("authProcessId", authProcessId);

        final String messageName = "authRequestMessage";
        client.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(authProcessId)
            .variables(variables)
            .send().join();

        LOGGER.info("Message name (sent): " + messageName);
        LOGGER.info("Correlation key (created and sent): " + authProcessId);
        LOGGER.info("Then process instance goes into subscription mode with use of catching message event.");

        jobClient.newCompleteCommand(job).variables(variables).send().join();
    }

}
