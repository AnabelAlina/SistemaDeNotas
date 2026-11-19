package app;

/**
 * Submenú académico: registro de estudiantes, ciclos y cursos.
 * Responsable: Estudiante 1 (Anabel Quispe).
 */
public class MenuAcademico {

    private GestorNotas gestor;
    private Entrada entrada;

    public MenuAcademico(GestorNotas gestor, Entrada entrada) {
        this.gestor = gestor;
        this.entrada = entrada;
    }

    public void mostrar() {
        System.out.println("\n--- GESTIÓN ACADÉMICA ---");
        System.out.println("(Módulo en construcción por Estudiante 1)");
    }
}