"""Genera le immagini usate nella relazione (python3 diagrammi.py)."""
import os

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

QUI = os.path.dirname(os.path.abspath(__file__))
IMG = os.path.join(QUI, "img")
os.makedirs(IMG, exist_ok=True)

NAVY = "#121c46"
BLU = "#1f4fd1"
ARANCIO = "#ff9d2e"
GRIGIO = "#5a6378"
plt.rcParams["font.family"] = "DejaVu Sans"


def box(ax, x, y, w, h, testo, fc, ec=None, colore="#1a2033", size=9, bold=False, r=0.02, align="center"):
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle=f"round,pad=0,rounding_size={r}",
                                fc=fc, ec=ec or fc, lw=1.2))
    tx = x + w / 2 if align == "center" else x + 0.012
    ax.text(tx, y + h / 2, testo, ha=align, va="center", fontsize=size, color=colore,
            fontweight="bold" if bold else "normal", wrap=True, linespacing=1.25)


def freccia(ax, a, b, colore=GRIGIO, testo=None, stile="-|>", lw=1.4, rad=0.0, size=8):
    ax.add_patch(FancyArrowPatch(a, b, arrowstyle=stile, mutation_scale=12, color=colore, lw=lw,
                                 connectionstyle=f"arc3,rad={rad}"))
    if testo:
        ax.text((a[0] + b[0]) / 2, (a[1] + b[1]) / 2 + 0.015, testo, ha="center", va="bottom",
                fontsize=size, color=colore)


def tela(w, h):
    fig = plt.figure(figsize=(w, h), dpi=200)
    ax = fig.add_axes([0, 0, 1, 1])
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.axis("off")
    return fig, ax


def salva(fig, nome):
    fig.savefig(os.path.join(IMG, nome), facecolor=fig.get_facecolor())
    plt.close(fig)
    print("creato", nome)


# ---------------------------------------------------------------- Architettura
def architettura():
    fig, ax = tela(8.2, 6.6)
    fig.set_facecolor("white")
    box(ax, 0.03, 0.73, 0.94, 0.24, "", "#eef3ff", ec="#c7d8ff", r=0.02)
    ax.text(0.05, 0.935, "FRONTEND · Angular 19 + Angular Material  (localhost:4200)", fontsize=9.5, fontweight="bold", color=BLU)
    fe = ["Componenti\n(View)", "Servizi RxJS\nApiService", "AuthInterceptor\n(Bearer JWT)", "Guard RBAC\nauth / role"]
    for k, t in enumerate(fe):
        box(ax, 0.05 + k * 0.232, 0.76, 0.21, 0.13, t, "white", ec="#9db8f5", size=8.5)

    freccia(ax, (0.5, 0.73), (0.5, 0.635), colore=NAVY, stile="<|-|>", lw=1.8)
    ax.text(0.52, 0.675, "REST / JSON  ·  proxy /api → :8080  ·  Authorization: Bearer <JWT>", fontsize=8.2, color=NAVY)

    box(ax, 0.03, 0.2, 0.94, 0.43, "", "#fff6ea", ec="#ffd8a8", r=0.02)
    ax.text(0.05, 0.6, "BACKEND · Spring Boot 3.4 (localhost:8080)", fontsize=9.5, fontweight="bold", color="#a35a00")
    box(ax, 0.05, 0.5, 0.9, 0.07, "Spring Security: JwtAuthenticationFilter → @PreAuthorize(hasRole)", "#ffe3bf", size=8.5)
    box(ax, 0.05, 0.41, 0.28, 0.07, "@RestController (C)", "white", ec="#f2b76b", size=8.5, bold=True)
    box(ax, 0.36, 0.41, 0.28, 0.07, "@Service @Transactional", "white", ec="#f2b76b", size=8.5, bold=True)
    box(ax, 0.67, 0.41, 0.28, 0.07, "Repository JPA", "white", ec="#f2b76b", size=8.5, bold=True)
    freccia(ax, (0.33, 0.445), (0.36, 0.445))
    freccia(ax, (0.64, 0.445), (0.67, 0.445))
    box(ax, 0.05, 0.225, 0.9, 0.16,
        "Dominio (Model):  State Pattern (macchina a stati partita) · Strategy (ordinamento classifica)\n"
        "Factory (RefertoFactory) · RefertoValidator · ListaGaraValidator · GeneratoreCalendario (Berger)\n"
        "VincoliDesignazione · DTO record + DtoMapper · GlobalExceptionHandler (400/401/403/404/409/422)",
        "white", ec="#f2b76b", size=8)

    freccia(ax, (0.5, 0.2), (0.5, 0.135), colore=NAVY, stile="<|-|>", lw=1.8)
    ax.text(0.52, 0.155, "JDBC · Hibernate", fontsize=8.2, color=NAVY)
    box(ax, 0.03, 0.02, 0.94, 0.11, "", "#e9f7ee", ec="#bfe5cc", r=0.02)
    ax.text(0.05, 0.075, "DATABASE · MySQL 8 schema webapp (webappuser) — 10 tabelle, FK, vincoli UNIQUE, tabella ponte N:M",
            fontsize=8.8, fontweight="bold", color="#116336", va="center")
    salva(fig, "architettura.png")


# ---------------------------------------------------------------- Schema ER
def schema_er():
    fig, ax = tela(8.2, 6.6)
    fig.set_facecolor("white")
    tabelle = {
        "utente": (0.02, 0.62, ["id PK", "email UQ", "password (BCrypt)", "ruolo ENUM", "id_societa FK", "provincia"]),
        "societa": (0.02, 0.2, ["id PK", "nome", "codice_fipav UQ", "citta", "provincia"]),
        "campionato": (0.36, 0.74, ["id PK", "nome, categoria", "stagione, girone", "stato ENUM"]),
        "squadra": (0.36, 0.38, ["id PK", "nome", "id_societa FK", "id_campionato FK"]),
        "tesserato": (0.36, 0.02, ["id PK", "nome, cognome", "ruolo_campo ENUM", "numero_maglia", "id_squadra FK", "id_utente FK UQ"]),
        "partita": (0.7, 0.62, ["id PK", "id_campionato FK", "id_squadra_casa FK", "id_squadra_ospite FK", "id_arbitro FK",
                                "data_ora, stato ENUM", "set_casa, set_ospite", "hash_referto"]),
        "set_partita": (0.7, 0.4, ["id_partita FK", "numero_set (UQ)", "punti / cambi"]),
        "statistica_giocatore": (0.7, 0.18, ["id_partita FK", "id_tesserato FK", "punti, ace, muri", "errori, cartellino"]),
        "lista_gara (+ _atleta N:M)": (0.7, 0.0, ["id_partita FK", "id_squadra FK", "allenatore, staff"]),
    }
    w = 0.28
    pos = {}
    for nome, (x, y, campi) in tabelle.items():
        h = 0.042 + 0.032 * len(campi)
        ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0,rounding_size=0.01", fc="white", ec=BLU, lw=1.2))
        ax.add_patch(FancyBboxPatch((x, y + h - 0.042), w, 0.042, boxstyle="round,pad=0,rounding_size=0.01", fc=BLU, ec=BLU))
        ax.text(x + 0.01, y + h - 0.021, nome, color="white", fontsize=8.5, fontweight="bold", va="center")
        for k, c in enumerate(campi):
            ax.text(x + 0.012, y + h - 0.06 - k * 0.032, c, fontsize=7.4, color="#1a2033", va="center")
        pos[nome] = (x, y, w, h)

    def lato(n, verso):
        x, y, ww, h = pos[n]
        return {"r": (x + ww, y + h / 2), "l": (x, y + h / 2), "t": (x + ww / 2, y + h), "b": (x + ww / 2, y)}[verso]

    rel = [("societa", "t", "utente", "b", "1:N"), ("societa", "r", "squadra", "l", "1:N"),
           ("campionato", "b", "squadra", "t", "1:N"), ("squadra", "b", "tesserato", "t", "1:N"),
           ("campionato", "r", "partita", "l", "1:N"), ("partita", "b", "set_partita", "t", "1:N"),
           ("squadra", "r", "partita", "l", "2:N"), ("tesserato", "r", "statistica_giocatore", "l", "1:N"),
           ("tesserato", "r", "lista_gara (+ _atleta N:M)", "l", "N:M")]
    for a, la, b, lb, t in rel:
        freccia(ax, lato(a, la), lato(b, lb), colore="#7a8499", stile="-", lw=1.0, testo=t, size=7)
    freccia(ax, lato("utente", "t"), lato("partita", "t"), colore="#7a8499", stile="-", lw=1.0, rad=-0.25)
    ax.text(0.5, 0.94, "utente (ARBITRO) 1:N partita", ha="center", fontsize=7, color="#7a8499")
    salva(fig, "schema_er.png")


# ---------------------------------------------------------------- Macchina a stati
def stati():
    fig, ax = tela(8.4, 1.9)
    fig.set_facecolor("white")
    nomi = ["PROGRAMMATA", "ROSTER_INVIATI", "IN_CORSO", "REFERTATA", "OMOLOGATA"]
    colori = ["#eceff4", "#e3f0ff", "#fff1d6", "#f3e5ff", "#dcf5e6"]
    eventi = ["2 liste gara", "avvio arbitro", "firma referto", "omologa"]
    w = 0.145
    for k, n in enumerate(nomi):
        x = 0.01 + k * 0.2
        box(ax, x, 0.4, w, 0.3, n, colori[k], ec="#9aa0ad", size=8, bold=True, r=0.04)
        if k < 4:
            freccia(ax, (x + w, 0.55), (x + 0.2, 0.55), colore=NAVY)
            ax.text(x + w + 0.0275, 0.78, eventi[k], ha="center", fontsize=7.5, color=NAVY)
    freccia(ax, (0.6825, 0.4), (0.4825, 0.4), colore="#b3261e", rad=-0.5)
    ax.text(0.5825, 0.05, "respingi (giudice)", ha="center", fontsize=7.5, color="#b3261e")
    salva(fig, "stati_partita.png")


if __name__ == "__main__":
    architettura()
    schema_er()
    stati()
