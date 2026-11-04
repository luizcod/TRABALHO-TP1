package dominio.analiseEstatistica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regra de arredondamento do sistema: percentuais com 1 casa decimal,
 * usando o método do maior resto para que a soma seja sempre exatamente 100,0%.
 */
public final class CalculadoraPercentual {

    private CalculadoraPercentual() {
    }

    /** Distribui 100,0% entre as contagens (base = soma das contagens). */
    public static Map<String, Double> distribuir(Map<String, Integer> contagens) {
        Map<String, Double> saida = new LinkedHashMap<>();
        long total = 0;
        for (int v : contagens.values()) {
            total += v;
        }
        if (total == 0) {
            contagens.keySet().forEach(k -> saida.put(k, 0.0));
            return saida;
        }
        Map<String, Long> base = new LinkedHashMap<>();
        Map<String, Long> resto = new HashMap<>();
        long usado = 0;
        for (Map.Entry<String, Integer> e : contagens.entrySet()) {
            long escalado = e.getValue() * 1000L; // décimos de ponto percentual
            long piso = escalado / total;
            base.put(e.getKey(), piso);
            resto.put(e.getKey(), escalado % total);
            usado += piso;
        }
        long faltam = 1000 - usado;
        List<String> ordem = new ArrayList<>(contagens.keySet());
        ordem.sort(Comparator.comparing((String k) -> resto.get(k)).reversed()
                .thenComparing(Comparator.naturalOrder()));
        for (int i = 0; i < faltam; i++) {
            base.merge(ordem.get(i), 1L, Long::sum);
        }
        base.forEach((k, v) -> saida.put(k, v / 10.0));
        return saida;
    }

    /** Percentual simples (HALF_UP, 1 casa) de parte sobre total. */
    public static double percentual(int parte, int total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(parte * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
