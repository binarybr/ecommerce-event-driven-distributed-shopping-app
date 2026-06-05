# Running The Online Shopping App - Quick Reference

This is a **Spring Boot 4.0.6** microservices application with 13 services (3 infrastructure + 10 business), MySQL, MongoDB, Kafka, and more.

**📖 For detailed setup instructions, see: [RUNNING_LOCAL_GUIDE.md](RUNNING_LOCAL_GUIDE.md)**

---

## 🚀 Quick Start (5 minutes)

### Prerequisites Check
```powershell
java -version                    # Java 25 required
docker --version                 # Docker required
.\mvnw.cmd -version             # Maven (included)
```

### Build & Run Everything
```powershell
# 1. Build
.\mvnw.cmd clean package -DskipTests

# 2. Start with Docker Compose (RECOMMENDED)
docker compose -f deployment\docker\docker-compose.yml up -d

# 3. Monitor
docker compose -f deployment\docker\docker-compose.yml logs -f

# 4. Verify
curl.exe http://localhost:8080/actuator/health
```

---

## 📊 Services & Ports

| Service | Port | Database | Type |
|---------|------|----------|------|
| **API Gateway** | 8080 | N/A | Infrastructure |
| **Discovery Server (Eureka)** | 8761 | N/A | Infrastructure |
| **Config Server** | 8888 | N/A | Infrastructure |
| **Product Service** | 8081 | MongoDB | Business |
| **Order Service** | 8083 | MySQL | Business |
| **Inventory Service** | 8082 | MySQL | Business |
| **Notification Service** | 8084 | MySQL | Business |
| **User Service** | 8085 | MySQL | Business |
| **Payment Service** | 8086 | MySQL | Business |
| **Cart Service** | 8087 | MySQL | Business |
| **Review Service** | 8088 | MySQL | Business |
| **Recommendation Service** | 8089 | MySQL | Business |
| **Admin Service** | 8090 | N/A (aggregator) | Business |

**Supporting Infrastructure**
- MySQL: `localhost:3307` (user: `root`, password: `binary777Code`)
- MongoDB: `localhost:27017`
- Kafka: `localhost:9092`
- Zookeeper: `localhost:2181`
- MailHog: `localhost:8025` (Web UI)

---

## 🎯 Running Options

### Option 1: Docker Compose (Recommended ⭐)

```powershell
# Start all services in one command
docker compose -f deployment\docker\docker-compose.yml up -d

# Check status
docker compose -f deployment\docker\docker-compose.yml ps

# View logs
docker compose -f deployment\docker\docker-compose.yml logs -f api-gateway

# Stop everything
docker compose -f deployment\docker\docker-compose.yml down

# Stop and reset databases
docker compose -f deployment\docker\docker-compose.yml down -v
```

### Option 2: Local JVM (DIY)

See [RUNNING_LOCAL_GUIDE.md - Running Services Locally](RUNNING_LOCAL_GUIDE.md)

Start each service in separate terminal:
```powershell
cd infrastructure-services\discovery-server && .\mvnw.cmd spring-boot:run
cd infrastructure-services\config-server && .\mvnw.cmd spring-boot:run
cd infrastructure-services\api-gateway && .\mvnw.cmd spring-boot:run
# ... and all business services
```

### Option 3: Kubernetes

See [RUNNING_LOCAL_GUIDE.md - Running with Kubernetes](RUNNING_LOCAL_GUIDE.md)

```powershell
# Build images
docker build -t online-shopping/api-gateway:0.0.1-SNAPSHOT infrastructure-services\api-gateway
# ... build all other services

# Deploy
kubectl apply -k deployment\kubernetes\base

# Check
kubectl get pods -n online-shopping
```

---

## 🧪 Testing APIs

> **Auth model (important):** The gateway forwards `/api/**` to services, which
> enforce **JWT Bearer + role** auth. Product/review reads are **public** (no
> token). Writes and protected reads need a **Bearer JWT** obtained from
> `/api/users/login`. The `admin/change-me` Basic credentials only protect the
> gateway's own actuator endpoints — they are **not** used for `/api/**`.

### Quick Test Commands (PowerShell)

```powershell
# Health (gateway actuator — Basic auth)
curl.exe http://localhost:8080/actuator/health

# Public read — NO token needed
curl.exe http://localhost:8080/api/products

# 1) Register + log in to get a JWT
$body = @{ email="demo@example.com"; password="Password123!"; firstName="Demo"; lastName="User"; phone="5550001234" } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8080/api/users/register -Method Post -ContentType application/json -Body $body
$auth = Invoke-RestMethod -Uri http://localhost:8080/api/users/login -Method Post -ContentType application/json `
  -Body (@{ email="demo@example.com"; password="Password123!" } | ConvertTo-Json)
$H = @{ Authorization = "Bearer $($auth.accessToken)"; "Content-Type"="application/json" }

# 2) Place an order (CUSTOMER) — JWT required
Invoke-RestMethod -Uri http://localhost:8080/api/orders -Method Post -Headers $H `
  -Body (@{ userId="$($auth.user.id)"; productId="<productId>"; quantity=1; price=99.99 } | ConvertTo-Json)

# Product/inventory WRITES require an ADMIN JWT (promote a user to ADMIN in the DB first):
#   docker exec mysql mysql -uroot -pbinary777Code -D user_service -e "UPDATE users SET role='ADMIN' WHERE email='demo@example.com';"
# then re-login to get a token carrying the ADMIN role.
Invoke-RestMethod -Uri http://localhost:8080/api/products -Method Post -Headers $H `
  -Body (@{ name="Keyboard"; description="Gaming Keyboard"; price=99.99; category="Electronics" } | ConvertTo-Json)
```

> A complete 14-section seed + regression script (customers, products, orders,
> payments, reviews, recommendations, admin dashboard, DB checks) lives in
> **RUNNING_LOCAL_GUIDE.md**.

### Test Dashboards

- **Eureka (Service Registry)**: http://localhost:8761
- **MailHog (Emails)**: http://localhost:8025
- **API Gateway**: http://localhost:8080

#### Gateway actuator credentials (NOT for /api/**)
- Username: `admin`
- Password: `change-me`  (Docker default; `admin` locally)

### Run Unit/Integration Tests

```powershell
# All tests
.\mvnw.cmd clean test

# Specific service
.\mvnw.cmd clean test -pl business-services\product-service

# Specific test class
.\mvnw.cmd clean test -Dtest=ProductControllerTest
```

---

## 🏗️ Build Options

```powershell
# Clean and build all (RECOMMENDED)
.\mvnw.cmd clean package -DskipTests

# Build with tests (slower)
.\mvnw.cmd clean package

# Build single service
.\mvnw.cmd clean package -DskipTests -pl business-services\product-service -am

# Fast offline build (if dependencies cached)
.\mvnw.cmd package -DskipTests -o
```

---

## 🔧 Troubleshooting

| Problem | Solution |
|---------|----------|
| Port 8080 already in use | `netstat -ano \| findstr :8080` → `taskkill /PID <PID> /F` |
| MySQL won't start | `docker compose down -v` → `docker compose up -d` |
| Services not in Eureka | Wait 30s, check: `docker compose logs discovery-server` |
| Out of memory | Docker Desktop → Settings → Resources → 8GB+ |
| "ImagePullBackOff" in K8s | For Minikube: `minikube docker-env \| Invoke-Expression` → rebuild |

**For more issues, see: [RUNNING_LOCAL_GUIDE.md - Troubleshooting](RUNNING_LOCAL_GUIDE.md)**

---

## 📚 Key Commands Reference

```powershell
# Maven builds
.\mvnw.cmd clean              # Clean artifacts
.\mvnw.cmd compile            # Compile only
.\mvnw.cmd package            # Build JARs
.\mvnw.cmd test               # Run tests
.\mvnw.cmd spring-boot:run    # Run service

# Docker Compose
docker compose -f deployment\docker\docker-compose.yml up -d          # Start
docker compose -f deployment\docker\docker-compose.yml down           # Stop
docker compose -f deployment\docker\docker-compose.yml down -v        # Stop & reset
docker compose -f deployment\docker\docker-compose.yml restart svc    # Restart service
docker compose -f deployment\docker\docker-compose.yml logs -f        # View logs
docker compose -f deployment\docker\docker-compose.yml ps             # List containers

# Docker
docker ps                              # List running containers
docker logs -f <container>             # View logs
docker exec -it <container> bash       # Access container shell
docker build -t name:tag .             # Build image

# Kubernetes
kubectl apply -k deployment\kubernetes\base          # Deploy stack
kubectl get pods -n online-shopping                  # List pods
kubectl logs -f deployment/api-gateway -n online-shopping  # View logs
kubectl port-forward svc/api-gateway 8080:8080 -n online-shopping  # Port forward
kubectl delete -k deployment\kubernetes\base         # Delete stack
```

---

## 📖 Documentation Index

| Document | Purpose | Read Time |
|----------|---------|-----------|
| **RUNNING_LOCAL_GUIDE.md** | Complete local setup & testing guide | 20 min |
| **PROJECT_SUMMARY.md** | Project status & architecture overview | 5 min |
| **DOCUMENTATION_INDEX.md** | Index of all docs (incl. per-service) | 5 min |
| **ARCHITECTURE_DIAGRAM.md** | System architecture and flows | 10 min |
| **business-services/&lt;svc&gt;/IMPLEMENTATION.md** | Per-service implementation detail | 5 min each |

**Start here**: Read [RUNNING_LOCAL_GUIDE.md](RUNNING_LOCAL_GUIDE.md) for detailed instructions!

---

## ✅ Verification Checklist

After following the quick start:

- [ ] All Docker containers UP: `docker compose ps` (18 containers)
- [ ] API Gateway responds: `curl http://localhost:8080/actuator/health`
- [ ] Eureka shows 11 services (api-gateway + 10 business): http://localhost:8761
- [ ] Public read works: `curl http://localhost:8080/api/products`
- [ ] Login returns a JWT: `POST /api/users/login`
- [ ] MySQL has 8 custom databases (order, inventory, notification, user, payment, cart, review, recommendation)
- [ ] Kafka topics created
- [ ] MailHog accessible: http://localhost:8025

---

## 🎯 Next Steps

1. **First time?** → Read [RUNNING_LOCAL_GUIDE.md](RUNNING_LOCAL_GUIDE.md)
2. **Want to build?** → `.\mvnw.cmd clean package -DskipTests`
3. **Want to run?** → `docker compose -f deployment\docker\docker-compose.yml up -d`
4. **Want to test?** → Use curl commands above or follow [RUNNING_LOCAL_GUIDE.md - Testing](RUNNING_LOCAL_GUIDE.md)
5. **Having issues?** → Check [RUNNING_LOCAL_GUIDE.md - Troubleshooting](RUNNING_LOCAL_GUIDE.md)

---

**Project**: Online Shopping Microservices Application  
**Tech Stack**: Spring Boot 4.0.6, Java 25, Docker, Kubernetes, MySQL, MongoDB, Kafka  
**Last Updated**: May 20, 2026  
**Status**: Ready for Local Development

For detailed instructions, see: [RUNNING_LOCAL_GUIDE.md](RUNNING_LOCAL_GUIDE.md) ✨

