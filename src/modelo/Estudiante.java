package modelo;

import java.util.ArrayList;

public class Estudiante extends Persona implements Calculable {
    private String codigo;
    private String carrera;
    private ArrayList<Ciclo> ciclos; // composición: los ciclos pertenecen al estudiante

    public Estudiante(String nombre, String apellido, String dni, String correo,
                    String codigo, String carrera) {
        super(nombre, apellido, dni, correo);
        this.codigo = validarTexto(codigo, "codigo");
        this.carrera = validarTexto(carrera, "carrera");
        this.ciclos = new ArrayList<>();
    }

    public String getCodigo() { return codigo; }
    public String getCarrera() { return carrera; }
    public ArrayList<Ciclo> getCiclos() { return ciclos; }

    public void agregarCiclo(Ciclo ciclo) {
        if (ciclo == null) {
            throw new IllegalArgumentException("El ciclo no puede ser nulo.");
        }
        ciclos.add(ciclo);
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