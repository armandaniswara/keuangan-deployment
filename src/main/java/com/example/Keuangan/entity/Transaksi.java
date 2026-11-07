package com.example.Keuangan.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JsonProperty("date")
    private LocalDate tanggal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("type")
    private JenisTransaksi jenis;

    @NotBlank
    @JsonProperty("category")
    private String kategori;

    @JsonProperty("title")
    private String deskripsi;

    @NotNull
    @Positive
    @JsonProperty("amount")
    @Column
    private BigDecimal jumlah;


}
