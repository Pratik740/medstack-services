package com.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrcpClient {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrcpClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    // localhost:9001/Billingservice/CreatePatientAccount
    // aws.grcp:123123/BillingService/CreatePatientAccount
    // service.address :service.grcp.port/BillingService/CreatePatientAccount
    public BillingServiceGrcpClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grcp.port:9001}") int serverPort
    ) {
        log.info("Connecting to Billing Service GRPC service at {}:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();

        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email) {
        BillingRequest request = BillingRequest.newBuilder().setName(name).setEmail(email).setPatientId(patientId).build();
        BillingResponse response = blockingStub.createBillingAccount(request);

        log.info("Received response from Billing service via GRPC: {}", response);
        return response;
    }
}
