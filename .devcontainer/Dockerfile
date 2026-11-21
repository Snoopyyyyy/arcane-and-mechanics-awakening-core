FROM eclipse-temurin:21-jdk-noble

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        fish \
        git \
        curl \
        wget \
        ca-certificates \
        openssh-client \
        unzip \
        zip \
        jq \
        ripgrep \
        tree \
        less \
        nano \
        htop \
        procps \
    && rm -rf /var/lib/apt/lists/*
