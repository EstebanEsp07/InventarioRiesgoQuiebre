"""Servicio de pronóstico (stub). Reemplazar por el modelo real."""
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="Pronóstico de demanda")


class Request(BaseModel):
    producto_id: str
    bodega_id: str
    consumo_diario_historico: list[float]
    horizonte_dias: int = 7


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/forecast")
def forecast(req: Request):
    h = req.consumo_diario_historico[-14:] or [0.0]
    promedio = sum(h) / len(h)
    return {
        "producto_id": req.producto_id,
        "bodega_id": req.bodega_id,
        "demanda_diaria_estimada": round(promedio, 2),
        "demanda_horizonte": round(promedio * req.horizonte_dias, 2),
        "modelo": "promedio-movil-14d-stub",
    }
