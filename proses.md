# Proses Setup CI/CD GitHub Actions

## 1. File yang Dibuat
- `.github/workflows/ci-cd.yml` - Pipeline CI/CD lengkap

## 2. Secrets yang Harus Didaftarkan di GitHub
Masuk ke **Settings > Secrets and variables > Actions > New repository secret**

| Secret Name | Deskripsi | Contoh Nilai |
|-------------|-----------|--------------|
| `VPS_HOST` | IP atau hostname VPS | `123.45.67.89` atau `vps.domain.com` |
| `VPS_USER` | Username SSH VPS | `root` atau `ubuntu` |
| `VPS_SSH_KEY` | Private key SSH (format OpenSSH) | `-----BEGIN OPENSSH PRIVATE KEY-----\n...\n-----END OPENSSH PRIVATE KEY-----` |
| `VPS_PORT` | Port SSH (default 22) | `22` |
| `VPS_DEPLOY_PATH` | Path absolut di VPS tempat docker-compose.yml berada | `/opt/keuangan` atau `/home/ubuntu/keuangan` |
| `GHCR_TOKEN` | Personal Access Token GitHub dengan scope `write:packages`, `read:packages`, `delete:packages` | `ghp_xxxxxxxxxxxx` |

> **Catatan**: `GITHUB_TOKEN` sudah otomatis tersedia, tidak perlu ditambahkan manual.

## 3. Persiapan di VPS

### 3.1 Generate SSH Key Pair (jika belum punya)
```bash
ssh-keygen -t ed25519 -C "github-actions-deploy"
# Simpan di ~/.ssh/github_actions_deploy
```

### 3.2 Tambahkan Public Key ke VPS
```bash
ssh-copy-id -i ~/.ssh/github_actions_deploy.pub user@VPS_HOST
# Atau manual: cat public key >> ~/.ssh/authorized_keys
```

### 3.3 Buat Directory Deploy di VPS
```bash
mkdir -p /opt/keuangan
cd /opt/keuangan
```

### 3.4 Copy `docker-compose.yml` ke VPS
File `docker-compose.yml` harus sudah ada di path `VPS_DEPLOY_PATH` di VPS.
Pastikan file tersebut sudah dikonfigurasi dengan benar untuk production:
- Gunakan environment variables untuk password database
- Jangan hardcode password di file

### 3.5 Buat File `.env` di VPS (Opsional tapi Direkomendasikan)
```bash
# /opt/keuangan/.env
POSTGRES_DB=keuangan
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password_yang_aman_dari_secrets
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/keuangan
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password_yang_aman_dari_secrets
APP_JWT_SECRET=jwt_secret_yang_panjang_dan_aman_min_32_karakter
```

Lalu update `docker-compose.yml` di VPS untuk menggunakan file `.env`:
```yaml
services:
  app:
    image: ghcr.io/username/keuangan:latest
    env_file: .env
    ...
  postgres:
    env_file: .env
    ...
```

## 4. Setup GitHub Personal Access Token (GHCR_TOKEN)
1. Buka GitHub Settings > Developer settings > Personal access tokens > Tokens (classic)
2. Generate new token (classic)
3. Scopes yang dipilih:
   - `write:packages`
   - `read:packages`
   - `delete:packages`
   - `repo` (jika repo private)
4. Simpan token sebagai secret `GHCR_TOKEN`

## 5. Mengubah Registry ke Docker Hub (Opsional)
Jika ingin pakai Docker Hub bukan GHCR, ubah di workflow:
```yaml
env:
  REGISTRY: docker.io
  IMAGE_NAME: dockerhub_username/keuangan
```
Dan ganti secret login:
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

## 6. Menjalankan Pipeline
Push ke branch `main` akan otomatis trigger pipeline:
```bash
git add .github/workflows/ci-cd.yml proses.md
git commit -m "ci: add GitHub Actions CI/CD pipeline"
git push origin main
```

## 7. Verifikasi
- Cek tab **Actions** di GitHub repo
- Pastikan 3 job berjalan: `Build & Test` → `Docker Build & Push` → `Deploy to VPS`
- Cek logs deploy apakah container berhasil restart di VPS

## 8. Troubleshooting Umum

| Masalah | Solusi |
|---------|--------|
| SSH connection timeout | Cek `VPS_HOST`, `VPS_PORT`, firewall VPS allow port SSH |
| Permission denied (publickey) | Cek `VPS_SSH_KEY` format benar, public key sudah di authorized_keys |
| Docker pull unauthorized | Cek `GHCR_TOKEN` scope, pastikan `GITHUB_TOKEN` punya akses packages |
| Container gagal start | Cek logs `docker compose logs -f`, pastikan `.env` lengkap di VPS |
| Database connection refused | Pastikan service postgres healthy sebelum app start, cek `depends_on` dengan `condition: service_healthy` |