package code.groovy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GroovyTest {

    @Test
    public void testGroovy() {
        GroovyClassLoader gcl = new GroovyClassLoader();
        GroovyShell groovyShell = new GroovyShell(gcl);

        Object result = groovyShell.evaluate("3+5");
        assertEquals(3+5, (Integer)result);
    }

    @Test
    public void testGrape() {
        GroovyClassLoader gcl = new GroovyClassLoader();
        GroovyShell groovyShell = new GroovyShell(gcl);
        StringBuilder sb = new StringBuilder()
                //.append("@Grab(group='commons-codec', module='commons-codec', version='1.14')\n")
                .append("@Grab('commons-codec:commons-codec:1.14')\n")
                .append("import org.apache.commons.codec.language.bm.Lang\n")
                .append("import org.apache.commons.codec.language.bm.NameType\n")
                .append("lang = Lang.instance(NameType.GENERIC)\n")
                .append("lang.guessLanguage('båtflykting')\n");

        //System.out.println(sb);
        Object result = groovyShell.evaluate(sb.toString());
        //System.out.println(result);
        assertEquals("any",result);
    }

    @Test
    public void testGetProperty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        GroovyClassLoader gcl = new GroovyClassLoader();
        GroovyShell groovyShell = new GroovyShell(gcl);
        StringBuilder sb = new StringBuilder()
                //.append("@Grab(group='commons-codec', module='commons-codec', version='1.14')\n")
                .append("@Grab(group='org.jdom', module='jdom2', version='2.0.6')\n")
                .append("import org.jdom2.Document\n")
                .append("import org.jdom2.Element\n")
                .append("root = new Element('theRootElement')\n")
                .append("print('root element name is ' + root.getName() + System.getProperty(\"line.separator\"))");

        //System.out.println("--- script:");
        //System.out.println(sb);
        //System.out.println("---");
        Object result = groovyShell.evaluate(sb.toString());
        //System.out.println("\nResult is " + result);
        Object o = groovyShell.getProperty("root");
        Method m = o.getClass().getMethod("getName");
        assertEquals("theRootElement", m.invoke(o));
    }
}
