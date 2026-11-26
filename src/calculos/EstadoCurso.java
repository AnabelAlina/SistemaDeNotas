package calculos;

// Estado de un curso segun lo que ya ganaste y lo que falta rendir.
// El texto es lo que muestra la ventana; los colores estan en gui/Estilo.
public enum EstadoCurso {
    APROBADO("Aprobado"),
    VAS_APROBANDO("Vas aprobando"),
    EN_RIESGO("En riesgo"),
    DESAPROBADO("Desaprobado");

    private final String texto;

    EstadoCurso(String texto) {
        this.texto = texto;
    }

    // Texto para mostrar en pantalla
    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}