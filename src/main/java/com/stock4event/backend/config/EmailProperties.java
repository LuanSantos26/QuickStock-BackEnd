package com.stock4event.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do e-mail de boas-vindas (prefixo {@code stock4event.email}).
 * Os valores vêm de variáveis de ambiente; veja application.properties e docs/EMAIL_BOAS_VINDAS.md.
 * Servidor SMTP e credenciais ficam nas propriedades padrão {@code spring.mail.*}.
 */
@Data
@ConfigurationProperties(prefix = "stock4event.email")
public class EmailProperties {

    /** Desligado por padrão: sem configuração, o cadastro funciona e nenhum e-mail é enviado. */
    private boolean habilitado = false;

    /** Remetente, ex.: "Stock4event &lt;nao-responda@seudominio.com.br&gt;". */
    private String remetente;

    /** Endereço real de acesso ao aplicativo, usado no botão "Acessar minha conta". */
    private String urlApp;

    /** Limite de tentativas por e-mail (a primeira conta como 1). */
    private int maxTentativas = 5;

    /** Espera antes da 2ª tentativa; dobra a cada nova falha (1 min, 2 min, 4 min...). */
    private long esperaInicialSegundos = 60;

    /** Liga o agendador que refaz tentativas pendentes. */
    private boolean reprocessamentoAutomatico = true;

    /** Intervalo entre as rodadas do agendador. */
    private long intervaloReprocessamentoMs = 60_000;

    /** Tempo após o qual uma tentativa presa em ENVIANDO vira INDETERMINADO. */
    private long limiteTentativaTravadaMinutos = 15;
}
