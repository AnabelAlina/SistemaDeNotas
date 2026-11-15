package pruebas;

import java.util.ArrayList;
import calculos.EstadisticasNotas;
import calculos.PromedioPonderado;
import calculos.PromedioSimple;
import calculos.SimuladorNotas;
import evaluaciones.*;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import modelo.Curso;
import plan.PlanEstudios;
import plan.PlanEstudios.CursoPlan;

// Estudiante 2 - Ayelen Choquecota - Pruebas del modelo UNSA: Examen, Continua con trabajos, sustitutorio
// y catalogo de cursos. Ejecutar y revisar que todo diga OK.
public class PruebaUNSA {
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

    // Ponerle a una continua un unico trabajo de 100% con esa nota
    private static void continuaConNota(Continua c, double nota) throws Exception {
        TrabajoContinua t = new TrabajoContinua("Trabajo unico", 100, false);
        t.setNota(nota);
        c.agregarTrabajo(t);
    }

    private static CursoPlan buscar(String codigo) {
        for (CursoPlan p : PlanEstudios.getCursosPrimerAnioSegundoSemestre()) {
            if (p.getCodigo().equals(codigo)) return p;
        }
        throw new IllegalStateException("No esta el curso " + codigo);
    }

    private static Evaluacion ev(Curso c, String tipo, int numero) {
        for (Evaluacion e : c.getEvaluaciones()) {
            if (e instanceof Continua && tipo.equals("C") && ((Continua) e).getNumero() == numero) return e;
            if (e instanceof Examen && tipo.equals("E") && ((Examen) e).getNumero() == numero) return e;
        }
        throw new IllegalStateException("No esta " + tipo + numero);
    }

    public static void main(String[] args) throws Exception {
        // --- Catalogo: 7 cursos, 24 creditos, cada curso suma 100 ---
        ArrayList<CursoPlan> plan = PlanEstudios.getCursosPrimerAnioSegundoSemestre();
        verificar("hay 7 cursos", 7, plan.size());
        int creditos = 0;
        boolean todosSuman100 = true;
        for (CursoPlan p : plan) {
            creditos += p.getCreditos();
            double suma = 0;
            for (double x : p.getPesosExamen()) suma += x;
            for (double x : p.getPesosContinua()) suma += x;
            if (Math.abs(suma - 100) > 0.0001) { todosSuman100 = false; System.out.println("  suma mal en " + p); }
            Curso c = PlanEstudios.crearCurso(p, 15); // no debe lanzar excepcion
            if (c.getEvaluaciones().size() != 6) todosSuman100 = false;
        }
        verificar("suma de creditos del semestre", 24, creditos);
        verificar("cada curso: 6 evaluaciones y pesos suman 100", true, todosSuman100);
        verificar("DSOO tiene 4 creditos", 4, buscar("2501211").getCreditos());

        // --- Examen y Continua: validaciones ---
        try { new Examen(4, 10); verificar("Examen 4 debe fallar", true, false); }
        catch (IllegalArgumentException e) { verificar("Examen 4 lanza excepcion", true, true); }
        try { new Continua(0, 10); verificar("Continua 0 debe fallar", true, false); }
        catch (IllegalArgumentException e) { verificar("Continua 0 lanza excepcion", true, true); }
        verificar("tipo Examen", "Examen", new Examen(1, 10).getTipo());
        verificar("tipo Continua", "Continua", new Continua(1, 10).getTipo());
        verificar("tipo Sustitutorio", "Sustitutorio", new ExamenSustitutorio().getTipo());

        // --- Continua con trabajos 40% / 30% / 30% ---
        Continua c1 = new Continua(1, 20);
        verificar("continua vacia: no rendida", false, c1.isRendida());
        TrabajoContinua ta = new TrabajoContinua("Tarea 1", 40, false);
        TrabajoContinua tb = new TrabajoContinua("Tarea 2", 30, true);
        TrabajoContinua tc = new TrabajoContinua("Exposicion", 30, true);
        c1.agregarTrabajo(ta); c1.agregarTrabajo(tb); c1.agregarTrabajo(tc);
        verificar("peso interno suma 100", 100.0, c1.getPesoInterno());
        try { c1.agregarTrabajo(new TrabajoContinua("Extra", 5, false)); verificar("pasar de 100 debe fallar", true, false); }
        catch (PesoInvalidoException e) { verificar("trabajos mayores a 100% lanza PesoInvalidoException", true, true); }
        try { ta.setNota(21); verificar("nota 21 debe fallar", true, false); }
        catch (NotaInvalidaException e) { verificar("trabajo con nota 21 lanza NotaInvalidaException", true, true); }
        ta.setNota(10);
        verificar("con un trabajo rendido ya cuenta", true, c1.isRendida());
        verificar("nota parcial = solo lo rendido", 10.0, c1.getNota());
        verificar("todavia no esta completa", false, c1.isCompleta());
        tb.setNota(15);
        tc.setNota(20);
        // (10x40 + 15x30 + 20x30) / 100 = (400 + 450 + 600) / 100 = 14.5
        verificar("nota de la continua", 14.5, c1.getNota());
        verificar("continua completa", true, c1.isCompleta());
        // aporta al curso: 14.5 x 20 / 100 = 2.9
        verificar("getNotaPonderada de la continua", 2.9, c1.getNotaPonderada());
        try { c1.setNota(10); verificar("setNota en continua debe fallar", true, false); }
        catch (UnsupportedOperationException e) { verificar("setNota en continua lanza UnsupportedOperationException", true, true); }

        // --- Curso DSOO (meta 15): C1 10, E1 15, C2 10, E2 20, C3 25, E3 20 ---
        Curso dsoo = PlanEstudios.crearCurso(buscar("2501211"), 15);
        // C1: trabajos 50% nota 12 y 50% nota 16 -> 14
        Continua cc1 = (Continua) ev(dsoo, "C", 1);
        TrabajoContinua t1 = new TrabajoContinua("T1", 50, false); t1.setNota(12);
        TrabajoContinua t2 = new TrabajoContinua("T2", 50, false); t2.setNota(16);
        cc1.agregarTrabajo(t1); cc1.agregarTrabajo(t2);
        ev(dsoo, "E", 1).setNota(8);
        continuaConNota((Continua) ev(dsoo, "C", 2), 12);
        ev(dsoo, "E", 2).setNota(14);
        // suma = 14x10 + 8x15 + 12x10 + 14x20 = 140+120+120+280 = 660 ; peso rendido = 55 ; 660/55 = 12
        SimuladorNotas sim = new SimuladorNotas();
        verificar("promedio actual del curso", 12.0, dsoo.calcularPromedio());
        // (15x100 - 660) / (25+20) = 840/45
        verificar("nota necesaria meta 15", 840.0 / 45, sim.notaNecesaria(dsoo, 15));
        verificar("curso.notaNecesaria() usa la meta del curso", 840.0 / 45, dsoo.notaNecesaria());
        // (10.5x100 - 660) / 45 = 390/45
        verificar("nota necesaria para aprobar (10.5)", 390.0 / 45, sim.notaNecesaria(dsoo, 10.5));
        verificar("se puede aprobar", true, sim.esPosibleAprobar(dsoo));
        // simular que la proxima pendiente (Continua 3, peso 25) sale 20: (660 + 20x25)/(55+25) = 1160/80 = 14.5
        verificar("simular Continua 3 = 20", 14.5, sim.simularNota(dsoo, 20));
        verificar("la simulacion no cambia el curso", false, ev(dsoo, "C", 3).isRendida());

        // --- Sustitutorio: reemplaza la mas baja entre Examen 1 y 2 si la mejora ---
        ExamenSustitutorio susti = new ExamenSustitutorio();
        dsoo.getEvaluaciones().add(susti); // directo a la lista: Curso rechaza peso 0 (ver nota para Ani en ExamenSustitutorio)
        verificar("sustitutorio sin rendir no cambia el promedio", 12.0, dsoo.calcularPromedio());
        verificar("sustitutorio sin rendir no es pendiente (sigue habiendo C3 y E3)", true, sim.hayEvaluacionesPendientes(dsoo));
        susti.setNota(7); // menor que la mas baja (8): no la reemplaza
        verificar("sustitutorio 7 no mejora el 8: promedio igual", 12.0, dsoo.calcularPromedio());
        susti.setNota(13); // reemplaza a E1 (8, la mas baja de E1 y E2)
        // suma = 140 + 13x15 + 120 + 280 = 735 ; 735/55
        verificar("sustitutorio 13 reemplaza el Examen 1", 735.0 / 55, dsoo.calcularPromedio());
        // (1500 - 735) / 45 = 17
        verificar("nota necesaria considera el sustitutorio", 17.0, sim.notaNecesaria(dsoo, 15));
        verificar("promedio simple tambien lo considera", (14 + 13 + 12 + 14) / 4.0, new PromedioSimple().calcular(dsoo.getEvaluaciones()));

        // --- El sustitutorio NUNCA reemplaza el Examen 3 ---
        Curso d2 = PlanEstudios.crearCurso(buscar("2501211"), 15);
        continuaConNota((Continua) ev(d2, "C", 1), 10);
        continuaConNota((Continua) ev(d2, "C", 2), 10);
        continuaConNota((Continua) ev(d2, "C", 3), 10);
        ev(d2, "E", 1).setNota(15);
        ev(d2, "E", 2).setNota(16);
        ev(d2, "E", 3).setNota(5);
        ExamenSustitutorio s2 = new ExamenSustitutorio();
        d2.getEvaluaciones().add(s2);
        // sin susti: 100+225+100+320+250+100 = 1095 -> 10.95
        verificar("todo rendido sin susti", 10.95, d2.calcularPromedio());
        s2.setNota(18);
        // E1 (15, la mas baja de E1/E2) pasa a 18: 100+270+100+320+250+100 = 1140 -> 11.4  (si reemplazara E3 seria 13.55)
        verificar("susti 18 reemplaza E1 y no E3", 11.4, d2.calcularPromedio());
        verificar("curso aprobado (>= 10.5)", true, d2.estaAprobado());
        verificar("todo rendido: ya no hay pendientes aunque exista susti", false, sim.hayEvaluacionesPendientes(d2));
        // estadisticas con notas efectivas: C1 10, E1 18, C2 10, E2 16, C3 10, E3 5
        EstadisticasNotas est = new EstadisticasNotas();
        est.calcularFrecuencias(d2.getEvaluaciones());
        verificar("moda con notas efectivas", 10.0, est.getModa());
        verificar("frecuencia de la moda", 3.0, est.getFrecuenciaModa());
        verificar("maxima", 18.0, est.getMaxima());
        verificar("minima", 5.0, est.getMinima());

        // --- Curso manual con 3+3 porcentajes ---
        Curso manual = PlanEstudios.crearCurso("X1", "Curso repetido", 3, 14,
            new double[]{10, 10, 20}, new double[]{20, 20, 20});
        verificar("curso manual: 6 evaluaciones", 6, manual.getEvaluaciones().size());
        try {
            PlanEstudios.crearCurso("X2", "Mal", 3, 14, new double[]{10, 10, 10}, new double[]{10, 10, 10});
            verificar("pesos que no suman 100 deben fallar", true, false);
        } catch (PesoInvalidoException e) {
            verificar("pesos que no suman 100 lanzan PesoInvalidoException", true, true);
        }

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}
