package com.synergymarket.service;

import com.synergymarket.dto.ProdutoDTO;
import com.synergymarket.entity.Produto;
import com.synergymarket.exception.ResourceNotFoundException;
import com.synergymarket.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<ProdutoDTO> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ProdutoDTO buscarPorId(Long id) {
        return toDTO(findOrThrow(id));
    }

    public ProdutoDTO criar(ProdutoDTO dto) {
        return toDTO(produtoRepository.save(toEntity(dto)));
    }

    public ProdutoDTO atualizar(Long id, ProdutoDTO dto) {
        Produto produto = findOrThrow(id);
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        return toDTO(produtoRepository.save(produto));
    }

    public void deletar(Long id) {
        findOrThrow(id);
        produtoRepository.deleteById(id);
    }

    public Produto findOrThrow(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + id));
    }

    public ProdutoDTO toDTO(Produto p) {
        return ProdutoDTO.builder()
                .id(p.getId())
                .nome(p.getNome())
                .descricao(p.getDescricao())
                .preco(p.getPreco())
                .quantidadeEstoque(p.getQuantidadeEstoque())
                .build();
    }

    private Produto toEntity(ProdutoDTO dto) {
        return Produto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .preco(dto.getPreco())
                .quantidadeEstoque(dto.getQuantidadeEstoque())
                .build();
    }
}
