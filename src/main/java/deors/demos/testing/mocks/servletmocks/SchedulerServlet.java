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
 * Servlet used to initialize and manager a task scheduler using HTTP request.<br>
 *
 * The scheduler is started when the servlet is initialized if the <code>iniFileName</code>
 * servlet parameter is informed.<br>
 *
 * To change the schedule list, HTTP GET and POST requests can be sent to the servlet. The
 * <code>command</code> request parameter value is the configuration command that is requested:<br>
 *
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
 * starting time the configured daemon id string. Scheduling times are in HH:MM:SS format.</li><br>
 *
 * @author jorge.hidalgo
 * @version 2.5
 */
public final class SchedulerServlet
    extends HttpServlet {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = 4170931961517840836L;

    /**
     * Flag that indicates whether the scheduler is running.
     */
    private static boolean initialized;

    /**
     * The scheduler.
     */
    private static Scheduler sch;

    /**
     * The last tasks information file name used.
     */
    private static String lastIniFileName;

    /**
     * Request parameter with the configuration command.
     */
    private static final String PARAM_COMMAND = "command";

    /**
     * Command for showing the scheduler help information.
     */
    private static final String MODE_HELP = "help";

    /**
     * Command for starting the scheduler.
     */
    private static final String MODE_START = "start";

    /**
     * Command for stoping the scheduler or a single task.
     */
    private static final String MODE_STOP = "stop";

    /**
     * Command for stopping and removing a single task.
     */
    private static final String MODE_REMOVE = "remove";

    /**
     * Command for adding a new task.
     */
    private static final String MODE_ADD = "add";

    /**
     * Command for scheduling a task.
     */
    private static final String MODE_SCHEDULE = "schedule";

    /**
     * Command for killing a task.
     */
    private static final String MODE_KILL = "kill";

    /**
     * Request parameter that contains the configuration file name.
     */
    private static final String PARAM_INI_FILE_NAME = "iniFileName";

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_NAME = "taskName";

    /**
     * Request parameter that contains the task class name.
     */
    private static final String PARAM_TASK_CLASS_NAME = "taskClassName";

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_DESCRIPTION = "taskDescription";

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_START_TIME = "taskStartTime";

    /**
     * Request parameter that contains the task name.
     */
    private static final String PARAM_TASK_STOP_TIME = "taskStopTime";

    /**
     * Token used in templates to print the task class name.
     */
    private static final String TEMPLATE_TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /**
     * Token used in templates to print the task description.
     */
    private static final String TEMPLATE_TASK_DESCRIPTION = "TASK_DESCRIPTION";

    /**
     * Token used in templates to print the task info.
     */
    private static final String TEMPLATE_TASK_INFO = "TASK_INFO";

    /**
     * Token used in templates to print the task name.
     */
    private static final String TEMPLATE_TASK_NAME = "TASK_NAME";

    /**
     * Token used in templates to print the task state.
     */
    private static final String TEMPLATE_TASK_STATE = "TASK_STATE";

    /**
     * Token used in templates to print an action.
     */
    private static final String TEMPLATE_ACTION = "ACTION";

    /**
     * Token used in templates to print a date & time.
     */
    private static final String TEMPLATE_DATE_TIME = "DATE_TIME";

    /**
     * Token used in templates to print an error message.
     */
    private static final String TEMPLATE_ERROR = "ERROR";

    /**
     * Token used in templates to print the task array index.
     */
    private static final String TEMPLATE_I = "I";

    /**
     * Token used in templates to print a message.
     */
    private static final String TEMPLATE_MESSAGE = "MESSAGE";

    /**
     * The date formatter.
     */
    private final SimpleDateFormat dateFormatter;

    /**
     * Date format string used to print time information in the scheduler messages (not the same
     * that the time information printed by the default log).
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    /**
     * Content type used when writing servlet responses.
     */
    private static final String CONTENT_TYPE = "text/html";

    /**
     * Character encoding used when writing servlet responses.
     */
    private static final String CHARACTER_ENCODING = "UTF-8";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_HEADER = "/deors/demos/testing/mocks/servletmocks/scheduler-header.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_FOOTER = "/deors/demos/testing/mocks/servletmocks/scheduler-footer.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_MESSAGE_HEADER = "/deors/demos/testing/mocks/servletmocks/scheduler-message-header.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_MESSAGE_FOOTER = "/deors/demos/testing/mocks/servletmocks/scheduler-message-footer.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_MESSAGE_ITEM = "/deors/demos/testing/mocks/servletmocks/scheduler-message-item.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_ERROR_HEADER = "/deors/demos/testing/mocks/servletmocks/scheduler-error-header.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_ERROR_FOOTER = "/deors/demos/testing/mocks/servletmocks/scheduler-error-footer.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_ERROR_ITEM = "/deors/demos/testing/mocks/servletmocks/scheduler-error-item.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_HELP = "/deors/demos/testing/mocks/servletmocks/scheduler-help.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_COMMAND_1 = "/deors/demos/testing/mocks/servletmocks/scheduler-command-1.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_COMMAND_2 = "/deors/demos/testing/mocks/servletmocks/scheduler-command-2.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_COMMAND_3 = "/deors/demos/testing/mocks/servletmocks/scheduler-command-3.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_TASK_HEADER = "/deors/demos/testing/mocks/servletmocks/scheduler-task-header.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_TASK_FOOTER = "/deors/demos/testing/mocks/servletmocks/scheduler-task-footer.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_TASK_ITEM_1 = "/deors/demos/testing/mocks/servletmocks/scheduler-task-item-1.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_TASK_ITEM_2 = "/deors/demos/testing/mocks/servletmocks/scheduler-task-item-2.tmpl";

    /**
     * A scheduler template path.
     */
    private static final String TEMPLATE_TASK_ITEM_3 = "/deors/demos/testing/mocks/servletmocks/scheduler-task-item-3.tmpl";

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
     * @throws javax.servlet.ServletException a servlet exception
     */
    public void init(ServletConfig config)
        throws javax.servlet.ServletException {

        super.init(config);

        String iniFileName = config.getInitParameter(PARAM_INI_FILE_NAME);

        if (iniFileName != null && iniFileName.length() != 0) {
            runScheduler(iniFileName);
        }
    }

    /**
     * Process HTTP GET requests intended to change the scheduler state.<br>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     *
     * @throws javax.servlet.ServletException a servlet exception
     * @throws java.io.IOException an i/o exception
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws javax.servlet.ServletException,
               java.io.IOException {

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

                if (initialized) {
                    messages.add("ERR_ALREADY_STARTED");

                } else {

                    String iniFileName = request.getParameter(PARAM_INI_FILE_NAME);

                    if (iniFileName == null || iniFileName.length() == 0) {

                        if (lastIniFileName == null || lastIniFileName.length() == 0) {

                            runScheduler();
                            messages.add("ERR_STARTED");

                        } else {

                            runScheduler(lastIniFileName);
                            messages.add("ERR_STARTED");
                        }
                    } else {

                        runScheduler(iniFileName);
                        messages.add("ERR_STARTED");
                    }
                }
            } else if (command.equalsIgnoreCase(MODE_STOP)) {

                if (initialized) {

                    String taskName = request.getParameter(PARAM_TASK_NAME);

                    if (taskName == null || taskName.length() == 0) {

                        stopAllTasks();
                        messages.add("ERR_STOPPED");

                    } else {
                        if (sch.existsTask(taskName)) {

                            stopTask(taskName);
                            messages.add("ERR_TASK_STOPPED");
                        } else {
                            errors.add("ERR_TASK_NOT_EXIST");
                        }
                    }
                }
            } else if (command.equalsIgnoreCase(MODE_REMOVE)) {

                if (initialized) {

                    String taskName = request.getParameter(PARAM_TASK_NAME);

                    if (taskName == null || taskName.length() == 0) {

                        errors.add("ERR_NO_TASK_NAME");

                    } else {
                        if (sch.existsTask(taskName)) {

                            stopAndRemoveTask(taskName);
                            messages.add("ERR_TASK_REMOVED");
                        } else {
                            errors.add("ERR_TASK_NOT_EXIST");
                        }
                    }
                }
            } else if (command.equalsIgnoreCase(MODE_ADD)) {

                if (initialized) {

                    boolean flag = false;

                    String taskName = request.getParameter(PARAM_TASK_NAME);

                    if (taskName == null || taskName.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_NAME");
                    }

                    String taskClassName = request.getParameter(PARAM_TASK_CLASS_NAME);

                    if (taskClassName == null || taskClassName.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_CLASS");
                    }

                    String taskDescription = request.getParameter(PARAM_TASK_DESCRIPTION);

                    if (taskDescription == null || taskDescription.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_DESCRIPTION");
                    }

                    String tempStartTime = request.getParameter(PARAM_TASK_START_TIME);
                    Calendar taskStartTime = null;

                    if (tempStartTime == null || tempStartTime.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_START");

                    } else {
                        try {
                            taskStartTime = sch.parseTime(tempStartTime);
                        } catch (IllegalArgumentException iae) {

                            flag = true;
                            errors.add("ERR_INVALID_TASK_START");
                        }
                    }

                    String tempStopTime = request.getParameter(PARAM_TASK_STOP_TIME);
                    Calendar taskStopTime = null;

                    if (tempStopTime == null || tempStopTime.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_STOP");

                    } else {
                        try {
                            taskStopTime = sch.parseTime(tempStopTime);
                        } catch (IllegalArgumentException iae) {

                            flag = true;
                            errors.add("ERR_INVALID_TASK_STOP");
                        }
                    }

                    if (!flag) {
                        try {
                            scheduleTask(taskName, taskClassName, taskDescription, taskStartTime,
                                         taskStopTime);
                            messages.add("ERR_TASK_SCHEDULED");

                        } catch (IllegalArgumentException iae) {
                            errors.add(iae.getMessage());
                        }
                    }
                }
            } else if (command.equalsIgnoreCase(MODE_SCHEDULE)) {

                if (initialized) {

                    boolean flag = false;

                    String taskName = request.getParameter(PARAM_TASK_NAME);

                    if (taskName == null || taskName.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_NAME");
                    }

                    String tempStartTime = request.getParameter(PARAM_TASK_START_TIME);
                    Calendar taskStartTime = null;

                    if (tempStartTime == null || tempStartTime.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_START");

                    } else {
                        try {
                            taskStartTime = sch.parseTime(tempStartTime);
                        } catch (IllegalArgumentException iae) {

                            flag = true;
                            errors.add("ERR_INVALID_TASK_START");
                        }
                    }

                    String tempStopTime = request.getParameter(PARAM_TASK_STOP_TIME);
                    Calendar taskStopTime = null;

                    if (tempStopTime == null || tempStopTime.length() == 0) {

                        flag = true;
                        errors.add("ERR_NO_TASK_STOP");

                    } else {
                        try {
                            taskStopTime = sch.parseTime(tempStopTime);
                        } catch (IllegalArgumentException iae) {

                            flag = true;
                            errors.add("ERR_INVALID_TASK_STOP");
                        }
                    }

                    if (!flag) {
                        if (sch.existsTask(taskName)) {

                            try {
                                scheduleTask(taskName, (Class) null, null, taskStartTime, taskStopTime);
                                messages.add("ERR_TASK_SCHEDULED");

                            } catch (IllegalArgumentException iae) {
                                errors.add(iae.getMessage());
                            }
                        } else {
                            errors.add("ERR_TASK_NOT_EXIST");
                        }
                    }
                }
            } else if (command.equalsIgnoreCase(MODE_KILL)) {

                if (initialized) {

                    String taskName = request.getParameter(PARAM_TASK_NAME);

                    if (taskName == null || taskName.length() == 0) {

                        errors.add("ERR_NO_TASK_NAME");

                    } else {
                        if (sch.existsTask(taskName)) {

                            killTask(taskName);
                            messages.add("ERR_TASK_KILLED");

                        } else {
                            errors.add("ERR_TASK_NOT_EXIST");
                        }
                    }
                }
            } else {
                help = true;
            }
        }

        createServletResponse(request, response, messages, errors, help);
    }

    /**
     * Process HTTP POST requests intended to change the scheduler state.<br>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     *
     * @throws javax.servlet.ServletException a servlet exception
     * @throws java.io.IOException an i/o exception
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws javax.servlet.ServletException,
               java.io.IOException {
        doGet(request, response);
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
     * @throws java.io.IOException an I/O exception
     */
    private void createServletResponse(HttpServletRequest request, HttpServletResponse response,
                                       List<String> messages, List<String> errors, boolean help)
        throws java.io.IOException {

        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(CHARACTER_ENCODING);

        PrintWriter out = response.getWriter();

        try {
            Map<String, String> replacements = new HashMap<String, String>();
            replacements.put(TEMPLATE_ACTION, request.getRequestURI());
            replacements.put(TEMPLATE_DATE_TIME,
                dateFormatter.format(Calendar.getInstance().getTime()));

            Template templateHeader =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_HEADER));
            templateHeader.processTemplate(null, out);

            if (messages != null) {
                if (!initialized) {
                    messages.add("ERR_NOT_RUNNING");
                }

                int n = messages.size();

                if (n != 0) {
                    Template templateMessageHeader =
                        new Template(this.getClass().getResourceAsStream(TEMPLATE_MESSAGE_HEADER));
                    templateMessageHeader.processTemplate(null, out);

                    Template templateMessageItem =
                        new Template(this.getClass().getResourceAsStream(TEMPLATE_MESSAGE_ITEM));

                    for (int i = 0; i < n; i++) {
                        replacements.put(TEMPLATE_MESSAGE, messages.get(i));
                        templateMessageItem.processTemplate(replacements, out);
                    }

                    Template templateMessageFooter =
                        new Template(this.getClass().getResourceAsStream(TEMPLATE_MESSAGE_FOOTER));
                    templateMessageFooter.processTemplate(null, out);
                }
            }

            if (errors != null) {
                int n = errors.size();

                if (n != 0) {
                    Template templateErrorHeader =
                        new Template(this.getClass().getResourceAsStream(TEMPLATE_ERROR_HEADER));
                    templateErrorHeader.processTemplate(null, out);

                    Template templateErrorItem =
                        new Template(this.getClass().getResourceAsStream(TEMPLATE_ERROR_ITEM));

                    for (int i = 0; i < n; i++) {
                        replacements.put(TEMPLATE_ERROR, errors.get(i));
                        templateErrorItem.processTemplate(replacements, out);
                    }

                    Template templateErrorFooter =
                        new Template(this.getClass().getResourceAsStream(TEMPLATE_ERROR_FOOTER));
                    templateErrorFooter.processTemplate(null, out);
                }
            }

            if (initialized) {
                Template templateCommand1 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_COMMAND_1));
                templateCommand1.processTemplate(replacements, out);
            } else {
                Template templateCommand2 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_COMMAND_2));
                templateCommand2.processTemplate(replacements, out);
            }

            if (help) {
                Template templateHelp =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_HELP));
                templateHelp.processTemplate(null, out);
            }

            if (initialized) {
                Template templateCommand3 =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_COMMAND_3));
                templateCommand3.processTemplate(replacements, out);

                Template templateTaskHeader =
                    new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_HEADER));
                templateTaskHeader.processTemplate(replacements, out);

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
                        replacements.put(TEMPLATE_I, Integer.toString(i++));
                        replacements.put(TEMPLATE_TASK_NAME, task.getTaskName());
                        replacements.put(TEMPLATE_TASK_DESCRIPTION, task.getTaskDescription());
                        replacements.put(TEMPLATE_TASK_CLASS_NAME, task.getClass().getName());

                        if (task.isStarting()) {
                            replacements.put(TEMPLATE_TASK_STATE, "SERVLET_STATE_STARTING");
                        } else if (task.isStopping()) {
                            replacements.put(TEMPLATE_TASK_STATE, "SERVLET_STATE_STOPPING");
                        } else if (task.isExecuting()) {
                            if (task.taskThread == null) {
                                replacements.put(TEMPLATE_TASK_STATE, "SERVLET_STATE_THREAD_NULL");
                            } else {
                                replacements.put(TEMPLATE_TASK_STATE, "SERVLET_STATE_RUNNING");
                            }
                        } else {
                            replacements.put(TEMPLATE_TASK_STATE, "SERVLET_STATE_IDLE");
                        }

                        if (task.isDaemonTask()) {
                            if (task.isDaemonExecuted()) {
                                replacements.put(TEMPLATE_TASK_INFO, "SERVLET_INFO_DAEMON_EXECUTED");
                            } else {
                                replacements.put(TEMPLATE_TASK_INFO, "SERVLET_INFO_DAEMON_IDLE");
                            }
                        } else if (task.getTaskNextStartTime() != null
                                   && task.getTaskNextStopTime() != null) {
                            replacements.put(TEMPLATE_TASK_INFO, "SERVLET_INFO_SCHEDULED");
                        } else if (task.getTaskStartTime() != null
                                   && task.getTaskStopTime() != null) {
                            replacements.put(TEMPLATE_TASK_INFO, "SERVLET_INFO_SCHEDULED");
                        } else {
                            replacements.put(TEMPLATE_TASK_INFO, "SERVLET_INFO_NOT_AVAILABLE");
                        }

                        templateTaskItem1.processTemplate(replacements, out);
                        if (task.isExecuting() || task.isStarting()) {
                            templateTaskItem3.processTemplate(replacements, out);
                        } else {
                            templateTaskItem2.processTemplate(replacements, out);
                        }
                    }
                }
            }

            Template templateTaskFooter =
                new Template(this.getClass().getResourceAsStream(TEMPLATE_TASK_FOOTER));
            templateTaskFooter.processTemplate(null, out);

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
     * Runs the scheduler without tasks.
     *
     * @throws javax.servlet.ServletException a servlet exception
     */
    private void runScheduler()
        throws javax.servlet.ServletException {

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
     * @throws javax.servlet.ServletException a servlet exception
     */
    private void runScheduler(String iniFileName)
        throws javax.servlet.ServletException {

        if (initialized) {
            return;
        }

        if (iniFileName == null || iniFileName.length() == 0) {
            throw new ServletException("ERR_PARAMETER_INI_FILE");
        }

        try {
            sch = new Scheduler(iniFileName);

            sch.schedulerThread = new Thread(sch);
            sch.schedulerThread.start();
        } catch (java.io.IOException ioe) {
            throw new ServletException("ERR_EXCEPTION_INI_FILE_MISSING", ioe);
        } catch (IllegalArgumentException iae) {
            throw new ServletException("ERR_EXCEPTION_INI_FILE_INVALID", iae);
        }

        initialized = true;
        lastIniFileName = iniFileName;
    }

    /**
     * Checks the existence of a task with the given name.
     *
     * @return whether a task with the given name exists
     *
     * @param taskName the task name
     */
    public static boolean existsTask(String taskName) {

        return sch.existsTask(taskName);
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
     * Clears the last tasks information file name used to initialize the servlet.
     */
    public static void resetScheduler() {

        lastIniFileName = null;
    }

    /**
     * Schedules a new task or re-schedules an existing task.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the task class
     * could not be successfully created.
     *
     * @param taskName the task name
     * @param taskClass the task class
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     */
    public static void scheduleTask(String taskName, Class taskClass, String taskDescription,
                                     Calendar taskStartTime, Calendar taskStopTime) {

        sch.scheduleTask(taskName, taskClass, taskDescription, taskStartTime, taskStopTime);
    }

    /**
     * Schedules a new task or re-schedules an existing task.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the task class
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

        sch.scheduleTask(taskName, taskClassName, taskDescription, taskStartTime, taskStopTime);
    }

    /**
     * Stops and removes all tasks from the scheduling table and ends the scheduler thread.
     */
    public static void stopAllTasks() {

        sch.stopAllTasks();
        initialized = false;
    }

    /**
     * Stops a task and removes it from the scheduling table. If the task does not exist the method
     * does nothing. If the task exists but is not running, it is only removed.
     *
     * @param taskName the task name
     */
    public static void stopAndRemoveTask(String taskName) {

        sch.stopAndRemoveTask(taskName);
    }

    /**
     * Stops a task. If the task does not exist or it is not running, the method does nothing.
     *
     * @param taskName the task name
     */
    public static void stopTask(String taskName) {

        sch.stopTask(taskName);
    }
}
