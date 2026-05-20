package com.synergymarket.service;

import com.synergymarket.dto.ItemVendaDTO;
import com.synergymarket.dto.VendaDTO;
import com.synergymarket.entity.*;
import com.synergymarket.exception.BusinessException;
import com.synergymarket.exception.ResourceNotFoundException;
import com.synergymarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<VendaDTO> listarTodas() {
        return vendaRepository.findAll().stream().map(this::toDTO).toList();
    }

    public VendaDTO buscarPorId(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public VendaDTO registrarVenda(VendaDTO dto) {
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new BusinessException("A venda deve conter pelo menos um item.");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        String usernameLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(usernameLogado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));

        Venda venda = Venda.builder()
                .cliente(cliente)
                .usuario(usuario)
                .itens(new ArrayList<>())
                .build();

        for (ItemVendaDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto ID " + itemDTO.getProdutoId() + " não encontrado."));

            if (produto.getQuantidadeEstoque() < itemDTO.getQuantidade()) {
                throw new BusinessException("Estoque insuficiente para o produto: " + produto.getNome()
                        + ". Disponível: " + produto.getQuantidadeEstoque());
            }

            produto.atualizarEstoque(itemDTO.getQuantidade());
            produtoRepository.save(produto);

            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDTO.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
            venda.getItens().add(item);
        }

        venda.setValorTotal(venda.calcularTotal());
        return toDTO(vendaRepository.save(venda));
    }

    private Venda findOrThrow(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID: " + id));
    }

    public VendaDTO toDTO(Venda v) {
        List<ItemVendaDTO> itensDTO = v.getItens().stream().map(i ->
                ItemVendaDTO.builder()
                        .id(i.getId())
                        .produtoId(i.getProduto().getId())
                        .produtoNome(i.getProduto().getNome())
                        .quantidade(i.getQuantidade())
                        .precoUnitario(i.getPrecoUnitario())
                        .subtotal(i.getSubtotal())
                        .build()
        ).toList();

        return VendaDTO.builder()
                .id(v.getId())
                .data(v.getData())
                .valorTotal(v.getValorTotal())
                .clienteId(v.getCliente().getId())
                .clienteNome(v.getCliente().getNome())
                .itens(itensDTO)
                .build();
    }
}
