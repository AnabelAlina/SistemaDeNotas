package calculos;

// Ani (Estudiante 2) - Estado de un curso segun su promedio actual y lo que falta rendir.
// El texto es lo que muestra la ventana; los colores (verde, ambar, rojo) estan en gui/Estilo.
public enum EstadoCurso {
    APROBADO("Aprobado"),
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