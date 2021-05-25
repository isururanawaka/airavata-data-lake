package org.apache.airavata.datalake.orchestrator.processor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.orchestrator.Configuration;
import org.apache.airavata.datalake.orchestrator.core.adaptors.StorageAdaptor;
import org.apache.airavata.datalake.orchestrator.core.processor.MessageProcessor;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEntity;
import org.apache.airavata.datalake.orchestrator.registry.persistance.DataOrchestratorEventRepository;
import org.apache.airavata.datalake.orchestrator.registry.persistance.EntityStatus;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationRequest;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowInvocationResponse;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowMessage;
import org.apache.airavata.datalake.orchestrator.workflow.engine.WorkflowServiceGrpc;
import org.apache.airavata.dataorchestrator.messaging.model.NotificationEvent;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * This class is responsible for pick events from inmemory store and publish events to registry and
 * Workflow engine
 */
public class OutboundEventProcessor implements MessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundEventProcessor.class);

    private Configuration configuration;
    private StorageAdaptor store;

    private DozerBeanMapper dozerBeanMapper;
    private DataOrchestratorEventRepository repository;

    private  final  ManagedChannel channel;
    private final  WorkflowServiceGrpc.WorkflowServiceBlockingStub workflowServiceStub;

    public OutboundEventProcessor(Configuration configuration, DataOrchestratorEventRepository repository) throws Exception {
        this.configuration = configuration;
        this.repository = repository;
        this.channel = ManagedChannelBuilder
                .forAddress(configuration.getOutboundEventProcessor().getWorkflowEngineHost(),
                        configuration.getOutboundEventProcessor().getWorkflowPort()).usePlaintext().build();
        this.workflowServiceStub = WorkflowServiceGrpc.newBlockingStub(channel);
        this.init();
    }

    @Override
    public void init() throws Exception {
        try {
            Class<StorageAdaptor> storeClass = (Class<StorageAdaptor>) Class.
                    forName(this.configuration.getInMemoryStorageAdaptor());
            store = storeClass.getDeclaredConstructor().newInstance();
            dozerBeanMapper = new DozerBeanMapper();
            BeanMappingBuilder orchestratorEventMapper = new BeanMappingBuilder() {
                @Override
                protected void configure() {
                    mapping(NotificationEvent.class, DataOrchestratorEntity.class);
                }
            };
            dozerBeanMapper.addMapping(orchestratorEventMapper);
        } catch (ClassNotFoundException | NoSuchMethodException |
                IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            LOGGER.error(" Error occurred while initiating Inbound event processor ", exception);
            throw exception;
        }

    }

    @Override
    public void close() throws Exception {
        this.channel.shutdown();
    }

    @Override
    public void run() {
        try {
            List<NotificationEvent> notificationEventList =
                    store.poll(configuration.getOutboundEventProcessor().getNumOfEventsPerPoll());

            List<NotificationEvent> notificationEvents = getLatestEventOfGivenPath(notificationEventList);

            notificationEvents.forEach(event -> {
                DataOrchestratorEntity entity = dozerBeanMapper.map(event, DataOrchestratorEntity.class);
                entity.setOccurredTime(new Date(event.getContext().getOccuredTime()));
                entity.setStatus(EntityStatus.RECEIVED.name());
                repository.save(entity);
                WorkflowMessage workflowMessage = WorkflowMessage
                        .newBuilder()
                        .setMessageId(event.getId())
                        .build();
                WorkflowInvocationRequest invocationRequest = WorkflowInvocationRequest
                        .newBuilder()
                        .setMessage(workflowMessage)
                        .build();
                this.workflowServiceStub.invokeWorkflow(invocationRequest);

            });


        } catch (Exception exception) {
            LOGGER.error("Error occurred at outbound event processor ", exception);
        }
    }


    private List<NotificationEvent> getLatestEventOfGivenPath(List<NotificationEvent> events) {
        Map<String, NotificationEvent> eventMap = new HashMap<>();
        events.forEach(event -> {
            eventMap.put(event.getResourceId(), event);
        });
        return new ArrayList<NotificationEvent>(eventMap.values());
    }
}