package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import evaluaciones.Evaluacion;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import modelo.Curso;
import modelo.Estudiante;
import persistencia.Repositorio;

/**
 * Coordina la aplicación: guarda los estudiantes en memoria, registra notas
 * y delega el guardado/carga en un Repositorio.
 *
 * IMPORTANTE: solo conoce la INTERFAZ Repositorio, no RepositorioArchivo.
 */
public class GestorNotas {

    // Clave = código (CUI) del estudiante. LinkedHashMap conserva el orden de registro.
    private HashMap<String, Estudiante> estudiantes;
    private Estudiante actual;
    private Repositorio repositorio;

    public GestorNotas(Repositorio repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo.");
        }
        this.repositorio = repositorio;
        this.estudiantes = new LinkedHashMap<>();
        this.actual = null;
    }

    // Devuelve true si se registró, false si el CUI o el DNI ya existían
    public boolean registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException("El estudiante no puede ser nulo.");
        }
        // 1. Evita duplicados por CUI (código)
        if (estudiantes.containsKey(estudiante.getCodigo())) {
            System.out.println("Ya existe un estudiante con el CUI/código " + estudiante.getCodigo() + ".");
            return false;
        }
        // 2. Evita duplicados por DNI
        if (existeDni(estudiante.getDni())) {
            System.out.println("Ya existe un estudiante con el DNI " + estudiante.getDni() + ".");
            return false;
        }
        // 3. Lo guardamos en el mapa y lo dejamos como estudiante actual
        estudiantes.put(estudiante.getCodigo(), estudiante);
        actual = estudiante;
        return true;
    }

    // Verifica si ya hay un estudiante registrado con ese DNI
    public boolean existeDni(String dni) {
        if (dni == null) {
            return false;
        }
        String limpio = dni.trim();
        for (Estudiante e : estudiantes.values()) {
            if (e.getDni().equals(limpio)) {
                return true;
            }
        }
        return false;
    }

    // Devuelve el estudiante por CUI/código o null si no existe
    public Estudiante buscarEstudiante(String codigo) {
        if (codigo == null) {
            return null;
        }
        return estudiantes.get(codigo.trim());
    }

    // Registra la nota de una evaluación (ej. Examen o ExamenSustitutorio).
    public boolean registrarNota(Curso curso, Evaluacion evaluacion, double nota) {
        if (curso == null || evaluacion == null) {
            System.out.println("Debes elegir un curso y una evaluación.");
            return false;
        }
        if (!curso.getEvaluaciones().contains(evaluacion)) {
            System.out.println("Esa evaluación no pertenece al curso " + curso.getCodigo() + ".");
            return false;
        }
        try {
            evaluacion.setNota(nota);
            return true;
        } catch (NotaInvalidaException | UnsupportedOperationException e) {
            System.out.println("No se pudo registrar la nota: " + e.getMessage());
            return false;
        }
    }

    // Registra la nota de un trabajo dentro de una Continua
    public boolean registrarNotaTrabajo(TrabajoContinua trabajo, double nota) {
        if (trabajo == null) {
            System.out.println("Debes elegir un trabajo.");
            return false;
        }
        try {
            trabajo.setNota(nota);
            return true;
        } catch (NotaInvalidaException e) {
            System.out.println("Nota inválida: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarTodo() {
        try {
            repositorio.guardar(new ArrayList<>(estudiantes.values()));
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
            return false;
        }
    }

    public boolean cargarTodo() {
        try {
            ArrayList<Estudiante> cargados = repositorio.cargar();
            estudiantes.clear();
            for (Estudiante e : cargados) {
                estudiantes.put(e.getCodigo(), e);
            }
            actual = cargados.isEmpty() ? null : cargados.get(cargados.size() - 1);
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo cargar: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String> getAdvertenciasCarga() {
        return repositorio.getAdvertencias();
    }

    public ArrayList<Estudiante> getEstudiantes() {
        return new ArrayList<>(estudiantes.values());
    }

    public Estudiante getActual() { return actual; }

    public void setActual(Estudiante estudiante) { this.actual = estudiante; }
}