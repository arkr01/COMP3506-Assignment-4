import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ContactTracerTest {
    
    @Test
    public void testSpecExample() {
        ContactTracer tracer = new ContactTracer();
        // we start at 100 here, but really its arbitrary.. only the relative time differences matter.
        tracer.addTrace(new Trace("Anna", "Sanni", 100));
        tracer.addTrace(new Trace("Anna", "Matt", 1740));
        tracer.addTrace(new Trace("Matt", "Kristian", 3240));
        tracer.addTrace(new Trace("Kristian", "Sanni", 3270));
        tracer.addTrace(new Trace("Kristian", "Kenton", 3360));
        tracer.addTrace(new Trace("Kristian", "Max", 3360));
        tracer.addTrace(new Trace("Kenton", "Kristian", 4020));

        assertEquals(Set.of("Matt", "Kristian", "Kenton", "Max"), 
                tracer.contactTrace("Anna", 130));
    }

    @Test
    public void testSpecExampleWithConstructor() {
        ArrayList<Trace> traces = new ArrayList<>();
        traces.add(new Trace("Anna", "Sanni", 100));
        // we start at 100 here, but really its arbitrary.. only the relative time differences matter.
        traces.add(new Trace("Anna", "Sanni", 100));
        traces.add(new Trace("Anna", "Matt", 1740));
        traces.add(new Trace("Matt", "Kristian", 3240));
        traces.add(new Trace("Kristian", "Sanni", 3270));
        traces.add(new Trace("Kristian", "Kenton", 3360));
        traces.add(new Trace("Kristian", "Max", 3360));
        traces.add(new Trace("Kenton", "Kristian", 4020));
        ContactTracer tracer = new ContactTracer(traces);

        assertEquals(Set.of("Matt", "Kristian", "Kenton", "Max"),
                tracer.contactTrace("Anna", 130));
    }

    @Test
    public void testMyExample() {
        ContactTracer tracer = new ContactTracer();
        // we start at 100 here, but really its arbitrary.. only the relative time differences matter.
        tracer.addTrace(new Trace("Anna", "Fred", 105));

        assertEquals(Set.of(),
                tracer.contactTrace("Anna", 130));
    }

    @Test
    public void testGetContacts() {
        ContactTracer tracer = new ContactTracer();
        // we start at 100 here, but really its arbitrary.. only the relative time differences matter.
        tracer.addTrace(new Trace("Anna", "Sanni", 100));
        tracer.addTrace(new Trace("Anna", "Matt", 1740));
        tracer.addTrace(new Trace("Matt", "Kristian", 3240));
        tracer.addTrace(new Trace("Kristian", "Sanni", 3270));
        tracer.addTrace(new Trace("Kristian", "Kenton", 3360));
        tracer.addTrace(new Trace("Kristian", "Max", 3360));
        tracer.addTrace(new Trace("Kenton", "Kristian", 4020));

        assertEquals(Set.of("Sanni", "Matt"), tracer.getContacts("Anna"));
        assertEquals(Set.of("Anna", "Kristian"), tracer.getContacts("Matt"));
        assertEquals(Set.of("Matt", "Sanni", "Kenton", "Max"), tracer.getContacts("Kristian"));
        assertEquals(Set.of("Anna", "Kristian"), tracer.getContacts("Sanni"));
        assertEquals(Set.of("Kristian"), tracer.getContacts("Max"));
        assertEquals(Set.of("Kristian"), tracer.getContacts("Kenton"));
    }
    
    @Test
    public void testGetContactTimes() {
        ContactTracer tracer = new ContactTracer();
        // we start at 100 here, but really its arbitrary.. only the relative time differences matter.
        tracer.addTrace(new Trace("Anna", "Sanni", 100));
        tracer.addTrace(new Trace("Anna", "Matt", 1740));
        tracer.addTrace(new Trace("Matt", "Kristian", 3240));
        tracer.addTrace(new Trace("Kristian", "Sanni", 3270));
        tracer.addTrace(new Trace("Kristian", "Kenton", 3360));
        tracer.addTrace(new Trace("Kristian", "Max", 3360));
        tracer.addTrace(new Trace("Kenton", "Kristian", 4020));

        assertEquals(List.of(3360, 4020), tracer.getContactTimes("Kristian", "Kenton"));
        assertEquals(List.of(3360, 4020), tracer.getContactTimes("Kenton", "Kristian"));
        
        assertEquals(List.of(), tracer.getContactTimes("Anna", "Kristian"));
        
        assertEquals(List.of(3360), tracer.getContactTimes("Kristian", "Max"));
        assertEquals(List.of(3360), tracer.getContactTimes("Max", "Kristian"));
    }
    
    @Test 
    public void testGetContactsAfter() {
        ContactTracer tracer = new ContactTracer();
        // we start at 100 here, but really its arbitrary.. only the relative time differences matter.
        tracer.addTrace(new Trace("Anna", "Sanni", 100));
        tracer.addTrace(new Trace("Anna", "Matt", 1740));
        tracer.addTrace(new Trace("Matt", "Kristian", 3240));
        tracer.addTrace(new Trace("Kristian", "Sanni", 3270));
        tracer.addTrace(new Trace("Kristian", "Kenton", 3360));
        tracer.addTrace(new Trace("Kristian", "Max", 3360));
        tracer.addTrace(new Trace("Kenton", "Kristian", 4020));
        
        assertEquals(Set.of("Sanni", "Matt"), tracer.getContactsAfter("Anna", 0));
        // note below: inclusive of the timestamp
        assertEquals(Set.of("Sanni", "Matt"), tracer.getContactsAfter("Anna", 100));
        assertEquals(Set.of("Matt"), tracer.getContactsAfter("Anna", 101));
        assertEquals(Set.of(), tracer.getContactsAfter("Anna", 1741));
    }
}