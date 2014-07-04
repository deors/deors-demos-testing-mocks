package deors.demos.testing.mocks.servletmocks;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchedulerServletTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public SchedulerServletTestCase() {

        super();
    }

    @Test
    public void testServletCommandNull()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn(null);

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doPost(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_NOT_RUNNING</b><br/>"));
            assertTrue(s.contains("<form id=\"commandForm\" name=\"commandForm\" method=\"post\" action=\"[ACTION]\">"));
            assertTrue(s.contains("<input type=\"button\" name=\"start\" value=\"start\""));
            assertTrue(s.contains("<b>Configuration parameters</b>"));
            assertFalse(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandEmpty()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doGet(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_NOT_RUNNING</b><br/>"));
            assertTrue(s.contains("<form id=\"commandForm\" name=\"commandForm\" method=\"post\" action=\"[ACTION]\">"));
            assertTrue(s.contains("<input type=\"button\" name=\"start\" value=\"start\""));
            assertTrue(s.contains("<b>Configuration parameters</b>"));
            assertFalse(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandHelp()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("help");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doGet(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_NOT_RUNNING</b><br/>"));
            assertTrue(s.contains("<form id=\"commandForm\" name=\"commandForm\" method=\"post\" action=\"[ACTION]\">"));
            assertTrue(s.contains("<input type=\"button\" name=\"start\" value=\"start\""));
            assertTrue(s.contains("<b>Configuration parameters</b>"));
            assertFalse(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStartEmpty()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("start");
        expect(request.getParameter("iniFileName")).andReturn("");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doGet(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_STARTED</b><br/>"));

            assertFalse(ss.existsTask("task"));
            assertFalse(ss.existsTask("daemon"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStartWithFile()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("start");
        expect(request.getParameter("iniFileName")).andReturn("target/test-classes/deors/demos/testing/mocks/servletmocks/scheduler.ini");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doGet(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_STARTED</b><br/>"));

            assertTrue(ss.existsTask("task"));
            assertTrue(ss.existsTask("daemon"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStartMissingFile()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        thrown.expect(ServletException.class);
        thrown.expectMessage("SCHED_LOG_EXCEPTION_INI_FILE_MISSING");

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("start");
        expect(request.getParameter("iniFileName")).andReturn("target/test-classes/missing.ini");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doGet(request, response);
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStartAgain()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("start");
        expect(request.getParameter("iniFileName")).andReturn("target/test-classes/deors/demos/testing/mocks/servletmocks/scheduler.ini");
        expect(request.getParameter("command")).andReturn("stop");
        expect(request.getParameter("command")).andReturn("start");
        expect(request.getParameter("iniFileName")).andReturn("");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp1 = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp1));
        File temp2 = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp2));
        File temp3 = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp3));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            // stars scheduler with ini file including tasks
            ss.doGet(request, response);

            // stops scheduler
            ss.doGet(request, response);

            // starts again the scheduler without ini file - the tasks are maintained from previous execution
            ss.doGet(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp3);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_STARTED</b><br/>"));

            assertTrue(ss.existsTask("task"));
            assertTrue(ss.existsTask("daemon"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp1.delete();
            temp2.delete();
            temp3.delete();
        }
    }

    @Test
    public void testServletCommandStartAlreadyInit()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("start");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_ALREADY_STARTED</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStopNotInit()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("stop");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.doGet(request, response);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_NOT_RUNNING</b><br/>"));

            assertFalse(ss.existsTask("task"));
            assertFalse(ss.existsTask("daemon"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStopIfInit()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("stop");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_STOPPED</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStopTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("stop");
        expect(request.getParameter("taskName")).andReturn("task");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.scheduleTask("task", MyTask.class, "description", null, null);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_TASK_STOPPEDtask</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandStopMissingTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("stop");
        expect(request.getParameter("taskName")).andReturn("task1");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("SCHED_SERVLET_LOG_TASK_NOT_EXISTtask1<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandRemoveNoTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("remove");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_NAME<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandRemoveTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("remove");
        expect(request.getParameter("taskName")).andReturn("task1");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);

            Calendar c1 = Calendar.getInstance();
            c1.add(Calendar.HOUR_OF_DAY, 1);
            Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.HOUR_OF_DAY, 2);
            ss.scheduleTask("task1", MyTask.class, "description", c1, c2);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_TASK_REMOVEDtask1</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandRemoveMissingTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("remove");
        expect(request.getParameter("taskName")).andReturn("task1");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_LOG_TASK_NOT_EXISTtask1<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandKillNoTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("kill");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_NAME<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandKillTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("kill");
        expect(request.getParameter("taskName")).andReturn("task1");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.scheduleTask("task1", MyTask.class, "description", null, null);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_TASK_KILLEDtask1</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandKillMissingTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("kill");
        expect(request.getParameter("taskName")).andReturn("task1");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_LOG_TASK_NOT_EXISTtask1<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandAddNoData()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("add");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_NAME<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_CLASS<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_DESCRIPTION<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_START<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_STOP<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandAddBadDates()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("add");
        expect(request.getParameter("taskStartTime")).andReturn("bad");
        expect(request.getParameter("taskStopTime")).andReturn("bad");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_NAME<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_CLASS<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_DESCRIPTION<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_INVALID_TASK_START<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_INVALID_TASK_STOP<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandAddBadClass()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("add");
        expect(request.getParameter("taskName")).andReturn("task");
        expect(request.getParameter("taskClassName")).andReturn("bad");
        expect(request.getParameter("taskDescription")).andReturn("description");
        expect(request.getParameter("taskStartTime")).andReturn("*");
        expect(request.getParameter("taskStopTime")).andReturn("*");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_ERR_TASK_NOT_FOUND<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandAddOk()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("add");
        expect(request.getParameter("taskName")).andReturn("task1");
        expect(request.getParameter("taskClassName")).andReturn("deors.demos.testing.mocks.servletmocks.SchedulerServletTestCase$MyTask");
        expect(request.getParameter("taskDescription")).andReturn("description");
        expect(request.getParameter("taskStartTime")).andReturn("*");
        expect(request.getParameter("taskStopTime")).andReturn("*");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_TASK_SCHEDULEDtask1</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandScheduleNoData()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("schedule");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_NAME<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_START<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_STOP<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandScheduleBadDates()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("schedule");
        expect(request.getParameter("taskStartTime")).andReturn("bad");
        expect(request.getParameter("taskStopTime")).andReturn("bad");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_NO_TASK_NAME<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_INVALID_TASK_START<br/>"));
            assertTrue(s.contains("SCHED_SERVLET_ERR_INVALID_TASK_STOP<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandScheduleMissingTask()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("schedule");
        expect(request.getParameter("taskName")).andReturn("task1");
        expect(request.getParameter("taskStartTime")).andReturn("*");
        expect(request.getParameter("taskStopTime")).andReturn("*");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("SCHED_SERVLET_LOG_TASK_NOT_EXISTtask1<br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletCommandScheduleOk()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("command")).andReturn("schedule");
        expect(request.getParameter("taskName")).andReturn("task1");
        expect(request.getParameter("taskStartTime")).andReturn("*");
        expect(request.getParameter("taskStopTime")).andReturn("*");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        ServletConfig config = createNiceMock(ServletConfig.class);
        expect(config.getInitParameter("iniFileName")).andReturn("");

        replay(request);
        replay(response);
        replay(config);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            ss.init(config);
            ss.scheduleTask("task1", MyTask.class, "description", null, null);
            ss.doGet(request, response);

            verify(request);
            verify(response);
            verify(config);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_TASK_SCHEDULEDtask1</b><br/>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletResponseNotInitializedNoMessages()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException {

        Method mCreate = SchedulerServlet.class.getDeclaredMethod("createServletResponse", HttpServletRequest.class, HttpServletResponse.class, List.class, List.class, boolean.class);
        mCreate.setAccessible(true);

        Field fInit = SchedulerServlet.class.getDeclaredField("initialized");
        fInit.setAccessible(true);

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getRequestURI()).andReturn("/testURI");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        List<String> messages = new ArrayList<String>();
        List<String> errors = new ArrayList<String>();

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            fInit.setBoolean(ss, false);
            mCreate.invoke(ss, request, response, messages, errors, true);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_NOT_RUNNING</b><br/>"));
            assertTrue(s.contains("<form id=\"commandForm\" name=\"commandForm\" method=\"post\" action=\"/testURI\">"));
            assertTrue(s.contains("<input type=\"button\" name=\"start\" value=\"start\""));
            assertTrue(s.contains("<b>Configuration parameters</b>"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletResponseNotInitializedWithMessages()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

        Method mCreate = SchedulerServlet.class.getDeclaredMethod("createServletResponse", HttpServletRequest.class, HttpServletResponse.class, List.class, List.class, boolean.class);
        mCreate.setAccessible(true);

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getRequestURI()).andReturn("/testURI");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        List<String> messages = new ArrayList<String>();
        messages.add("message test 1");
        messages.add("message test 2");

        List<String> errors = new ArrayList<String>();
        errors.add("error test 1");
        errors.add("error test 2");

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            mCreate.invoke(ss, request, response, messages, errors, true);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("<title>Scheduler Command Center</title>"));
            assertTrue(s.contains("<b>SCHED_SERVLET_LOG_NOT_RUNNING</b><br/>"));
            assertTrue(s.contains("<form id=\"commandForm\" name=\"commandForm\" method=\"post\" action=\"/testURI\">"));
            assertTrue(s.contains("<input type=\"button\" name=\"start\" value=\"start\""));
            assertTrue(s.contains("<b>Configuration parameters</b>"));
            assertTrue(s.contains("<b>message test 1</b><br/>"));
            assertTrue(s.contains("<b>message test 2</b><br/>"));
            assertTrue(s.contains("<b>Error(s) with configuration parameters</b><br/>"));
            assertTrue(s.contains("error test 1<br/>"));
            assertTrue(s.contains("error test 2<br/>"));
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletResponseInitialized()
        throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, IOException {

        Method mCreate = SchedulerServlet.class.getDeclaredMethod("createServletResponse", HttpServletRequest.class, HttpServletResponse.class, List.class, List.class, boolean.class);
        mCreate.setAccessible(true);

        Field fInit = SchedulerServlet.class.getDeclaredField("initialized");
        fInit.setAccessible(true);

        Field fSch = SchedulerServlet.class.getDeclaredField("sch");
        fSch.setAccessible(true);

        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getRequestURI()).andReturn("/testURI");

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        File temp = File.createTempFile("deors.core.commons.", ".test");
        expect(response.getWriter()).andReturn(new PrintWriter(temp));

        List<String> messages = new ArrayList<String>();
        List<String> errors = new ArrayList<String>();

        Scheduler sch = new Scheduler();
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.HOUR_OF_DAY, 1);
        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.HOUR_OF_DAY, 2);
        sch.scheduleTask("task1", MyTask.class, "description1", null, null);
        sch.scheduleTask("task2", MyTask.class, "description2", c1, c2);

        replay(request);
        replay(response);

        try {
            SchedulerServlet ss = new SchedulerServlet();

            // the scheduler servlet is marked as initialized and the scheduler is fed
            fInit.setBoolean(ss, true);
            fSch.set(ss, sch);
            mCreate.invoke(ss, request, response, messages, errors, true);

            verify(request);
            verify(response);

            byte[] output = IOToolkit.readFile(temp);
            String s = new String(output);

            assertTrue(s.contains("Task <b>task1</b>SCHED_SERVLET_STATE_IDLE"));
            assertTrue(s.contains("onclick=\"taskStart('task1')\"/>&nbsp;&nbsp;"));
            assertTrue(s.contains("Task <b>task2</b>SCHED_SERVLET_STATE_IDLE"));
            assertTrue(s.contains("onclick=\"taskStart('task2')\"/>&nbsp;&nbsp;"));

            ss.stopAllTasks();
            ss.resetScheduler();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testServletInitWithFile()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        ServletConfig sc = createNiceMock(ServletConfig.class);
        expect(sc.getInitParameter("iniFileName")).andReturn("target/test-classes/deors/demos/testing/mocks/servletmocks/scheduler.ini");

        replay(sc);

        SchedulerServlet ss = new SchedulerServlet();

        ss.init(sc);

        verify(sc);

        assertTrue(ss.existsTask("task"));
        assertTrue(ss.existsTask("daemon"));

        ss.stopAllTasks();
        ss.resetScheduler();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
        }
    }

    @Test
    public void testServletInitNoFile()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        ServletConfig sc = createNiceMock(ServletConfig.class);

        replay(sc);

        SchedulerServlet ss = new SchedulerServlet();

        ss.init(sc);

        verify(sc);

        assertFalse(ss.existsTask("task"));
        assertFalse(ss.existsTask("daemon"));

        ss.stopAllTasks();
        ss.resetScheduler();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
        }
    }

    @Test
    public void testServletInitBlankFile()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        ServletConfig sc = createNiceMock(ServletConfig.class);
        expect(sc.getInitParameter("iniFileName")).andReturn("");

        replay(sc);

        SchedulerServlet ss = new SchedulerServlet();

        ss.init(sc);

        verify(sc);

        assertFalse(ss.existsTask("task"));
        assertFalse(ss.existsTask("daemon"));

        ss.stopAllTasks();
        ss.resetScheduler();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
        }
    }

    @Test
    public void testServletInitError1()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ServletException {

        thrown.expect(ServletException.class);
        thrown.expectMessage("SCHED_LOG_EXCEPTION_INI_FILE_INVALID");

        ServletConfig sc = createNiceMock(ServletConfig.class);
        expect(sc.getInitParameter("iniFileName")).andReturn("target/test-classes/deors/demos/testing/mocks/servletmocks/scheduler-err1.ini");

        replay(sc);

        SchedulerServlet ss = new SchedulerServlet();

        ss.init(sc);
    }

    public static class MyTask
        extends SchedulerTask {

        private int count;

        public MyTask(String taskName, String taskDescription, Calendar taskStartTime,
                      Calendar taskStopTime) {

            super(taskName, taskDescription, taskStartTime, taskStopTime);
        }

        @Override
        protected void taskLogic() {

            info("task counts " + count++);

            if (count == 100) {
                taskAutoStop();
                return;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
            }
        }

        @Override
        protected void taskPrepareStart() throws Throwable {

            info("starting task");
        }

        @Override
        protected void taskPrepareStop() throws Throwable {

            info("stopping task");
        }

        /**
         * Dummy log method.
         *
         * @param message the log message
         */
        private static void info(String message) {
            System.out.println(message);
        }
    }
}
