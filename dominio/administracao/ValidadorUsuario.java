package dominio.administracao;

import excecoes.ExcecaoRegraNegocio;
import java.util.regex.Pattern;

public final class ValidadorUsuario {
    private static final Pattern EMAIL =
        Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private ValidadorUsuario() {}

    public static void validarEmail(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new ExcecaoRegraNegocio("E-mail em formato inválido.");
        }
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new ExcecaoRegraNegocio("A senha deve ter pelo menos 8 caracteres.");
        }
        boolean temLetra  = senha.chars().anyMatch(Character::isLetter);
        boolean temNumero = senha.chars().anyMatch(Character::isDigit);
        if (!temLetra || !temNumero) {
            throw new ExcecaoRegraNegocio("A senha deve conter letras e números.");
        }
    }
}