"""Genera le immagini usate nella relazione (python3 diagrammi.py)."""
import csv
import os
import textwrap

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


# ---------------------------------------------------------------- Miro: disegno funzionale
def miro():
    fig, ax = tela(13, 6.2)
    fig.set_facecolor("#f4f4f2")
    for i in range(0, 60):
        for j in range(0, 30):
            ax.plot(i / 60, j / 30, ".", color="#dcdcd6", ms=1.2)
    box(ax, 0.39, 0.86, 0.22, 0.1, "VOLLEY LEAGUE\nGestione campionati di pallavolo", NAVY, colore="white", size=11, bold=True)

    attori = [("Giudice sportivo", "#ffd6d6"), ("Dirigente", "#d6e6ff"), ("Arbitro", "#ffe9c2"), ("Giocatore", "#d9f2df")]
    ax.text(0.015, 0.94, "Attori", fontsize=10, fontweight="bold", color=GRIGIO)
    for k, (a, c) in enumerate(attori):
        box(ax, 0.015, 0.84 - k * 0.075, 0.11, 0.06, a, c, ec="#b9b9b0", size=8.5)

    colonne = [
        ("MF1 · Organizzazione campionato e accesso", "10 SP", [
            ("Login e profilo (JWT)", "#ffffff"), ("Guard per ruolo (RBAC)", "#ffffff"),
            ("Crea campionato / girone", "#ffd6d6"), ("Genera calendario (Berger)", "#ffd6d6"),
            ("Designa arbitro con vincoli", "#ffd6d6")]),
        ("MF2 · Gestione squadra e lista gara", "8 SP", [
            ("Squadre della società", "#d6e6ff"), ("Tesseramento atleti (rosa)", "#d6e6ff"),
            ("Iscrizione al campionato", "#d6e6ff"), ("Compila lista gara (≤14 + staff)", "#d6e6ff"),
            ("PROGRAMMATA → ROSTER_INVIATI", "#ffffff")]),
        ("MF3 · Referto, omologazione e classifica", "12 SP", [
            ("Avvio gara (arbitro)", "#ffe9c2"), ("Referto set per set + statistiche", "#ffe9c2"),
            ("Firma digitale e invio", "#ffe9c2"), ("Omologa / respingi referto", "#ffd6d6"),
            ("Classifica FIPAV + area giocatore", "#d9f2df")]),
    ]
    for c, (titolo, sp, note) in enumerate(colonne):
        x = 0.16 + c * 0.28
        box(ax, x, 0.71, 0.25, 0.08, textwrap.fill(titolo, 30), BLU, colore="white", size=9, bold=True)
        box(ax, x + 0.19, 0.775, 0.05, 0.03, sp, ARANCIO, colore=NAVY, size=8, bold=True, r=0.01)
        freccia(ax, (0.5, 0.86), (x + 0.125, 0.79), colore="#9aa0ad", rad=0.0)
        for k, (n, colore) in enumerate(note):
            yy = 0.58 - k * 0.11
            ax.add_patch(FancyBboxPatch((x + 0.012, yy - 0.006), 0.226, 0.085, boxstyle="square,pad=0", fc="#00000012", ec="none"))
            box(ax, x + 0.005, yy, 0.226, 0.085, n, colore, ec="#c9c9c0", size=8.5, r=0.004)
        ax.text(x + 0.125, 0.035, ["FE · BE · DB", "FE · BE · DB", "FE · BE · DB"][c], ha="center", fontsize=8, color=GRIGIO)
    salva(fig, "miro_disegno_funzionale.png")


# ---------------------------------------------------------------- Trello
def trello():
    with open(os.path.join(QUI, "..", "trello_backlog.csv"), encoding="utf-8") as f:
        righe = list(csv.DictReader(f))
    fig, ax = tela(13, 6.2)
    fig.set_facecolor("#0c66e4")
    ax.text(0.015, 0.955, "Volley League · Ingegneria del Software", fontsize=13, color="white", fontweight="bold")
    ax.text(0.015, 0.915, "Board di progetto · 30 SP · 3 sprint", fontsize=9, color="#dfe9ff")
    liste = ["Sprint 1", "Sprint 2", "Sprint 3"]
    etichette = {"FE": "#4bce97", "BE": "#9f8fef", "DB": "#f5cd47"}
    colonne = [("Backlog", []), ("Sprint 1 · Done", [r for r in righe if r["Sprint"] == "Sprint 1"]),
               ("Sprint 2 · Done", [r for r in righe if r["Sprint"] == "Sprint 2"]),
               ("Sprint 3 · Done", [r for r in righe if r["Sprint"] == "Sprint 3"]),
               ("Test & Documentazione", None)]
    w = 0.185
    for c, (nome, cards) in enumerate(colonne):
        x = 0.012 + c * (w + 0.012)
        altezza = 0.84
        ax.add_patch(FancyBboxPatch((x, 0.03), w, altezza, boxstyle="round,pad=0,rounding_size=0.012", fc="#f1f2f4", ec="none"))
        ax.text(x + 0.01, 0.845, nome, fontsize=9.5, fontweight="bold", color="#172b4d")
        if cards is None:
            cards = [{"Card": "94 test JUnit/Mockito verdi", "Livello": "BE", "Story Points": "-"},
                     {"Card": "Relazione 10 slide (PDF)", "Livello": "DOC", "Story Points": "-"},
                     {"Card": "Dump DB + Dockerfile", "Livello": "DB", "Story Points": "-"},
                     {"Card": "Demo video journey utente", "Livello": "DOC", "Story Points": "-"}]
        if not cards:
            cards = [{"Card": "Notifiche email (fuori perimetro)", "Livello": "IDEA", "Story Points": "-"},
                     {"Card": "Playoff / fasi finali (fuori perimetro)", "Livello": "IDEA", "Story Points": "-"}]
        for k, r in enumerate(cards):
            y = 0.675 - k * 0.16
            ax.add_patch(FancyBboxPatch((x + 0.008, y), w - 0.016, 0.14, boxstyle="round,pad=0,rounding_size=0.008",
                                        fc="white", ec="#dcdfe4"))
            liv = r["Livello"]
            ax.add_patch(FancyBboxPatch((x + 0.016, y + 0.105), 0.035, 0.022, boxstyle="round,pad=0,rounding_size=0.004",
                                        fc=etichette.get(liv, "#8590a2"), ec="none"))
            ax.text(x + 0.0335, y + 0.116, liv, fontsize=6.5, ha="center", va="center", color="#172b4d", fontweight="bold")
            ax.text(x + 0.016, y + 0.06, textwrap.fill(r["Card"], 32), fontsize=7.8, color="#172b4d", va="center", wrap=True)
            if r["Story Points"] != "-":
                ax.text(x + w - 0.02, y + 0.018, f'{r["Story Points"]} SP', fontsize=7.5, ha="right", color="#44546f", fontweight="bold")
                ax.text(x + 0.016, y + 0.018, "✓ test", fontsize=7, color="#1f845a")
    salva(fig, "trello_board.png")


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


# ---------------------------------------------------------------- Burndown
def burndown():
    fig = plt.figure(figsize=(6.2, 3.4), dpi=200)
    ax = fig.add_subplot(111)
    giorni = list(range(0, 16))
    ideale = [30 - 30 * g / 15 for g in giorni]
    # S1 = 10 SP (giorni 1-5), S2 = 11 SP (6-10), S3 = 9 SP (11-15)
    reale = [30, 30, 28, 25, 23, 20, 20, 17, 15, 12, 9, 9, 7, 5, 2, 0]
    ax.plot(giorni, ideale, "--", color="#9aa0ad", label="Ideale")
    ax.plot(giorni, reale, "-o", color=BLU, ms=3.5, lw=2, label="Reale")
    for x, t in [(5, "Review S1"), (10, "Review S2"), (15, "Review S3")]:
        ax.axvline(x, color=ARANCIO, lw=1, alpha=.6)
        ax.text(x - 0.2, 29, t, rotation=90, va="top", ha="right", fontsize=7, color="#a35a00")
    ax.set_xlabel("Giorni lavorativi")
    ax.set_ylabel("Story point residui")
    ax.set_ylim(0, 31)
    ax.grid(alpha=.25)
    ax.legend(frameon=False, fontsize=8)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    fig.tight_layout()
    fig.savefig(os.path.join(IMG, "burndown.png"))
    plt.close(fig)
    print("creato burndown.png")


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
    miro()
    trello()
    architettura()
    schema_er()
    burndown()
    stati()
