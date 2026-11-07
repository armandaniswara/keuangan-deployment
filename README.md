# Dashboard Keuangan Pribadi

Spring Boot 3.5.7 (Java 25) + Thymeleaf + Chart.js + H2.

Catatan: Java 25 membutuhkan Spring Boot 3.5.5 ke atas (atau 4.0+). Spring Boot 3.3.x belum mendukungnya.

## Menjalankan

```bash
mvn spring-boot:run
```

Buka http://localhost:8080. Data contoh 6 bulan terakhir terisi otomatis saat database masih kosong.
Data disimpan di folder `./data` (H2 file), jadi tetap ada setelah aplikasi di-restart.

## Endpoint

| Method | URL | Keterangan |
|---|---|---|
| GET | `/api/stats/ringkasan?bulan=2026-11` | Pemasukan, pengeluaran, selisih, total saldo |
| GET | `/api/stats/kategori?bulan=2026-11` | Pengeluaran per kategori (GROUP BY) |
| GET | `/api/stats/bulanan?jumlah=6` | Pemasukan vs pengeluaran per bulan |
| GET | `/api/transaksi/terbaru` | 10 transaksi terakhir |
| POST | `/api/transaksi` | Tambah transaksi (JSON) |
| DELETE | `/api/transaksi/{id}` | Hapus transaksi |

Contoh body POST:

```json
{"tanggal":"2026-11-04","jenis":"PENGELUARAN","kategori":"Makan","deskripsi":"Makan siang","jumlah":35000}
```

## Struktur

```
src/main/java/com/contoh/keuangan
  KeuanganApplication.java
  model/        Transaksi, JenisTransaksi
  repository/   TransaksiRepository (query agregasi JPQL)
  service/      StatistikService
  controller/   StatistikController, TransaksiController, HalamanController
  dto/          KategoriTotal
  config/       DataSeeder
src/main/resources
  application.properties
  templates/dashboard.html
```

## Pindah ke React / Vue

Backend tidak perlu diubah: front end baru cukup memanggil endpoint `/api/...` di atas.
Jika front end berjalan di port lain (mis. Vite di 5173), tambahkan konfigurasi CORS di Spring Boot.

## Ide pengembangan

- Anggaran (budget) per kategori dan peringatan jika terlampaui
- Filter rentang tanggal dan pencarian transaksi
- Login (Spring Security) agar tiap pengguna punya data sendiri
- Ganti H2 dengan PostgreSQL/MySQL
- Ekspor ke CSV/Excel
