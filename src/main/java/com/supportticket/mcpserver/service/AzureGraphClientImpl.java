package com.supportticket.mcpserver.service;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.UsersRequestBuilder;
import com.supportticket.mcpserver.dto.Assignee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Microsoft Graph API implementation of {@link AzureGraphClient}.
 *
 * <p>Authenticates with Azure Entra ID using a client credential (app registration)
 * and queries the {@code /users} endpoint to look up users by display name.
 * The tenant ID, client ID, and client secret are read from application
 * properties at startup.</p>
 */
@Service
@ConditionalOnProperty(prefix = "azure.entra", name = {"tenant-id", "client-id", "client-secret"})
public class AzureGraphClientImpl implements AzureGraphClient {

    private final GraphServiceClient graphServiceClient;

    /**
     * Constructs the client using app-registration credentials from
     * application properties.
     *
     * @param tenantId     Azure Entra ID tenant ID
     * @param clientId     app registration client ID
     * @param clientSecret app registration client secret
     */
    public AzureGraphClientImpl(
            @Value("${azure.entra.tenant-id}") String tenantId,
            @Value("${azure.entra.client-id}") String clientId,
            @Value("${azure.entra.client-secret}") String clientSecret) {

        this.graphServiceClient = new GraphServiceClient(
                new ClientSecretCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .build(),
                "https://graph.microsoft.com/.default"
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>Queries the Graph API with an OData {@code startswith} filter on
     * {@code displayName} and selects {@code id}, {@code displayName}, and
     * {@code mail} fields. The display name is sanitised before being embedded
     * in the filter to prevent OData injection.</p>
     */
    @Override
    public List<Assignee> findByDisplayName(String displayName) {
        String sanitised = sanitise(displayName);

        var response = graphServiceClient.users().get(config -> {
            UsersRequestBuilder.GetQueryParameters params = config.queryParameters;
            params.filter = "startswith(displayName,'" + sanitised + "')";
            params.select = new String[]{"id", "displayName", "mail"};
        });

        if (response == null || response.getValue() == null) {
            return Collections.emptyList();
        }

        return response.getValue().stream()
                .filter(u -> u.getId() != null)
                .map(this::toAssignee)
                .toList();
    }

    private Assignee toAssignee(User user) {
        return new Assignee(
                Objects.requireNonNull(user.getId()),
                user.getDisplayName() != null ? user.getDisplayName() : "",
                user.getMail() != null ? user.getMail() : ""
        );
    }

    /**
     * Strips characters that have special meaning in OData filter strings to
     * prevent filter injection.
     *
     * @param value the raw input value
     * @return the sanitised value safe for use in an OData filter
     */
    private String sanitise(String value) {
        return value.replace("'", "''")
                    .replace("\\", "");
    }
}
