# Prosedur Pembuatan Backend Spring Boot (Struktur Keuangan)

## 1. Persiapan Project

### 1.1 Inisialisasi Project
- Gunakan [Spring Initializr](https://start.spring.io/) atau Spring Boot CLI
- Dependencies wajib:
  - **Spring Web** - REST API
  - **Spring Data JPA** - Database access
  - **H2 Database** / **PostgreSQL** / **MySQL** - Database
  - **Validation** - Bean Validation (@NotNull, @NotBlank, dll)
  - **Lombok** (opsional) - Boilerplate reduction

### 1.2 Struktur Package
```
com.contoh.keuangan
├── config/          # Konfigurasi & DataSeeder
├── controller/      # REST endpoints
├── dto/             # Data Transfer Objects
├── model/           # Entity JPA & Enum
├── repository/      # Spring Data JPA interfaces
├── service/         # Business logic
└── KeuanganApplication.java
```

---

## 2. Model (Entity & Enum)

### 2.1 Enum (JenisTransaksi.java)
```java
package com.contoh.keuangan.model;

public enum JenisTransaksi {
    PEMASUKAN,
    PENGELUARAN
}
```

### 2.2 Entity (Transaksi.java)
**Annotations wajib:**
- `@Entity` - JPA entity
- `@Id` + `@GeneratedValue` - Primary key
- `@Column(precision, scale)` - Untuk BigDecimal (uang)
- `@Enumerated(EnumType.STRING)` - Enum disimpan sebagai string
- Validasi: `@NotNull`, `@NotBlank`, `@Positive`

**Konvensi:**
- Package: `model`
- Nama class: singular (Transaksi, bukan Transaksis)
- Field: camelCase, private + getter/setter
- Constructor all-args + no-args

---

## 3. Repository

### 3.1 Interface Dasar
```java
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    // Method query otomatis dari nama method
    List<Transaksi> findTop10ByOrderByTanggalDescIdDesc();
}
```

### 3.2 Custom Query (@Query)
- Gunakan JPQL / SQL native
- Parameter: `@Param("nama")`
- Return DTO projection: `select new com.contoh.keuangan.dto.KategoriTotal(...)`
- Aggregasi: `sum()`, `count()`, `avg()` dengan `group by`

**Best Practice:**
- Method repository hanya untuk query data
- Business logic (perhitungan, filter) di Service

---

## 4. DTO (Data Transfer Object)

### 4.1 Record DTO (Java 17+)
```java
public record KategoriTotal(String kategori, BigDecimal total) {}
```

### 4.2 Kapan pakai DTO:
- Response yang bukan entity penuh (projeksi, agregasi)
- Request body yang beda struktur dengan entity
- Menyembunyikan field sensitif (password, dll)

---

## 5. Service (Business Logic)

### 5.1 Anotasi & Constructor Injection
```java
@Service
public class StatistikService {
    private final TransaksiRepository repository;

    public StatistikService(TransaksiRepository repository) {
        this.repository = repository;
    }
}
```

### 5.2 Record untuk Return Type Kompleks
```java
public record Ringkasan(BigDecimal pemasukan, BigDecimal pengeluaran,
                        BigDecimal saldoBulanIni, BigDecimal totalSaldo) {}
```

### 5.3 Pola Umum:
- **Ringkasan**: hitung total per periode (bulan/tahun)
- **Per Kategori**: GROUP BY kategori
- **Time Series**: loop bulan-bulan terakhir untuk grafik
- **Helper method**: `private static BigDecimal nol(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }`

---

## 6. Controller (REST API)

### 6.1 REST Controller (@RestController)
```java
@RestController
@RequestMapping("/api/transaksi")
public class TransaksiController {
    private final TransaksiRepository repository;

    public TransaksiController(TransaksiRepository repository) {
        this.repository = repository;
    }
}
```

### 6.2 Endpoint Standar CRUD
| Method | Path | Deskripsi |
|--------|------|-----------|
| GET | `/terbaru` | List 10 terbaru |
| POST | `/` | Create (validasi @Valid) |
| DELETE | `/{id}` | Delete by ID |

### 6.3 Controller Statistik
- Path: `/api/stats`
- Query param opsional: `?bulan=2026-11` (default: bulan ini)
- Return: Record DTO / List DTO

### 6.4 Controller Halaman (Server-Side Rendering)
```java
@Controller  // Bukan @RestController
public class HalamanController {
    @GetMapping("/")
    public String dashboard() {
        return "dashboard";  // -> src/main/resources/templates/dashboard.html
    }
}
```

---

## 7. Config & DataSeeder

### 7.1 CommandLineRunner untuk Seed Data
```java
@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner isiDataContoh(TransaksiRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            // generate data...
            repository.saveAll(data);
        };
    }
}
```

---

## 8. Database Configuration (application.properties / .yml)

### 8.1 PostgreSQL (Production & Development)
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/keuangan
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool (HikariCP - default)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.connection-timeout=20000

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false
```

### 8.2 H2 (Development Only - Alternative)
```properties
# H2 (in-memory, untuk testing cepat tanpa install PostgreSQL)
spring.datasource.url=jdbc:h2:mem:keuangan
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

### 8.3 Dependency PostgreSQL (pom.xml)
```xml
<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 8.4 Setup Database PostgreSQL
```bash
# 1. Install PostgreSQL (Windows: installer, Linux: apt, Mac: brew)
# 2. Buat database & user
psql -U postgres
CREATE DATABASE keuangan;
CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE keuangan TO postgres;
\q

# 3. Atau via Docker (lebih cepat)
docker run --name postgres-keuangan \
  -e POSTGRES_DB=keuangan \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### 8.5 Verifikasi Koneksi
```bash
# Test koneksi
psql -h localhost -U postgres -d keuangan -c "SELECT version();"

# Atau cek log Spring Boot startup:
# Hibernate: create table transaksi (...)
# Started KeuanganApplication in X seconds
```

---

## 9. Swagger / OpenAPI Documentation

### 9.1 Dependency (Spring Boot 3.x)
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

### 9.2 Konfigurasi (application.properties)
```properties
# Swagger/OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.packages-to-scan=com.contoh.keuangan.controller
```

### 9.3 Konfigurasi OpenAPI Info (OpenApiConfig.java)
```java
package com.contoh.keuangan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Keuangan API")
                        .version("1.0.0")
                        .description("API untuk manajemen keuangan pribadi")
                        .contact(new Contact()
                                .name("Tim Keuangan")
                                .email("dev@contoh.com")));
    }
}
```

### 9.4 Anotasi di Controller (Opsional tapi Direkomendasikan)

#### Class-level:
```java
@RestController
@RequestMapping("/api/transaksi")
@Tag(name = "Transaksi", description = "API manajemen transaksi keuangan")
public class TransaksiController { ... }
```

#### Method-level:
```java
@GetMapping("/terbaru")
@Operation(summary = "Ambil 10 transaksi terbaru",
           description = "Mengembalikan daftar 10 transaksi terbaru berdasarkan tanggal dan ID")
@ApiResponse(responseCode = "200", description = "Berhasil",
             content = @Content(array = @ArraySchema(schema = @Schema(implementation = Transaksi.class))))
public List<Transaksi> terbaru() { ... }

@PostMapping
@Operation(summary = "Tambah transaksi baru")
@ApiResponse(responseCode = "201", description = "Dibuat")
@ApiResponse(responseCode = "400", description = "Validasi gagal")
public Transaksi tambah(@Valid @RequestBody Transaksi transaksi) { ... }
```

#### Parameter:
```java
@GetMapping("/ringkasan")
@Operation(summary = "Ringkasan keuangan per bulan")
@Parameter(name = "bulan", description = "Format YYYY-MM (opsional, default: bulan ini)",
           schema = @Schema(type = "string", pattern = "^\\d{4}-\\d{2}$"))
public StatistikService.Ringkasan ringkasan(@RequestParam(required = false) String bulan) { ... }
```

#### Request Body (DTO):
```java
public record TransaksiRequest(
    @Schema(description = "Tanggal transaksi", example = "2026-11-04", requiredMode = RequiredMode.REQUIRED)
    LocalDate tanggal,
    
    @Schema(description = "Jenis transaksi", example = "PEMASUKAN", requiredMode = RequiredMode.REQUIRED)
    JenisTransaksi jenis,
    
    @Schema(description = "Kategori", example = "Gaji", requiredMode = RequiredMode.REQUIRED)
    String kategori,
    
    String deskripsi,
    
    @Schema(description = "Jumlah uang", example = "8000000", requiredMode = RequiredMode.REQUIRED)
    BigDecimal jumlah
) {}
```

### 9.5 Akses Swagger UI
Setelah aplikasi berjalan:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

### 9.6 Anotasi Umum (Cheat Sheet)
| Anotasi | Gunanya |
|---------|---------|
| `@Tag` | Kelompokkan endpoint (di class) |
| `@Operation` | Deskripsi endpoint (di method) |
| `@Parameter` | Dokumentasi path/query/header param |
| `@ApiResponse` | Response code & deskripsi |
| `@Schema` | Dokumentasi field DTO/entity |
| `@Content` + `@ArraySchema` | Response body array/object |
| `@RequiredMode.REQUIRED` | Field wajib di request |

### 9.7 Tips
- Gunakan `@Schema` di **record DTO** (bukan entity) untuk request/response
- `@Parameter` untuk query param kompleks (pattern, example)
- `@ApiResponse` minimal 200/201 + 400 + 404 + 500
- Group controller dengan `@Tag` biar UI terorganisir

---

## 10. Testing Checklist

### 10.1 Unit Test Service
- Test `ringkasan()`, `pengeluaranPerKategori()`, `bulanan()`
- Mock `TransaksiRepository` dengan `@MockBean`

### 10.2 Integration Test Controller
- `@SpringBootTest` + `@AutoConfigureMockMvc`
- Test endpoint `/api/transaksi`, `/api/stats/*`

### 10.3 Manual Test (Postman/cURL)
```bash
# Create
curl -X POST http://localhost:8080/api/transaksi \
  -H "Content-Type: application/json" \
  -d '{"tanggal":"2026-11-04","jenis":"PEMASUKAN","kategori":"Gaji","deskripsi":"Gaji Nov","jumlah":8000000}'

# Read terbaru
curl http://localhost:8080/api/transaksi/terbaru

# Stats ringkasan
curl "http://localhost:8080/api/stats/ringkasan?bulan=2026-11"

# Delete
curl -X DELETE http://localhost:8080/api/transaksi/1
```

---

## 11. Urutan Pembuatan (Workflow)

```
1. Project setup (Spring Initializr)
2. application.properties (DB config)
3. Model: Enum -> Entity
4. Repository: JpaRepository + custom @Query
5. DTO: Record untuk response custom
6. Service: Business logic + record return types
7. Controller: REST endpoints
8. Config: DataSeeder (opsional)
9. Swagger: Dependency + OpenApiConfig + anotasi @Tag/@Operation/@Schema
10. Test: Unit + Integration
11. Run & verify
```

---

## 12. Catatan Penting

- **BigDecimal** untuk uang (bukan double/float)
- **LocalDate** untuk tanggal (bukan Date/String)
- **Constructor injection** (bukan @Autowired field)
- **Validation** di entity + `@Valid` di controller
- **Null safety**: selalu handle null dari aggregasi DB (`nol()` helper)
- **Locale Indonesia** untuk format bulan (`Locale.of("id", "ID")`)
- **Record** (Java 17+) untuk DTO immutable & concise