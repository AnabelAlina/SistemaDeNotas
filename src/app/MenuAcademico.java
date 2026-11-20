package app;

import excepciones.PesoInvalidoException;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import modelo.Persona;

//Submenú académico: registro de estudiantes, ciclos y cursos.
//Responsable: Estudiante 1 (Anabel Quispe)

public class MenuAcademico {

    private GestorNotas gestor;
    private Entrada entrada;

    public MenuAcademico(GestorNotas gestor, Entrada entrada) {
        this.gestor = gestor;
        this.entrada = entrada;
    }

    public void mostrar() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN ACADÉMICA ---");
            System.out.println(" 1. Registrar estudiante");
            System.out.println(" 2. Agregar ciclo");
            System.out.println(" 3. Cargar los cursos predeterminados del semestre");
            System.out.println(" 4. Agregar un curso manual");
            System.out.println(" 5. Ver estudiantes, ciclos y cursos");
            System.out.println(" 0. Volver");
            int opcion = entrada.leerEntero("Elige una opción: ", 0, 5);
            try {
                switch (opcion) {
                    case 1: registrarEstudiante(); break;
                    case 2: agregarCiclo(); break;
                    case 3: cargarPredeterminados(); break;
                    case 4: agregarCursoManual(); break;
                    case 5: verResumen(); break;
                    case 0: volver = true; break;
                }
            } catch (IllegalArgumentException | PesoInvalidoException e) {
                System.out.println("No se pudo completar la operación: " + e.getMessage());
            }
        }
    }

    // ===================== ESTUDIANTE =====================

    private void registrarEstudiante() {
        System.out.println("\n== Registrar estudiante ==");
        String nombre = entrada.leerTexto("Nombres: ");
        String apellido = entrada.leerTexto("Apellidos: ");

        // Cada dato se valida por separado: si falla, solo se vuelve a pedir ESE dato
        String dni = pedirValido("DNI (8 dígitos): ", texto -> {
            String valido = Persona.validarDni(texto);
            if (gestor.existeDni(valido)) {
                throw new IllegalArgumentException("Ya existe un estudiante con ese DNI.");
            }
            return valido;
        });
        String cui = pedirValido("CUI (8 cifras): ", texto -> {
            String valido = Estudiante.validarCui(texto);
            if (gestor.buscarEstudiante(valido) != null) {
                throw new IllegalArgumentException("Ya existe un estudiante con ese CUI.");
            }
            return valido;
        });
        String correo = pedirValido("Correo institucional (@unsa.edu.pe): ",
                Estudiante::validarCorreoInstitucional);
        String carrera = entrada.leerTexto("Carrera: ");

        Estudiante estudiante = new Estudiante(nombre, apellido, dni, correo, cui, carrera);
        if (gestor.registrarEstudiante(estudiante)) {
            System.out.println("✔ Estudiante registrado: " + estudiante.getNombreCompleto()
                    + " (CUI " + estudiante.getCodigo() + ")");
        }
    }

    // Repite la pregunta hasta que el validador acepte el dato
    private String pedirValido(String mensaje, UnaryOperator<String> validador) {
        while (true) {
            String texto = entrada.leerLinea(mensaje);
            try {
                return validador.apply(texto);
            } catch (IllegalArgumentException e) {
                System.out.println("  ✗ " + e.getMessage());
            }
        }
    }

    // ===================== CICLO =====================

    private void agregarCiclo() {
        Estudiante est = entrada.seleccionarEstudiante(gestor);
        if (est == null) return;
        String nombre = entrada.leerTexto("Nombre del ciclo (ej. 2026-B): ");
        int anio = entrada.leerEntero("Año: ", 2000, 2100);
        est.agregarCiclo(new Ciclo(nombre, anio));
        System.out.println("✔ Ciclo " + nombre.trim() + " agregado a " + est.getNombreCompleto() + ".");
    }

    // ===================== CURSOS =====================

    private void cargarPredeterminados() throws PesoInvalidoException {
        Estudiante est = entrada.seleccionarEstudiante(gestor);
        if (est == null) return;
        Ciclo ciclo = entrada.elegirCiclo(est);
        if (ciclo == null) return;
        double meta = entrada.leerDouble("Meta de promedio para estos cursos (0-20): ", 0, 20);
        int agregados = ciclo.agregarCursosPredeterminados(meta);
        if (agregados == 0) {
            System.out.println("Ese ciclo ya tenía todos los cursos predeterminados.");
        } else {
            System.out.println("✔ Se cargaron " + agregados + " cursos en el ciclo " + ciclo.getNombre() + ".");
        }
    }

    private void agregarCursoManual() throws PesoInvalidoException {
        Estudiante est = entrada.seleccionarEstudiante(gestor);
        if (est == null) return;
        Ciclo ciclo = entrada.elegirCiclo(est);
        if (ciclo == null) return;

        String codigo = entrada.leerTexto("Código del curso: ");
        String nombre = entrada.leerTexto("Nombre del curso: ");
        int creditos = entrada.leerEntero("Créditos (1-10): ", 1, 10);
        double meta = entrada.leerDouble("Meta de promedio (0-20): ", 0, 20);

        System.out.println("Porcentajes del sílabo (sobre la nota final). Los 6 deben sumar 100.");
        double[] pesosExamen = new double[3];
        double[] pesosContinua = new double[3];
        for (int i = 0; i < 3; i++) {
            pesosContinua[i] = entrada.leerDouble("% de la Continua " + (i + 1) + ": ", 0, 100);
            pesosExamen[i] = entrada.leerDouble("% del Examen " + (i + 1) + ": ", 0, 100);
        }

        Curso curso = ciclo.agregarCursoManual(codigo, nombre, creditos, meta, pesosExamen, pesosContinua);
        System.out.println("✔ Curso agregado: " + curso.getCodigo() + " - " + curso.getNombre());
    }

    // ===================== RESUMEN =====================

    private void verResumen() {
        ArrayList<Estudiante> todos = gestor.getEstudiantes();
        if (todos.isEmpty()) {
            System.out.println("Todavía no hay estudiantes registrados.");
            return;
        }
        for (Estudiante e : todos) {
            System.out.println("\n" + e.getNombreCompleto() + " | CUI " + e.getCodigo()
                    + " | DNI " + e.getDni() + " | " + e.getCorreo());
            if (e.getCiclos().isEmpty()) {
                System.out.println("  (sin ciclos)");
            }
            for (Ciclo c : e.getCiclos()) {
                System.out.println("  Ciclo " + c.getNombre() + " (" + c.getCursos().size() + " cursos)");
                for (Curso curso : c.getCursos()) {
                    System.out.println("    - " + curso.getCodigo() + " " + curso.getNombre()
                            + " (" + curso.getCreditos() + " créditos)");
                }
            }
        }
    }
}