# QuickEats Production Deployment & Database Setup Guide

This guide provides step-by-step instructions for deploying **QuickEats Backend** (Render) and **QuickEats Frontend** (Vercel) with persistent **PostgreSQL Database**.

---

## 1. Setting up PostgreSQL Database on Render

To ensure data (users, orders, restaurants) persists across server restarts and redeployments, create a persistent PostgreSQL database on Render:

1. Log into your [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** -> **PostgreSQL**.
3. Fill in the details:
   - **Name:** `quickeats-db`
   - **Database Name:** `quickeatsdb`
   - **User:** `quickeats_user`
   - **Region:** Choose the same region as your Web Service (e.g., Singapore, Frankfurt, Oregon)
4. Click **Create Database**.
5. Once created, copy the following values from your PostgreSQL Info page:
   - **Internal Database URL** (e.g., `postgres://quickeats_user:password@dpg-xxxxx-a/quickeatsdb`)
   - **Username** (`quickeats_user`)
   - **Password** (`your_password`)

---

## 2. Configuring Render Backend Web Service

1. Go to your **QuickEats Web Service** in Render Dashboard.
2. Click on **Environment** in the left menu.
3. Add the following **Environment Variables**:

| Key | Value Example / Description |
| :--- | :--- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://dpg-xxxxx-a:5432/quickeatsdb` <br> *(Take Internal Database URL, change `postgres://` to `jdbc:postgresql://`)* |
| `SPRING_DATASOURCE_USERNAME` | `quickeats_user` *(From Render Postgres Info)* |
| `SPRING_DATASOURCE_PASSWORD` | `your_postgres_password` *(From Render Postgres Info)* |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` *(Use `update` on first run to auto-create tables; set to `validate` later for safety)* |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` *(256-bit secret key)* |
| `CORS_ALLOWED_ORIGINS` | `https://quickeats-sandy.vercel.app,http://localhost:5173` |
| `GROQ_API_KEY` | `gsk_your_groq_api_key_here` *(Optional for AI Chat & Recommendations)* |

4. Click **Save Changes**. Render will automatically trigger a deployment with PostgreSQL.

---

## 3. Database Schema Strategy (`ddl-auto` vs Flyway/Liquibase)

- **Initial Deployment (`update`):**
  When first connecting to PostgreSQL, set `SPRING_JPA_HIBERNATE_DDL_AUTO=update`. Spring Data JPA will automatically create all tables (`users`, `orders`, `restaurants`, `menu_items`, `refresh_tokens`, etc.).

- **Production Safety (`validate`):**
  Once your tables are created, you can update `SPRING_JPA_HIBERNATE_DDL_AUTO` to `validate` in Render Environment settings. This ensures Hibernate will only check the database structure on startup without risking accidental schema alterations or data loss.

---

## 4. Configuring Vercel Frontend

1. Log into your [Vercel Dashboard](https://vercel.com/dashboard).
2. Select your `QuickEats-Frontend` project.
3. Go to **Settings** -> **Environment Variables**.
4. Add the variable:
   - **Key:** `VITE_API_URL`
   - **Value:** `https://quickeats-ordering-system.onrender.com` *(No trailing slash)*
5. Click **Save** and trigger a **Redeploy** on Vercel.
