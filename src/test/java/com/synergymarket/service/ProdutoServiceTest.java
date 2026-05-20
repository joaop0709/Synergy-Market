package com.synergymarket.service;

import com.synergymarket.dto.ProdutoDTO;
import com.synergymarket.entity.Produto;
import com.synergymarket.exception.ResourceNotFoundException;
import com.synergymarket.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock private ProdutoRepository produtoRepository;
    @InjectMocks private ProdutoService produtoService;

    private ProdutoDTO dto;
    private Produto produto;

    @BeforeEach
    void setUp() {
        dto = ProdutoDTO.builder()
                .nome("Refrigerante")
                .descricao("Lata 350ml")
                .preco(new BigDecimal("5.00"))
                .quantidadeEstoque(100)
                .build();

        produto = Produto.builder()
                .id(1L)
                .nome("Refrigerante")
                .descricao("Lata 350ml")
                .preco(new BigDecimal("5.00"))
                .quantidadeEstoque(100)
                .build();
    }

    @Test
    void deveCriarProdutoComSucesso() {
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ProdutoDTO resultado = produtoService.criar(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Refrigerante");
        assertThat(resultado.getPreco()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    void deveAtualizarEstoqueCorretamente() {
        produto.atualizarEstoque(10);
        assertThat(produto.getQuantidadeEstoque()).isEqualTo(90);
    }
}
