package com.synergymarket.service;

import com.synergymarket.dto.ClienteDTO;
import com.synergymarket.entity.Cliente;
import com.synergymarket.exception.BusinessException;
import com.synergymarket.exception.ResourceNotFoundException;
import com.synergymarket.repository.ClienteRepository;
import com.synergymarket.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;

    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ClienteDTO buscarPorId(Long id) {
        return toDTO(findOrThrow(id));
    }

    public ClienteDTO criar(ClienteDTO dto) {
        if (clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }
        Cliente cliente = toEntity(dto);
        return toDTO(clienteRepository.save(cliente));
    }

    public ClienteDTO atualizar(Long id, ClienteDTO dto) {
        Cliente cliente = findOrThrow(id);
        // Verifica CPF duplicado apenas se mudou
        if (!cliente.getCpf().equals(dto.getCpf()) && clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
        return toDTO(clienteRepository.save(cliente));
    }

    public void deletar(Long id) {
        findOrThrow(id);
        if (vendaRepository.existsByClienteId(id)) {
            throw new BusinessException("Não é possível excluir cliente com histórico de compras.");
        }
        clienteRepository.deleteById(id);
    }

    private Cliente findOrThrow(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    public ClienteDTO toDTO(Cliente c) {
        return ClienteDTO.builder()
                .id(c.getId())
                .nome(c.getNome())
                .cpf(c.getCpf())
                .email(c.getEmail())
                .telefone(c.getTelefone())
                .endereco(c.getEndereco())
                .build();
    }

    private Cliente toEntity(ClienteDTO dto) {
        return Cliente.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .endereco(dto.getEndereco())
                .build();
    }
}
