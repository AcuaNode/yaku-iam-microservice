package io.github.rafaviv.yakubackend.iam.infrastructure.external;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;

@Service
public class EquipmentExternalServiceImpl implements EquipmentExternalService {
    private final WebClient webClient = WebClient.create("http://localhost:8082"); // URL del servicio Equipment

    @Override
    public boolean isValidAndUnusedFarmToken(String token) {
        // Aquí harías la petición real al otro servicio
        return Boolean.TRUE.equals(webClient.get()
                .uri("/api/v1/equipment/tokens/{token}/validate", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }

    @Override
    public Optional<Long> findFarmIdByToken(String token) {
        // Implementación para obtener el ID de la granja
        return Optional.ofNullable(webClient.get()
                .uri("/api/v1/equipment/tokens/{token}/farm-id", token)
                .retrieve()
                .bodyToMono(Long.class)
                .block());
    }
}