package com.example.Keuangan.controller;

import com.example.Keuangan.entity.Transaksi;
import com.example.Keuangan.repository.TransaksiRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaksi")
public class TransaksiController {

    private final TransaksiRepository repository;

    public TransaksiController(TransaksiRepository repository){
        this.repository = repository;
    }

    @GetMapping
    public List<Transaksi> getAll() {
        return repository.findAll();
    }

    @GetMapping("/terbaru")
    public List<Transaksi> terbaru(){
        return repository.findTop10ByOrderByTanggalDescIdDesc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaksi tambah(@Valid @RequestBody Transaksi transaksi){
        transaksi.setId(null);
        return repository.save(transaksi);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaksi> update(@PathVariable Long id, @Valid @RequestBody Transaksi transaksi) {
        return repository.findById(id)
            .map(existing -> {
                transaksi.setId(id);
                return ResponseEntity.ok(repository.save(transaksi));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> hapus(@PathVariable Long id) {
        if (!repository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
