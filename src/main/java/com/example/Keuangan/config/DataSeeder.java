package com.example.Keuangan.config;

import com.example.Keuangan.entity.JenisTransaksi;
import com.example.Keuangan.entity.Transaksi;
import com.example.Keuangan.repository.TransaksiRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Mengisi data contoh (6 bulan terakhir) jika database masih kosong. */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner isiDataContoh(TransaksiRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            List<Transaksi> data = new ArrayList<>();
            YearMonth sekarang = YearMonth.now();

            for (int i = 5; i >= 0; i--) {
                YearMonth ym = sekarang.minusMonths(i);
                // Bulan berjalan: batasi tanggal sampai hari ini agar tidak ada data di masa depan
                int hariTerakhir = ym.equals(sekarang)
                        ? Math.max(1, LocalDate.now().getDayOfMonth())
                        : ym.lengthOfMonth();
                int variasi = (i % 3) * 150_000;

                Transaksi t = new Transaksi();
                t.setTanggal(tanggalAman(ym, 1, hariTerakhir));
                t.setJenis(JenisTransaksi.PEMASUKAN);
                t.setKategori("Gaji");
                t.setDeskripsi("Gaji bulanan");
                t.setJumlah(rp(8_000_000));            }

            repository.saveAll(data);
        };
    }

    private static LocalDate tanggalAman(YearMonth ym, int hari, int hariMaks) {
        return ym.atDay(Math.min(hari, hariMaks));
    }

    private static BigDecimal rp(long nilai) {
        return BigDecimal.valueOf(nilai);
    }
}
