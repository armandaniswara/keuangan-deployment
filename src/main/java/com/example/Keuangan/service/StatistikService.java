package com.example.Keuangan.service;

import com.example.Keuangan.dto.KategoriTotal;
import com.example.Keuangan.entity.JenisTransaksi;
import com.example.Keuangan.repository.TransaksiRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class StatistikService {

    private static final Locale INDONESIA = Locale.of("id","ID");

    private final TransaksiRepository repository;

    public StatistikService(TransaksiRepository repository){
        this.repository = repository;
    }

    public record Ringkasan(BigDecimal pemasukan, BigDecimal pengeluaran,
                            BigDecimal saldoBulananIni, BigDecimal totalSaldo){

    }

    public record  Bulanan(List<String> labels, List<BigDecimal> pemasukan, List<BigDecimal> pengeluaran  ){
    }

    public Ringkasan ringkasan(YearMonth bulan){
        LocalDate dari = bulan.atDay(1);
        LocalDate sampai = bulan.atEndOfMonth();

        BigDecimal masuk = nol(repository.totalByJenis(JenisTransaksi.PEMASUKAN, dari, sampai));
        BigDecimal keluar = nol(repository.totalByJenis(JenisTransaksi.PENGELUARAN, dari, sampai));

        BigDecimal totalMasuk = nol(repository.totalByJenisSemua(JenisTransaksi.PEMASUKAN));
        BigDecimal totalKeluar = nol(repository.totalByJenisSemua(JenisTransaksi.PENGELUARAN));

        return new Ringkasan(masuk, keluar, masuk.subtract(keluar), totalMasuk.subtract(totalKeluar));
    }

    public List<KategoriTotal> pengeluaranPerKategori(YearMonth bulan){
        return repository.totalPerKategori(JenisTransaksi.PENGELUARAN, bulan.atDay(1), bulan.atEndOfMonth());
    }

    public Bulanan bulanan(int jumlahBulan){
        List<String> Labels = new ArrayList<>();
        List<BigDecimal> pemasukan = new ArrayList<>();
        List<BigDecimal> pengeluaran = new ArrayList<>();

        YearMonth sekarang = YearMonth.now();
        for (int i = jumlahBulan - 1; i >= 0; i--){
            YearMonth ym = sekarang.minusMonths(i);
            LocalDate dari = ym.atDay(1);
            LocalDate sampai = ym.atEndOfMonth();

            Labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, INDONESIA) + " " + (ym.getYear() % 100));
            pemasukan.add(nol(repository.totalByJenis(JenisTransaksi.PEMASUKAN, dari, sampai)));
            pengeluaran.add(nol(repository.totalByJenis(JenisTransaksi.PENGELUARAN, dari, sampai)));
        }
        return new Bulanan(Labels, pemasukan, pengeluaran);
    }

    private static BigDecimal nol(BigDecimal nilai) {
        return nilai == null ? BigDecimal.ZERO : nilai;
    }
}
