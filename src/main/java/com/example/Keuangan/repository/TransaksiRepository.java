package com.example.Keuangan.repository;

import com.example.Keuangan.dto.KategoriTotal;
import com.example.Keuangan.entity.JenisTransaksi;
import com.example.Keuangan.entity.Transaksi;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransaksiRepository extends JpaRepository <Transaksi, Long> {
    List<Transaksi> findTop10ByOrderByTanggalDescIdDesc();

    @Query("""
            select sum(t.jumlah) from Transaksi t
            where t.jenis = :jenis and t.tanggal between :dari and :sampai
            """)
    BigDecimal totalByJenis(@Param("jenis") JenisTransaksi jenis,
                            @Param("dari") LocalDate dari,
                            @Param("sampai")LocalDate sampai);

    @Query("select sum(t.jumlah) from Transaksi t where t.jenis = :jenis")
    BigDecimal totalByJenisSemua(@Param("jenis") JenisTransaksi jenis);

    @Query("""
            select new com.example.Keuangan.dto.KategoriTotal(t.kategori, sum(t.jumlah))
            from Transaksi t
            where t.jenis = :jenis and t.tanggal between :dari and :sampai
            group by t.kategori
            order by sum(t.jumlah) desc
            """)
    List<KategoriTotal> totalPerKategori(@Param("jenis") JenisTransaksi jenis,
                                         @Param("dari") LocalDate dari,
                                         @Param("sampai") LocalDate sampai);

}
