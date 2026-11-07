package com.example.Keuangan.controller;

import com.example.Keuangan.dto.KategoriTotal;
import com.example.Keuangan.service.StatistikService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class StatistikController {

    private final StatistikService service;

    public StatistikController (StatistikService service) {
        this.service = service;
    }

    @GetMapping("/ringkasan")
    public StatistikService.Ringkasan ringkasan (@RequestParam(required = false) String bulan) {
        return service.ringkasan(parseBulan(bulan));
    }

    @GetMapping("/kategori")
    public List<KategoriTotal> kategori(@RequestParam(required = false) String bulan){
        return  service.pengeluaranPerKategori(parseBulan(bulan));
    }

    @GetMapping("/bulanan")
    public StatistikService.Bulanan bulanan (@RequestParam(defaultValue = "6") int jumlah){
        int batas = Math.max(1, Math.min(jumlah, 24));
        return service.bulanan(batas);
    }

    private static YearMonth parseBulan(String bulan) {
        return (bulan == null || bulan.isBlank()) ? YearMonth.now() : YearMonth.parse(bulan);
    }
}
