# Struktur Folder Project Keuangan

## Overview
Aplikasi manajemen keuangan sederhana berbasis Spring Boot dengan arsitektur layered (Controller - Service - Repository).

---

## 📁 `config/`
**Konfigurasi Aplikasi**
- **DataSeeder.java** - Inisialisasi data awal (seeding) saat aplikasi pertama kali dijalankan

---

## 📁 `controller/`
**Layer Presentation - Endpoint HTTP**
Menangani request/response HTTP dan validasi input.

| File | Deskripsi |
|------|-----------|
| `HalamanController.java` | Mengatur routing halaman web (Thymeleaf) |
| `StatistikController.java` | Endpoint untuk statistik keuangan (pemasukan, pengeluaran, saldo) |
| `TransaksiController.java` | CRUD transaksi (tambah, edit, hapus, daftar) |

---

## 📁 `dto/`
**Data Transfer Object**
Objek transfer data antar layer, terutama untuk response API.

| File | Deskripsi |
|------|-----------|
| `KategoriTotal.java` | DTO untuk ringkasan total per kategori (pemasukan/pengeluaran) |

---

## 📁 `model/`
**Entity / Domain Model**
Representasi tabel database (JPA Entities).

| File | Deskripsi |
|------|-----------|
| `JenisTransaksi.java` | Enum: `PEMASUKAN` / `PENGELUARAN` |
| `Transaksi.java` | Entity utama transaksi keuangan (id, jumlah, kategori, deskripsi, tanggal, jenis) |

---

## 📁 `repository/`
**Data Access Layer**
Interface Spring Data JPA untuk operasi database.

| File | Deskripsi |
|------|-----------|
| `TransaksiRepository.java` | Custom query: findByJenis, sumByJenis, findByKategori, dll |

---

## 📁 `service/`
**Business Logic Layer**
Logika bisnis, validasi, dan koordinasi antar repository.

| File | Deskripsi |
|------|-----------|
| `StatistikService.java` | Menghitung total pemasukan, pengeluaran, saldo, dan ringkasan per kategori |

---

## 📄 `KeuanganApplication.java`
**Entry Point**
Class utama Spring Boot (`@SpringBootApplication`) untuk menjalankan aplikasi.

---

## Arsitektur Alur Data
```
HTTP Request
    │
    ▼
Controller (Validasi & Mapping)
    │
    ▼
Service (Business Logic)
    │
    ▼
Repository (Database Access)
    │
    ▼
Database (H2 / MySQL / PostgreSQL)
```