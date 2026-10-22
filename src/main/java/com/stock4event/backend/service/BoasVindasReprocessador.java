package com.stock4event.backend.service;

import com.stock4event.backend.config.EmailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Refaz periodicamente os e-mails de boas-vindas pendentes: falhas temporárias e envios que
 * não chegaram a ser tentados (ex.: o servidor reiniciou logo após o cadastro).
 * Desligue com stock4event.email.reprocessamento-automatico=false.
 */
@Component
@ConditionalOnProperty(name = "stock4event.email.reprocessamento-automatico", havingValue = "true", matchIfMissing = true)
public class BoasVindasReprocessador {

    private final BoasVindasEmailService servico;
    private final EmailProperties propriedades;

    public BoasVindasReprocessador(BoasVindasEmailService servico, EmailProperties propriedades) {
        this.servico = servico;
        this.propriedades = propriedades;
    }

    @Scheduled(fixedDelayString = "${stock4event.email.intervalo-reprocessamento-ms:60000}",
               initialDelayString = "${stock4event.email.intervalo-reprocessamento-ms:60000}")
    public void executar() {
        if (propriedades.isHabilitado()) {
            servico.reprocessarPendentes();
        }
    }
}
