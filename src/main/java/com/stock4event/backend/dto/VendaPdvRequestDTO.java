package com.stock4event.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Venda no PDV. Não há campo de preço nem de total: o servidor usa o preço cadastrado
 * de cada produto e calcula subtotais, total e troco.
 */
@Data
public class VendaPdvRequestDTO {

    /** Quiosque (ponto de venda) da empresa onde a venda acontece. */
    @NotNull(message = "Selecione o ponto de venda.")
    private Long barracaId;

    /** Gerada pelo app a cada tentativa de venda; reenviar a mesma chave não duplica a venda. */
    @NotBlank(message = "Chave da venda é obrigatória.")
    @Pattern(regexp = "[A-Za-z0-9-]{8,64}", message = "Chave da venda inválida.")
    private String chaveVenda;

    @NotEmpty(message = "Adicione ao menos um produto.")
    @Size(max = 200, message = "A venda pode ter no máximo 200 itens.")
    @Valid
    private List<@NotNull(message = "Item inválido.") ItemVendaPdvRequestDTO> itens = new ArrayList<>();

    @NotBlank(message = "Selecione a forma de pagamento.")
    @Size(max = 20)
    private String formaPagamento;

    /** Obrigatório só no pagamento em dinheiro; deve cobrir o total. */
    @DecimalMin(value = "0.00", message = "Valor recebido não pode ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Valor recebido inválido.")
    private BigDecimal valorRecebido;

    @Size(max = 100, message = "Nome do cliente deve ter no máximo 100 caracteres.")
    private String clienteNome;

    /** CPF (11 dígitos) ou CNPJ (14 dígitos); máscara é aceita. */
    @Size(max = 18, message = "Documento do cliente inválido.")
    private String clienteDocumento;
}
