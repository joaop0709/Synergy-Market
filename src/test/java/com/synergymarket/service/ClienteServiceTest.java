package com.synergymarket.service;

import com.synergymarket.dto.ClienteDTO;
import com.synergymarket.entity.Cliente;
import com.synergymarket.exception.BusinessException;
import com.synergymarket.exception.ResourceNotFoundException;
import com.synergymarket.repository.ClienteRepository;
import com.synergymarket.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private VendaRepository vendaRepository;
    @InjectMocks private ClienteService clienteService;

    private ClienteDTO dto;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        dto = ClienteDTO.builder()
                .nome("João Silva")
                .cpf("123.456.789-00")
                .email("joao@email.com")
                .telefone("61999999999")
                .endereco("Rua A, 123")
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nome("João Silva")
                .cpf("123.456.789-00")
                .email("joao@email.com")
                .build();
    }

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteDTO resultado = clienteService.criar(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("João Silva");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoQuandoCpfDuplicado() {
        when(clienteRepository.existsByCpf(dto.getCpf())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já cadastrado");
    }

    @Test
    void deveLancarExcecaoAoDeletarClienteComVendas() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vendaRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("histórico de compras");
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }
}
