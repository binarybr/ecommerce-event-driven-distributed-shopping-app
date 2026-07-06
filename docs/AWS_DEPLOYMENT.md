# Deploying ShopSphere to AWS — Single EC2 (Portfolio / Lowest Cost)

A step-by-step guide, from a fresh AWS account to a public URL, running the **entire
stack** (13 services + MySQL + MongoDB + Kafka + frontend) on **one EC2 instance** with
your existing `docker-compose` files. No managed services — cheapest possible.

> This is the right approach for a **demo/portfolio**. It is NOT
> highly-available or auto-scaling (one box = one point of failure). That's a
> deliberate cost trade-off.

---

## 0. Cost reality (read this first)

This stack runs ~18 containers, including 13 JVMs and Kafka. It needs **~7 GB RAM**, so
the AWS **free tier (t2.micro, 1 GB) cannot run it** — be honest with yourself about that.

| Item | Spec | ~Cost (us-east-1, on-demand) |
|---|---|---|
| EC2 instance | **t3.large** (2 vCPU, 8 GB) — minimum, tight | ~$0.083/hr ≈ **$60/mo if 24×7** |
| EC2 instance | t3.xlarge (4 vCPU, 16 GB) — comfortable | ~$0.166/hr ≈ $120/mo if 24×7 |
| EBS storage | 30 GB gp3 | ~$2.40/mo |
| Elastic IP | while instance **running** | free |
| Elastic IP | while instance **stopped** | ~$3.60/mo |

### How to keep it cheap (the key trick)
**Stop the instance when you're not demoing.** You're billed for compute only while
*running*; stopped, you pay only for EBS (~$2–3/mo) and the Elastic IP (~$3.60/mo).

- Demo for an interview → **start** it (boots in a couple of minutes).
- Done → **stop** it.
- Realistic cost if you run it a few hours a week: **~$5–10/mo**.

> Tip: don't attach an Elastic IP if you don't mind the public IP changing on each
> start — that saves the stopped-EIP charge entirely. Trade-off: the URL changes.

---

## 1. Create an AWS account & an admin IAM user

1. Sign up at https://aws.amazon.com (needs a card; identity-verified).
2. Sign in to the **root** account → search **IAM**.
3. **Users → Create user** → name `shopsphere-admin`.
4. **Attach policies directly → AdministratorAccess** (fine for a personal demo).
5. Create the user, then create an **access key** only if you want the CLI (optional —
   this guide uses the web Console + SSH, so you can skip CLI keys).
6. From now on, sign in as this IAM user, not root.

> Pick a region and stick to it (e.g. **us-east-1** / N. Virginia, usually cheapest).
> The region selector is top-right in the Console.

---

## 2. Create an SSH key pair

1. Console → **EC2 → Key Pairs → Create key pair**.
2. Name: `shopsphere-key`. Type: **RSA**, format **.pem** (Linux/Mac) or **.ppk** if
   you'll use PuTTY on Windows.
3. It downloads `shopsphere-key.pem` — **keep it safe, you can't re-download it**.
4. On Mac/Linux, lock it down: `chmod 400 shopsphere-key.pem`.

---

## 3. Configure the firewall (Security Group)

1. EC2 → **Security Groups → Create security group**. Name `shopsphere-sg`.
2. **Inbound rules** — add:

   | Type | Port | Source | Why |
   |---|---|---|---|
   | SSH | 22 | **My IP** | Your admin access only (not 0.0.0.0/0) |
   | Custom TCP | 3000 | 0.0.0.0/0 | The storefront (public) |
   | Custom TCP | 8761 | My IP | Eureka dashboard (optional, demo) |
   | Custom TCP | 8025 | My IP | MailHog emails (optional, demo) |

3. Leave outbound as default (allow all).

> You do **not** need to expose 8080 (gateway) — the frontend's nginx proxies `/api`
> to the gateway over the internal Docker network. Only port 3000 must be public.

---

## 4. Launch the EC2 instance

1. EC2 → **Launch instance**.
2. **Name:** `shopsphere`.
3. **AMI:** *Amazon Linux 2023* (x86_64) — the bootstrap script targets this.
4. **Instance type:** **t3.large** (minimum) or t3.xlarge (smoother).
5. **Key pair:** `shopsphere-key`.
6. **Network settings → Select existing security group →** `shopsphere-sg`.
7. **Storage:** change the root volume to **30 GB gp3** (8 GB default is too small for
   13 images + build artifacts).
8. **Launch instance.**

Wait until **Instance state = running** and **Status checks = 2/2**.

---

## 5. (Recommended) Allocate an Elastic IP

So your URL doesn't change across stop/start:
1. EC2 → **Elastic IPs → Allocate**.
2. Select it → **Actions → Associate** → choose your `shopsphere` instance.
3. Note this IP — it's your public address. (Skip this section if you prefer to avoid
   the stopped-EIP charge and don't mind a changing IP.)

---

## 6. Connect via SSH

From your laptop (replace with your key path and the instance's public IP / Elastic IP):

```bash
ssh -i shopsphere-key.pem ec2-user@<PUBLIC_IP>
```

(Windows: use the same command in PowerShell/Windows Terminal, or PuTTY with the .ppk.)

---

## 7. Install prerequisites (one command)

On the instance:

```bash
# Pull just the setup script, or clone the repo first (next step) and run it from there.
curl -SLo ec2-setup.sh \
  https://raw.githubusercontent.com/<your-org>/<backend-repo>/main/deployment/aws/ec2-setup.sh
chmod +x ec2-setup.sh
./ec2-setup.sh
```

This installs Docker, the Compose plugin, Git, Corretto (JDK), and Node.js.

**Then log out and back in** (`exit`, then SSH again) so your user picks up the
`docker` group — otherwise every `docker` command needs `sudo`.

> If you're not hosting the repo publicly, skip the curl and just `git clone` in the
> next step, then run `deployment/aws/ec2-setup.sh` from the cloned copy.

---

## 8. Get the code onto the box

You need **both** repos — backend and frontend.

```bash
# Backend
git clone <your-backend-repo-url> shopsphere
cd shopsphere

# Frontend (clone alongside, or wherever you like)
cd ~
git clone <your-frontend-repo-url> shopsphere-frontend
```

> If your code isn't in a Git host yet, push it to a **private GitHub repo** first
> (free). Don't `scp` 3 GB of images — building on the box is simpler.

---

## 9. Create the Stripe `.env`

```bash
cd ~/shopsphere
cp deployment/docker/.env.example deployment/docker/.env
nano deployment/docker/.env      # paste your real STRIPE_API_KEY=sk_test_...
```

---

## 10. Build the JARs (Maven)

The service Dockerfiles `COPY target/*.jar` — they don't compile inside Docker, so
build first. With nothing else running, an 8 GB box handles this fine.

```bash
cd ~/shopsphere
./mvnw -q clean package -DskipTests
```

(Takes several minutes the first time while Maven downloads dependencies.)

---

## 11. Start the backend (with memory limits)

This is the critical part — use the **prod override** so the JVMs don't OOM the box:

```bash
cd ~/shopsphere
docker compose \
  -f deployment/docker/docker-compose.yml \
  -f deployment/docker/docker-compose.prod.yml \
  up -d --build
```

Give it **2–3 minutes** to boot and register with Eureka. Watch progress:

```bash
docker compose -f deployment/docker/docker-compose.yml ps
docker stats --no-stream            # confirm memory is within limits
```

Sanity check the gateway and seeded data:

```bash
curl -s localhost:8080/api/products | head -c 200      # should return product JSON
```

---

## 12. Start the frontend

```bash
cd ~/shopsphere-frontend
docker compose up -d --build
```

The frontend's nginx joins the backend's Docker network (`docker_microservices-net`)
and proxies `/api` to the gateway — same as on your laptop.

---

## 13. Open it

In your browser:

```
http://<PUBLIC_IP>:3000
```

- Log in as the seeded admin: `admin@shopsphere.com` / `Admin@1234`
- Browse the 12 demo products, add to cart, check out with `tok_visa`.
- (Optional) Eureka at `http://<PUBLIC_IP>:8761`, MailHog at `http://<PUBLIC_IP>:8025`
  (only reachable from *your* IP per the security group).

🎉 It's live.

---

## 14. (Optional) A real domain + HTTPS

For a cleaner URL like `https://shop.yourdomain.com` instead of `http://1.2.3.4:3000`:

1. **Buy/point a domain** (Route 53, Namecheap, etc.). Create an **A record** →
   your Elastic IP.
2. **Open ports 80 and 443** in the security group (Source 0.0.0.0/0).
3. **Put Caddy in front** (easiest auto-HTTPS via Let's Encrypt). On the box:

   ```bash
   # /home/ec2-user/Caddyfile
   shop.yourdomain.com {
       reverse_proxy localhost:3000
   }
   ```
   ```bash
   docker run -d --name caddy --network host \
     -v ~/Caddyfile:/etc/caddy/Caddyfile \
     -v caddy_data:/data \
     caddy:2
   ```
   Caddy fetches a TLS cert automatically. Now `https://shop.yourdomain.com` works.

---

## 15. Day-to-day operations

```bash
# Stop the WHOLE box to save money (from the AWS Console: Instances → Stop)
# ...later: Start it; SSH back in; then:
cd ~/shopsphere
docker compose -f deployment/docker/docker-compose.yml \
               -f deployment/docker/docker-compose.prod.yml up -d
cd ~/shopsphere-frontend && docker compose up -d

# View logs for one service
docker logs order-service --tail 50

# Update after a code change (git pull → rebuild that service)
cd ~/shopsphere && git pull
./mvnw -q clean package -pl business-services/order-service -am -DskipTests
docker compose -f deployment/docker/docker-compose.yml \
               -f deployment/docker/docker-compose.prod.yml up -d --build order-service
```

> Data persists across stop/start because MySQL and MongoDB use named Docker volumes
> (`mysql-data`, `mongo-data`) — those live on the EBS disk, which survives instance
> stop/start. (They're only lost if you terminate the instance or `down --volumes`.)

---

## 16. Security hardening (do at least these)

- **SSH (22) restricted to your IP**, never `0.0.0.0/0`.
- **Eureka (8761) / MailHog (8025) restricted to your IP** — they expose internal info.
- Change default creds you're shipping: `MYSQL_ROOT_PASSWORD`, `GATEWAY_PASSWORD`,
  the seeded admin password, and `ADMIN_REGISTRATION_KEY` (set via env, don't keep
  defaults on a public box).
- Keep the **Stripe key in `.env` only** (already gitignored) — use a **test** key.
- Run `sudo dnf update -y` periodically.
- Consider AWS **Budgets** → set a billing alert (e.g. alert at $20) so a forgotten
  running instance can't surprise you.

---

## 17. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| A container keeps restarting; `docker ps` shows exit **137** | OOM-killed (out of memory) | Raise that service's `mem_limit` in `docker-compose.prod.yml`, or use a bigger instance (t3.xlarge) |
| `curl localhost:8080/api/products` hangs/empties right after start | Services not registered with Eureka yet | Wait 2–3 min; check `docker logs api-gateway`, `docker logs discovery-server` |
| 503 from the gateway | Target service not registered / still booting | Check that service's logs; confirm it's `Up` in `docker compose ps` |
| Frontend loads but `/api` calls fail | Frontend not on the backend network | Ensure backend came up first (creates `docker_microservices-net`); restart frontend compose |
| Browser can't reach `:3000` | Security group | Confirm inbound TCP 3000 from 0.0.0.0/0 |
| `./mvnw` fails: wrong Java version | Corretto 25 wasn't available; 21 installed | Install Corretto 25 manually, or lower the project's target Java version |
| Build itself gets OOM-killed | Too little RAM during compile | Build is fine on 8 GB with nothing running; if on a smaller box, build module-by-module with `-pl` |

---

## 18. Tearing it all down (stop paying)

When you no longer need it:

1. EC2 → **Instances → Terminate** the instance.
2. EC2 → **Elastic IPs → Release** the address (else you keep paying for it).
3. EC2 → **Volumes** → delete any leftover EBS volume.
4. (Optional) Delete the security group and key pair.

---

## Appendix — Why this shape, and when to graduate

- **Single EC2 + docker-compose** is the cheapest way to put the *whole* system online
  and is perfectly legitimate for a portfolio. You can honestly say "deployed to AWS on
  EC2, containerized with Docker Compose, memory-tuned for a constrained host."
- **When to move up:** if you want to *talk about* production AWS in interviews, the
  next step is **ECS Fargate** (managed containers) + **RDS** (managed MySQL) +
  **DocumentDB/Atlas** + **MSK** (Kafka) behind an **ALB**. That's the
  "production-grade learning" path — more cost, more realism. Ask and I'll write that
  guide too.
