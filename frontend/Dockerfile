# ==============================================================================
# ProcessHub Frontend - Production Multi-stage Dockerfile
# ==============================================================================

# Stage 1: Build Static Assets
FROM node:22-alpine AS builder

WORKDIR /app

# Copy dependency manifests
COPY package.json package-lock.json ./

# Install exact dependencies
RUN npm ci

# Copy frontend source code
COPY . .

# Build production bundle to dist/
RUN npm run build

# Stage 2: Serve via Nginx Alpine
FROM nginx:1.27-alpine AS production

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy built distribution from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom Nginx server configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose HTTP port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
