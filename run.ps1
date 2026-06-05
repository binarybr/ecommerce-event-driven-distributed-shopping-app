# =============================================================================
#  ShopSphere - full-stack run script (Windows PowerShell)
# =============================================================================
#  Builds the backend JARs with Maven (REQUIRED - the service Dockerfiles only
#  COPY pre-built target/*.jar, they do NOT compile inside Docker), then brings
#  up the backend stack and the frontend container.
#
#  USAGE:
#    .\run.ps1                 # normal run (Maven build + docker up)
#    .\run.ps1 -Clean          # wipe containers + volumes first (re-seeds data)
#    .\run.ps1 -SkipBuild      # skip Maven (use existing JARs) - faster restarts
#    .\run.ps1 -SkipFrontend   # backend only
#
#  PREREQUISITES:
#    - Docker Desktop running
#    - deployment\docker\.env present with your real STRIPE_API_KEY
#      (copy from .env.example if missing)
# =============================================================================

param(
    [switch]$Clean,
    [switch]$SkipBuild,
    [switch]$SkipFrontend
)

$ErrorActionPreference = 'Stop'

# --- Paths -------------------------------------------------------------------
$BackendRoot  = $PSScriptRoot
$Compose      = Join-Path $BackendRoot 'deployment\docker\docker-compose.yml'
$EnvFile      = Join-Path $BackendRoot 'deployment\docker\.env'
$EnvExample   = Join-Path $BackendRoot 'deployment\docker\.env.example'
$FrontendRoot = 'C:\Binary Labyrinth\VSCode Workspace\React Workspace\online-shopping-app-frontend'

function Section($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }

# --- 0. Pre-flight checks ----------------------------------------------------
Section '0. Pre-flight checks'
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker is not running. Start Docker Desktop and retry.' }

if (-not (Test-Path $EnvFile)) {
    Write-Host '  .env not found - creating from .env.example' -ForegroundColor Yellow
    Copy-Item $EnvExample $EnvFile
    Write-Host "  >> EDIT $EnvFile and set your real STRIPE_API_KEY before payments will work." -ForegroundColor Yellow
}
Write-Host '  OK'

# --- 1. (optional) Clean slate ----------------------------------------------
if ($Clean) {
    Section '1. Clean slate (down --volumes)'
    docker compose -f $Compose down --volumes
    if (Test-Path $FrontendRoot) {
        Push-Location $FrontendRoot
        docker compose down 2>$null
        Pop-Location
    }
    docker rm -f shopsphere-frontend binaryshopsphere-frontend 2>$null | Out-Null
    Write-Host '  Cleaned.'
}

# --- 2. Maven build (the step that's easy to forget) ------------------------
if (-not $SkipBuild) {
    Section '2. Maven build (mvnw clean package -DskipTests)'
    & (Join-Path $BackendRoot 'mvnw.cmd') -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed - fix compile errors before continuing.' }
    Write-Host '  JARs built.'
} else {
    Section '2. Maven build SKIPPED (-SkipBuild) - using existing JARs'
}

# --- 3. Backend stack --------------------------------------------------------
Section '3. Backend stack (docker compose up -d --build)'
docker compose -f $Compose up -d --build
Write-Host '  Backend starting - give it ~60-90s to register with Eureka.'

# --- 4. Frontend -------------------------------------------------------------
if (-not $SkipFrontend) {
    Section '4. Frontend (docker compose up -d --build)'
    Push-Location $FrontendRoot
    docker compose up -d --build
    Pop-Location
} else {
    Section '4. Frontend SKIPPED (-SkipFrontend)'
}

# --- 5. Verify ---------------------------------------------------------------
Section '5. Verifying (waiting 75s for boot + seed)'
Start-Sleep -Seconds 75

$products = (docker exec mongo mongosh --quiet --eval "db.getSiblingDB('product_service').products.countDocuments()" 2>$null)
Write-Host "  Products seeded (Mongo) : $products"

Write-Host '  Users (MySQL):'
docker exec mysql mysql -uroot -pbinary777Code -t -e "SELECT id,email,role FROM user_service.users;" 2>$null |
    Select-String -NotMatch 'Warning' | ForEach-Object { Write-Host "    $_" }

$stripe = docker exec payment-service printenv STRIPE_API_KEY 2>$null
if ($stripe -like 'sk_test_replace*' -or [string]::IsNullOrWhiteSpace($stripe)) {
    $stripeShown = 'NOT SET (payments will fail)'
} else {
    $stripeShown = 'set (sk_test_...)'
}
Write-Host "  Stripe key in payment-service : $stripeShown"

# --- Done --------------------------------------------------------------------
Section 'Done'
Write-Host '  Frontend        : http://localhost:3000'  -ForegroundColor Green
Write-Host '  API Gateway     : http://localhost:8080'  -ForegroundColor Green
Write-Host '  Eureka          : http://localhost:8761'  -ForegroundColor Green
Write-Host '  MailHog (email) : http://localhost:8025'  -ForegroundColor Green
Write-Host '  MySQL           : localhost:3307  (root / binary777Code)' -ForegroundColor Green
Write-Host '  MongoDB         : localhost:27018' -ForegroundColor Green
Write-Host ''
Write-Host '  Logins:' -ForegroundColor Green
Write-Host '    Admin (seeded): admin@shopsphere.com / Admin@1234' -ForegroundColor Green
Write-Host '    Admin reg key : ShopSphere@Admin2024' -ForegroundColor Green
