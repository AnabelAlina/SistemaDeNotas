package modelo;

import java.util.ArrayList;

public class Estudiante extends Persona implements Calculable {
    private String codigo; // CUI: 8 cifras
    private String carrera;
    private ArrayList<Ciclo> ciclos; // composición: los ciclos pertenecen al estudiante

    public Estudiante(String nombre, String apellido, String dni, String correo,
                    String codigo, String carrera) {
        super(nombre, apellido, dni, correo);
        this.codigo = validarCui(codigo);
        this.carrera = validarTexto(carrera, "carrera");
        this.ciclos = new ArrayList<>();
    }

    public String getCodigo() { return codigo; }
    public String getCarrera() { return carrera; }
    public ArrayList<Ciclo> getCiclos() { return ciclos; }
    // CUI: exactamente 8 cifras
    public static String validarCui(String cui) {
        String limpio = validarTexto(cui, "CUI");
        if (!limpio.matches("\\d{8}")) {
            throw new IllegalArgumentException("El CUI debe tener exactamente 8 cifras.");
        }
        return limpio;
    }
    // Correo institucional: usuario@unsa.edu.pe
    public static String validarCorreoInstitucional(String correo) {
        String limpio = Persona.validarCorreo(correo);
        if (!limpio.matches("(?i)[^@\\s]+@unsa\\.edu\\.pe")) {
            throw new IllegalArgumentException("El correo debe terminar en @unsa.edu.pe.");
        }
        return limpio;
    }
    // Sobrescribe el de Persona: el estudiante además exige correo @unsa.edu.pe
    @Override
    public void setCorreo(String correo) {
        super.setCorreo(validarCorreoInstitucional(correo));
    }

    public void agregarCiclo(Ciclo ciclo) {
        if (ciclo == null) {
            throw new IllegalArgumentException("El ciclo no puede ser nulo.");
        }
        if (buscarCiclo(ciclo.getNombre()) != null) {
            throw new IllegalArgumentException("Ya existe el ciclo " + ciclo.getNombre() + ".");
        }
        ciclos.add(ciclo);
    }

    // Quita el ciclo con ese nombre. Devuelve true si lo quitó, false si no existía
    public boolean quitarCiclo(String nombre) {
        Ciclo ciclo = buscarCiclo(nombre);
        if (ciclo == null) {
            return false;
        }
        return ciclos.remove(ciclo);
    }

    // Devuelve el ciclo con ese nombre o null si no existe
    public Ciclo buscarCiclo(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (Ciclo c : ciclos) {
            if (c.getNombre().equalsIgnoreCase(nombre.trim())) {
                return c;
            }
        }
        return null;
    }

    // El ciclo actual es el último que se agregó
    public Ciclo getCicloActual() {
        if (ciclos.isEmpty()) {
            return null;
        }
        return ciclos.get(ciclos.size() - 1);
    }

    // Promedio acumulado: suma(promedio curso x créditos) / suma(créditos)
    @Override
    public double calcularPromedio() {
        double suma = 0;
        double creditos = 0;
        for (Ciclo ciclo : ciclos) {
            for (Curso curso : ciclo.getCursos()) {
                if (curso.tieneNotas()) { // los cursos sin notas no cuentan
                    suma += curso.calcularPromedio() * curso.getCreditos();
                    creditos += curso.getCreditos();
                }
            }
        }
        return creditos == 0 ? 0 : suma / creditos;
    }

    @Override
    public String getRol() {
        return "Estudiante";
    }
}