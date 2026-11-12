package pruebas;

import java.time.LocalDate;
import calculos.EstadisticasNotas;
import calculos.PromedioSimple;
import calculos.SimuladorNotas;
import evaluaciones.*;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import modelo.Curso;

// Estudiante 2 - Ayelen Choquecota - Pruebas manuales de evaluaciones, promedios, estadisticas y simulador.
// Se puede borrar o dejar al final del proyecto.
// Pruebas manuales del Estudiante 2. Ejecutar y revisar que todo diga OK.
public class PruebaCalculos {
    private static int fallos = 0;

    private static void verificar(String descripcion, double esperado, double obtenido) {
        boolean ok = Math.abs(esperado - obtenido) < 0.0001;
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        boolean ok = esperado.equals(obtenido);
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    public static void main(String[] args) throws Exception {
        LocalDate hoy = LocalDate.of(2026, 11, 12);

        // --- Evaluacion: validacion de notas ---
        Practica p1 = new Practica("Practica 1", 20, hoy, 1);
        try { p1.setNota(21); verificar("nota 21 debe lanzar excepcion", true, false); }
        catch (NotaInvalidaException e) { verificar("nota 21 lanza NotaInvalidaException", true, true); }
        try { p1.setNota(-1); verificar("nota -1 debe lanzar excepcion", true, false); }
        catch (NotaInvalidaException e) { verificar("nota -1 lanza NotaInvalidaException", true, true); }
        verificar("antes de poner nota no esta rendida", false, p1.isRendida());
        p1.setNota(14);
        verificar("con nota valida queda rendida", true, p1.isRendida());
        verificar("getNotaPonderada = 14 x 20 / 100", 2.8, p1.getNotaPonderada());

        // --- Polimorfismo: getTipo ---
        Evaluacion[] tipos = {
            new Practica("P", 10, hoy, 1), new Parcial("Par", 10, hoy, true),
            new ExamenFinal("Fin", 10, hoy, false), new Trabajo("Tra", 10, hoy, true) };
        String esperados = "Practica,Parcial,Examen Final,Trabajo";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tipos.length; i++) sb.append(i > 0 ? "," : "").append(tipos[i].getTipo());
        verificar("getTipo de las 4 clases", esperados, sb.toString());

        // --- Curso: practicas 30%, parcial 30%, final 40% ---
        Curso curso = new Curso("CS101", "Programacion", 4, 15);
        Practica pr = new Practica("Practica", 30, hoy, 1);
        Parcial pa = new Parcial("Parcial", 30, hoy, false);
        ExamenFinal fi = new ExamenFinal("Final", 40, hoy, false);
        curso.agregarEvaluacion(pr);
        curso.agregarEvaluacion(pa);
        curso.agregarEvaluacion(fi);

        verificar("sin notas: promedio 0", 0.0, curso.calcularPromedio());
        pr.setNota(12);
        pa.setNota(16);
        // ponderado sobre lo rendido: (12x30 + 16x30) / 60 = 14
        verificar("promedio ponderado de lo rendido", 14.0, curso.calcularPromedio());
        verificar("promedio simple", 14.0, new PromedioSimple().calcular(curso.getEvaluaciones()));
        pr.setNota(10);
        // (10x30 + 16x30) / 60 = 13 ; simple = 13 tambien, probamos pesos distintos abajo
        verificar("promedio tras cambiar nota", 13.0, curso.calcularPromedio());

        // --- Nota necesaria (meta 15): (15x100 - (10x30+16x30)) / 40 = (1500-780)/40 = 18 ---
        SimuladorNotas sim = new SimuladorNotas();
        verificar("nota necesaria para meta 15", 18.0, sim.notaNecesaria(curso, 15));
        verificar("curso.notaNecesaria() usa metaPromedio", 18.0, curso.notaNecesaria());
        // meta 10.5: (1050-780)/40 = 6.75
        verificar("nota necesaria para aprobar (10.5)", 6.75, sim.notaNecesaria(curso, 10.5));
        verificar("es posible aprobar", true, sim.esPosibleAprobar(curso));
        // meta 19: (1900-780)/40 = 28 -> imposible
        verificar("meta 19 pide mas de 20", 28.0, sim.notaNecesaria(curso, 19));
        // meta baja: ya la tienes -> 0
        verificar("meta ya alcanzada devuelve 0", 0.0, sim.notaNecesaria(curso, 5));

        // --- Simulador ---
        // Si el final sale 20: (10x30+16x30+20x40)/100 = (300+480+800)/100 = 15.8
        verificar("simular final = 20", 15.8, sim.simularNota(curso, 20));
        verificar("la simulacion no modifica el curso", false, fi.isRendida());
        try { sim.simularNota(curso, 25); verificar("simular 25 debe fallar", true, false); }
        catch (NotaInvalidaException e) { verificar("simular 25 lanza excepcion", true, true); }

        // --- Todo rendido: no queda peso restante (evitar dividir entre 0) ---
        fi.setNota(8);
        verificar("sin pendientes: nota necesaria 0", 0.0, sim.notaNecesaria(curso, 15));
        verificar("sin pendientes: hayEvaluacionesPendientes", false, sim.hayEvaluacionesPendientes(curso));
        // (10x30 + 16x30 + 8x40)/100 = (300+480+320)/100 = 11 -> aprobado
        verificar("promedio final", 11.0, curso.calcularPromedio());
        verificar("sin pendientes: esPosibleAprobar = estaAprobado", true, sim.esPosibleAprobar(curso));

        // --- Imposible aprobar ---
        Curso malo = new Curso("CS102", "Calculo", 3, 11);
        Parcial m1 = new Parcial("P1", 50, hoy, false);
        Parcial m2 = new Parcial("P2", 50, hoy, false);
        malo.agregarEvaluacion(m1);
        malo.agregarEvaluacion(m2);
        m1.setNota(0);
        // (10.5x100 - 0) / 50 = 21 -> imposible
        verificar("necesita 21 en lo que falta", 21.0, sim.notaNecesaria(malo, 10.5));
        verificar("no es posible aprobar", false, sim.esPosibleAprobar(malo));

        // --- Estadisticas ---
        {
            java.util.ArrayList<Evaluacion> lista = new java.util.ArrayList<>();
            double[] notas = {12, 14, 14, 16, 10};
            for (int i = 0; i < notas.length; i++) {
                Evaluacion e = new Practica("P" + (i + 1), 10, hoy, i + 1);
                e.setNota(notas[i]);
                lista.add(e);
            }
            lista.add(new Trabajo("Pendiente", 10, hoy, false)); // no rendida: no cuenta
            EstadisticasNotas est = new EstadisticasNotas();
            est.calcularFrecuencias(lista);
            verificar("moda", 14.0, est.getModa());
            verificar("frecuencia de la moda", 2.0, est.getFrecuenciaModa());
            verificar("mediana (impar)", 14.0, est.getMediana());
            verificar("maxima", 16.0, est.getMaxima());
            verificar("minima", 10.0, est.getMinima());
            verificar("promedio", 13.2, est.getPromedio());
            // varianza = ((1.2^2)+(0.8^2)+(0.8^2)+(2.8^2)+(3.2^2))/5 = (1.44+0.64+0.64+7.84+10.24)/5 = 4.16
            verificar("desviacion poblacional", Math.sqrt(4.16), est.getDesviacion());
            // ultima = 10, anteriores = (12+14+14+16)/4 = 14 -> baja
            verificar("tendencia", "baja", est.getTendencia());

            // Mediana par
            lista.remove(lista.size() - 1);
            lista.remove(0); // quedan 14,14,16,10
            est.calcularFrecuencias(lista);
            verificar("mediana (par): ordenadas 10,14,14,16", 14.0, est.getMediana());

            // Empate de moda: gana la menor
            java.util.ArrayList<Evaluacion> empate = new java.util.ArrayList<>();
            double[] ne = {18, 11, 18, 11};
            for (int i = 0; i < ne.length; i++) {
                Evaluacion e = new Practica("E" + i, 10, hoy, i + 1);
                e.setNota(ne[i]);
                empate.add(e);
            }
            est.calcularFrecuencias(empate);
            verificar("moda con empate devuelve la menor", 11.0, est.getModa());

            // Sin datos y una sola nota
            est.calcularFrecuencias(new java.util.ArrayList<>());
            verificar("sin datos: hayDatos", false, est.hayDatos());
            verificar("sin datos: moda 0", 0.0, est.getModa());
            verificar("sin datos: tendencia", "sin datos suficientes", est.getTendencia());
            java.util.ArrayList<Evaluacion> una = new java.util.ArrayList<>();
            Evaluacion u = new Practica("U", 10, hoy, 1);
            u.setNota(15);
            una.add(u);
            est.calcularFrecuencias(una);
            verificar("una nota: desviacion 0", 0.0, est.getDesviacion());
            verificar("una nota: tendencia", "sin datos suficientes", est.getTendencia());
        }

        // --- Pesos: lo valida Curso (Estudiante 1) ---
        try {
            curso.agregarEvaluacion(new Trabajo("Extra", 10, hoy, false));
            verificar("pesos > 100 debe fallar", true, false);
        } catch (PesoInvalidoException e) {
            verificar("pesos > 100 lanza PesoInvalidoException", true, true);
        }

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}
