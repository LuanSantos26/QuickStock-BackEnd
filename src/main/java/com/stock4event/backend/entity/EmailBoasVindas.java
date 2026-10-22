package com.stock4event.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Registro (caixa de saída) do e-mail de boas-vindas de um cadastro.
 *
 * <p>É gravado na mesma transação do cadastro: só existe se a conta foi salva. A coluna
 * {@code usuario_id} é única, o que garante no máximo um e-mail de boas-vindas por conta,
 * mesmo com novas tentativas. Destinatário, nome e versão ficam congelados como estavam
 * no momento do cadastro.</p>
 */
@Entity
@Table(name = "email_boas_vindas")
@Data
public class EmailBoasVindas {

    /** Versão do e-mail, definida pelo perfil salvo no cadastro. */
    public enum Tipo { CLIENTE, FORNECEDOR }

    /**
     * PENDENTE: aguardando envio (primeira vez ou nova tentativa após falha temporária).
     * ENVIANDO: reservado por uma tentativa em andamento.
     * ENVIADO: aceito pelo servidor SMTP.
     * FALHOU: falha definitiva ou limite de tentativas atingido.
     * INDETERMINADO: a tentativa foi interrompida sem resultado; não é reenviado
     * automaticamente para não arriscar um e-mail duplicado.
     */
    public enum Status { PENDENTE, ENVIANDO, ENVIADO, FALHOU, INDETERMINADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(name = "nome_destinatario", length = 100)
    private String nomeDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDENTE;

    @Column(nullable = false)
    private Integer tentativas = 0;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    @Column(name = "proxima_tentativa_em", nullable = false)
    private LocalDateTime proximaTentativaEm;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;
}
