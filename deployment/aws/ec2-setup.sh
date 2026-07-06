#!/usr/bin/env bash
# =============================================================================
#  ShopSphere — EC2 bootstrap (Amazon Linux 2023, x86_64)
# =============================================================================
#  Installs everything needed to BUILD and RUN the stack on a single EC2 box:
#  Docker + Compose plugin, Git, Amazon Corretto 25 (to run the Maven wrapper),
#  and Node.js (to build the frontend).
#
#  Usage (after SSHing into the instance):
#    chmod +x ec2-setup.sh
#    ./ec2-setup.sh
#    # then LOG OUT and back in so the docker group takes effect
# =============================================================================
set -euo pipefail

echo "==> Updating system packages"
sudo dnf update -y

echo "==> Installing Docker"
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"   # run docker without sudo (after re-login)

echo "==> Installing Docker Compose plugin (v2)"
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL \
  "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

echo "==> Installing Git"
sudo dnf install -y git

echo "==> Installing Amazon Corretto 25 (JDK, to run ./mvnw)"
# Corretto RPM repo
sudo rpm --import https://yum.corretto.aws/corretto.key
sudo curl -SL https://yum.corretto.aws/corretto.repo -o /etc/yum.repos.d/corretto.repo
# Try Corretto 25; fall back to 21 if 25 isn't in the repo yet (only matters if
# the project still targets 25 — then install 25 manually from the Corretto site).
sudo dnf install -y java-25-amazon-corretto-devel \
  || { echo "Corretto 25 not in repo — installing 21 (adjust if build needs 25)"; \
       sudo dnf install -y java-21-amazon-corretto-devel; }

echo "==> Installing Node.js (for the frontend build)"
sudo dnf install -y nodejs

echo
echo "==> Versions:"
docker --version
docker compose version
git --version
java -version 2>&1 | head -1
node --version

echo
echo "DONE. Log out and back in (so the 'docker' group applies), then continue"
echo "with the deploy steps in docs/AWS_DEPLOYMENT.md."
