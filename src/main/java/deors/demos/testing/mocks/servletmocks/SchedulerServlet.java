package deors.demos.testing.mocks.servletmocks;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used to initialize and manager a task scheduler using HTTP request.
 *
 * The scheduler is started when the servlet is initialized if the <code>iniFileName</code>
 * servlet parameter is informed.
 *
 * To change the schedule list, HTTP GET and POST requests can be sent to the servlet. The
 * <code>command</code> request parameter value is the configuration command that is requested:
 *
 * <ol>
 * <li>The <code>help</code> command includes help information in the servlet output.</li>
 *
 * <li>The <code>start</code> command starts the scheduler using the configuration file
 * referenced in the request parameter <code>iniFileName</code>.</li>
 *
 * <li>The <code>stop</code> command stops the scheduler or a single task if the
 * <code>taskName</code> request parameter is in the request.</li>
 *
 * <li>The <code>remove</code> command stops and removes a task given by the
 * <code>taskName</code> request parameter.</li>
 *
 * <li>The <code>add</code> command is used to add a new task to the schedule list. The task
 * parameters are given by the request parameters <code>taskName</code>,
 * <code>taskClassName</code>, <code>taskDescription</code>, <code>taskStartTime</code> and
 * <code>taskStopTime</code>. A daemon is created using as starting time the configured daemon id
 * string. Scheduling times are in HH:MM:SS format.</li>
 *
 * <li>The <code>schedule</code> command is used to re-schedule a single task. The task
 * parameters are given by the request parameters <code>taskName</code>,
 * <code>taskStartTime</code> and <code>taskStopTime</code>. A daemon is created using as
 * starting time the configured daemon id string. Scheduling times are in HH:MM:SS format.</li>
 * </ol>
 *
 * @author deors
 * @version 1.0
 */
public final class SchedulerServlet
    extends HttpServlet {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = 4170931961517840836L;

    /**
     * The scheduler.
     */
    private static Scheduler sch;

    /**
     * Flag that indicates whether the scheduler is running.
     */
    private static boolean initialized;

    /**
     * The last tasks information file name used.
     */
    private static String lastIniFileName;

    /**
     * Request parameter with the configuration command.
     */
    private static final String PARAM_COMMAND = "command"; //$NON-NLS-1$

    /**
     * Command for showing the scheduler help information.
     */
    private static final String MODE_HELP = "help"; //$NON-NLS-1$

    /**
     * Command for starting the scheduler.
     */
    private static final String MODE_START = "start"; //$NON-NLS-1$

    /**
     * Command for stopping the scheduler or a single task.
     */
    private static final String MODE_STOP = "stop"; //$NON-NLS-1$

    /**
     * Command for stopping and removing a single task.
     */
    private static final String MODE_REMOVE = "remove"; //$NON-NLS-1$

    /**
     * Command for adding a new task.
     */
    private static final String MODE_ADD = "add"; //$NON-NLS-1$

    /**
     * Command for scheduling a task.
     */
    private static final String MODE_SCHEDULE = "schedule"; //$NON-NLS-1$

    /**
     * Command for killing a task.
     */
    private static final String MODE_KILL = "kill"; //$NON-NLS-1$

    /**
     * Request parameter that contains the configuration file name.
     */
    private static final String PARAM_INI_FILE_NAME = "iniFileName"; //$NON-NLS-1$

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_NAME = "taskName"; //$NON-NLS-1$

    /**
     * Request parameter that contains the task class name.
     */
    private static final String PARAM_TASK_CLASS_NAME = "taskClassName"; //$NON-NLS-1$

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_DESCRIPTION = "taskDescription"; //$NON-NLS-1$

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_START_TIME = "taskStartTime"; //$NON-NLS-1$

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_STOP_TIME = "taskStopTime"; //$NON-NLS-1$

    /**
     * Token used in templates to print the task class name.
     */
    private static final String TEMPLATE_TASK_CLASS_NAME = "TASK_CLASS_NAME"; //$NON-NLS-1$

    /**
     * Token used in templates to print the task description.
     */
    private static final String TEMPLATE_TASK_DESCRIPTION = "TASK_DESCRIPTION"; //$NON-NLS-1$

    /**
     * Token used in templates to print the task info.
     */
    private static final String TEMPLATE_TASK_INFO = "TASK_INFO"; //$NON-NLS-1$

    /**
     * Token used in templates to print the task name.
     */
    private static final String TEMPLATE_TASK_NAME = "TASK_NAME"; //$NON-NLS-1$

    /**
     * Token used in templates to print the task state.
     */
    private static final String TEMPLATE_TASK_STATE = "TASK_STATE"; //$NON-NLS-1$

    /**
     * Token used in templates to print an action.
     */
    private static final String TEMPLATE_ACTION = "ACTION"; //$NON-NLS-1$

    /**
     * Token used in templates to print a date & time.
     */
    private static final String TEMPLATE_DATE_TIME = "DATE_TIME"; //$NON-NLS-1$

    /**
     * Token used in templates to print an error message.
     */
    private static final String TEMPLATE_ERROR = "ERROR"; //$NON-NLS-1$

    /**
     * Token used in templates to print the task array index.
     */
    private static final String TEMPLATE_INDEX = "INDEX"; //$NON-NLS-1$

    /**
     * Token used in templates to print a message.
     */
    private static final String TEMPLATE_MESSAGE = "MESSAGE"; //$NON-NLS-1$

    /**
     * The date formatter.
     */
    private final SimpleDateFormat dateFormatter;

    /**
     * Character encoding used when writing servlet responses. Configurable in the properties file
     * using the key <code>sched.characterEncoding</code>. Default value is <code>UTF-8</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * Content type used when writing servlet responses. Configurable in the properties file using
     * the key <code>sched.contentType</code>. Default value is <code>text/html</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String CONTENT_TYPE = "text/html"; //$NON-NLS-1$

    /**
     * Date format string used to print time information in the scheduler messages (not the same
     * that the time information printed by the default log). Configurable in the properties file
     * using the key <code>sched.dateFormat</code>. Default value is <code>yyyy/MM/dd HH:mm:ss</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$

    /**
     * The scheduler header template path. Configurable in the properties file using the key
     * <code>sched.templateHeader</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-header.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_HEADER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-header.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler footer template path. Configurable in the properties file using the key
     * <code>sched.templateFooter</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-footer.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_FOOTER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-footer.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler message header template path. Configurable in the properties file using the key
     * <code>sched.templateMessageHeader</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-message-header.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_MESSAGE_HEADER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-message-header.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler message footer template path. Configurable in the properties file using the key
     * <code>sched.templateMessageFooter</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-message-footer.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_MESSAGE_FOOTER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-message-footer.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler message item template path. Configurable in the properties file using the key
     * <code>sched.templateMessageItem</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-message-item.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_MESSAGE_ITEM =
        "/deors/demos/testing/mocks/servletmocks/scheduler-message-item.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler error header template path. Configurable in the properties file using the key
     * <code>sched.templateErrorHeader</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-error-header.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_ERROR_HEADER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-error-header.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler error footer template path. Configurable in the properties file using the key
     * <code>sched.templateErrorFooter</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-error-footer.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_ERROR_FOOTER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-error-footer.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler error item template path. Configurable in the properties file using the key
     * <code>sched.templateErrorItem</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-error-item.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_ERROR_ITEM =
        "/deors/demos/testing/mocks/servletmocks/scheduler-error-item.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler help template path. Configurable in the properties file using the key
     * <code>sched.templateHelp</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-help.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_HELP =
        "/deors/demos/testing/mocks/servletmocks/scheduler-help.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler command 1 template path. Configurable in the properties file using the key
     * <code>sched.templateCommand1</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-command-1.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_COMMAND_1 =
        "/deors/demos/testing/mocks/servletmocks/scheduler-command-1.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler command 2 template path. Configurable in the properties file using the key
     * <code>sched.templateCommand2</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-command-2.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_COMMAND_2 =
        "/deors/demos/testing/mocks/servletmocks/scheduler-command-2.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler command 3 template path. Configurable in the properties file using the key
     * <code>sched.templateCommand3</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-command-3.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_COMMAND_3 =
        "/deors/demos/testing/mocks/servletmocks/scheduler-command-3.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler task header template path. Configurable in the properties file using the key
     * <code>sched.templateTaskHeader</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-task-header.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_TASK_HEADER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-task-header.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler task footer template path. Configurable in the properties file using the key
     * <code>sched.templateTaskFooter</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-task-footer.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_TASK_FOOTER =
        "/deors/demos/testing/mocks/servletmocks/scheduler-task-footer.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler task item 1 template path. Configurable in the properties file using the key
     * <code>sched.templateTaskItem1</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-task-item-1.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_TASK_ITEM_1 =
        "/deors/demos/testing/mocks/servletmocks/scheduler-task-item-1.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler task item 2 template path. Configurable in the properties file using the key
     * <code>sched.templateTaskItem2</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-task-item-2.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_TASK_ITEM_2 =
        "/deors/demos/testing/mocks/servletmocks/scheduler-task-item-2.tmpl"; //$NON-NLS-1$

    /**
     * The scheduler task item 3 template path. Configurable in the properties file using the key
     * <code>sched.templateTaskItem3</code>. Default value is
     * <code>/deors/demos/testing/mocks/servletmocks/scheduler-task-item-3.tmpl</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String TEMPLATE_TASK_ITEM_3 =
        "/deors/demos/testing/mocks/servletmocks/scheduler-task-item-3.tmpl"; //$NON-NLS-1$

    /**
     * Default constructor.
     */
    public SchedulerServlet() {

        super();

        dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        dateFormatter.setLenient(false);
    }

    /**
     * Initializes the servlet and the scheduler if the <code>iniFile</code> servlet parameter is
     * given.
     *
     * @param config the servlet configuration
     *
     * @throws ServletException a servlet exception
     */
    public void init(ServletConfig config)
        throws ServletException {

        super.init(config);

        String iniFileName = config.getInitParameter(PARAM_INI_FILE_NAME);

        if (iniFileName == null || iniFileName.isEmpty()) {
            runScheduler();
        } else {
            runScheduler(iniFileName);
        }
    }

    /**
     * Runs the scheduler without tasks.
     *
     * @throws ServletException a servlet exception
     */
    private void runScheduler()
        throws ServletException {

        if (initialized) {
            return;
        }

        sch = new Scheduler();

        sch.schedulerThread = new Thread(sch);
        sch.schedulerThread.start();

        initialized = true;
        lastIniFileName = null;
    }

    /**
     * Runs the scheduler.
     *
     * @param iniFileName the name of the file with the tasks information
     *
     * @throws ServletException a servlet exception
     */
    private void runScheduler(String iniFileName)
        throws ServletException {

        if (initialized) {
            return;
        }

        if (iniFileName == null || iniFileName.length() == 0) {
            throw new ServletException("SCHED_LOG_PARAMETER_INI_FILE"); //$NON-NLS-1$
        }

        try {
            sch = new Scheduler(iniFileName);

            sch.schedulerThread = new Thread(sch);
            sch.schedulerThread.start();
        } catch (IOException ioe) {
            throw new ServletException("SCHED_LOG_EXCEPTION_INI_FILE_MISSING", ioe); //$NON-NLS-1$
        } catch (IllegalArgumentException iae) {
            throw new ServletException("SCHED_LOG_EXCEPTION_INI_FILE_INVALID", iae); //$NON-NLS-1$
        }

        initialized = true;
        lastIniFileName = iniFileName;
    }

    /**
     * Process HTTP GET requests intended to change the scheduler state.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     *
     * @throws ServletException a servlet exception
     * @throws IOException an i/o exception
     *
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        doRequest(request, response);
    }

    /**
     * Process HTTP POST requests intended to change the scheduler state.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     *
     * @throws ServletException a servlet exception
     * @throws IOException an i/o exception
     *
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        doRequest(request, response);
    }

    /**
     * Process HTTP requests intended to change the scheduler state.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     *
     * @throws ServletException a servlet exception
     * @throws IOException an i/o exception
     */
    private void doRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        List<String> messages = new ArrayList<String>();
        List<String> errors = new ArrayList<String>();

        boolean help = false;

        String command = request.getParameter(PARAM_COMMAND);
        if (command == null || command.length() == 0) {
            help = true;
        } else {
            if (command.equalsIgnoreCase(MODE_HELP)) {
                help = true;
            } else if (command.equalsIgnoreCase(MODE_START)) {
                processCommandStart(request, messages);
            } else if (command.equalsIgnoreCase(MODE_STOP)) {
                processCommandStop(request, messages, errors);
            } else if (command.equalsIgnoreCase(MODE_REMOVE)) {
                processCommandRemove(request, messages, errors);
            } else if (command.equalsIgnoreCase(MODE_ADD)) {
                processCommandAdd(request, messages, errors);
            } else if (command.equalsIgnoreCase(MODE_SCHEDULE)) {
                processCommandSchedule(request, messages, errors);
            } else if (command.equalsIgnoreCase(MODE_KILL)) {
                processCommandKill(request, messages, errors);
            } else {
                help = true;
            }
        }

        createServletResponse(request, response, messages, errors, help);
    }

    /**
     * Processes a start command.
     *
     * @param request the HTTP request
     * @param messages the messages list
     *
     * @throws ServletException a servlet exception
     */
    private void processCommandStart(HttpServletRequest request, List<String> messages)
        throws ServletException {

        if (initialized) {

            messages.add("SCHED_SERVLET_LOG_ALREADY_STARTED"); //$NON-NLS-1$

        } else {

            String iniFileName = request.getParameter(PARAM_INI_FILE_NAME);

            if (iniFileName == null || iniFileName.length() == 0) {
                if (lastIniFileName == null || lastIniFileName.length() == 0) {
                    runScheduler();
                } else {
                    runScheduler(lastIniFileName);
                }
            } else {
                runScheduler(iniFileName);
            }

            messages.add("SCHED_SERVLET_LOG_STARTED"); //$NON-NLS-1$
        }
    }

    /**
     * Processes a stop command.
     *
     * @param request the HTTP request
     * @param messages the message list
     * @param errors the error list
     */
    private void processCommandStop(HttpServletRequest request, List<String> messages, List<String> errors) {

        if (!initialized) {
            return;
        }

        String taskName = request.getParameter(PARAM_TASK_NAME);

        if (taskName == null || taskName.length() == 0) {

            stopAllTasks();
            messages.add("SCHED_SERVLET_LOG_STOPPED"); //$NON-NLS-1$

        } else {
            if (sch.existsTask(taskName)) {

                stopTask(taskName);
                messages.add("SCHED_SERVLET_LOG_TASK_STOPPED" + taskName); //$NON-NLS-1$
            } else {
                errors.add("SCHED_SERVLET_LOG_TASK_NOT_EXIST" + taskName); //$NON-NLS-1$
            }
        }
    }

    /**
     * Processes a remove command.
     *
     * @param request the HTTP request
     * @param messages the message list
     * @param errors the error list
     */
    private void processCommandRemove(HttpServletRequest request, List<String> messages, List<String> errors) {

        if (!initialized) {
            return;
        }

        String taskName = request.getParameter(PARAM_TASK_NAME);

        if (taskName == null || taskName.length() == 0) {

            errors.add("SCHED_SERVLET_ERR_NO_TASK_NAME"); //$NON-NLS-1$

        } else {
            if (sch.existsTask(taskName)) {

                stopAndRemoveTask(taskName);
                messages.add("SCHED_SERVLET_LOG_TASK_REMOVED" + taskName); //$NON-NLS-1$
            } else {
                errors.add("SCHED_SERVLET_LOG_TASK_NOT_EXIST" + taskName); //$NON-NLS-1$
            }
        }
    }

    /**
     * Processes an add command.
     *
     * @param request the HTTP request
     * @param messages the message list
     * @param errors the error list
     */
    private void processCommandAdd(HttpServletRequest request, List<String> messages, List<String> errors) {

        if (!initialized) {
            return;
        }

        List<String> newErrors = new ArrayList<String>();

        String taskName = parseTaskName(request, newErrors);

        String taskClassName = parseTaskClassName(request, newErrors);

        String taskDescription = parseTaskDescription(request, newErrors);

        Calendar taskStartTime = parseTaskStartTime(request, newErrors);

        Calendar taskStopTime = parseTaskStopTime(request, newErrors);

        if (newErrors.isEmpty()) {
            try {
                scheduleTask(taskName, taskClassName, taskDescription, taskStartTime, taskStopTime);
                messages.add("SCHED_SERVLET_LOG_TASK_SCHEDULED" + taskName); //$NON-NLS-1$

            } catch (IllegalArgumentException iae) {
                errors.add(iae.getMessage());
            }
        } else {
            errors.addAll(newErrors);
        }
    }

    /**
     * Processes a schedule command.
     *
     * @param request the HTTP request
     * @param messages the message list
     * @param errors the error list
     */
    private void processCommandSchedule(HttpServletRequest request, List<String> messages, List<String> errors) {

        if (!initialized) {
            return;
        }

        List<String> newErrors = new ArrayList<String>();

        String taskName = parseTaskName(request, newErrors);

        Calendar taskStartTime = parseTaskStartTime(request, newErrors);

        Calendar taskStopTime = parseTaskStopTime(request, newErrors);

        if (newErrors.isEmpty()) {
            if (sch.existsTask(taskName)) {

                try {
                    scheduleTask(taskName, (Class<?>) null, null, taskStartTime, taskStopTime);
                    messages.add("SCHED_SERVLET_LOG_TASK_SCHEDULED" + taskName); //$NON-NLS-1$

                } catch (IllegalArgumentException iae) {
                    errors.add(iae.getMessage());
                }
            } else {
                errors.add("SCHED_SERVLET_LOG_TASK_NOT_EXIST" + taskName); //$NON-NLS-1$
            }
        } else {
            errors.addAll(newErrors);
        }
    }

    /**
     * Parses the task name.
     *
     * @param request the HTTP request
     * @param newErrors list for errors during validation
     *
     * @return the task name
     */
    private String parseTaskName(HttpServletRequest request, List<String> newErrors) {

        String taskName = request.getParameter(PARAM_TASK_NAME);

        if (taskName == null || taskName.length() == 0) {

            newErrors.add("SCHED_SERVLET_ERR_NO_TASK_NAME"); //$NON-NLS-1$
        }

        return taskName;
    }

    /**
     * Parses the task class name.
     *
     * @param request the HTTP request
     * @param newErrors list for errors during validation
     *
     * @return the task class name
     */
    private String parseTaskClassName(HttpServletRequest request, List<String> newErrors) {

        String taskClassName = request.getParameter(PARAM_TASK_CLASS_NAME);

        if (taskClassName == null || taskClassName.length() == 0) {

            newErrors.add("SCHED_SERVLET_ERR_NO_TASK_CLASS"); //$NON-NLS-1$
        }

        return taskClassName;
    }

    /**
     * Parses the task description.
     *
     * @param request the HTTP request
     * @param newErrors list for errors during validation
     *
     * @return the task description
     */
    private String parseTaskDescription(HttpServletRequest request, List<String> newErrors) {

        String taskDescription = request.getParameter(PARAM_TASK_DESCRIPTION);

        if (taskDescription == null || taskDescription.length() == 0) {

            newErrors.add("SCHED_SERVLET_ERR_NO_TASK_DESCRIPTION"); //$NON-NLS-1$
        }

        return taskDescription;
    }

    /**
     * Parses the task start time.
     *
     * @param request the HTTP request
     * @param newErrors list for errors during validation
     *
     * @return the task start time
     */
    private Calendar parseTaskStartTime(HttpServletRequest request, List<String> newErrors) {

        String tempStartTime = request.getParameter(PARAM_TASK_START_TIME);
        Calendar taskStartTime = null;

        if (tempStartTime == null || tempStartTime.length() == 0) {

            newErrors.add("SCHED_SERVLET_ERR_NO_TASK_START"); //$NON-NLS-1$

        } else {
            try {
                taskStartTime = sch.parseTime(tempStartTime);

            } catch (IllegalArgumentException iae) {

                newErrors.add("SCHED_SERVLET_ERR_INVALID_TASK_START"); //$NON-NLS-1$
            }
        }

        return taskStartTime;
    }

    /**
     * Parses the task stop time.
     *
     * @param request the HTTP request
     * @param newErrors list for errors during validation
     *
     * @return the task stop time
     */
    private Calendar parseTaskStopTime(HttpServletRequest request, List<String> newErrors) {

        String tempStopTime = request.getParameter(PARAM_TASK_STOP_TIME);
        Calendar taskStopTime = null;

        if (tempStopTime == null || tempStopTime.length() == 0) {

            newErrors.add("SCHED_SERVLET_ERR_NO_TASK_STOP"); //$NON-NLS-1$

        } else {
            try {
                taskStopTime = sch.parseTime(tempStopTime);

            } catch (IllegalArgumentException iae) {

                newErrors.add("SCHED_SERVLET_ERR_INVALID_TASK_STOP"); //$NON-NLS-1$
            }
        }

        return taskStopTime;
    }

    /**
     * Processes a kill command.
     *
     * @param request the HTTP request
     * @param messages the message list
     * @param errors the error list
     */
    private void processCommandKill(HttpServletRequest request, List<String> messages, List<String> errors) {

        if (!initialized) {
            return;
        }

        String taskName = request.getParameter(PARAM_TASK_NAME);

        if (taskName == null || taskName.length() == 0) {

            errors.add("SCHED_SERVLET_ERR_NO_TASK_NAME"); //$NON-NLS-1$

        } else {
            if (sch.existsTask(taskName)) {

                killTask(taskName);
                messages.add("SCHED_SERVLET_LOG_TASK_KILLED" + taskName); //$NON-NLS-1$
            } else {
                errors.add("SCHED_SERVLET_LOG_TASK_NOT_EXIST" + taskName); //$NON-NLS-1$
            }
        }
    }

    /**
     * Sends to the servlet output a response page with messages, errors, the scheduler command
     * center, help information and tasks information and execution control.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param messages the messages to be printed
     * @param errors the errors to be printed
     * @param help whether to show help information
     *
     * @throws IOException an I/O exception
     */
    private void createServletResponse(HttpServletRequest request, HttpServletResponse response,
                                       List<String> messages, List<String> errors, boolean help)
        throws IOException {

        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(CHARACTER_ENCODING);

        PrintWriter out = response.getWriter();

        try {
            // hash table with template replacements
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put(TEMPLATE_ACTION, request.getRequestURI());
            replacements.put(TEMPLATE_DATE_TIME, dateFormatter.format(Calendar.getInstance().getTime()));

            // page header
            Template templateHeader =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_HEADER));
            templateHeader.processTemplate(null, out);

            // messages
            createSectionMessages(messages, replacements, out);

            // errors
            createSectionErrors(errors, replacements, out);

            // command center header
            if (initialized) {
                // process the scheduler-command-1 template
                Template templateCommand1 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_COMMAND_1));
                templateCommand1.processTemplate(replacements, out);
            } else {
                // process the scheduler-command-2 template
                Template templateCommand2 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_COMMAND_2));
                templateCommand2.processTemplate(replacements, out);
            }

            // help
            if (help) {
                // process the scheduler-help template
                Template templateHelp =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_HELP));
                templateHelp.processTemplate(null, out);
            }

            if (initialized && sch != null) {
                // command center add
                Template templateCommand3 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_COMMAND_3));
                templateCommand3.processTemplate(replacements, out);

                // tasks
                createSectionTasks(replacements, out);
            }

            // page footer
            Template templateFooter =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_FOOTER));
            templateFooter.processTemplate(null, out);

        } catch (TemplateException te) {
            throw new IOException(te.getMessage(), te);
        }

        out.flush();
        out.close();
    }

    /**
     * Creates the messages section.
     *
     * @param messages the message list
     * @param replacements the replacements map
     * @param out the output writer
     *
     * @throws TemplateException an error processing a template
     */
    private void createSectionMessages(List<String> messages, Map<String, String> replacements, PrintWriter out)
        throws TemplateException {

        if (!initialized) {
            messages.add("SCHED_SERVLET_LOG_NOT_RUNNING"); //$NON-NLS-1$
        }

        int n = messages.size();

        if (n != 0) {
            // process the scheduler-message-header template
            Template templateMessageHeader =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_MESSAGE_HEADER));
            templateMessageHeader.processTemplate(null, out);

            // each message uses the scheduler-message-item template
            Template templateMessageItem =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_MESSAGE_ITEM));

            for (int i = 0; i < n; i++) {
                replacements.put(TEMPLATE_MESSAGE, messages.get(i));
                templateMessageItem.processTemplate(replacements, out);
            }

            // process the scheduler-message-footer template
            Template templateMessageFooter =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_MESSAGE_FOOTER));
            templateMessageFooter.processTemplate(null, out);
        }
    }

    /**
     * Creates the errors section.
     *
     * @param errors the error list
     * @param replacements the replacements map
     * @param out the output writer
     *
     * @throws TemplateException an error processing a template
     */
    private void createSectionErrors(List<String> errors, Map<String, String> replacements, PrintWriter out)
        throws TemplateException {

        int n = errors.size();

        if (n != 0) {
            // process the scheduler-error-header template
            Template templateErrorHeader =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_ERROR_HEADER));
            templateErrorHeader.processTemplate(null, out);

            // each error uses the scheduler-error-item template
            Template templateErrorItem =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_ERROR_ITEM));

            for (int i = 0; i < n; i++) {
                replacements.put(TEMPLATE_ERROR, errors.get(i));
                templateErrorItem.processTemplate(replacements, out);
            }

            // process the scheduler-error-footer template
            Template templateErrorFooter =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_ERROR_FOOTER));
            templateErrorFooter.processTemplate(null, out);
        }
    }

    /**
     * Creates the tasks section.
     *
     * @param replacements the replacements map
     * @param out the output writer
     *
     * @throws TemplateException an error processing a template
     */
    private void createSectionTasks(Map<String, String> replacements, PrintWriter out)
        throws TemplateException {

        // process the scheduler-task-header template
        Template templateTaskHeader =
            new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_HEADER));
        templateTaskHeader.processTemplate(replacements, out);

        // each task uses the scheduler-task-item template
        if (initialized && sch != null) {
            List<SchedulerTask> tasks = sch.getTasks();
            if (!tasks.isEmpty()) {
                Template templateTaskItem1 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_ITEM_1));
                Template templateTaskItem2 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_ITEM_2));
                Template templateTaskItem3 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_ITEM_3));

                int i = 0;
                for (SchedulerTask task : tasks) {
                    i++;
                    createSectionTask(task, i,
                        templateTaskItem1, templateTaskItem2, templateTaskItem3,
                        replacements, out);
                }
            }
        }

        // process the scheduler-task-footer template
        Template templateTaskFooter =
            new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_FOOTER));
        templateTaskFooter.processTemplate(null, out);
    }

    /**
     * Creates a task section.
     *
     * @param task the task information
     * @param i the task index
     * @param templateTaskItem1 template used to create the section
     * @param templateTaskItem2 template used to create the section
     * @param templateTaskItem3 template used to create the section
     * @param replacements the replacements map
     * @param out the output writer
     *
     * @throws TemplateException an error processing a template
     */
    private void createSectionTask(SchedulerTask task, int i, Template templateTaskItem1,
                                   Template templateTaskItem2, Template templateTaskItem3,
                                   Map<String, String> replacements, PrintWriter out)
        throws TemplateException {

        replacements.put(TEMPLATE_INDEX, Integer.toString(i));
        replacements.put(TEMPLATE_TASK_NAME, task.getTaskName());
        replacements.put(TEMPLATE_TASK_DESCRIPTION, task.getTaskDescription());
        replacements.put(TEMPLATE_TASK_CLASS_NAME, task.getClass().getName());

        // task state
        prepareTaskState(task, replacements);

        // task info
        prepareTaskInformation(task, replacements);

        // task header
        templateTaskItem1.processTemplate(replacements, out);

        if (task.isExecuting() || task.isStarting()) {
            // adds stop and kill buttons
            templateTaskItem3.processTemplate(replacements, out);
        } else {
            // adds start and schedule buttons
            templateTaskItem2.processTemplate(replacements, out);
        }
    }

    /**
     * Prepares the task state.
     *
     * @param task the task information
     * @param replacements the replacements map
     */
    private void prepareTaskState(SchedulerTask task, Map<String, String> replacements) {

        if (task.isStarting()) {
            replacements.put(TEMPLATE_TASK_STATE, "SCHED_SERVLET_STATE_STARTING"); //$NON-NLS-1$
        } else if (task.isStopping()) {
            replacements.put(TEMPLATE_TASK_STATE, "SCHED_SERVLET_STATE_STOPPING"); //$NON-NLS-1$
        } else if (task.isExecuting()) {
            if (task.taskThread == null) {
                replacements.put(TEMPLATE_TASK_STATE, "SCHED_SERVLET_STATE_THREAD_NULL"); //$NON-NLS-1$
            } else {
                replacements.put(TEMPLATE_TASK_STATE, "SCHED_SERVLET_STATE_RUNNING" + task.taskThread.toString()); //$NON-NLS-1$
            }
        } else {
            replacements.put(TEMPLATE_TASK_STATE, "SCHED_SERVLET_STATE_IDLE"); //$NON-NLS-1$
        }
    }

    /**
     * Prepares the task information.
     *
     * @param task the task information
     * @param replacements the replacements map
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    private void prepareTaskInformation(SchedulerTask task, Map<String, String> replacements) {

        if (task.isDaemonTask()) {
            if (task.isDaemonExecuted()) {
                replacements.put(TEMPLATE_TASK_INFO, "SCHED_SERVLET_INFO_DAEMON_EXECUTED"); //$NON-NLS-1$
            } else {
                replacements.put(TEMPLATE_TASK_INFO, "SCHED_SERVLET_INFO_DAEMON_IDLE"); //$NON-NLS-1$
            }
        } else if (task.getTaskNextStartTime() != null
                   && task.getTaskNextStopTime() != null) {
            replacements.put(TEMPLATE_TASK_INFO,
                "SCHED_SERVLET_INFO_SCHEDULED" //$NON-NLS-1$
                + dateFormatter.format(task.getTaskNextStartTime().getTime())
                + dateFormatter.format(task.getTaskNextStopTime().getTime()));
        } else if (task.getTaskStartTime() != null
                   && task.getTaskStopTime() != null) {
            replacements.put(TEMPLATE_TASK_INFO,
                "SCHED_SERVLET_INFO_SCHEDULED" //$NON-NLS-1$
                + dateFormatter.format(task.getTaskStartTime().getTime())
                + dateFormatter.format(task.getTaskStopTime().getTime()));
        } else {
            replacements.put(TEMPLATE_TASK_INFO, "SCHED_SERVLET_INFO_NOT_AVAILABLE"); //$NON-NLS-1$
        }
    }

    /**
     * Checks the existence of a task with the given name.
     *
     * @return whether a task with the given name exists
     *
     * @param taskName the task name
     */
    public static boolean existsTask(String taskName) {

        return sch == null ? false : sch.existsTask(taskName);
    }

    /**
     * Kills a task. If the task does not exist or it is not running, the method does nothing. The
     * killing is done in another thread, because the servlet catches the
     * <code>java.lang.TreadDeath</code> error and does not throw it, so the thread does not stop.
     *
     * @param taskName the task name
     *
     * @see java.lang.ThreadDeath
     */
    public static void killTask(String taskName) {

        if (sch == null) {
            return;
        }

        final String t = taskName;

        Thread killThread = new Thread() {

            /**
             * Forces the killing of a task.
             */
            public void run() {

                sch.killTask(t);
            }
        };

        killThread.start();
    }

    /**
     * Schedules a new task or re-schedules an existing task.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the task class
     * could not be successfully created.
     *
     * @param taskName the task name
     * @param taskClass the task class
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     */
    public static void scheduleTask(String taskName, Class<?> taskClass, String taskDescription,
                                    Calendar taskStartTime, Calendar taskStopTime) {

        if (sch != null) {
            sch.scheduleTask(taskName, taskClass, taskDescription, taskStartTime, taskStopTime);
        }
    }

    /**
     * Schedules a new task or re-schedules an existing task.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the task class
     * could not be successfully created.
     *
     * @param taskName the task name
     * @param taskClassName the task class name
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     */
    public static void scheduleTask(String taskName, String taskClassName,
                                    String taskDescription, Calendar taskStartTime,
                                    Calendar taskStopTime) {

        if (sch != null) {
            sch.scheduleTask(taskName, taskClassName, taskDescription, taskStartTime, taskStopTime);
        }
    }

    /**
     * Stops and removes all tasks from the scheduling table and ends the scheduler thread.
     */
    public static void stopAllTasks() {

        if (sch != null) {
            sch.stopAllTasks();
        }
        initialized = false;
    }

    /**
     * Stops a task and removes it from the scheduling table. If the task does not exist the method
     * does nothing. If the task exists but is not running, it is only removed.
     *
     * @param taskName the task name
     */
    public static void stopAndRemoveTask(String taskName) {

        if (sch != null) {
            sch.stopAndRemoveTask(taskName);
        }
    }

    /**
     * Stops a task. If the task does not exist or it is not running, the method does nothing.
     *
     * @param taskName the task name
     */
    public static void stopTask(String taskName) {

        if (sch != null) {
            sch.stopTask(taskName);
        }
    }

    /**
     * Clears the scheduler static fields - to be used by unit tests only.
     */
    static void resetScheduler() {

        lastIniFileName = null;
        initialized = false;
        sch = null;
    }
}
