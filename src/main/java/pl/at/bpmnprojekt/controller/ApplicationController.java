package pl.at.bpmnprojekt.controller;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.at.bpmnprojekt.ProcessConstants;

import java.util.Map;

@RestController
@RequestMapping("/")
public class ApplicationController {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationController.class);

    private final ZeebeClient client;

    public ApplicationController(ZeebeClient client) {
        this.client = client;
    }

    @PostMapping("/start")
    public void startProcessInstance(@RequestBody Map<String, Object> variables) {

        LOG.info("Starting process " + ProcessConstants.BPMN_PROCESS_ID + " with variables: " + variables);


        client.newCreateInstanceCommand()
            .bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .send();
    }

}
