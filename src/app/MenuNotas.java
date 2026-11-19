package app;

/**
 * Submenú de notas: registro de exámenes, trabajos de continua y sustitutorio.
 * Responsable: Estudiante 1 (Anabel Quispe).
 */
public class MenuNotas {

    private GestorNotas gestor;
    private Entrada entrada;

    public MenuNotas(GestorNotas gestor, Entrada entrada) {
        this.gestor = gestor;
        this.entrada = entrada;
    }

    public void mostrar() {
        System.out.println("\n--- REGISTRO DE NOTAS ---");
        System.out.println("(Módulo en construcción por Estudiante 1)");
    }
}