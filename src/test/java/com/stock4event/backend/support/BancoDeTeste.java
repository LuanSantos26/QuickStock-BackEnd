package com.stock4event.backend.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Garante que o banco exclusivo de testes existe antes do Spring subir.
 * Usa as mesmas variáveis do application-test.properties.
 */
public final class BancoDeTeste {

    private static boolean pronto = false;

    private BancoDeTeste() {
    }

    public static synchronized void garantir() {
        if (pronto) return;
        String host = env("TEST_DB_HOST", "localhost");
        String porta = env("TEST_DB_PORT", "5432");
        String nome = env("TEST_DB_NAME", "stock4event_test");
        if (!nome.endsWith("_test")) {
            throw new IllegalStateException("Banco de teste precisa terminar em _test (recebido: " + nome + ").");
        }
        String url = "jdbc:postgresql://" + host + ":" + porta + "/postgres";
        try (Connection c = DriverManager.getConnection(url, env("TEST_DB_USER", "postgres"), env("TEST_DB_PASSWORD", "admin"));
             Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + nome + "'")) {
                if (!rs.next()) {
                    st.execute("CREATE DATABASE \"" + nome + "\" ENCODING 'UTF8'");
                }
            }
            pronto = true;
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível preparar o banco de testes " + nome
                    + ". Verifique se o PostgreSQL está ligado e as variáveis TEST_DB_*.", e);
        }
    }

    private static String env(String chave, String padrao) {
        String v = System.getenv(chave);
        return v == null || v.isBlank() ? padrao : v;
    }
}
