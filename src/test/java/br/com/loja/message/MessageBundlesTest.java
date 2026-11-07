package br.com.loja.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class MessageBundlesTest {

    private static Properties load(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = MessageBundlesTest.class.getResourceAsStream("/" + name)) {
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return properties;
    }

    @Test
    void allBundlesDeclareTheSameKeys() throws IOException {
        Properties pt = load("messages_pt_BR.properties");
        Properties en = load("messages_en_US.properties");
        Properties defaults = load("messages.properties");

        assertFalse(pt.isEmpty());
        assertEquals(pt.keySet(), en.keySet());
        assertEquals(pt.keySet(), defaults.keySet());
    }
}
